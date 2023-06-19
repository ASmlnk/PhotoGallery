package combignerdranch.android.photogallery

import androidx.paging.PagingSource
import androidx.paging.PagingState
import java.lang.Exception

class PhotoGalleryPageSource(
    private val searchString: String,
    private val flickrFetchr: FlickrFetchr
) : PagingSource<Int, GalleryItem>() {

    override fun getRefreshKey(state: PagingState<Int, GalleryItem>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchorPosition) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)

        /*return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }*/
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GalleryItem> {

        val pageIndex: Int = params.key ?: 0

        return try {
            val galleryItems = if (searchString.isBlank()) {
                flickrFetchr.getFlickrApi(page = pageIndex)
            } else {
                flickrFetchr.searchPhotos( searchString, page = pageIndex)
            }

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