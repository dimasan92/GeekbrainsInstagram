package ru.geekbrains.pictureapp.presentation.ui.screens.onlinesearch;

import javax.inject.Inject;

import io.reactivex.Scheduler;
import io.reactivex.functions.Action;
import ru.geekbrains.pictureapp.data.exception.NoNetworkConnectionException;
import ru.geekbrains.pictureapp.domain.interactor.photos.favorites.ChangePhotoFavoriteStatusUseCase;
import ru.geekbrains.pictureapp.domain.interactor.photos.common.DeletePhotoUseCase;
import ru.geekbrains.pictureapp.domain.interactor.photos.search.GetPhotosBySearchUseCase;
import ru.geekbrains.pictureapp.domain.interactor.photos.search.SaveSearchPhotoUseCase;
import ru.geekbrains.pictureapp.domain.interactor.photos.search.SearchPhotoUpdaterUseCase;
import ru.geekbrains.pictureapp.domain.interactor.photos.search.UpdateSearchPhotosUseCase;
import ru.geekbrains.pictureapp.domain.model.PhotoModel;
import ru.geekbrains.pictureapp.presentation.di.ui.home.HomeScope;
import ru.geekbrains.pictureapp.presentation.ui.base.BasePresenterImpl;
import ru.geekbrains.pictureapp.presentation.ui.base.photos.BaseListPresenter.ListView;
import ru.geekbrains.pictureapp.presentation.ui.base.photos.BaseListPresenterImpl;
import ru.geekbrains.pictureapp.presentation.ui.screens.onlinesearch.OnlineSearchListPresenter.OnlineSearchRowView;
import ru.geekbrains.pictureapp.presentation.ui.screens.onlinesearch.OnlineSearchPresenter.OnlineSearchView;
import ru.geekbrains.pictureapp.presentation.util.PictureUtils;

