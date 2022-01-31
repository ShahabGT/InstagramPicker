package ir.shahabazimi.instagrampicker

import android.app.Activity
import android.content.Intent
import androidx.annotation.NonNull
import ir.shahabazimi.instagrampicker.classes.Const
import ir.shahabazimi.instagrampicker.gallery.SelectActivity
import kotlin.math.abs


class InstagramPicker (private val activity: Activity) {




//        private final BroadcastReceiver br = new BroadcastReceiver()
//        {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if (multiSelect)
//                    mListener.selectedPics(addresses);
//                else
//                    sListener.selectedPic(addresses.get(0));
//                activity.unregisterReceiver(br);
//            }
//        };

    fun show(

        @NonNull CropXRatio: Int,
        @NonNull CropYRatio: Int,
        numberOfPictures: Int = 1,
        @NonNull selectedImages:(List<String>)->Unit
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
