package combignerdranch.android.photogallery

import android.os.HandlerThread
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

private const val TAG = "ThumbnailDownloader"

class ThumbnailDownloader<in T> : HandlerThread(TAG), LifecycleObserver {
    /*Классу передается один обобщенный аргумент <T>. Пользователю ThumbnailDownloader
    понадобится объект для идентификации каждой загрузки и определения элемента
    пользовательского интерфейса, который должен обновляться
    после завершения загрузки*/

    /*Реализация LifecycleObserver означает, что вы можете
    подписать ThumbnailDownloader на получение обратных вызовов жизненного
    цикла от любого владельца LifecycleOwner*/

    private var hasQuit = false

    override fun quit(): Boolean { //функции quit() говорит о завершении потока
        hasQuit = true
        return super.quit()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun setup() {
        Log.i(TAG, "Starting background thread")

        start()  //запуск при вызове функции PhotoGalleryFragment.onCreate(...)
        looper   //доступ к looper после вызова функции start() на ThumbnailDownloader
    }
    /* аннотация @OnLifecycleEvent(Lifecycle.Event), позволяющая ассоциировать
    функцию в вашем классе с обратным вызовом жизненного цикла.
    Lifecycle.Event.ON_CREATE регистрирует вызов функции ThumbnailDownloader.setup()
    при вызове функции LifecycleOwner.onCreate(...).
    Lifecycle.Event.ON_DESTROY регистрирует вызов функции ThumbnailDownloader.
    tearDown()при вызове функции LifecycleOwner.onDestroy()*/

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun tearDown() {
        Log.i(TAG, "Destroying background thread")
        quit()  //остановка при вызове функции PhotoGalleryFragment.onDestroy()
        /* нужно вызывать функцию quit() для завершения потока. Это важно.
        Если вы не выйдете из HandlerThreads, он будет жить вечно*/
    }

    fun queueThumbnail(target: T, url: String) {
        /*queueThumbnail() ожидает получить объект типа T, выполняющий
        функции идентификатора загрузки, и String с URL-адресом для загрузки. Эта
        функция будет вызываться PhotoAdapter в его реализации onBindViewHolder(...)*/
        Log.i(TAG, "Got a URL: $url")
    }
}