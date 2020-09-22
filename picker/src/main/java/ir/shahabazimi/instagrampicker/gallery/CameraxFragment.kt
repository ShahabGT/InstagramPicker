package ir.shahabazimi.instagrampicker.gallery

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.theartofdev.edmodo.cropper.CropImage
import ir.shahabazimi.instagrampicker.InstagramPicker
import ir.shahabazimi.instagrampicker.R
import ir.shahabazimi.instagrampicker.filter.FilterActivity
import kotlinx.android.synthetic.main.fragment_camerax.*
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit

class CameraxFragment : Fragment(R.layout.fragment_camerax) {


    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    private lateinit var ctx: Context
    private lateinit var act: FragmentActivity

    private var isfront = false

    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera(cameraSelector: CameraSelector) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(act)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()


            val imageAnalyzer = ImageAnalysis.Builder()
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->

                        })
                    }

            val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(c_viewFinder.surfaceProvider)
                    }

            imageCapture = ImageCapture.Builder()
                    .build()
            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                        act, cameraSelector, preview, imageCapture, imageAnalyzer)

            } catch (exc: Exception) {
            }

        }, ContextCompat.getMainExecutor(act))
    }


    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File.createTempFile(
                SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()),
                ".jpg",
                outputDirectory
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()


        imageCapture.takePicture(
                outputOptions, ContextCompat.getMainExecutor(ctx), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
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

private class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()    // Rewind the buffer to zero
        val data = ByteArray(remaining())
        get(data)   // Copy the buffer into a byte array
        return data // Return the byte array
    }

    override fun analyze(image: ImageProxy) {

        val buffer = image.planes[0].buffer
        val data = buffer.toByteArray()
        val pixels = data.map { it.toInt() and 0xFF }
        val luma = pixels.average()

        listener(luma)

        image.close()
    }
}