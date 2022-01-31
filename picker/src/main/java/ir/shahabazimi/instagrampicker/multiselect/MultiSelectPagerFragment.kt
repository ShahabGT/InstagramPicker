package ir.shahabazimi.instagrampicker.multiselect

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import ir.shahabazimi.instagrampicker.databinding.FragmentMultiSelectPagerBinding

private const val ARG_PARAM1 = "pic"
private const val ARG_PARAM2 = "position"

class MultiSelectPagerFragment private constructor (private val click:(String,Int)->Unit) : Fragment() {
    private var pic: String = ""
    private var position=0

private lateinit var b: FragmentMultiSelectPagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            pic = it.getString(ARG_PARAM1) ?:""
            position = it.getInt(ARG_PARAM2)
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

        b.multiSelectPagerPic.setOnClickListener {
            click(pic,position)
        }

    }

    companion object {

        @JvmStatic
        fun newInstance(pic: String,position:Int,click:(String,Int)->Unit) =
            MultiSelectPagerFragment(click).apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, pic)
                    putInt(ARG_PARAM2, position)
                }
            }
    }
}