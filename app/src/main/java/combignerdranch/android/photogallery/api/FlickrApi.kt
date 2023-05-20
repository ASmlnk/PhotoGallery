package combignerdranch.android.photogallery.api

import androidx.annotation.IntRange
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface FlickrApi {

    @GET(
        "services/rest/?method=flickr.interestingness.getList" +
                "&api_key=e6dbf158a5322e590962f3653190009a" +   //код ролученый на сайте
                "&format=json" +                                //по умолчанию формат xml, мы указываем json
                "&nojsoncallback=1" +                           //указываем убрать все круглые скобки в ответе
                "&extras=url_s"                                 //добавить url_s адрес мини-версии изображения если он есть
               // "&pages = 2"
    )
    fun fetchPhotos(@Query("page")@IntRange(from = 3) page :Int = 3): Call<PhotoResponse>


}