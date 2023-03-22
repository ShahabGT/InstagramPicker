package ir.shahabazimi.instagrampicker.fragments

import android.Manifest.permission.CAMERA
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import ir.shahabazimi.instagrampicker.R
import ir.shahabazimi.instagrampicker.databinding.FragmentCameraBinding
import ir.shahabazimi.instagrampicker.utils.Const.FILENAME_FORMAT
import ir.shahabazimi.instagrampicker.utils.InstaPickerSharedPreference
import ir.shahabazimi.instagrampicker.utils.popBackStack
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * @Author: Shahab Azimi
 * @Date: 2023 - 03 - 22
 **/
class CameraFragment : BaseFragment<FragmentCameraBinding>() {

    private lateinit var cameraPermission: ActivityResultLauncher<String>
    private var isFront = false
    private var imageCapture: ImageCapture? = null
    private var flash = FlashMode.FLASH_OFF
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        cameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) initCamera()
            else popBackStack()
        }
    }

    override fun bindView(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentCameraBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().findViewById<MaterialToolbar>(R.id.select_toolbar).visibility = View.GONE
        setupPermissions()
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            requireContext(),
            CAMERA
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    CAMERA
                )
            ) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage(getString(R.string.camera_permission_message))
                    .setTitle(getString(R.string.camera_permission_title))

                builder.setPositiveButton(
                    getString(R.string.camera_permission_positive)
                ) { _, _ ->
                    cameraPermission.launch(CAMERA)
                }
                builder.setNegativeButton(getString(R.string.camera_permission_negative)) { a, _ ->
                    a.dismiss()
                }

                val dialog = builder.create()
                dialog.show()
            } else if (!InstaPickerSharedPreference(requireContext()).getCameraPermission()) {
                cameraPermission.launch(CAMERA)
                InstaPickerSharedPreference(requireContext()).setCameraPermission()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.camera_permission_deny),
                    Toast.LENGTH_SHORT
                ).show()

                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", requireActivity().packageName, null)
                }
                )
                popBackStack()
            }
        } else {
            initCamera()
        }
    }


    private fun initCamera() = with(binding) {
        startCamera(CameraSelector.DEFAULT_BACK_CAMERA)
        cCapture.setOnClickListener { takePhoto() }

        cClose.setOnClickListener { popBackStack() }

        cChange.setOnClickListener {
            cFocus.visibility = View.INVISIBLE
            isFront = if (isFront) {
                startCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                false
            } else {
                startCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                true
            }
        }

        cFlash.setOnClickListener {
            when (flash) {
                FlashMode.FLASH_OFF -> {
                    flash = FlashMode.FLASH_AUTO
                    cFlash.setImageResource(R.drawable.vector_flash_auto)
                }
                FlashMode.FLASH_AUTO -> {
                    flash = FlashMode.FLASH_ON
                    cFlash.setImageResource(R.drawable.vector_flash_on)
                }
                FlashMode.FLASH_ON -> {
                    flash = FlashMode.FLASH_OFF
                    cFlash.setImageResource(R.drawable.vector_flash_off)
                }
            }

        }

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun startCamera(cameraSelector: CameraSelector) = with(binding) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(cViewFinder.surfaceProvider) }

            imageCapture = Builder()
                .build()
            try {
                cameraProvider.unbindAll()

                val camera = cameraProvider.bindToLifecycle(
                    requireActivity(), cameraSelector, preview, imageCapture
                )

                cViewFinder.afterMeasured {
                    cViewFinder.setOnTouchListener { _, event ->
                        return@setOnTouchListener when (event.action) {
                            MotionEvent.ACTION_DOWN -> {
                                true
                            }
                            MotionEvent.ACTION_UP -> {
                                cFocus.visibility = View.VISIBLE
                                val factory: MeteringPointFactory =
                                    SurfaceOrientedMeteringPointFactory(
                                        cViewFinder.width.toFloat(),
                                        cViewFinder.height.toFloat()
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
                                    cFocus.translationX = event.x - (cFocus.width / 2)
                                    cFocus.translationY = event.y - (cFocus.height / 2)
                                } catch (_: CameraInfoUnavailableException) {
                                }
                                true
                            }
                            else -> false
                        }
                    }
                }
            } catch (_: Exception) {
            }

        }, ContextCompat.getMainExecutor(requireActivity()))
    }

    private inline fun View.afterMeasured(crossinline block: () -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
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
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                }

                override fun onImageSaved(output: OutputFileResults) {
                    //todo start cropping photoFile
                }
            })
    }


    private fun getOutputDirectory(): File {
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else requireActivity().filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

}

enum class FlashMode {
    FLASH_ON,
    FLASH_OFF,
    FLASH_AUTO
}