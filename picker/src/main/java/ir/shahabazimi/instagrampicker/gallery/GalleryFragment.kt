package ir.shahabazimi.instagrampicker.gallery

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
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
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.yalantis.ucrop.UCrop
import ir.shahabazimi.instagrampicker.InstagramPicker
import ir.shahabazimi.instagrampicker.R
import ir.shahabazimi.instagrampicker.classes.Const
import ir.shahabazimi.instagrampicker.classes.InstaPickerSharedPreference
import ir.shahabazimi.instagrampicker.classes.Statics
import ir.shahabazimi.instagrampicker.databinding.FragmentGalleryBinding
import ir.shahabazimi.instagrampicker.filter.FilterActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.max


class GalleryFragment : Fragment() {


    private lateinit var adapter: GalleryAdapter
    private var multiSelect = false
    private val data = mutableListOf<GalleryModel>()
    private val selectedPics = mutableListOf<String>()

    private lateinit var b: FragmentGalleryBinding

    private lateinit var storageExecutor: ExecutorService
    private lateinit var storagePermission: ActivityResultLauncher<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storageExecutor = Executors.newSingleThreadExecutor()
        storagePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it)
                init()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentGalleryBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.title = getString(R.string.instagrampicker_gallery_title)
        setHasOptionsMenu(true)
        setupPermissions()

    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<MaterialToolbar>(R.id.select_toolbar).visibility=View.VISIBLE

    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage(getString(R.string.storage_permission_message))
                    .setTitle(getString(R.string.storage_permission_title))

                builder.setPositiveButton(
                    getString(R.string.storage_permission_positive)
                ) { _, _ ->
                    storagePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                builder.setNegativeButton(getString(R.string.storage_permission_negative)) { a, _ ->
                    a.dismiss()
                }

                val dialog = builder.create()
                dialog.show()
            } else if (!InstaPickerSharedPreference(requireContext()).getStoragePermission()) {
                storagePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
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

    private fun init() {
        data.clear()
        selectedPics.clear()
        if (!Const.multiSelect) {
            b.galleryMultiselect.visibility = View.GONE
        }
        b.galleryCamera.setOnClickListener {
            NavHostFragment.findNavController(this).navigate(R.id.action_bnv_gallery_to_bnv_camera)
        }
        b.galleryMultiselectLayout.setOnClickListener {
            selectedPics.clear()
            val positionView =
                (b.galleryRecycler.layoutManager as GridLayoutManager).findFirstVisibleItemPosition()
            multiSelect = !multiSelect
            b.galleryMultiselectLayout.setBackgroundResource(if (multiSelect) R.drawable.img_bg_selected else R.drawable.img_bg)

            adapter = GalleryAdapter(
                data, multiSelect
            ) { addresses ->
                if (addresses.isNotEmpty()) {
                    selectedPics.clear()
                    selectedPics.addAll(addresses)
                    //   b.galleryView.setImageBitmap(scale(selectedPics.last()))
                    Glide.with(this)
                        .load(Uri.parse(selectedPics.last()))
                        //.override(150,150)
                        .fitCenter()
                        .into(b.galleryView)
                }
            }
            b.galleryRecycler.adapter = adapter
            adapter.notifyDataSetChanged()
            b.galleryRecycler.layoutManager?.scrollToPosition(positionView)
        }


        b.galleryRecycler.layoutManager = GridLayoutManager(
            context,
            4,
            RecyclerView.VERTICAL,
            false
        )

        adapter = GalleryAdapter(
            data, multiSelect
        ) { addresses ->
            if (addresses.isNotEmpty()) {
                selectedPics.clear()
                selectedPics.addAll(addresses)
                //    b.galleryView.setImageBitmap(scale(selectedPics.last()))
                Glide.with(this)
                    .load(Uri.parse(selectedPics.last()))
                    //.override(150,150)
                    .fitCenter()
                    .into(b.galleryView)
            }
        }
        b.galleryRecycler.adapter = adapter
        getPicturePaths()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP && data != null) {
            val resultUri = UCrop.getOutput(data)
            FilterActivity.picAddress = resultUri
            startActivity(Intent(requireContext(), FilterActivity::class.java).apply {
                putExtra("Uri", resultUri)
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        val options = UCrop.Options()
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
        options.withMaxResultSize(2000, 2000)
        options.setToolbarTitle(getString(R.string.instagrampicker_crop_title))
        if (id == R.id.action_open) {
            when {
                selectedPics.size == 1 -> {
                    UCrop.of(
                        Uri.parse(selectedPics[0]),
                        Uri.fromFile(
                            File(
                                requireActivity().cacheDir,
                                Statics.getCurrentDate()
                            )
                        )
                    )
                        .withAspectRatio(Const.cropXRatio, Const.cropYRatio)
                        .withOptions(options)
                        .start(requireContext(), this)
                }
                selectedPics.size > 1 -> {
                    MultiSelectActivity.addresses = selectedPics
                    startActivity(Intent(requireActivity(), MultiSelectActivity::class.java))

                }
            }

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    private fun getOrientation(filepath: Uri): Int {
        val attr = try {
            filepath.path?.let {
                ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    -1
                )
            }
        } catch (e: FileNotFoundException) {
            -1
        }
        return when (attr) {
            -1 -> -1
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
    }

    private fun scaleX(address: String): Bitmap? {
        val photoUri = Uri.parse(address)
        val inputStream = requireContext().contentResolver.openInputStream(photoUri)
        val dbo = BitmapFactory.Options()
        dbo.inJustDecodeBounds = true
        BitmapFactory.decodeStream(inputStream, null, dbo)

        val rotatedWidth: Int
        val rotatedHeight: Int
        val orientation = getOrientation(photoUri)

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight
            rotatedHeight = dbo.outWidth
        } else {
            rotatedWidth = dbo.outWidth
            rotatedHeight = dbo.outHeight
        }

        val maxImageDimension = 960
        val srcBitmap: Bitmap?
        if (rotatedWidth > maxImageDimension || rotatedHeight > maxImageDimension) {
            val widthRatio = rotatedWidth / maxImageDimension
            val heightRatio = rotatedHeight / maxImageDimension
            val maxRatio = max(widthRatio, heightRatio)

            val options = BitmapFactory.Options()
            options.inSampleSize = maxRatio
            srcBitmap = BitmapFactory.decodeStream(inputStream, null, options)
        } else {
            srcBitmap = BitmapFactory.decodeStream(inputStream)
        }
        inputStream?.close()

        if (orientation > 0) {
            val matrix = Matrix()
            matrix.postRotate(orientation.toFloat())

            if (srcBitmap != null) Bitmap.createBitmap(
                srcBitmap, 0, 0, srcBitmap.width,
                srcBitmap.height, matrix, true
            )

        }
        return srcBitmap
    }

    @SuppressLint("Range")
    private fun getPicturePaths() {
        val allImagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = Array(2) {
            if (it == 0)
                MediaStore.Images.ImageColumns.DATA
            else
                MediaStore.Images.Media._ID
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
                val model = GalleryModel(dataPath, false)
                data.add(0, model)
                    adapter.notifyItemInserted(data.size)
            } while (cursor.moveToNext())
            if (data[0].address.isNotEmpty()) {
                selectedPics.clear()
                selectedPics.add(data[0].address)
                    Glide.with(requireContext())
                        .load(Uri.parse(data[0].address))
                        .fitCenter()
                        .into(b.galleryView)

            }
            cursor.close()
        }
    }
}
