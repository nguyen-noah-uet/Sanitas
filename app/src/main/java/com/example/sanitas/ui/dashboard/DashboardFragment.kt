package com.example.sanitas.ui.dashboard

import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sanitas.databinding.FragmentDashboardBinding
import com.example.sanitas.dataprocessing.StepMonitor
import com.example.sanitas.dataprocessing.checkStep
import com.example.sanitas.dataprocessing.filteredResult
import com.example.sanitas.ui.dashboard.DashboardViewModel
import kotlin.math.sqrt

class DashboardFragment : Fragment(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var _binding: FragmentDashboardBinding? = null

    private var mAccelerometer: Sensor? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var stepCounter = 0
    private var stepMonitor = StepMonitor()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        setUpSensor()
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        return root
    }

    private fun setUpSensor() {
        // Create the sensor manager
        sensorManager = requireActivity().getSystemService(SENSOR_SERVICE) as SensorManager

        // Specify the sensor you want to listen to
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_GAME,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.also { gyroscope ->
            sensorManager.registerListener(
                this,
                gyroscope,
                SensorManager.SENSOR_DELAY_GAME,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // Checks for the sensor we have registered
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            stepMonitor.setAccelerometer(x, y, z)

            // using low pass filter
//            val totalAcc = sqrt(Math.pow(x.toDouble(),2.0) + Math.pow(y.toDouble(),2.0) + Math.pow(z.toDouble(),2.0))
//            filteredResult(totalAcc)
//            if (checkStep()) {
//                stepCounter += 1
//                binding.StepEditText.text = "Steps: ${stepCounter}"
//            }
        }
        if (event?.sensor?.type == Sensor.TYPE_GYROSCOPE) {
            val raw = event.values[0]
            val pitch = event.values[1]
            stepMonitor.setGyro(raw, pitch)
        }
        if (stepMonitor.detectStep()) {
            stepCounter += 1
        }
        binding.StepEditText.text = "Steps: ${stepCounter}"
//        binding.StepEditText.text = "Steps: ${stepMonitor.rawRoll}"
    }

    override fun onResume() {
        super.onResume()
//        mAccelerometer?.also { accelerometer ->
//            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME,
//                SensorManager.SENSOR_DELAY_FASTEST)
//        }
    }

    override fun onPause() {
        super.onPause()
//        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }
}