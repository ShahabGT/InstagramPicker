package ir.shahabazimi.instagrampicker.filter

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.zomato.photofilters.FilterPack
import com.zomato.photofilters.imageprocessors.Filter
import ir.shahabazimi.instagrampicker.R
import ir.shahabazimi.instagrampicker.databinding.FragmentFilterBinding
import ir.shahabazimi.instagrampicker.filter.FilterActivity.ViewPagerAdapter
import ir.shahabazimi.instagrampicker.filter.FiltersListFragment.FiltersListFragmentListener
import java.lang.Exception


class FilterFragment : Fragment(), FiltersListFragmentListener {
    init {
        System.loadLibrary("NativeImageProcessor")
    }

    private lateinit var b: FragmentFilterBinding
    private var picAddress: Uri? = null
    private lateinit var originalImage: Bitmap
    private lateinit var filteredImage: Bitmap
    private lateinit var finalImage: Bitmap
    private lateinit var filtersListFragment: FiltersListFragment

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

    private fun init() {
        picAddress = requireArguments().getParcelable("pic")
        if (picAddress == null) NavHostFragment.findNavController(this).popBackStack()

        filtersListFragment = FiltersListFragment()
        filtersListFragment.setListener(this)
        setupViewPager()
        renderImage(picAddress!!)

    }

    override fun onFilterSelected(filter: Filter) {
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true)
        Glide.with(this)
            .load(filter.processFilter(filteredImage))
            .fitCenter()
            .into(b.imagePreview)
        finalImage = filteredImage.copy(Bitmap.Config.ARGB_8888, true)
    }

    private fun renderImage(uri: Uri) {
        val bitmap = if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(
                requireActivity().contentResolver,
                uri
            )
        } else {
            val source = ImageDecoder.createSource(requireActivity().contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
        originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true)
        finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true)
        Glide.with(this)
            .load(originalImage)
            .fitCenter()
            .into(b.imagePreview)
        bitmap.recycle()
        filtersListFragment.prepareThumbnail(originalImage)
    }

    private fun setupViewPager() {

        val adapter = FilterPagerAdapter(this, filtersListFragment)

        b.viewpager.adapter = adapter


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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == 101) {
            val bitmap = BitmapUtils.getBitmapFromGallery(requireContext(), data!!.data)
            originalImage = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            filteredImage = originalImage.copy(Bitmap.Config.ARGB_8888, true)
            finalImage = originalImage.copy(Bitmap.Config.ARGB_8888, true)
            Glide.with(this)
                .load(originalImage)
                .fitCenter()
                .into(b.imagePreview)
            bitmap.recycle()
            filtersListFragment.prepareThumbnail(originalImage)
        } else {
            NavHostFragment.findNavController(this).popBackStack()
        }
    }


    inner class FilterPagerAdapter(
        f: Fragment,
        private val filterListFragment: FiltersListFragment
    ) : FragmentStateAdapter(f) {
        override fun getItemCount() = 1

        override fun createFragment(position: Int) = filterListFragment

    }


}