package ru.geekbrains.geekbrainsinstagram.ui.screens.savedsearch;

import javax.inject.Inject;

import io.reactivex.Scheduler;
import ru.geekbrains.domain.interactor.photos.DeletePhotoUseCase;
import ru.geekbrains.domain.interactor.photos.GetSavedSearchPhotosUseCase;
import ru.geekbrains.domain.interactor.photos.SearchPhotoUpdaterUseCase;
import ru.geekbrains.domain.interactor.photos.changeFavoritePhotoStatusUseCase;
import ru.geekbrains.domain.model.PhotoModel;
import ru.geekbrains.geekbrainsinstagram.di.ui.home.HomeScope;
import ru.geekbrains.geekbrainsinstagram.ui.base.BasePresenterImpl;
import ru.geekbrains.geekbrainsinstagram.ui.base.photos.BaseListPresenter.ListView;
import ru.geekbrains.geekbrainsinstagram.ui.base.photos.BaseListPresenterImpl;
import ru.geekbrains.geekbrainsinstagram.ui.screens.savedsearch.SavedSearchListPresenter.SavedSearchRowView;
import ru.geekbrains.geekbrainsinstagram.ui.screens.savedsearch.SavedSearchPresenter.SavedSearchView;

@HomeScope
public final class SavedSearchPresenterImpl extends BasePresenterImpl<SavedSearchView>
        implements SavedSearchPresenter {

    private final GetSavedSearchPhotosUseCase getSavedSearchPhotosUseCase;
    private final changeFavoritePhotoStatusUseCase changeFavoritePhotoStatusUseCase;
    private final DeletePhotoUseCase deletePhotoUseCase;
    private final SearchPhotoUpdaterUseCase searchPhotoUpdaterUseCase;

    private final Scheduler uiScheduler;
    private final SavedSearchListPresenterImpl listPresenter;

    private boolean wasPhotosLoad;

    @Inject SavedSearchPresenterImpl(final GetSavedSearchPhotosUseCase getSavedSearchPhotosUseCase,
                                     final changeFavoritePhotoStatusUseCase changeFavoritePhotoStatusUseCase,
                                     final DeletePhotoUseCase deletePhotoUseCase,
                                     final SearchPhotoUpdaterUseCase searchPhotoUpdaterUseCase,
                                     final Scheduler uiScheduler) {
        this.getSavedSearchPhotosUseCase = getSavedSearchPhotosUseCase;
        this.changeFavoritePhotoStatusUseCase = changeFavoritePhotoStatusUseCase;
        this.deletePhotoUseCase = deletePhotoUseCase;
        this.searchPhotoUpdaterUseCase = searchPhotoUpdaterUseCase;
        this.uiScheduler = uiScheduler;
        listPresenter = new SavedSearchListPresenterImpl();
    }

    @Override public void create() {
        view.init(listPresenter);
        searchPhotoUpdaterUseCase.subscribe(b -> wasPhotosLoad = false);
    }

    @Override public void userVisibleHint() {
        checkPhotosLoading();
    }

    @Override public void start() {
        checkPhotosLoading();
    }

    @Override public void stop() {
        super.stop();
        listPresenter.detachView();
    }

    @Override public void attachListView(final ListView listView) {
        listPresenter.attachView(listView);
    }

    private void checkPhotosLoading() {
        if (!wasPhotosLoad) {
            wasPhotosLoad = true;
            uploadPhotos();
        }
    }

    private void uploadPhotos() {
        addDisposable(getSavedSearchPhotosUseCase.execute()
                .observeOn(uiScheduler)
                .subscribe(listPresenter::setPhotoModels,
                        throwable -> wasPhotosLoad = false));
    }

    private void deletePhoto(final PhotoModel photoModel) {
        addDisposable(deletePhotoUseCase.execute(photoModel)
                .observeOn(uiScheduler)
                .subscribe(() -> {
                            listPresenter.deletePhotoModel(photoModel);
                            searchPhotoUpdaterUseCase.execute();
                        },
                        throwable -> view.showErrorDeletingPhoto()));
    }

    private void changePhotoFavoriteState(final PhotoModel photoModel) {
        addDisposable(changeFavoritePhotoStatusUseCase
                .execute(photoModel)
                .observeOn(uiScheduler)
                .subscribe(newPhotoModel -> {
                            listPresenter.updatePhotoModel(newPhotoModel);
                            searchPhotoUpdaterUseCase.execute();
                        },
                        throwable -> {
                            if (photoModel.isFavorite()) {
                                view.showErrorDeletingFromFavoritesMessage();
                            } else {
                                view.showErrorAddingToFavoritesMessage();
                            }
                        }));
    }

    class SavedSearchListPresenterImpl extends BaseListPresenterImpl<SavedSearchRowView> implements SavedSearchListPresenter {

        @Override public void bind(final int position, final SavedSearchRowView rowView) {
            final PhotoModel photoModel = photoModels.get(position);
            rowView.loadImage(photoModel.getFilePath());
            rowView.setFavorite(photoModel.isFavorite());
        }

        @Override public void onFavoriteClick(final int position) {
            changePhotoFavoriteState(photoModels.get(position));
        }

        @Override public void onIoActionClick(final int position) {
            final PhotoModel photoModel = photoModels.get(position);
            deletePhoto(photoModel);
        }
    }
}
