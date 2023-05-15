package combignerdranch.android.photogallery

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import combignerdranch.android.photogallery.api.PhotoResponse
import java.lang.reflect.Type

class PhotoDeserializer: JsonDeserializer<PhotoResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): PhotoResponse? {
        val ob = json?.asJsonObject?.get("photo")
        val x = context?.deserialize<PhotoResponse>(ob, PhotoResponse::class.java)
        return x
    }
}