package combignerdranch.android.photogallery

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

private const val TAG = "PhotoGalleryFragment"

class PhotoGalleryFragment : Fragment() {

    private lateinit var photoRecyclerView: RecyclerView
    private val flickrFetchr = FlickrFetchr()
    private val photoGalleryPageRepository = PhotoGalleryPageRepository(flickrFetchr)
    private lateinit var thumbnailDownloader
            : ThumbnailDownloader<PhotoGalleryPagerAdapter.PhotoHolder>

    private val photoGalleryViewModel: PhotoGalleryViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory(photoGalleryPageRepository)
        )[PhotoGalleryViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true //сохранение фрагмента

        val responseHandler = Handler() //Handler основного потока
        thumbnailDownloader = ThumbnailDownloader(responseHandler) {photoHolder, bitmap ->
            val drawable = BitmapDrawable(resources, bitmap)
            photoHolder.bindDrawable(drawable)
            /*функция, переданная в функцию высшего порядка onThumbnailDownloaded ,
            устанавливает Drawable запрошенного PhotoHolder на только что загруженный Bitmap*/
        }

        /*наблюдение за жизненым циклом фрагмента*/
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)

        /*наблюдение за жизненым циклом представления 2 вариант*/
        viewLifecycleOwnerLiveData.isInitialized

/*        val flickrLiveData: LiveData<List<GalleryItem>> = FlickrFetchr().fetchPhotos()
        flickrLiveData.observe(this) { galleryItems ->
            Log.d(TAG, "Response received: $galleryItems ")
        }*/
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        /*наблюдение за жизненым циклом представления 1 вариант*/
        /*viewLifecycleOwner.lifecycle.addObserver(
            thumbnailDownloader.viewLifecycleObserver
        )*/

        val view = inflater.inflate(
            R.layout.fragment_photo_gallery,
            container,
            false
        )

        photoRecyclerView = view.findViewById(R.id.photo_recycler_view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = PhotoGalleryPagerAdapter(thumbnailDownloader)
        photoRecyclerView.adapter = adapter

        /*наблюдение за жизненым циклом представления 2 вариант*/
        viewLifecycleOwnerLiveData.value?.lifecycle?.addObserver(
            thumbnailDownloader.viewLifecycleObserver
        )

        viewLifecycleOwner.lifecycleScope.launch {
            photoGalleryViewModel.getMovieList()
                .observe(viewLifecycleOwner) { pagingData ->
                    pagingData?.let {
                        adapter.submitData(lifecycle, it)
                    }
                }
        }

        val photoRecyclerObserver = photoRecyclerView.viewTreeObserver

        photoRecyclerObserver
            .addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {

                    photoRecyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    val width = photoRecyclerView.width
                    val height = photoRecyclerView.measuredHeight
                    //updateSize(width, height)
                    val spanCount: Int = width / 360
                    photoRecyclerView.layoutManager = GridLayoutManager(context, spanCount)
                    photoRecyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()

        /*наблюдение за жизненым циклом представления*/
        viewLifecycleOwner.lifecycle.removeObserver(
            thumbnailDownloader.viewLifecycleObserver
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        /*наблюдение за жизненым циклом фрагмента*/
        lifecycle.removeObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }

    /*Вызов lifecycle.addObserver(thumbnailDownloader) подписывает экземпляр загрузчика
    эскизов на получение обратных вызовов жизненного цикла фрагмента. Теперь при вызове
     функции PhotoGalleryFragment. onCreate(...) вызывается функция
     ThumbnailDownloader.setup(). При вызове функции PhotoGalleryFragment.onDestroy()
     вызывается функция ThumbnailDownloader.tearDown()*/

    private class PhotoHolder(private val itemImageView: ImageView) :
        RecyclerView.ViewHolder(itemImageView) {
        val bindDrawable: (Drawable) -> Unit = itemImageView::setImageDrawable
    }
    /*private class PhotoHolder(itemTextView: TextView) : RecyclerView.ViewHolder(itemTextView) {

        val bindTitle: (CharSequence) -> Unit = itemTextView::setText
    }*/

    private inner class PhotoAdapter(private val galleryIem: List<GalleryItem>) : //inner  позволякт получить доступ к свойству layoutInflater, но можно и через parent.context
        RecyclerView.Adapter<PhotoHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {

            val view = layoutInflater.inflate(
                R.layout.list_item_gallery,
                parent,
                false
            ) as ImageView
            return PhotoHolder(view)

            /*val textView = TextView(parent.context)
                return PhotoHolder(textView)*/
        }

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val galleryItem = galleryIem[position]
            val placeholder: Drawable = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.bill_up_close
            ) ?: ColorDrawable() //пустой обЪект
            holder.bindDrawable(placeholder)
            //thumbnailDownloader.queueThumbnail(holder, galleryItem.url)
            /*holder.bindTitle(galleryItem.title)*/
        }

        override fun getItemCount(): Int = galleryIem.size
    }

    companion object {
        fun newInstance() = PhotoGalleryFragment()
    }
}