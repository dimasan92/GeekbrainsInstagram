package ru.geekbrains.geekbrainsinstagram.di.fragment.module;

import dagger.Module;
import dagger.Provides;
import ru.geekbrains.domain.interactor.photos.ChangeFavoriteStatusPersonalPhotoUseCase;
import ru.geekbrains.domain.interactor.photos.DeletePersonalPhotoUseCase;
import ru.geekbrains.domain.interactor.photos.GetPersonalPhotosUseCase;
import ru.geekbrains.domain.interactor.photos.SaveNewPersonalPhotoUseCase;
import ru.geekbrains.geekbrainsinstagram.MainApplication;
import ru.geekbrains.geekbrainsinstagram.di.fragment.FragmentScope;
import ru.geekbrains.geekbrainsinstagram.model.mapper.IModelMapper;
import ru.geekbrains.geekbrainsinstagram.ui.screens.personalphotos.IPersonalPhotosPresenter;
import ru.geekbrains.geekbrainsinstagram.ui.screens.personalphotos.PersonalPhotosAdapter;
import ru.geekbrains.geekbrainsinstagram.ui.screens.personalphotos.PersonalPhotosPresenter;
import ru.geekbrains.geekbrainsinstagram.ui.screens.theme.AppThemePresenter;
import ru.geekbrains.geekbrainsinstagram.ui.screens.theme.IAppThemePresenter;
import ru.geekbrains.geekbrainsinstagram.utils.ICameraUtils;
import ru.geekbrains.geekbrainsinstagram.utils.IPictureUtils;

@Module
public final class FragmentModule {

    @FragmentScope
    @Provides
    IAppThemePresenter provideAppThemePresenter() {
        final AppThemePresenter appThemePresenter = new AppThemePresenter();
        MainApplication.getApp().getComponentsManager()
                .getFragmentComponent().inject(appThemePresenter);
        return appThemePresenter;
    }

    @FragmentScope
    @Provides
    IPersonalPhotosPresenter providePersonalPhotosPresenter(SaveNewPersonalPhotoUseCase saveNewPersonalPhotoUseCase,
                                                            GetPersonalPhotosUseCase getPersonalPhotosUseCase,
                                                            ChangeFavoriteStatusPersonalPhotoUseCase changeFavoriteStatusPersonalPhotoUseCase,
                                                            DeletePersonalPhotoUseCase deletePersonalPhotoUseCase,
                                                            ICameraUtils cameraUtils, IModelMapper mapper) {
        return new PersonalPhotosPresenter(
                saveNewPersonalPhotoUseCase, getPersonalPhotosUseCase,
                changeFavoriteStatusPersonalPhotoUseCase, deletePersonalPhotoUseCase,
                cameraUtils, mapper);
    }

    @FragmentScope
    @Provides
    PersonalPhotosAdapter provideCameraPhotoAdapter(IPictureUtils IPictureUtils) {
        return new PersonalPhotosAdapter(IPictureUtils);
    }
}
