package combignerdranch.android.photogallery

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class ViewModelFactory(val photoGalleryPageRepository: PhotoGalleryPageRepository,
val application: Application) :
    ViewModelProvider.AndroidViewModelFactory (application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PhotoGalleryViewModel(photoGalleryPageRepository, application) as T
    }
}