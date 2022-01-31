package ir.shahabazimi.instagrampicker.multiselect

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class MultiSelectPagerAdapter(f: Fragment, private val addresses: List<String>,private val click:(String,Int)->Unit) :
    FragmentStateAdapter(f) {
    override fun getItemCount() = addresses.size

    override fun createFragment(position: Int) =
        MultiSelectPagerFragment.newInstance(addresses[position],position,click)


}