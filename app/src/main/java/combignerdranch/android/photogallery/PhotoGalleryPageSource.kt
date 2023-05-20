package combignerdranch.android.photogallery

import androidx.paging.PagingSource
import androidx.paging.PagingState

class PhotoGalleryPageSource: PagingSource<Int, GalleryItem>() {
    override fun getRefreshKey(state: PagingState<Int, GalleryItem>): Int? {
        TODO("Not yet implemented")
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GalleryItem> {
        TODO("Not yet implemented")
    }
}