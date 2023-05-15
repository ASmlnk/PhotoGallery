package combignerdranch.android.photogallery.api

import com.google.gson.annotations.SerializedName
import combignerdranch.android.photogallery.GalleryItem

class PhotoResponse {
    @SerializedName("photo")
    lateinit var galleryItems: List<GalleryItem>
}