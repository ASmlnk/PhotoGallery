package combignerdranch.android.photogallery

import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class PhotoGalleryPagerAdapter: PagingDataAdapter<GalleryItem, PhotoGalleryPagerAdapter.PhotoHolder>(GalleryItemComparator) {
    object GalleryItemComparator : DiffUtil.ItemCallback<GalleryItem>() {
        override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
            return oldItem == newItem
        }
    }

    class PhotoHolder(itemTextView: TextView): RecyclerView.ViewHolder(itemTextView) {
        val bindTitle: (CharSequence) -> Unit = itemTextView::setText
    }

    override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
        val galleryItem = getItem(position)!!
        holder.bindTitle(galleryItem.title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
        val textView = TextView(parent.context)
        return PhotoHolder(textView)
    }
}