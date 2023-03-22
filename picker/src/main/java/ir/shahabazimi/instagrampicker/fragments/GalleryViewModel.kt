package ir.shahabazimi.instagrampicker.fragments

import androidx.lifecycle.ViewModel
import ir.shahabazimi.instagrampicker.models.GalleryModel
import java.text.FieldPosition

/**
 * @Author: Shahab Azimi
 * @Date: 2023 - 03 - 22
 **/
class GalleryViewModel : ViewModel() {


    private val data = mutableListOf<GalleryModel>()

    fun clearData() = data.clear()

    fun addData(position: Int = 0, model: GalleryModel) = data.add(position, model)

    fun getData() = data

    private val selectedPics = mutableListOf<String>()

    fun clearSelectedPics() = selectedPics.clear()

    fun addAllSelectedPics(pics:List<String>) = selectedPics.addAll(pics)

    fun addSelectedPic(position: Int=0,pic:String) = selectedPics.add(position,pic)

    fun lastSelectedPic() = selectedPics.last()


}