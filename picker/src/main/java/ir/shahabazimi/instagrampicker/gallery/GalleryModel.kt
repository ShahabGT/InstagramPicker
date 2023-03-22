package ir.shahabazimi.instagrampicker.gallery

import java.io.Serializable

data class GalleryModel(
    val address: String,
    var selectable: Boolean = false,
    var isSelected: Boolean = false
) : Serializable
