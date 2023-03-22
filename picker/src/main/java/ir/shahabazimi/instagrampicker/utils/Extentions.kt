package ir.shahabazimi.instagrampicker.utils

import android.Manifest
import android.os.Build
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import ir.shahabazimi.instagrampicker.R

/**
 * @Author: Shahab Azimi
 * @Date: 2023 - 03 - 22
 **/
fun View.visibilityState(visible: Boolean) {
    visibility =
        if (visible) View.VISIBLE
        else View.GONE
}

fun Fragment.navigate(resId: Int) = NavHostFragment.findNavController(this).navigate(resId)

fun Fragment.popBackStack() = NavHostFragment.findNavController(this).popBackStack()

fun externalStoragePermission() =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_IMAGES
    else Manifest.permission.READ_EXTERNAL_STORAGE