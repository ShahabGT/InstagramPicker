package ir.shahabazimi.instagrampicker.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import ir.shahabazimi.instagrampicker.R
import ir.shahabazimi.instagrampicker.adapters.GalleryAdapter
import ir.shahabazimi.instagrampicker.databinding.FragmentGalleryBinding
import ir.shahabazimi.instagrampicker.models.GalleryModel
import ir.shahabazimi.instagrampicker.utils.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * @Author: Shahab Azimi
 * @Date: 2023 - 03 - 22
 **/
class GalleryFragment : BaseFragment<FragmentGalleryBinding>() {

    private lateinit var viewModel: GalleryViewModel

    private lateinit var galleryAdapter: GalleryAdapter
    override fun bindView(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentGalleryBinding.inflate(inflater, container, false)

    private lateinit var storageExecutor: ExecutorService
    private lateinit var storagePermission: ActivityResultLauncher<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[GalleryViewModel::class.java]
        storageExecutor = Executors.newSingleThreadExecutor()
        storagePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) init()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.let {
            it.title = getString(R.string.instagrampicker_gallery_title)
            it.setDisplayHomeAsUpEnabled(false)
            it.setDisplayShowHomeEnabled(false)
        }
        setupPermissions()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<MaterialToolbar>(R.id.select_toolbar).visibility =
            View.VISIBLE
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            requireContext(),
            externalStoragePermission()
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    externalStoragePermission()
                )
            ) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage(getString(R.string.storage_permission_message))
                    .setTitle(getString(R.string.storage_permission_title))

                builder.setPositiveButton(
                    getString(R.string.storage_permission_positive)
                ) { _, _ ->
                    storagePermission.launch(externalStoragePermission())
                }
                builder.setNegativeButton(getString(R.string.storage_permission_negative)) { a, _ ->
                    a.dismiss()
                }

                val dialog = builder.create()
                dialog.show()
            } else if (!InstaPickerSharedPreference(requireContext()).getStoragePermission()) {
                storagePermission.launch(externalStoragePermission())
                InstaPickerSharedPreference(requireContext()).setStoragePermission()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.storage_permission_deny),
                    Toast.LENGTH_SHORT
                ).show()

                startActivity(Intent().also {
                    it.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    it.data = Uri.fromParts("package", requireActivity().packageName, null)
                }
                )
            }
        } else {
            init()
        }
    }

    private fun init() = with(binding) {
        viewModel.clearData()
        viewModel.clearSelectedPics()

        galleryMultiselect.visibilityState(Const.numberOfPictures != 1)

        galleryCamera.setOnClickListener { navigate(R.id.action_bnv_gallery_to_bnv_camera) }

        galleryMultiselectLayout.setOnClickListener {
            Const.multiSelect = !Const.multiSelect
            viewModel.clearSelectedPics()
            val positionView =
                (galleryRecycler.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
            galleryMultiselectLayout.setBackgroundResource(if (Const.multiSelect) R.drawable.img_bg_selected else R.drawable.img_bg)
            galleryAdapter.multiSelect(Const.multiSelect)
            galleryRecycler.layoutManager?.scrollToPosition(positionView)
        }

        galleryAdapter = GalleryAdapter { addresses ->
            if (addresses.isNotEmpty()) {
                viewModel.clearSelectedPics()
                viewModel.addAllSelectedPics(addresses)
                Glide.with(this@GalleryFragment)
                    .load(Uri.parse(viewModel.lastSelectedPic()))
                    .fitCenter()
                    .into(galleryView)

                appbarLayout.setExpanded(true, true)
            }
        }
        galleryRecycler.apply {
            layoutManager = GridLayoutManager(
                requireContext(),
                4,
                RecyclerView.VERTICAL,
                false
            )
            setHasFixedSize(true)
            adapter = galleryAdapter

        }
        getPicturePaths()

    }

    @SuppressLint("Range")
    private fun getPicturePaths() {
        val allImagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = Array(2) {
            if (it == 0) MediaStore.Images.ImageColumns.DATA
            else MediaStore.Images.Media._ID
        }

        val cursor = requireActivity().contentResolver.query(
            allImagesUri,
            projection,
            null,
            null,
            MediaStore.Images.Media.DATE_ADDED
        )

        if (cursor != null && cursor.moveToFirst()) {

            do {
                val dataPath = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))
                ).toString()
                val data = GalleryModel(dataPath, selectable = false, isSelected = false)
                viewModel.addData(model = data)
            } while (cursor.moveToNext())

            galleryAdapter.update(viewModel.getData())

            if (viewModel.getData().first().address.isNotEmpty()) {
                viewModel.clearSelectedPics()
                viewModel.addSelectedPic(pic = viewModel.getData().first().address)
                Glide.with(requireContext())
                    .load(Uri.parse(viewModel.getData().first().address))
                    .fitCenter()
                    .into(binding.galleryView)
            }
            cursor.close()
        }
    }
}
