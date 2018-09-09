package ru.geekbrains.domain.interactor.photos;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Completable;
import ru.geekbrains.domain.model.PhotoModel;
import ru.geekbrains.domain.repository.IPhotosRepository;

@Singleton
public final class DeletePhotoUseCase {

    private final IPhotosRepository repository;

    @Inject
    DeletePhotoUseCase(IPhotosRepository repository) {
        this.repository = repository;
    }

    public Completable execute(PhotoModel photo) {
        return repository.deletePhoto(photo);
    }
}