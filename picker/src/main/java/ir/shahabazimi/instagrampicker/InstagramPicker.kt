package ir.shahabazimi.instagrampicker

import android.app.Activity
import android.content.Intent
import ir.shahabazimi.instagrampicker.utils.Const
import kotlin.math.abs


class InstagramPicker (private val activity: Activity) {


    fun show(
        CropXRatio: Int,
        CropYRatio: Int,
        numberOfPictures: Int = 1,
        selectedImages:(List<String>)->Unit
    ) {
        val count = when {
            numberOfPictures <= 0 -> 1
            numberOfPictures > 100 -> 100
            else -> numberOfPictures
        }
        Const.addresses= mutableListOf()
        Const.cropXRatio = abs(CropXRatio).toFloat()
        Const.cropYRatio = abs(CropYRatio).toFloat()

        Const.numberOfPictures = count

        activity.startActivity(Intent(activity, SelectActivity::class.java))


    }
}
