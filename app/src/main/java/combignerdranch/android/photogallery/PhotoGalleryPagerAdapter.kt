package combignerdranch.android.photogallery

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
private const val TAG = "pos"

class PhotoGalleryPagerAdapter(val thumbnailDownloader: ThumbnailDownloader<PhotoHolder>)
    : PagingDataAdapter<GalleryItem,
        PhotoGalleryPagerAdapter.PhotoHolder>(GalleryItemComparator) {

    object GalleryItemComparator : DiffUtil.ItemCallback<GalleryItem>() {
        override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
            return oldItem == newItem
        }
    }

    class PhotoHolder(private val itemImageView: ImageView) :
        RecyclerView.ViewHolder(itemImageView) {
        val bindDrawable: (Drawable) -> Unit = itemImageView::setImageDrawable
    }

    /*class PhotoHolder(itemTextView: TextView): RecyclerView.ViewHolder(itemTextView) {

        val bindTitle: (CharSequence) -> Unit = itemTextView::setText
    }*/

    override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
        val galleryItem = getItem(position)!!
        val listGalleryItem = mutableListOf<GalleryItem>()
        for (n in position..(position+10)) {
            listGalleryItem.add(getItem(n)!!)
        }
        Log.i(TAG, "${listGalleryItem.size}")

        val placeholder: Drawable = ContextCompat.getDrawable(
            holder.itemView.context,
            R.drawable.bill_up_close
        ) ?: ColorDrawable()

        holder.bindDrawable(placeholder)

        thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
        /*вызываем queueThumbnail() потока и передаем целевую папку PhotoHolder,
        где в конечном итоге будет размещено изображение и
        URL-адрес GalleryItem для скачивания*/

        // holder.bindTitle(galleryItem.title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.list_item_gallery,
            parent,
            false
        ) as ImageView
        return PhotoHolder(view)

        /*val textView = TextView(parent.context)
            return PhotoHolder(textView)*/
    }
}