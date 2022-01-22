package ir.shahabazimi.instagrampicker.gallery

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ir.shahabazimi.instagrampicker.classes.Const
import ir.shahabazimi.instagrampicker.databinding.RowGalleryPicsBinding

class GalleryAdapter(
    private val select: (List<String>) -> Unit
) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    private val selectedPics = mutableListOf<String>()


    private val diffCallback = object : DiffUtil.ItemCallback<GalleryModel>() {
        override fun areItemsTheSame(oldItem: GalleryModel, newItem: GalleryModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: GalleryModel, newItem: GalleryModel): Boolean {
            return oldItem.address == newItem.address && oldItem.isSelected == newItem.isSelected && oldItem.selectable == newItem.selectable
        }
    }

    private val diff = AsyncListDiffer(this, diffCallback)

    inner class ViewHolder(private val v: RowGalleryPicsBinding) : RecyclerView.ViewHolder(v.root) {


        fun bind(model: GalleryModel) {
            Glide.with(v.rowGalleryPic)
                .load(Uri.parse(model.address))
                .override(150, 150)
                .centerCrop()
                .into(v.rowGalleryPic)

            if (model.selectable && model.isSelected) {
                v.rowGallerySelect.visibility = View.VISIBLE
            } else
                v.rowGallerySelect.visibility = View.GONE


            v.root.setOnClickListener {
                if (Const.numberOfPictures > 1 && model.selectable) {

                    if (Const.numberOfPictures == selectedPics.size) {
                        if (model.isSelected) {
                            v.rowGallerySelect.visibility = View.GONE
                            selectedPics.remove(model.address)
                        }
                    } else {

                        model.isSelected = !model.isSelected
                        if (model.isSelected) {
                            selectedPics.add(model.address)
                            v.rowGallerySelect.visibility = View.VISIBLE
                        } else {
                            v.rowGallerySelect.visibility = View.GONE
                            selectedPics.remove(model.address)
                        }
                    }
                    select(selectedPics)

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
        val model = diff.currentList[position]
        h.bind(model)
    }

    fun update(data: List<GalleryModel>) {
        diff.submitList(data)
    }

    fun multiSelect(enabled: Boolean) {
        selectedPics.clear()
        val data = diff.currentList.toMutableList()
        data.forEach { it.isSelected = false; it.selectable = enabled }
        diff.submitList(data)
    }

    override fun getItemCount() = diff.currentList.size
}
