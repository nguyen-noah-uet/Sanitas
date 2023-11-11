package com.example.sanitas.ui.heartmonitor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageFormat
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.example.sanitas.databinding.FragmentHeartmonitorBinding
import com.google.common.util.concurrent.ListenableFuture
import java.io.ByteArrayOutputStream

class HeartMonitorFragment : Fragment() {
    private val TAG = "HeartMonitorFragment"

    private lateinit var cameraSelector: CameraSelector
    private lateinit var preview: Preview
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var viewModel: HeartMonitorViewModel
    private lateinit var binding: FragmentHeartmonitorBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var previewView: PreviewView
    private lateinit var imageView: ImageView
    private lateinit var startStopButton: Button
    private var isAnalyzing = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHeartmonitorBinding.inflate(inflater, container, false)
        configureBinding()
        viewModel = ViewModelProvider(this).get(HeartMonitorViewModel::class.java)

        preview = Preview.Builder()
            .build()

        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())

        cameraProvider = cameraProviderFuture.get()
        cameraProviderFuture.addListener({
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(requireActivity()))

        imageAnalysis = ImageAnalysis.Builder()
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888) // for YUV format
//            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888) // for RGBA format
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // non-blocking mode
            .build()


        return binding.root
    }

    private fun configureBinding() {
        previewView = binding.previewView
        imageView = binding.imageView
        startStopButton = binding.startStopButton
        startStopButton.setOnClickListener {
            if (!isAnalyzing) {
                isAnalyzing = true
                startStopButton.text = "Stop"
                // start image analysis
                try {
                    imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(requireActivity()),
                        HeartbeatImageAnalyzer(previewView, ::updatePreview)
                    )
                    cameraProviderFuture.addListener({
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            requireActivity(),
                            cameraSelector,
                            imageAnalysis,
                            preview
                        )
                    }, ContextCompat.getMainExecutor(requireActivity()))
                } catch (e: Exception) {
                    Log.e(TAG, e.message.toString())
                    isAnalyzing = false
                    startStopButton.text = "Start"
                }
            } else {
                imageAnalysis.clearAnalyzer()
//                imageView.destroyDrawingCache()
                isAnalyzing = false
                startStopButton.text = "Start"
            }
        }
    }

    private fun updatePreview(bitmap: Bitmap) {
//        requireActivity().runOnUiThread {
//            // Update your UI element (e.g., ImageView) with the grayscale bitmap
//            imageView.setImageBitmap(bitmap)
//        }
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {


        preview.setSurfaceProvider(previewView.surfaceProvider)
        try {
            cameraProvider.unbindAll()
            var camera = cameraProvider.bindToLifecycle(
                requireActivity() as LifecycleOwner,
                cameraSelector,
                preview
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class HeartbeatImageAnalyzer(
        private val previewView: PreviewView,
        private val updatePreviewCallback: (Bitmap) -> Unit
    ) : ImageAnalysis.Analyzer {
        private val TAG = "HeartbeatImageAnalyzer"
        private var i = 0

        // Latest frame of camera preview can be analyze in this function
        override fun analyze(image: ImageProxy) {
            try {
                val yBuffer = image.planes[0].buffer
                val uBuffer = image.planes[1].buffer
                val vBuffer = image.planes[2].buffer
                val ySize = yBuffer.remaining()
                val uSize = uBuffer.remaining()
                val vSize = vBuffer.remaining()

                val nv21 = ByteArray(ySize + uSize + vSize)

                // Copy Y to output array
                yBuffer.get(nv21, 0, ySize)

                // Copy UV to output array
                vBuffer.get(nv21, ySize, vSize)
                uBuffer.get(nv21, ySize + vSize, uSize)

                val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
                val outputBitmap = yuvImage.toBitmap()

                i++
                Log.i(TAG, i.toString())
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
            } finally {
                image.close()
            }
        }

        private fun convertToGrayscale(bmpOriginal: Bitmap): Bitmap {
            val height: Int = bmpOriginal.height
            val width: Int = bmpOriginal.width
            val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val c = Canvas(bmpGrayscale)
            val paint = Paint()
            val cm = ColorMatrix()
            cm.setSaturation(0f)
            val f = ColorMatrixColorFilter(cm)
            paint.colorFilter = f
            c.drawBitmap(bmpOriginal, 0f, 0f, paint)
            return bmpGrayscale
        }


        private fun YuvImage.toBitmap(): Bitmap {
            val out = ByteArrayOutputStream()
            compressToJpeg(Rect(0, 0, width, height), 100, out)
            val imageBytes: ByteArray = out.toByteArray()
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }
    }
}
