package ir.shahabazimi.instagrampicker.filter

import android.graphics.Bitmap
import com.zomato.photofilters.imageprocessors.Filter

data class FilterItem(
    val filter:Filter,
    val image: Bitmap
)