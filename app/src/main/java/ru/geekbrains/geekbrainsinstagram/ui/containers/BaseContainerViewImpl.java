package ru.geekbrains.geekbrainsinstagram.ui.containers;

import android.os.Bundle;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import ru.geekbrains.domain.model.AppThemeModel;
import ru.geekbrains.geekbrainsinstagram.R;
import ru.geekbrains.geekbrainsinstagram.ui.navigator.Screens;
import ru.terrakok.cicerone.Navigator;
import ru.terrakok.cicerone.NavigatorHolder;

public abstract class BaseContainerViewImpl<V extends BaseContainerPresenter.View, P extends BaseContainerPresenter<V>>
        extends AppCompatActivity
        implements BaseContainerPresenter.View {

    @Inject
    NavigatorHolder navigatorHolder;

    @Inject
    Screens screens;

    @Inject
    protected P presenter;

    protected Navigator navigator;
    private boolean isViewSet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        inject();
        attachView();
        isViewSet = true;
        presenter.beforeOnCreate();

        super.onCreate(savedInstanceState);

        navigator = getNavigator();
        screens.init(getSupportFragmentManager());
        presenter.setScreens(screens);
        setupView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isViewSet) {
            attachView();
            isViewSet = true;
        }
        presenter.start();
    }

    @Override
    protected void onPause() {
        navigatorHolder.removeNavigator();
        super.onPause();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        navigatorHolder.setNavigator(navigator);
    }


    @Override
    protected void onStop() {
        super.onStop();
        presenter.stop();
        isViewSet = false;
    }

    @Override
    protected void onDestroy() {
        if (isFinishing()) {
            screens.release();
            release();
        }
        super.onDestroy();
    }

    @Override
    public void setTheme(AppThemeModel theme) {
        switch (theme) {
            case RED_THEME:
                setTheme(R.style.RedAppTheme);
                break;
            case BLUE_THEME:
                setTheme(R.style.BlueAppTheme);
                break;
            case GREEN_THEME:
                setTheme(R.style.GreenAppTheme);
                break;
        }
    }

    protected abstract void release();

    protected abstract void inject();

    protected abstract void attachView();

    protected abstract Navigator getNavigator();

    protected abstract void setupView();
}
