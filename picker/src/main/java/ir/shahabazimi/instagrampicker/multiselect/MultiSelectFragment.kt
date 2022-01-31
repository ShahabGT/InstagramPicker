package ir.shahabazimi.instagrampicker.multiselect

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.yalantis.ucrop.UCrop
import ir.shahabazimi.instagrampicker.R
import ir.shahabazimi.instagrampicker.classes.Const
import ir.shahabazimi.instagrampicker.databinding.FragmentMultiSelectBinding
import ir.shahabazimi.instagrampicker.filter.FilterActivity
import java.io.File
import kotlin.math.abs


class MultiSelectFragment : Fragment() {

    private lateinit var b: FragmentMultiSelectBinding
    private lateinit var addresses: List<String>
    private lateinit var multiSelectPagerAdapter: MultiSelectPagerAdapter
    private val finalAddresses = mutableListOf<String>()
    private var position = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentMultiSelectBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.let {
            it.title = getString(R.string.instagrampicker_multi_select_title)
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.vector_prev)
        }
        setHasOptionsMenu(true)
        init()
    }

    private fun init() {
        addresses = requireArguments().getStringArray("pics")?.toList() ?: emptyList()
        if (addresses.isNullOrEmpty()) NavHostFragment.findNavController(this).popBackStack()

        initViewPager()
    }

    private fun initViewPager() {
        multiSelectPagerAdapter = MultiSelectPagerAdapter(this, addresses){pic,pos->
            position=pos
            startCropping(pic)
        }
        b.multiSelectViewpager.apply {
            adapter = multiSelectPagerAdapter
            offscreenPageLimit = 1
            val nextItemVisiblePx = resources.getDimension(R.dimen.viewpager_next_item_visible)
            val currentItemHorizontalMarginPx = resources.getDimension(R.dimen.viewpager_current_item_horizontal_margin)
            val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
            val pageTransformer = ViewPager2.PageTransformer { page: View, position: Float ->
                page.translationX = -pageTranslationX * position
                page.scaleY = 1 - (0.25f * abs(position))
            }
            setPageTransformer(pageTransformer)
            val itemDecoration = HorizontalMarginItemDecoration(
                context,
                R.dimen.viewpager_current_item_horizontal_margin
            )
            addItemDecoration(itemDecoration)
        }
    }

    private fun startCropping(pic:String) {
        val options = UCrop.Options()
        options.setToolbarTitle(getString(R.string.instagrampicker_crop_title))
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
    //    options.withMaxResultSize(2000, 2000)
        UCrop.of(
            Uri.parse(pic),
            Uri.fromFile(File(requireActivity().cacheDir, Const.getCurrentDate()))
        )
            .withAspectRatio(Const.cropXRatio, Const.cropYRatio)
            .withOptions(options)
            .start(requireContext(), this)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == UCrop.REQUEST_CROP && data != null) {
            val resultUri = UCrop.getOutput(data)
            FilterActivity.picAddress = resultUri
            startActivity(Intent(requireContext(), FilterActivity::class.java).apply {
                putExtra("uri", resultUri)
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_next->{
                return true
            }
            android.R.id.home->{
                NavHostFragment.findNavController(this).popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}