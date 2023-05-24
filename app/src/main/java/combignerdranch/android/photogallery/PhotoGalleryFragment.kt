package combignerdranch.android.photogallery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
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
    private lateinit var photoRecyclerObserver: ViewTreeObserver


    private val flickrFetchr = FlickrFetchr()
    private val photoGalleryPageRepository = PhotoGalleryPageRepository(flickrFetchr)
  // private lateinit var flickrFetchr : FlickrFetchr
   // private lateinit var photoGalleryPageRepository : PhotoGalleryPageRepository

    private val photoGalleryViewModel: PhotoGalleryViewModel by lazy {
        ViewModelProvider(this, ViewModelFactory(photoGalleryPageRepository))[PhotoGalleryViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
         val adapter = PhotoGalleryPagerAdapter()
        photoRecyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {  photoGalleryViewModel.getMovieList().observe(
            viewLifecycleOwner,
            Observer { galleryItems ->
                Log.d("My", "Have gallery items from ViewModel $galleryItems")

                galleryItems?.let {
                    adapter.submitData(lifecycle, it)
                    Log.d("My", "Have gallery items from ViewModel $it")
                }
            }
        )
        }

        val photoRecyclerObserver = photoRecyclerView.viewTreeObserver
        photoRecyclerObserver.addOnGlobalLayoutListener (object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {

                photoRecyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this)

               val width = photoRecyclerView.width
                val height = photoRecyclerView.measuredHeight
                //updateSize(width, height)
                Log.d("My", "width = $width heidht = $height")
                val spanCount: Int = width/360
                photoRecyclerView.layoutManager = GridLayoutManager(context, spanCount)
            }
        })

    }



    private class PhotoHolder(itemTextView: TextView): RecyclerView.ViewHolder(itemTextView) {

        val bindTitle: (CharSequence) -> Unit = itemTextView::setText
    }

    private class PhotoAdapter(private val galleryIem: List<GalleryItem>) :
        RecyclerView.Adapter<PhotoHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            val textView = TextView(parent.context)
            return PhotoHolder(textView)
        }

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            val galleryItem = galleryIem[position]
            holder.bindTitle(galleryItem.title)
        }

        override fun getItemCount(): Int = galleryIem.size
    }

    companion object {
        fun newInstance() = PhotoGalleryFragment()
    }
}