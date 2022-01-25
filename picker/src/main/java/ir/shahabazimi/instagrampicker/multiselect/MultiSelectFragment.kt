package ir.shahabazimi.instagrampicker.multiselect

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import ir.shahabazimi.instagrampicker.R
import ir.shahabazimi.instagrampicker.databinding.FragmentMultiSelectBinding
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
        multiSelectPagerAdapter = MultiSelectPagerAdapter(this, addresses)
        b.multiSelectViewpager.apply {
            adapter = multiSelectPagerAdapter
            offscreenPageLimit = 1

            // Add a PageTransformer that translates the next and previous items horizontally
            // towards the center of the screen, which makes them visible
            val nextItemVisiblePx = resources.getDimension(R.dimen.viewpager_next_item_visible)
            val currentItemHorizontalMarginPx =
                resources.getDimension(R.dimen.viewpager_current_item_horizontal_margin)
            val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
            val pageTransformer = ViewPager2.PageTransformer { page: View, position: Float ->
                page.translationX = -pageTranslationX * position
                // Next line scales the item's height. You can remove it if you don't want this effect
                page.scaleY = 1 - (0.25f * abs(position))
                // If you want a fading effect uncomment the next line:
                // page.alpha = 0.25f + (1 - abs(position))
            }
            setPageTransformer(pageTransformer)

            // The ItemDecoration gives the current (centered) item horizontal margin so that
            // it doesn't occupy the whole screen width. Without it the items overlap
            val itemDecoration = HorizontalMarginItemDecoration(
                context,
                R.dimen.viewpager_current_item_horizontal_margin
            )
            addItemDecoration(itemDecoration)


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