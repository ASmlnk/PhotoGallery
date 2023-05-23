package combignerdranch.android.photogallery

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class ViewModelFactory(val photoGalleryPageRepository: PhotoGalleryPageRepository): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PhotoGalleryViewModel(photoGalleryPageRepository) as T
    }
}