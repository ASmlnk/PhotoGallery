package combignerdranch.android.photogallery

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import combignerdranch.android.photogallery.api.FlickrApi
import combignerdranch.android.photogallery.api.FlickrResponse
import combignerdranch.android.photogallery.api.PhotoResponse
import kotlinx.coroutines.Delay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

private const val TAG = "MY1"

class PhotoGalleryPageSource(
    private val flickrFetchr: FlickrFetchr
) : PagingSource<Int, GalleryItem>() {

    override fun getRefreshKey(state: PagingState<Int, GalleryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GalleryItem> {

        val pageIndex: Int = params.key ?: 0
        Log.d(TAG, "Response receiv ${pageIndex}")

        return try {
            val flickrApi = flickrFetchr.getFlickrApi()
            val flickrRequest = flickrApi.fetchPhotosPage(page = pageIndex)

            Log.d(TAG, "Response received ${flickrRequest}")

            val flickrResponse: FlickrResponse? = flickrRequest.body()
            Log.d(TAG, "Response received ${flickrResponse}")
            val photoResponse: PhotoResponse? = flickrResponse?.photos
            var galleryItems: List<GalleryItem> = photoResponse?.galleryItems
                ?: mutableListOf()
            galleryItems =
                galleryItems.filterNot {       // filterNot   исключить по условию т.е. исключаем строки где url содержит пробелы
                    it.url.isBlank()  // isBlank() возвращает true для строки, содержащей только пробелы
                }

            Log.d(TAG, "Response received'' ${galleryItems}")

            //Log.d(TAG, "Response received ${responseList}")
            LoadResult.Page(
                data = galleryItems,
                prevKey = if (pageIndex == 1) null else pageIndex,
                nextKey = pageIndex + 1
            )

        } catch (e: Exception) {
            LoadResult.Error(
                throwable = e
            )
        }
    }
}