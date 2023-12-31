package com.example.sanitas.ui.heartmonitor

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.example.sanitas.R
import com.example.sanitas.SanitasApp
import com.example.sanitas.databinding.FragmentHeartmonitorBinding
import com.example.sanitas.dataprocessing.heartBeatEvaluation
import com.google.common.util.concurrent.ListenableFuture
import java.io.ByteArrayOutputStream

class HeartMonitorFragment : Fragment() {
    private lateinit var camera: Camera
    private val TAG = "HeartMonitorFragment"

    private lateinit var cameraSelector: CameraSelector
    private lateinit var preview: Preview
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var viewModel: HeartMonitorViewModel
    private lateinit var binding: FragmentHeartmonitorBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var previewView: PreviewView
    private lateinit var startStopButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var infoTextView: TextView
    private var isAnalyzing = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if(!hasCameraPermission()) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.CAMERA), 0
            )
        }
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun configureBinding() {
        previewView = binding.previewView
        startStopButton = binding.startStopButton
        progressBar = binding.progressBar
        infoTextView = binding.infoTextView
        startStopButton.setOnClickListener {
            if (!isAnalyzing) {

                isAnalyzing = true
                startStopButton.text = resources.getString(R.string.stop)
                progressBar.visibility = View.VISIBLE
                infoTextView.text = resources.getString(R.string.waitingMeasurement)
                infoTextView.visibility = View.VISIBLE

                // start image analysis
                try {
                    imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(requireActivity()),
                        HeartbeatImageAnalyzer(previewView, ::onHeartbeatCalculated)
                    )
                    cameraProviderFuture.addListener({
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            requireActivity(),
                            cameraSelector,
                            imageAnalysis,
                            preview
                        )
                        camera.cameraControl.enableTorch(true)
                    }, ContextCompat.getMainExecutor(requireActivity()))
                } catch (e: Exception) {
                    Log.e(TAG, e.message.toString())
                    isAnalyzing = false
                    startStopButton.text = resources.getString(R.string.start)
                }
            } else {
                imageAnalysis.clearAnalyzer()
                camera.cameraControl.enableTorch(false)
//                imageView.destroyDrawingCache()
                isAnalyzing = false
                startStopButton.text = resources.getString(R.string.start)
                progressBar.visibility = View.GONE
                infoTextView.visibility = View.GONE
            }
        }
    }
    private fun hasCameraPermission() = ContextCompat.checkSelfPermission(
        requireActivity(), Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    // TODO: after complete analyze image, update UI in here
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun onHeartbeatCalculated(calculatedHeartBeat: Double) {
        imageAnalysis.clearAnalyzer()
        camera.cameraControl.enableTorch(false)
        isAnalyzing = false
        startStopButton.text = resources.getString(R.string.start)
        progressBar.visibility = View.GONE
        infoTextView.text = "${resources.getString(R.string.heartbeatMeasure)} ${String.format("%.2f", calculatedHeartBeat)} BPM"
        SanitasApp.measuredHeartBeat = calculatedHeartBeat
        Toast.makeText(requireActivity(), calculatedHeartBeat.toString(), Toast.LENGTH_SHORT).show()
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
        private val onCalculateComplete: (calculatedHeartBeat: Double) -> Unit
    ) : ImageAnalysis.Analyzer {
        private val TAG = "HeartbeatImageAnalyzer"
        private var i = 0
        private val signalArraySize = 128
        private var signal = DoubleArray(signalArraySize)
        private var currentSignal = 0
        private var calculatedHeartBeat = 0.0
        private var isCalculating = false

        // Latest frame of camera preview can be analyze in this function
        override fun analyze(image: ImageProxy) {
            val numberToCalMedian = 25
            try {
                if (currentSignal < signalArraySize) {
                    // get Y chanel data
                    val yBuffer = image.planes[0].buffer
                    // get size of buffer
                    val ySize = yBuffer.remaining()
                    val data = ByteArray(ySize)
                    // Copy Y to output array
                    yBuffer.get(data, 0, ySize)

                    var intensity = 0.0
                    var i = 0
                    while (i < data.size - numberToCalMedian) {
                        val median = data.slice(i until i + numberToCalMedian).toByteArray()
                        median.sort()
                        intensity += median[numberToCalMedian/2]
                        i += numberToCalMedian
                    }
//                    for (i in data.indices) {
//                        intensity += data[i]
//                    }
                    intensity /= data.size
//                    Log.i(TAG, intensity.toString())
                    signal[currentSignal] = intensity
//                    Log.i(TAG, signal[currentSignal].toString())

                    currentSignal += 1
                    if (currentSignal == signalArraySize) {
                        // calculatedHeartBeat
                        calculatedHeartBeat = heartBeatEvaluation(signal, 11.0)
                        Log.i(TAG, calculatedHeartBeat.toString())
                        onCalculateComplete(calculatedHeartBeat)
                    }
0
                } else {

                }
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
