package combignerdranch.android.photogallery

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import androidx.collection.LruCache
import androidx.lifecycle.*
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0
/*MESSAGE_DOWNLOAD будет использоваться для идентификации сообщений как запросов на загрузку.
* ( ThumbnailDownloader присваивает его полю what создаваемых сообщений загрузки.)*/

class ThumbnailDownloader<in T>(
    private val responseHandler: Handler,  //Handler основного потока
    private val onThumbnailDownloader: (T, Bitmap) -> Unit  // интерфейс слушателя для передачи ответов (загруженных изображений)
    // запрашивающей стороне (главному потоку)
) : HandlerThread(TAG) /*LifecycleObserver 1вариант наблюдателя за жизненым циклом фрагмента*/ {

    /* Свойство типа функции в конструкторе, будет рано или поздно использовано, когда полностью
    * загруженное изображение появится в интерфейсе. Использование слушателя передает ответственность за обработку
    * загруженного изображения другому классу, а не ThumbnailDownloader (в данном случае PhotoGalleryFragment )*/

    /* Классу передается один обобщенный аргумент <T>. Пользователю ThumbnailDownloader
    * понадобится объект для идентификации каждой загрузки и определения элемента
    * пользовательского интерфейса, который должен обновляться
    * после завершения загрузки*/

    /* Реализация LifecycleObserver означает, что вы можете
    * подписать ThumbnailDownloader на получение обратных вызовов жизненного
    * цикла от любого владельца LifecycleOwner*/

    private var hasQuit = false
    private lateinit var requestHandler: Handler
    /*храниться ссылка на объект Handler , отвечающий за постановку в очередь запросов на загрузку в фоновом потоке
    * ThumbnailDownloader . Этот объект также будет отвечать за обработку сообщений запросов на загрузку при извлечении их из очереди*/

    private val requestMap = ConcurrentHashMap<T, String>()
    /* тип T запроса на загрузку в качестве ключа позволяет хранить и загружать URL-адрес, связанный с конкретным запросом. (Здесь объектом-
    * идентификатором является PhotoHolder , так что по ответу на запрос можно легко вернуться к элементу пользовательского интерфейса,
    * в котором должно находиться загруженное изображение.)*/

    private val flickrFetchr = PhotoGalleryFragment.newInstance().flickrFetchr  //

    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory/8
    private val bitmapCache = LruCache<String, Bitmap>(cacheSize)


    @Suppress("UNCHECKED_CAST")

    /*Проблемы тут получаются только в том случае, если обработчик прикреплен к объекту Looper основного потока.
    * Предупреждение HandlerLeak убирается аннотацией @SuppressLint("HandlerLeak") , так как создаваемый
    * обработчик прикреплен к looper фонового потока*/
    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        requestHandler = object : Handler() {
            /* Handler.handleMessage(...) будет вызываться, когда сообщение загрузки извлечено
            * из очереди и готово к обработке*/
            override fun handleMessage(msg: Message) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    val target = msg.obj as T
                    Log.i(TAG, "Got a request for URL: ${requestMap[target]}")
                    handleRequest(target)
                }
            }
        }
    }

    private fun handleRequest(target: T) {
        val url = requestMap[target] ?: return

      //  val bitmap = flickrFetchr.fetchPhoto(url) ?: return
        val bitmap: Bitmap = bitmap(url) ?: return

        /*Мы проверяем существование URL-адреса, после чего передаем его новому экземпляру FlickrFetchr.
        * При этом используется функция FlickrFetchr.getUrlBytes(...) */

        responseHandler.post(Runnable {
            if (requestMap[target] != url || hasQuit) {
                return@Runnable
            }
            requestMap.remove(target)
            onThumbnailDownloader(target, bitmap)
        }
        )
        /*т responseHandler связывается с Looper главного потока, весь код функции run() в Runnable будет выполнен в главном потоке.
        * Сначала проверяет requestMap . Такая проверка необходима, потому что RecyclerView заново использует свои представления. К тому
        * времени, когда ThumbnailDownloader завершит загрузку Bitmap , может оказаться, что виджет RecyclerView уже переработал ImageView
        * и запросил для него изображение с другого URL-адреса. Эта проверка гарантирует, что каждый объект PhotoHolder получит правильное
        * изображение.
        * Затем проверяется hasQuit . Если выполнение ThumbnailDownloader уже завершилось, выполнение каких-либо обратных вызовов небезопасно.
        * Наконец, мы удаляем из requestMap связь « PhotoHolder —URL» и назначаем изображение для PhotoHolder*/
    }

    override fun quit(): Boolean { //функции quit() говорит о завершении потока
        hasQuit = true
        return super.quit()
    }

    //2 вариант наблюдателя за жизненым циклом фрагмента
    val fragmentLifecycleObserver: LifecycleObserver =
        object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
            fun setup() {
                Log.i(TAG, "Starting background thread")

                start()  //запуск при вызове функции PhotoGalleryFragment.onCreate(...)
                looper   //доступ к looper после вызова функции start() на ThumbnailDownloader
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun tearDown() {
                Log.i(TAG, "Destroying background thread")
                quit()  //остановка при вызове функции PhotoGalleryFragment.onDestroy()
            }
        }

    //наблюдатель за жизненым циклом представления
    val viewLifecycleObserver: LifecycleObserver =
        object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun clearQueue() {
                Log.i(TAG, "Clearing all requests from queue")
                requestHandler.removeMessages(MESSAGE_DOWNLOAD)
                requestMap.clear()
            }
        }
    /*
    *1вариант наблюдателя за жизненым циклом фрагмента
    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun setup() {
        Log.i(TAG, "Starting background thread")

        start()  //запуск при вызове функции PhotoGalleryFragment.onCreate(...)
        looper   //доступ к looper после вызова функции start() на ThumbnailDownloader
    }
    *//* аннотация @OnLifecycleEvent(Lifecycle.Event), позволяющая ассоциировать
    *функцию в вашем классе с обратным вызовом жизненного цикла.
    *Lifecycle.Event.ON_CREATE регистрирует вызов функции ThumbnailDownloader.setup()
    *при вызове функции LifecycleOwner.onCreate(...).
    *Lifecycle.Event.ON_DESTROY регистрирует вызов функции ThumbnailDownloader.
    *tearDown()при вызове функции LifecycleOwner.onDestroy()*//*

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun tearDown() {
        Log.i(TAG, "Destroying background thread")
        quit()  //остановка при вызове функции PhotoGalleryFragment.onDestroy()
        *//* нужно вызывать функцию quit() для завершения потока. Это важно.
        * Если вы не выйдете из HandlerThreads, он будет жить вечно*//*
    }*/

    fun queueThumbnail(target: T, url: String) {
        /*queueThumbnail() ожидает получить объект типа T, выполняющий
        *функции идентификатора загрузки, и String с URL-адресом для загрузки. Эта
        *функция будет вызываться PhotoAdapter в его реализации onBindViewHolder(...)*/
        Log.i(TAG, "Got a URL: $url")

        requestMap[target] = url    //Сообщение берется непосредственно из переменной requestHandler , в результате
        //чего поле target нового объекта Message немедленно заполняется переменной requestHandler
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
            .sendToTarget()
    }

    fun bitmap(url: String):Bitmap? {
        var bit = bitmapCache.get(url)

        if (bit == null) {
            bit = flickrFetchr.fetchPhoto(url)
            try {
                    bitmapCache.put(url, bit!!)
            } catch(e: Exception) {
                bit = null
            }
        }
        return bit
    }
}