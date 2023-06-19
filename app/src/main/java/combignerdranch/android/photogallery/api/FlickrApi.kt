package combignerdranch.android.photogallery.api

import androidx.annotation.IntRange
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface FlickrApi {

    @GET("services/rest/?method=flickr.interestingness.getList")
    suspend fun fetchPhotosPage(
        @Query("page") page: Int
    ): Response<FlickrResponse>

    @GET
    fun fetchUrlBytes(@Url url: String): Call<ResponseBody>

    @GET("services/rest/?method=flickr.photos.search")
    fun searchPhotos(
        @Query("text") query: String,
        @Query("page") page: Int
    ): Call<FlickrResponse>
    /* Аннотация @Query позволяет динамически добавлять к URL параметры запроса.
    * В данном случае мы добавляем параметр запроса text. Значение, присваиваемое
    * параметру, зависит от аргумента, переданного в searchPhotos(String). Например,
    * вызов searchPhotos("robot") добавит в URL приписку text=robot*/
}




/* до 26 главы без поиска
 interface FlickrApi {

    @GET(
        "services/rest/?method=flickr.interestingness.getList" +
                "&api_key=e6dbf158a5322e590962f3653190009a" +   //код ролученый на сайте
                "&format=json" +                                //по умолчанию формат xml, мы указываем json
                "&nojsoncallback=1" +                           //указываем убрать все круглые скобки в ответе
                "&extras=url_s"                                 //добавить url_s адрес мини-версии изображения если он есть
    )
    fun fetchPhotos(
        @Query("page") @IntRange(from = 1) page: Int = 1
    ): Call<FlickrResponse>

    @GET(
        "services/rest/?method=flickr.interestingness.getList" +
                "&api_key=e6dbf158a5322e590962f3653190009a" +   //код ролученый на сайте
                "&format=json" +                                //по умолчанию формат xml, мы указываем json
                "&nojsoncallback=1" +                           //указываем убрать все круглые скобки в ответе
                "&extras=url_s"                                 //добавить url_s адрес мини-версии изображения если он есть
    )
    fun fetchPhotosDeserializer(
        @Query("page") @IntRange(from = 1) page: Int = 1
    ): Call<PhotoResponse>

    @GET(
        "services/rest/?method=flickr.interestingness.getList" +
                "&api_key=e6dbf158a5322e590962f3653190009a" +   //код ролученый на сайте
                "&format=json" +                                //по умолчанию формат xml, мы указываем json
                "&nojsoncallback=1" +                           //указываем убрать все круглые скобки в ответе
                "&extras=url_s"                                 //добавить url_s адрес мини-версии изображения если он есть
    )
    suspend fun fetchPhotosPage(
        @Query("page") page: Int
    ): Response<FlickrResponse>

    @GET
    fun fetchUrlBytes(@Url url: String): Call<ResponseBody>
}
* */