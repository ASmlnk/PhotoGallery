package combignerdranch.android.photogallery

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import combignerdranch.android.photogallery.api.FlickrApi
import combignerdranch.android.photogallery.api.FlickrResponse
import combignerdranch.android.photogallery.api.PhotoResponse
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

    fun getFlickrApi(): FlickrApi {

        return flickrApi
    }

    fun fetchPhotos( page: Int ): LiveData<List<GalleryItem>> {

        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()
        val flickrRequest: Call<FlickrResponse> = flickrApi.fetchPhotos(page = 1)

        flickrRequest.enqueue(object : Callback<FlickrResponse> {                    //FlickrResponse

            override fun onResponse(
                call: Call<FlickrResponse>,
                response: Response<FlickrResponse>
            ) {

                val flickrResponse: FlickrResponse? = response.body()
                Log.d(TAG, "Response received ${flickrResponse}")
                val photoResponse: PhotoResponse? = flickrResponse?.photos
                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems
                    ?: mutableListOf()
                galleryItems = galleryItems.filterNot {       // filterNot   исключить по условию т.е. исключаем строки где url содержит пробелы
                    it.url.isBlank()  // isBlank() возвращает true для строки, содержащей только пробелы
                }
                responseLiveData.value = galleryItems
            }

            override fun onFailure(call: Call<FlickrResponse>, t: Throwable) {
                Log.e(TAG, "Failed to fetch photos", t)
            }
        })
        return responseLiveData
    }

    fun fetchPhotosPage ( page: Int ): LiveData<List<GalleryItem>> {
        Log.d(TAG, "Response received ${page}")

        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()
        val responseList: MutableList<GalleryItem> = mutableListOf()
        val flickrRequest: Call<FlickrResponse> = flickrApi.fetchPhotos(page = page)

        flickrRequest.enqueue(object : Callback<FlickrResponse> {                    //FlickrResponse

            override fun onResponse(
                call: Call<FlickrResponse>,
                response: Response<FlickrResponse>
            ) {

                val flickrResponse: FlickrResponse? = response.body()
                Log.d(TAG, "Response received ${flickrResponse}")
                val photoResponse: PhotoResponse? = flickrResponse?.photos
                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems
                    ?: mutableListOf()
                galleryItems = galleryItems.filterNot {       // filterNot   исключить по условию т.е. исключаем строки где url содержит пробелы
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
}

//класс для работ с Gson версия 2 с Deserializer
class FlickrFetchrDeserializer {

    private val flickrApi: FlickrApi

    init {

        val gSon = GsonBuilder().registerTypeAdapter(PhotoResponse::class.java, PhotoDeserializer()).create()  //+

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(GsonConverterFactory.create(gSon)) //меняем конвертер  //+
            .build()
        flickrApi = retrofit.create(FlickrApi::class.java)
    }

    fun fetchPhotos(): LiveData<List<GalleryItem>> {

        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()
        val flickrRequest: Call<PhotoResponse> = flickrApi.fetchPhotosDeserializer()          //+

        flickrRequest.enqueue(object : Callback<PhotoResponse> {                    //FlickrResponse  //+

            override fun onResponse(
                call: Call<PhotoResponse>,
                response: Response<PhotoResponse>                     //+
            ) {
                val  photoResponse: PhotoResponse? = response.body()     //+
                Log.d(TAG, "Response received ${photoResponse?.galleryItems}")
                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems
                    ?: mutableListOf()
                galleryItems = galleryItems.filterNot {       // filterNot   исключить по условию т.е. исключаем строки где url содержит пробелы
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
