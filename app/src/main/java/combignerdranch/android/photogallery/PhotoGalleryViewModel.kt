package combignerdranch.android.photogallery

import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.paging.PagingData
import androidx.paging.cachedIn
import java.security.Provider

class PhotoGalleryViewModel (private val photoGalleryPageRepository: PhotoGalleryPageRepository): ViewModel() {

   // val galleryItemLiveData: LiveData<List<GalleryItem>>
    private val galleryItemPageLiveData = MutableLiveData<PagingData<GalleryItem>>()

    init {
       //  galleryItemLiveData = FlickrFetchr().fetchPhotos(1)
        //galleryItemLiveData = FlickrFetchrDeserializer().fetchPhotos()
        /*запускает занрос при создании виевмодель*/
        //galleryItemPageLiveData = photoGalleryPageRepository.getAllGalleryItems()
    }

   suspend fun getMovieList(): LiveData<PagingData<GalleryItem>> {
        val responce = photoGalleryPageRepository.getAllGalleryItems().cachedIn(viewModelScope)
       galleryItemPageLiveData.value = responce.value

       return responce
    }


}