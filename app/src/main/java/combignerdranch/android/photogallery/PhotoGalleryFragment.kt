package combignerdranch.android.photogallery

import android.app.ProgressDialog
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.ImageView
import android.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private const val TAG = "PhotoGalleryFragment"
private const val POLL_WORK = "POLL_WORK"

/*class PhotoGalleryFragment : Fragment() {*/
class PhotoGalleryFragment : VisibleFragment() {

    private lateinit var progressDialog: ProgressDialog //для отображения индикатора загрузки (с неопределенным состоянием)

    // сразу же после отправки запроса и до завершения загрузки данных JSON
    private lateinit var photoRecyclerView: RecyclerView
    val flickrFetchr = FlickrFetchr()
    private val photoGalleryPageRepository = PhotoGalleryPageRepository(flickrFetchr)
    private lateinit var thumbnailDownloader
            : ThumbnailDownloader<PhotoGalleryPagerAdapter.PhotoHolder>

    private val photoGalleryViewModel: PhotoGalleryViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelFactory(
                photoGalleryPageRepository,
                application = requireActivity().application
            )
        )[PhotoGalleryViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        progressDialog = ProgressDialog(requireContext()).apply {
            setTitle("Downloading photos")
            setMessage("It might take a few seconds.. ")
            setProgressStyle(ProgressDialog.STYLE_SPINNER)
        }  //далее в searchView

        //  retainInstance = true //сохранение фрагмента
        setHasOptionsMenu(true)

        val responseHandler = Handler() //Handler основного потока
        thumbnailDownloader = ThumbnailDownloader(responseHandler) { photoHolder, bitmap ->
            val drawable = BitmapDrawable(resources, bitmap)
            photoHolder.bindDrawable(drawable)
            /* функция, переданная в функцию высшего порядка onThumbnailDownloaded ,
            * устанавливает Drawable запрошенного PhotoHolder на только что загруженный Bitmap*/
        }

        /* наблюдение за жизненым циклом фрагмента*/
        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)

        /* наблюдение за жизненым циклом представления 2 вариант*/
        // viewLifecycleOwnerLiveData.isInitialized


