package ir.shahabazimi.instagrampicker.gallery

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import ir.shahabazimi.instagrampicker.InstagramPicker
import ir.shahabazimi.instagrampicker.databinding.RowGalleryPicsBinding

class GalleryAdapter(
    private val list: List<GalleryModel>,
    private val galleySelectedListener: GalleySelectedListener,
    private val multiSelect: Boolean
) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {
    var count = 0
    val selectedPics = mutableListOf<String>()

    init {
        if (multiSelect) {
            count = InstagramPicker.numberOfPictures
        }
        if (!multiSelect) {
            list.map { it.isSelected = false }
        }
    }

    inner class ViewHolder(private val v: RowGalleryPicsBinding) : RecyclerView.ViewHolder(v.root) {


        fun bind(model: GalleryModel) {
            Picasso.get().load(Uri.parse(model.address))
                .resize(150, 150)
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
                        galleySelectedListener.onMultiSelect(selectedPics)
                    }
                } else
                    galleySelectedListener.onSingleSelect(model.address)


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
