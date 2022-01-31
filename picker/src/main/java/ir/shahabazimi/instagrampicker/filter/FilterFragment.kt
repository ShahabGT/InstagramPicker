package ir.shahabazimi.instagrampicker.filter

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zomato.photofilters.FilterPack
import com.zomato.photofilters.imageprocessors.Filter
import com.zomato.photofilters.utils.ThumbnailItem
import com.zomato.photofilters.utils.ThumbnailsManager
import ir.shahabazimi.instagrampicker.R
import ir.shahabazimi.instagrampicker.databinding.FragmentFilterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FilterFragment : Fragment() {
    init {
        System.loadLibrary("NativeImageProcessor")
    }

    private lateinit var b: FragmentFilterBinding
    private lateinit var picAddress: Uri
    private lateinit var picBitmap: Bitmap
    private val thumbnails = mutableListOf<FilterItem>()
    private lateinit var filtersAdapter: FiltersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentFilterBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.let {
            it.title = getString(R.string.instagrampicker_filter_title)
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.vector_prev)
        }
        setHasOptionsMenu(true)
        init()

    }

    private fun getBitmap(uri: Uri) =
        if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(
                requireActivity().contentResolver,
                uri
            )
        } else {
            val source = ImageDecoder.createSource(requireActivity().contentResolver, uri)
            ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.RGBA_F16, true)
        }


    private fun init() {
        picBitmap = getBitmap(requireArguments().getParcelable("pic")!!)

        Glide.with(this)
            .load(picBitmap)
            .fitCenter()
            .into(b.filtersPreview)



        filtersAdapter = FiltersAdapter {
            Glide.with(this)
                .load(it.filter.processFilter(Bitmap.createScaledBitmap(picBitmap, 1024, 1024, false)))
                .fitCenter()
                .into(b.filtersPreview)
        }

        b.filtersRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = filtersAdapter
        }

        FilterPack.getFilterPack(requireActivity()).forEach {
            thumbnails.add(
                FilterItem(
                    it,
                    it.processFilter(Bitmap.createScaledBitmap(picBitmap, 150, 150, false))
                )
            )
        }

        filtersAdapter.update(thumbnails)


    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_next -> {
                return true
            }
            android.R.id.home -> {
                NavHostFragment.findNavController(this).popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


}