/*        val flickrLiveData: LiveData<List<GalleryItem>> = FlickrFetchr().fetchPhotos()
        flickrLiveData.observe(this) { galleryItems ->
            Log.d(TAG, "Response received: $galleryItems ")
        }*/


        /*убираем в конце главы 27*/
        /* //конструируем условие запроса
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)  //указываем тип сети
                .build()
            //создаем рабочий запрос
            val workRequest =
                OneTimeWorkRequest //В OneTimeWorkRequest используется конструктор для создания экземпляра
                    .Builder(PollWorker::class.java) //передаем класс Worker конструктору, который будет запущен в рабочем запросе
                    .setConstraints(constraints)  //добавляем условие запроса
                    .build()
            *//* Как только ваш рабочий запрос будет готов, вам нужно запланировать его с помощью класса WorkManager.
        * Мы вызываем функцию getInstance() для доступа к WorkManager, затем функцию enqueue(...) с рабочим запросом
        * в качестве параметра*//*
        WorkManager.getInstance()
            .enqueue(workRequest)*/
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        /* наблюдение за жизненым циклом представления 1 вариант*/
        /* viewLifecycleOwner.lifecycle.addObserver(
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

        /* наблюдение за жизненым циклом представления 2 вариант*/
        viewLifecycleOwnerLiveData.value?.lifecycle?.addObserver(
            thumbnailDownloader.viewLifecycleObserver
        )

        viewLifecycleOwner.lifecycleScope.launch {
            photoGalleryViewModel.galleryItemLiveData
                .observe(viewLifecycleOwner) { pagingData ->
                    pagingData?.let {
                        progressDialog.dismiss()  //скрытие диологового окна после загрузки содержимого
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

        /* наблюдение за жизненым циклом представления*/
        viewLifecycleOwner.lifecycle.removeObserver(
            thumbnailDownloader.viewLifecycleObserver
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        /* наблюдение за жизненым циклом фрагмента*/
        lifecycle.removeObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.fragment_photo_gallery, menu)

        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView

        searchView.apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(p0: String): Boolean {
                    progressDialog.show()  //делаем диологовое окно видимым
                    photoGalleryViewModel.fetchPhotos(p0)
                    clearFocus() //скрытие клавиатуры
                    return true
                }

                override fun onQueryTextChange(p0: String?): Boolean {
                    return false
                }
            })
            setOnFocusChangeListener { view, hasFocus ->
                if (!hasFocus) {
                    searchItem.collapseActionView()
                }  //прослушиваю изменение фокуса, чтобы свернуть searchview
            }
            setOnSearchClickListener {
                searchView.setQuery(
                    photoGalleryViewModel.searchTerm, false
                )
            }
        }

        val toggleItem = menu.findItem(R.id.menu_item_toggle_polling)
        val isPolling = QueryPreferences.isPolling(requireContext())

        /*по умолчанию для этого элемента является строка start_polling. изменим
         этот текст, если работник уже запущен*/
        val toggleItemTitle = if (isPolling) {
            R.string.stop_polling
        } else {
            R.string.start_polling
        }
        toggleItem.setTitle(toggleItemTitle)
    }

    //очистите сохраненный запрос (установите его равным ""),
    // когда пользователь выберет элемент «Clear Search» в меню
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.menu_item_clear -> {
                photoGalleryViewModel.fetchPhotos("")
                true
            }

            /* реакция на щелчки переключателя опросов. Если работник не запущен, создаем новый
            * PeriodicWorkRequest и запланируйте его с помощью WorkManager. Если работник
            * запущен, его нужно остановить*/
            R.id.menu_item_toggle_polling -> {
                val isPolling = QueryPreferences.isPolling(requireContext())
                if (isPolling) {
                    /* Если работник уже запущен, то вам необходимо сообщить WorkManager об отмене запроса на работу.
                    * В этом случае для удаления периодического запроса на работу вызывается функция cancelUniqueWork(...)
                    * с именем POLL_WORK*/
                    WorkManager.getInstance().cancelUniqueWork(POLL_WORK)
                    QueryPreferences.setPolling(requireContext(), false)
                } else {
                    /* сли работник в данный момент не запущен, то
                    * мы назначим новый запрос на работу с WorkManager*/
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .build()
                    /* PeriodicWorkRequest заставляет нашего
                    * работника самого запланировать себя через некоторый интервал(15 минут)*/
                    val periodicRequest = PeriodicWorkRequest
                        .Builder(PollWorker::class.java, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build()
                    /* enqueueUniquePeriodicWork(...) принимает имя типа String, политику и рабочий запрос.
                    * Имя позволяет однозначно идентифицировать запрос, что полезно, если вы захотите его отменить*/
                    WorkManager.getInstance().enqueueUniquePeriodicWork(
                        POLL_WORK,
                        ExistingPeriodicWorkPolicy.KEEP,
                        periodicRequest
                    )
                    /* политика KEEP, которая отказывается от нового запроса в пользу уже существующего.
                     * опция — REPLACE, которая, как следует из названия, заменяет существующий запрос на новый*/
                    QueryPreferences.setPolling(requireContext(), true)
                }
                activity?.invalidateOptionsMenu()
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /* Вызов lifecycle.addObserver(thumbnailDownloader) подписывает экземпляр загрузчика
     * эскизов на получение обратных вызовов жизненного цикла фрагмента. Теперь при вызове
     * функции PhotoGalleryFragment. onCreate(...) вызывается функция
     * ThumbnailDownloader.setup(). При вызове функции PhotoGalleryFragment.onDestroy()
     * вызывается функция ThumbnailDownloader.tearDown()*/

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