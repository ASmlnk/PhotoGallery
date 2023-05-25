package combignerdranch.android.photogallery

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData

class PhotoGalleryPageRepository(private val flickrFetchr: FlickrFetchr) {

    fun getAllGalleryItems(): LiveData<PagingData<GalleryItem>> {

        return Pager(
            config = PagingConfig(pageSize = 5),
            pagingSourceFactory = { PhotoGalleryPageSource(flickrFetchr) },
            initialKey = 1
        ).liveData
    }
}