@HomeScope
public final class OnlineSearchPresenterImpl extends BasePresenterImpl<OnlineSearchView>
        implements OnlineSearchPresenter {

    private static final int PHOTOS_COUNT = 30;

    private final GetPhotosBySearchUseCase getPhotosBySearchUseCase;
    private final SearchPhotoUpdaterUseCase searchPhotoUpdaterUseCase;

    private final Scheduler uiScheduler;
    private final OnlineSearchListPresenterImpl listPresenter;
    private boolean wasPhotosUpdated;

    @Inject OnlineSearchPresenterImpl(final GetPhotosBySearchUseCase getPhotosBySearchUseCase,
                                      final SearchPhotoUpdaterUseCase searchPhotoUpdaterUseCase,
                                      final OnlineSearchListPresenterImpl listPresenter,
                                      final Scheduler uiScheduler) {
        this.getPhotosBySearchUseCase = getPhotosBySearchUseCase;
        this.searchPhotoUpdaterUseCase = searchPhotoUpdaterUseCase;
        this.listPresenter = listPresenter;
        this.uiScheduler = uiScheduler;
    }

    @Override public void create() {
        view.init(listPresenter);
        searchPhotoUpdaterUseCase.subscribe(b -> wasPhotosUpdated = true);
    }

    @Override public void userVisibleHint() {
        checkPhotosUpdates();
    }

    @Override public void start() {
        checkPhotosUpdates();
    }

    @Override public void stop() {
        super.stop();
        listPresenter.detachView();
    }

    @Override public void attachListView(final ListView listView) {
        listPresenter.attachView(view, listView);
    }

    @Override public void onSearchClick(final String query) {
        addDisposable(getPhotosBySearchUseCase.execute(query, PHOTOS_COUNT)
                .observeOn(uiScheduler)
                .subscribe(listPresenter::setPhotoModels,
                        throwable -> {
                            if (throwable instanceof NoNetworkConnectionException) {
                                view.showErrorNetworkMessage();
                            } else {
                                view.showErrorDownloadingPhotosMessage();
                            }
                        }));
    }

    private void checkPhotosUpdates() {
        if (wasPhotosUpdated) {
            wasPhotosUpdated = false;
            listPresenter.photosWasUpdated(() -> wasPhotosUpdated = true);
        }
    }

    @HomeScope
    static class OnlineSearchListPresenterImpl extends BaseListPresenterImpl<OnlineSearchView, OnlineSearchRowView>
            implements OnlineSearchListPresenter {

        final ChangePhotoFavoriteStatusUseCase changePhotoFavoriteStatusUseCase;
        private final SaveSearchPhotoUseCase saveSearchPhotoUseCase;
        private final DeletePhotoUseCase deletePhotoUseCase;
        private final UpdateSearchPhotosUseCase updateSearchPhotosUseCase;
        private final SearchPhotoUpdaterUseCase searchPhotoUpdaterUseCase;
        private final PictureUtils pictureUtils;
        private final Scheduler uiScheduler;

        @Inject OnlineSearchListPresenterImpl(final ChangePhotoFavoriteStatusUseCase changePhotoFavoriteStatusUseCase,
                                              final SaveSearchPhotoUseCase saveSearchPhotoUseCase,
                                              final DeletePhotoUseCase deletePhotoUseCase,
                                              final UpdateSearchPhotosUseCase updateSearchPhotosUseCase,
                                              final SearchPhotoUpdaterUseCase searchPhotoUpdaterUseCase,
                                              final PictureUtils pictureUtils, final Scheduler uiScheduler) {
            this.changePhotoFavoriteStatusUseCase = changePhotoFavoriteStatusUseCase;
            this.saveSearchPhotoUseCase = saveSearchPhotoUseCase;
            this.deletePhotoUseCase = deletePhotoUseCase;
            this.updateSearchPhotosUseCase = updateSearchPhotosUseCase;
            this.searchPhotoUpdaterUseCase = searchPhotoUpdaterUseCase;
            this.pictureUtils = pictureUtils;
            this.uiScheduler = uiScheduler;
        }


        @Override public void bind(final int position, final OnlineSearchRowView rowView) {
            final PhotoModel photoModel = photoModels.get(position);
            rowView.loadImage(photoModel.getSmallPhotoUrl());
            final boolean isSaved = photoModel.isSaved();
            rowView.setSaving(isSaved);
            rowView.favoriteVisibility(isSaved);
            if (isSaved) {
                rowView.setFavorite(photoModel.isFavorite());
            }
        }

        @Override public void onFavoriteClick(final int position) {
            setPhotoFavoriteState(photoModels.get(position));
        }

        @Override public void onIoActionClick(final int position) {
            final PhotoModel photoModel = photoModels.get(position);
            if (photoModel.isSaved()) {
                deletePhoto(photoModel);
            } else {
                savePhoto(photoModel);
            }
        }

        void photosWasUpdated(final Action errorAction) {
            addDisposable(updateSearchPhotosUseCase.execute(photoModels)
                    .observeOn(uiScheduler)
                    .subscribe(this::setPhotoModels,
                            throwable -> errorAction.run()));
        }

        private void setPhotoFavoriteState(final PhotoModel photoModel) {
            addDisposable(changePhotoFavoriteStatusUseCase
                    .execute(photoModel)
                    .observeOn(uiScheduler)
                    .subscribe(this::updatePhotoView,
                            throwable -> {
                                if (photoModel.isFavorite()) {
                                    mainView.showErrorDeletingFromFavoritesMessage();
                                } else {
                                    mainView.showErrorAddingToFavoritesMessage();
                                }
                            }));
        }

        private void savePhoto(final PhotoModel photoModel) {
            addDisposable(pictureUtils.getImageArray(photoModel.getRegularPhotoUrl())
                    .subscribe(bytes -> addDisposable(saveSearchPhotoUseCase
                                    .execute(photoModel, bytes)
                                    .observeOn(uiScheduler)
                                    .subscribe(this::updatePhotoView,
                                            throwable -> mainView.showErrorSavingPhoto())),
                            throwable -> {
                                mainView.showErrorSavingPhoto();
                                throwable.printStackTrace();
                            }));
        }

        private void deletePhoto(final PhotoModel photoModel) {
            addDisposable(deletePhotoUseCase.execute(photoModel)
                    .observeOn(uiScheduler)
                    .subscribe(() -> updatePhotoView(new PhotoModel(photoModel, "")),
                            throwable -> mainView.showErrorDeletingPhoto()));
        }

        private void updatePhotoView(final PhotoModel photoModel) {
            updatePhotoModel(photoModel);
            searchPhotoUpdaterUseCase.execute();
        }
    }
}