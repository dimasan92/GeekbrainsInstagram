package ru.geekbrains.geekbrainsinstagram.ui.screens.personalphotos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import ru.geekbrains.geekbrainsinstagram.R;
import ru.geekbrains.geekbrainsinstagram.model.PhotoDiffUtilCallback;
import ru.geekbrains.geekbrainsinstagram.model.PhotoModel;
import ru.geekbrains.geekbrainsinstagram.utils.IPictureUtils;

public final class PersonalPhotosAdapter extends RecyclerView.Adapter<PersonalPhotosAdapter.PersonalPhotoHolder> {

    private final IPictureUtils pictureUtils;
    private List<PhotoModel> photos = Collections.emptyList();

    private final Subject<PhotoModel> onFavoritesClickObservable = BehaviorSubject.create();
    private final Subject<PhotoModel> onLongItemClickObservable = BehaviorSubject.create();

    public PersonalPhotosAdapter(IPictureUtils IPictureUtils) {
        this.pictureUtils = IPictureUtils;
    }

    @NonNull
    @Override
    public PersonalPhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new PersonalPhotoHolder(inflater.inflate(R.layout.item_personal_photo,
                parent, false),
                onFavoritesClickObservable, onLongItemClickObservable, pictureUtils);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonalPhotoHolder holder, int position) {
        holder.bind(photos.get(position));
    }

    void updatePhotos(List<PhotoModel> photos) {
        DiffUtil.DiffResult diffResult =
                DiffUtil.calculateDiff(new PhotoDiffUtilCallback(this.photos, photos));
        this.photos = photos;
        diffResult.dispatchUpdatesTo(this);
    }

    @Override
    public final int getItemCount() {
        return photos.size();
    }

    Observable<PhotoModel> onFavoritesClick() {
        return onFavoritesClickObservable.doOnNext(this::changeFavoritesStatus);
    }

    Observable<PhotoModel> onDeleteClick() {
        return onLongItemClickObservable;
    }

    private void changeFavoritesStatus(final PhotoModel photoModel) {
        final int position = photos.indexOf(photoModel);
        photoModel.setFavorite(!photoModel.isFavorite());
        notifyItemChanged(position);
    }

    static final class PersonalPhotoHolder extends RecyclerView.ViewHolder {

        private final ImageView photoImageView;
        private final ImageView isFavoritesImageView;
        private final Subject<PhotoModel> onFavoritesClickObservable;
        private final Subject<PhotoModel> onLongItemClickObservable;

        private final IPictureUtils pictureUtils;

        private PhotoModel photoModel;

        PersonalPhotoHolder(@NonNull View itemView, Subject<PhotoModel> onFavoritesClickObservable,
                            Subject<PhotoModel> onLongItemClickObservable, IPictureUtils pictureUtils) {
            super(itemView);
            this.onFavoritesClickObservable = onFavoritesClickObservable;
            this.onLongItemClickObservable = onLongItemClickObservable;
            this.pictureUtils = pictureUtils;

            photoImageView = itemView.findViewById(R.id.iv_personal_photo);
            isFavoritesImageView = itemView.findViewById(R.id.iv_is_photo_favorite);

            itemView.setOnLongClickListener(v -> {
                onLongItemClickObservable.onNext(photoModel);
                return true;
            });

            isFavoritesImageView.setOnClickListener(v -> onFavoritesClickObservable.onNext(photoModel));
        }

        void bind(final PhotoModel photoModel) {
            this.photoModel = photoModel;
            pictureUtils.loadImageIntoImageView(photoModel, photoImageView);
            isFavoritesImageView.setImageResource(photoModel.isFavorite() ?
                    R.drawable.ic_star_filled_24dp :
                    R.drawable.ic_star_border_24dp);
        }
    }
}

