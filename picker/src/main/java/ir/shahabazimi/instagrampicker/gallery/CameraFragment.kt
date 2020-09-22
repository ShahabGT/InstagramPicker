package ir.shahabazimi.instagrampicker.gallery

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.theartofdev.edmodo.cropper.CropImage
import ir.shahabazimi.instagrampicker.InstagramPicker
import ir.shahabazimi.instagrampicker.R
import ir.shahabazimi.instagrampicker.filter.FilterActivity
import kotlinx.android.synthetic.main.fragment_camera.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

enum class FlashMode {
    FLASH_ON,
    FLASH_OFF,
    FLASH_AUTO
}

class CameraFragment : Fragment(R.layout.fragment_camera) {


    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    private lateinit var ctx: Context
    private lateinit var act: FragmentActivity

    private var isfront = false

    private var imageCapture: ImageCapture? = null

    private var flash = FlashMode.FLASH_OFF

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        actionBar?.title = getString(R.string.instagrampicker_camera_title)
        ctx = requireContext()
        act = requireActivity()

        startCamera(CameraSelector.DEFAULT_BACK_CAMERA)
        c_capture.setOnClickListener { takePhoto() }

        c_change.setOnClickListener {
            isfront = if (isfront) {
                startCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                false
            } else {
                startCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                true
            }
        }

        c_flash.setOnClickListener {
            when (flash) {
                FlashMode.FLASH_OFF -> {
                    flash = FlashMode.FLASH_AUTO
                    c_flash.setImageResource(R.mipmap.ic_flash_auto)
                }
                FlashMode.FLASH_AUTO -> {
                    flash = FlashMode.FLASH_ON
                    c_flash.setImageResource(R.mipmap.ic_flash_on)
                }
                FlashMode.FLASH_ON -> {
                    flash = FlashMode.FLASH_OFF
                    c_flash.setImageResource(R.mipmap.ic_flash_off)
                }
            }

        }

        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun startCamera(cameraSelector: CameraSelector) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(act)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(c_viewFinder.surfaceProvider)
                    }





            imageCapture = Builder()
                    .build()
            try {
                cameraProvider.unbindAll()

               val camera =  cameraProvider.bindToLifecycle(
                        act, cameraSelector, preview, imageCapture)

                c_viewFinder.afterMeasured {
                    c_viewFinder.setOnTouchListener { _, event ->
                        return@setOnTouchListener when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                true
                            }
                            MotionEvent.ACTION_UP -> {
                                c_focus.visibility=View.VISIBLE
                                val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                                        c_viewFinder.width.toFloat(), c_viewFinder.height.toFloat()
                                )
                                val autoFocusPoint = factory.createPoint(event.x, event.y)
                                try {

                                    camera.cameraControl.startFocusAndMetering(
                                            FocusMeteringAction.Builder(
                                                    autoFocusPoint,
                                                    FocusMeteringAction.FLAG_AF
                                            ).apply {
                                                disableAutoCancel()
                                            }.build()
                                    )
                                    c_focus.translationX=event.x-(c_focus.width/2)
                                    c_focus.translationY=event.y-(c_focus.height/2)
                                } catch (e: CameraInfoUnavailableException) { }
                                true
                            }
                            else -> false // Unhandled event.
                        }
                    }
                }
            } catch (exc: Exception) {
            }

        }, ContextCompat.getMainExecutor(act))
    }

    private inline fun View.afterMeasured(crossinline block: () -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    block()
                }
            }
        })
    }


    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File.createTempFile(
                SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()),
                ".jpg",
                outputDirectory
        )

        val outputOptions = OutputFileOptions.Builder(photoFile).build()

        when (flash) {
            FlashMode.FLASH_ON -> imageCapture.flashMode = FLASH_MODE_ON
            FlashMode.FLASH_OFF -> imageCapture.flashMode = FLASH_MODE_OFF
            FlashMode.FLASH_AUTO -> imageCapture.flashMode = FLASH_MODE_AUTO

        }
        imageCapture.takePicture(
                outputOptions, ContextCompat.getMainExecutor(ctx), object : OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
            }

            override fun onImageSaved(output: OutputFileResults) {
                startCropping(photoFile)
            }
        })
    }

    private fun startCropping(f: File) {
        val x = InstagramPicker.x
        val y = InstagramPicker.y
        CropImage.activity(Uri.fromFile(f))
                .setAspectRatio(x, y)
                .start(ctx, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                val `in` = Intent(ctx, FilterActivity::class.java)
                `in`.putExtra("uri", resultUri)
                FilterActivity.picAddress = resultUri
                startActivityForResult(`in`, 444)
            }
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = act.externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else act.filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

}