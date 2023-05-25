package combignerdranch.android.photogallery

import android.util.Log
import com.google.gson.*
import combignerdranch.android.photogallery.api.PhotoResponse
import java.lang.reflect.Type

private const val TAG = "PhotoDeserializer"

class PhotoDeserializer : JsonDeserializer<PhotoResponse> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): PhotoResponse? {

        val jsonObject = json as JsonObject
        val jsonList = jsonObject.get("photos").asJsonObject.get("photo").asJsonArray.toList()

        //val ob = obz.asJsonObject.get("photo").asJsonArray.toList()
        //val list: List<JsonElement> = ob.asJsonArray.toList()

        val list: MutableList<GalleryItem> = mutableListOf()
        for (jsonElement in jsonList) {
            val galleryItem = context?.deserialize<GalleryItem>(jsonElement, GalleryItem::class.java)
            list.let {
                list.add(galleryItem!!)
            }
        }
        val listGalleryItem = list.toList()
        val photoResponse = PhotoResponse()
        photoResponse.galleryItems = listGalleryItem

        // val x = context?.deserialize<PhotoResponse>(ob, PhotoResponse::class.java)
        return photoResponse
    }
}

