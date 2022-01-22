package ir.shahabazimi.instagrampicker.gallery

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ir.shahabazimi.instagrampicker.classes.Const
import ir.shahabazimi.instagrampicker.databinding.RowGalleryPicsBinding

class GalleryAdapter(
    private val list: List<GalleryModel>,
    private val multiSelect: Boolean,
    private val select:(List<String>) -> Unit
) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {
    var count = 0
    val selectedPics = mutableListOf<String>()

    init {
        if (multiSelect) {
            count = Const.numberOfPictures
        }
        if (!multiSelect) {
            list.map { it.isSelected = false }
        }
    }

    inner class ViewHolder(private val v: RowGalleryPicsBinding) : RecyclerView.ViewHolder(v.root) {


        fun bind(model: GalleryModel) {
            Glide.with(v.rowGalleryPic)
                .load(Uri.parse(model.address))
                .override(150,150)
                .centerCrop()
                .into(v.rowGalleryPic)

            if (model.isSelected)
                v.rowGallerySelect.visibility = View.VISIBLE
            else
                v.rowGallerySelect.visibility = View.GONE

            v.root.setOnClickListener {
                if (multiSelect) {

                    if (count == selectedPics.size) {
                        if (model.isSelected) {
                            v.rowGallerySelect.visibility = View.GONE
                            selectedPics.remove(model.address)
                        }
                    } else {

                        model.isSelected = !model.isSelected
                        if (model.isSelected)
                            v.rowGallerySelect.visibility = View.VISIBLE
                        else
                            v.rowGallerySelect.visibility = View.GONE
                        if (model.isSelected) {
                            v.rowGallerySelect.isChecked = !v.rowGallerySelect.isChecked
                            selectedPics.add(model.address)
                        } else {
                            selectedPics.remove(model.address)
                        }
                        select(selectedPics)
                    }
                } else
                    select(listOf(model.address))


            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            RowGalleryPicsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(h: ViewHolder, position: Int) {
        val model = list[position]
        h.bind(model)
    }

    override fun getItemCount() = list.size
}
