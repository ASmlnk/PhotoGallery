package combignerdranch.android.photogallery

import androidx.lifecycle.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import androidx.paging.cachedIn


class PhotoGalleryViewModel(private val photoGalleryPageRepository: PhotoGalleryPageRepository) :
    ViewModel() {

    val galleryItemLiveData: LiveData<PagingData<GalleryItem>>


    private val mutableSearchTerm = MutableLiveData<String>()

    init {
         mutableSearchTerm.value = ""
        galleryItemLiveData = mutableSearchTerm.switchMap { searchTerm ->
            photoGalleryPageRepository.getAllGalleryItems(searchTerm).cachedIn(viewModelScope)
        }
    }

    fun fetchPhotos(query: String = "") {
        mutableSearchTerm.value = query
    }
}

/*версия без поиска
* class PhotoGalleryViewModel(private val photoGalleryPageRepository: PhotoGalleryPageRepository) :
    ViewModel() {

    // val galleryItemLiveData: LiveData<List<GalleryItem>>
    private val galleryItemPageLiveData = MutableLiveData<PagingData<GalleryItem>>()

    init {
        //  galleryItemLiveData = FlickrFetchr().fetchPhotos(1)
        //galleryItemLiveData = FlickrFetchrDeserializer().fetchPhotos()
        /*запускает занрос при создании виевмодель*/
        //galleryItemPageLiveData = photoGalleryPageRepository.getAllGalleryItems()
    }

    fun getMovieList(): LiveData<PagingData<GalleryItem>> {
        return photoGalleryPageRepository.getAllGalleryItems().cachedIn(viewModelScope)
        // galleryItemPageLiveData.value = responce.value
        //return responce
    }
}*/