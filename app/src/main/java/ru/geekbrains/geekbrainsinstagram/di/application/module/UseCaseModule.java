package ru.geekbrains.geekbrainsinstagram.di.application.module;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.geekbrains.domain.interactor.settings.ChangeThemeUseCase;
import ru.geekbrains.domain.interactor.settings.GetCurrentThemeUseCase;
import ru.geekbrains.domain.repository.SettingsRepository;

@Module
public final class UseCaseModule {

    @Singleton
    @Provides
    ChangeThemeUseCase provideChangeThemeUseCase(final SettingsRepository repository) {
        return new ChangeThemeUseCase(repository);
    }

    @Singleton
    @Provides
    GetCurrentThemeUseCase provideGetCurrentThemeUseCase(final SettingsRepository repository) {
        return new GetCurrentThemeUseCase(repository);
    }
}