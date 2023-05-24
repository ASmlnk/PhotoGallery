package combignerdranch.android.photogallery

import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.paging.PagingData
import androidx.paging.cachedIn
import java.security.Provider

class PhotoGalleryViewModel (private val photoGalleryPageRepository: PhotoGalleryPageRepository): ViewModel() {

   // val galleryItemLiveData: LiveData<List<GalleryItem>>
    // val galleryItemPageLiveData: LiveData<PagingData<GalleryItem>>

    init {
       //  galleryItemLiveData = FlickrFetchr().fetchPhotos(1)
        //galleryItemLiveData = FlickrFetchrDeserializer().fetchPhotos()
        /*запускает занрос при создании виевмодель*/
        //galleryItemPageLiveData = photoGalleryPageRepository.getAllGalleryItems()
    }

    fun getMovieList(): LiveData<PagingData<GalleryItem>> {
        return photoGalleryPageRepository.getAllGalleryItems().cachedIn(viewModelScope)
    }


}