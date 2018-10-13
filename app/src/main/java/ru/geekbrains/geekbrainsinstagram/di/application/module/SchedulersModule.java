package ru.geekbrains.geekbrainsinstagram.di.application.module;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

@Module
public final class SchedulersModule {

    @Singleton
    @Provides
    Scheduler provideUiScheduler() {
        return AndroidSchedulers.mainThread();
    }
}