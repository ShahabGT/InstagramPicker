package ir.shahabazimi.instagrampicker.multiselect

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import ir.shahabazimi.instagrampicker.R
import ir.shahabazimi.instagrampicker.databinding.FragmentMultiSelectPagerBinding

private const val ARG_PARAM1 = ""

class MultiSelectPagerFragment : Fragment() {
    private var pic: String? = null

private lateinit var b: FragmentMultiSelectPagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pic = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b= FragmentMultiSelectPagerBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(this)
            .load(Uri.parse(pic))
            .fitCenter()
            .into(b.multiSelectPagerPic)

    }

    companion object {

        @JvmStatic
        fun newInstance(pic: String) =
            MultiSelectPagerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, pic)
                }
            }
    }
}