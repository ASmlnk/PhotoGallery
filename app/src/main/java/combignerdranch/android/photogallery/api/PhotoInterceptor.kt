package combignerdranch.android.photogallery.api

import android.util.Log
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

private const val API_KEY = "e6dbf158a5322e590962f3653190009a" //api ключ полученый на сайте
private const val TAG = "MY"

class PhotoInterceptor: Interceptor {   //перехвадчик
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request() //функция для доступа к исходному запросу
        Log.i(TAG,"$API_KEY")
        val newUrl: HttpUrl = originalRequest.url.newBuilder()
            .addQueryParameter("api_key", API_KEY)
           .addQueryParameter("format", "json")
            .addQueryParameter("nojsoncallback", "1")
            .addQueryParameter("extras", "url_s")
            .addQueryParameter("safesearch", "1")
            .build()
        Log.i(TAG,"$API_KEY")

        val newRequest: Request = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}