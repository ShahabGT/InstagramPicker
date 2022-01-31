package ir.shahabazimi.instagrampicker.filter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ir.shahabazimi.instagrampicker.databinding.RowFilterThumbnailsBinding

class FiltersAdapter(private val func: (FilterItem) -> Unit) :
    RecyclerView.Adapter<FiltersAdapter.ViewHolder>() {

    private val diffCallback = object : DiffUtil.ItemCallback<FilterItem>() {
        override fun areItemsTheSame(oldItem: FilterItem, newItem: FilterItem): Boolean {
            return oldItem == newItem
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: FilterItem, newItem: FilterItem): Boolean {
            return oldItem.image == newItem.image
        }
    }

    private val diff = AsyncListDiffer(this, diffCallback)

    inner class ViewHolder(private val v: RowFilterThumbnailsBinding) :
        RecyclerView.ViewHolder(v.root) {

        fun bind(model: FilterItem) {

            v.filterName.text = model.filter.name

            Glide.with(v.thumbnail)
                .load(model.image)
                .override(150, 150)
                .centerCrop()
                .into(v.thumbnail)

            v.root.setOnClickListener {
                func(model)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        RowFilterThumbnailsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun getItemCount() = diff.currentList.size

    override fun onBindViewHolder(h: ViewHolder, position: Int) {
        h.bind(diff.currentList[position])
    }

    fun update(data: List<FilterItem>) {
        diff.submitList(data)
    }
}