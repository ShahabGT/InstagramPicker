package ir.shahabazimi.instagrampicker.models

import java.io.Serializable

/**
 * @Author: Shahab Azimi
 * @Date: 2023 - 03 - 22
 **/
data class GalleryModel(
    val address: String,
    var selectable: Boolean = false,
    var isSelected: Boolean = false
) : Serializable
