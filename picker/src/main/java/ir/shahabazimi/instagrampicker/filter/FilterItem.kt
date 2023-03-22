package ir.shahabazimi.instagrampicker.filter

import android.graphics.Bitmap
import com.zomato.photofilters.imageprocessors.Filter
import java.io.Serializable

data class FilterItem(
    val filter: Filter,
    val image: Bitmap
) : Serializable {
    override fun equals(other: Any?): Boolean {
        return if (other is FilterItem) {
            this.filter == other.filter &&
                    this.image.sameAs(other.image)
        } else false
    }

    override fun hashCode(): Int {
        var result = filter.hashCode()
        result = 31 * result + image.hashCode()
        return result
    }
}