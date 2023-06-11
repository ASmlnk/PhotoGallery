package combignerdranch.android.photogallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import combignerdranch.android.photogallery.api.FlickrApi
import combignerdranch.android.photogallery.api.FlickrResponse
import combignerdranch.android.photogallery.api.PhotoResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "MY"

//класс для работ с Gson версия 1
class FlickrFetchr {

    private val flickrApi: FlickrApi

    init {

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(GsonConverterFactory.create()) //меняем конвертер
            .build()
        flickrApi = retrofit.create(FlickrApi::class.java)

    }

    suspend fun getFlickrApi(page: Int): List<GalleryItem> {

        val flickrRequest = flickrApi.fetchPhotosPage(page)
        val flickrResponse: FlickrResponse? = flickrRequest.body()
        val photoResponse: PhotoResponse? = flickrResponse?.photos
        var galleryItems: List<GalleryItem> = photoResponse?.galleryItems
            ?: mutableListOf()

        galleryItems =
            galleryItems.filterNot {       // filterNot   исключить по условию т.е. исключаем строки где url содержит пробелы
                it.url.isBlank()  // isBlank() возвращает true для строки, содержащей только пробелы
            }
        return galleryItems
    }

    fun fetchPhotos(page: Int): LiveData<List<GalleryItem>> {

        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()
        val flickrRequest: Call<FlickrResponse> = flickrApi.fetchPhotos(page = 1)

        flickrRequest.enqueue(object :
            Callback<FlickrResponse> {                    //FlickrResponse

            override fun onResponse(
                call: Call<FlickrResponse>,
                response: Response<FlickrResponse>
            ) {

                val flickrResponse: FlickrResponse? = response.body()
                val photoResponse: PhotoResponse? = flickrResponse?.photos
                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems
                    ?: mutableListOf()
                galleryItems =
                    galleryItems.filterNot {       // filterNot   исключить по условию т.е. исключаем строки где url содержит пробелы
                        it.url.isBlank()  // isBlank() возвращает true для строки, содержащей только пробелы
                    }
                responseLiveData.value = galleryItems
            }

            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
            }
        })
        return responseLiveData
    }

    fun fetchPhotosPage(page: Int): LiveData<List<GalleryItem>> {

        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()
        val responseList: MutableList<GalleryItem> = mutableListOf()
        val flickrRequest: Call<FlickrResponse> = flickrApi.fetchPhotos(page = page)

        flickrRequest.enqueue(object :
            Callback<FlickrResponse> {                    //FlickrResponse

            override fun onResponse(
                call: Call<FlickrResponse>,
                response: Response<FlickrResponse>
            ) {

                val flickrResponse: FlickrResponse? = response.body()
                val photoResponse: PhotoResponse? = flickrResponse?.photos
                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems
                    ?: mutableListOf()
                galleryItems =
                    galleryItems.filterNot {       // filterNot   исключить по условию т.е. исключаем строки где url содержит пробелы
                        it.url.isBlank()  // isBlank() возвращает true для строки, содержащей только пробелы
                    }

                responseLiveData.value = galleryItems
                // responseList.addAll(galleryItems)
            }

            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
                Log.e(TAG, "Failed to fetch photos", t)
            }
        })
        return responseLiveData
    }

    @WorkerThread  //указывает что функция выполняется в фоноыом потоке
    fun fetchPhoto(url: String): Bitmap? {
        val response: Response<ResponseBody> = flickrApi.fetchUrlBytes(url).execute()
        val bitmap = response.body()?.byteStream()?.use(BitmapFactory::decodeStream)
        /*Объект java.io.InputStream извлекается из тела ответа с помощью функции
          ResponseBody.byteStream(). Получив поток байтов, мы передаем его функции
          BitmapFactory.decodeStream(InputStream), которая создаст Bitmap из данных в потоке
          Ответный и байтовый потоки должны быть закрытыми. Так как InputStream реализует атрибут Closeable, то стандартная функция библиотеки Kotlin use(...)
          выполнит чистку при возвращении BitmapFactory.decodeStream(...)*/

        Log.i(TAG, "Decoded bitmap=$bitmap from Response=$response")
        return bitmap
    }
}

//класс для работ с Gson версия 2 с Deserializer
class FlickrFetchrDeserializer {

    private val flickrApi: FlickrApi

    init {

        val gSon = GsonBuilder().registerTypeAdapter(PhotoResponse::class.java, PhotoDeserializer())
            .create()  //+

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(GsonConverterFactory.create(gSon)) //меняем конвертер  //+
            .build()
        flickrApi = retrofit.create(FlickrApi::class.java)
    }

    fun fetchPhotos(): LiveData<List<GalleryItem>> {

        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()
        val flickrRequest: Call<PhotoResponse> = flickrApi.fetchPhotosDeserializer()          //+

        flickrRequest.enqueue(object :
            Callback<PhotoResponse> {                    //FlickrResponse  //+

            override fun onResponse(
                call: Call<PhotoResponse>,
                response: Response<PhotoResponse>                     //+
            ) {
                val photoResponse: PhotoResponse? = response.body()     //+
                Log.d(TAG, "Response received ${photoResponse?.galleryItems}")
                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems
                    ?: mutableListOf()
                galleryItems =
                    galleryItems.filterNot {       // filterNot   исключить по условию т.е. исключаем строки где url содержит пробелы
                        it.url.isBlank()  // isBlank() возвращает true для строки, содержащей только пробелы
                    }
                responseLiveData.value = galleryItems
            }

            override fun onFailure(call: Call<PhotoResponse>, t: Throwable) {
                Log.e(TAG, "Failed to fetch photos", t)
            }
        })
        return responseLiveData
    }
}


/* класс для работы со скаларс конвертером
class FlickrFetchr {

    private val flickrApi: FlickrApi

    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        flickrApi = retrofit.create(FlickrApi::class.java)
    }

    fun fetchPhotos(): LiveData<String> {

        val responseLiveData: MutableLiveData<String> = MutableLiveData()
        val flickrRequest: Call<String> = flickrApi.fetchPhotos()

        flickrRequest.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d(TAG, "Response received")
                responseLiveData.value = response.body()
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e(TAG, "Failed to fetch photos", t)
            }
        })
        return responseLiveData
    }
}*/
