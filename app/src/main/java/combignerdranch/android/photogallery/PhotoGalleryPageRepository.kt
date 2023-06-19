package combignerdranch.android.photogallery

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData

class PhotoGalleryPageRepository(
    private val flickrFetchr: FlickrFetchr
    ) {

    fun getAllGalleryItems(searchString: String): LiveData<PagingData<GalleryItem>> {

        return Pager(
            config = PagingConfig(pageSize = 5),
            pagingSourceFactory = { PhotoGalleryPageSource( searchString, flickrFetchr) },
            initialKey = 1
        ).liveData
    }
}