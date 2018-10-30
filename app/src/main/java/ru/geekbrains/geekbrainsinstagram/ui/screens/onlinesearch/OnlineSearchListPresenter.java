package ru.geekbrains.geekbrainsinstagram.ui.screens.onlinesearch;

import ru.geekbrains.geekbrainsinstagram.ui.base.photos.BaseListPresenter;

interface OnlineSearchListPresenter extends BaseListPresenter<OnlineSearchListPresenter.OnlineSearchRowView> {

    interface OnlineSearchRowView extends BaseListPresenter.RowView {

        void favoriteVisibility(final boolean isVisible);

        void setFavorite(final boolean isFavorite);

        void setSaving(final boolean isSaving);
    }

    void onFavoriteClick(final int position);

    void onIoActionClick(final int position);
}
