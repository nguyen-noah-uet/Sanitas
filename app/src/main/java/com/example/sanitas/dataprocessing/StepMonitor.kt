package com.example.sanitas.dataprocessing

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.sanitas.SanitasApp

class StepMonitor private constructor() {
    private object Holder { val INSTANCE = StepMonitor() }
    companion object {

        @JvmStatic
        fun getInstance(): StepMonitor{
            return Holder.INSTANCE
        }
    }
    private var onStepDetected: () -> Unit = {}
    private var dynamicThreshold = 0.05
    private var defaultThreshold = 0.05

    private var reductionRateValue = 0.01
    private var frameCountMin = 11
    private var frameCountMax = 15

    // Initialize variables
    private var stepCountFlag = false
    private var frameCount = 0
    private var predVa = 0.0
    private var verticalAcc = 0.0
    private var isStep = false

    private var rawAx = 0.0f
    private var rawAy = 0.0f
    private var rawAz = 0.0f
    private var rawRoll = 0.0f
    private var rawPitch = 0.0f

    fun setOnStepDetectedCallback(onStepDetected: () -> Unit) {
        this.onStepDetected = onStepDetected
    }

    public fun getVa(): Double {
        return verticalAcc
    }
    public fun getDynamicThreshold(): Double {
        return dynamicThreshold
    }

    public fun getDefaultThreshold(): Double {
        return defaultThreshold
    }

    public fun setAccelerometer(rawAx: Float, rawAy: Float, rawAz: Float) {
        this.rawAx = rawAx
        this.rawAx = rawAy
        this.rawAz = rawAz
    }

    public fun setGyro(rawRoll: Float, rawPitch: Float ) {
        this.rawRoll = rawRoll
        this.rawPitch = rawPitch
    }

    private fun updateThreshold() {
        if (verticalAcc > dynamicThreshold) {
            // Update threshold if va is greater than the current threshold
            dynamicThreshold = verticalAcc
            reductionRateValue = (verticalAcc - defaultThreshold) / frameCountMax
        } else if (dynamicThreshold > defaultThreshold) {
            // Reduce threshold if it's greater than the default threshold
            dynamicThreshold -= reductionRateValue
        } else {
            // Reset threshold to default value
            dynamicThreshold = defaultThreshold
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public fun detectStep() : Boolean {
        // Convert raw rotation to degrees
        val roll = rawRoll * 180 / Math.PI
        val pitch = rawPitch * 180 / Math.PI

        // Rotation rate normalization
        val roll90 = if (roll > 90) 90.0 - (roll - 90.0)
        else if (roll < -90) -90.0 - (roll + 90.0)
        else roll

        val pitch90 = pitch

        // Rotation rate normalization (-1.0 to 1.0)
        val rollNormalize = roll90 / 90
        val pitchNormalize = pitch90 / 90

        // Calculate the weight of components Ax, Ay, Az
        val xw = rollNormalize * (1.0 - kotlin.math.abs(pitchNormalize))
        val yw = -1.0 * pitchNormalize
        val zw = if (kotlin.math.abs(roll) > 90) 1.0 - (kotlin.math.abs(xw) + kotlin.math.abs(yw))
        else -1.0 * (1.0 - (kotlin.math.abs(xw) + kotlin.math.abs(yw)))

        // Calculate vertical acceleration
        predVa = rawAx * xw + rawAy * yw + rawAz * zw

        // Smooth vertical acceleration
        verticalAcc = predVa * 0.1 + verticalAcc * 0.9

        // Step detection
        isStep = false
        if (verticalAcc > dynamicThreshold && stepCountFlag && frameCount > frameCountMin) {
            stepCountFlag = false
            frameCount = 0
            isStep = true
            SanitasApp.currentSteps += 1
            if(onStepDetected != null) {
                onStepDetected()
            }
        } else if (verticalAcc < 0.0) {
            stepCountFlag = true
        }
        frameCount++
        updateThreshold()
        return isStep
    }

}

