package com.example.sanitas.workers

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.sanitas.dataprocessing.StepMonitor

class StepCounterWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams), SensorEventListener {
    private val stepMonitor = StepMonitor.getInstance()
    init {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(
            this,
            accSensor,
            SensorManager.SENSOR_DELAY_GAME,
//            SensorManager.SENSOR_DELAY_FASTEST
        )
        val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        sensorManager.registerListener(
            this,
            gyroSensor,
            SensorManager.SENSOR_DELAY_GAME,
//            SensorManager.SENSOR_DELAY_FASTEST
        )
        Log.i(TAG, "StepCounterWorker: Registered")
    }

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        if (sensorEvent.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = sensorEvent.values[0]
            val y = sensorEvent.values[1]
            val z = sensorEvent.values[2]
            stepMonitor.setAccelerometer(x, y, z)
            if (stepMonitor.detectStep()) {
                Log.i(TAG, String.format("Step %d", StepMonitor.stepCounter))
            }
        } else if (sensorEvent.sensor.type == Sensor.TYPE_GYROSCOPE) {
            val raw = sensorEvent.values[0]
            val pitch = sensorEvent.values[1]
            stepMonitor.setGyro(raw, pitch)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
    override fun doWork(): Result {
        Log.i(TAG, "doWork: ")
        return Result.success()
    }

    companion object {
        private const val TAG = "StepCounterWorker"
    }
}