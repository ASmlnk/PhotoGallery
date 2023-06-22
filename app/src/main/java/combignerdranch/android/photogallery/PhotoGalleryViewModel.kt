package combignerdranch.android.photogallery

import android.app.Application
import androidx.lifecycle.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import androidx.paging.cachedIn


class PhotoGalleryViewModel(
    private val photoGalleryPageRepository: PhotoGalleryPageRepository,
    private val app: Application
) : AndroidViewModel(app) {

    val galleryItemLiveData: LiveData<PagingData<GalleryItem>>

    private val mutableSearchTerm = MutableLiveData<String>()

    val searchTerm: String
    get() = mutableSearchTerm.value ?: ""

    init {
        mutableSearchTerm.value = QueryPreferences.getStoredQuery(getApplication())

        galleryItemLiveData = mutableSearchTerm.switchMap { searchTerm ->
            photoGalleryPageRepository.getAllGalleryItems(searchTerm).cachedIn(viewModelScope)
        }
    }

    fun fetchPhotos(query: String = "") {
        QueryPreferences.setStoredQuery(app, query)
        mutableSearchTerm.value = query
    }
}

/*версия 2 без sharedPreference
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
}*/

/*версия 1 без поиска
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