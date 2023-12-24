package com.example.sanitas.dataprocessing

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.sanitas.SanitasApp
import kotlin.math.pow
import kotlin.math.sqrt

class StepCounter {
    private var filter = LowPassFilter()

    private var dynamicNumberSample = 10.0
    private var minNumberSample = 3
    private var maxNumberSample = 20

    private var onTop = false
    private var onBottom = false

    private var currentAcc = 9.8
    private var dynamicUpThreshold = 10.5
    private var dynamicDownThreshold = 8.7
    private var defaultUpThreshold = 10.5
    private var defaultDownThreshold = 8.7
    private var reductionRateValue = 0.02
    private var raiseRateValue = 0.02

    private var inRest = false
    public fun updateThreshold() {
        if (currentAcc > dynamicUpThreshold) {
            // Update threshold if va is greater than the current threshold
            dynamicNumberSample -= 0.3
            dynamicUpThreshold = currentAcc
            reductionRateValue = (currentAcc - defaultUpThreshold) / maxNumberSample
        } else if (dynamicUpThreshold > defaultUpThreshold) {
            // Reduce threshold if it's greater than the default threshold
            dynamicUpThreshold -= reductionRateValue
        } else {
            // Reset threshold to default value
            dynamicUpThreshold = defaultUpThreshold
        }

        if (currentAcc < dynamicDownThreshold) {
            dynamicNumberSample -= 0.3
            dynamicDownThreshold = currentAcc
            raiseRateValue = (defaultDownThreshold - currentAcc) / maxNumberSample
        } else if (dynamicDownThreshold < defaultUpThreshold) {
            dynamicDownThreshold += reductionRateValue
        } else {
            dynamicDownThreshold = defaultUpThreshold
        }

        if (kotlin.math.abs(currentAcc - 9.8) < 1) {
            dynamicNumberSample += 0.3
            if (dynamicNumberSample > maxNumberSample) {
                inRest = true
            }
        }

        if (dynamicNumberSample > maxNumberSample) {
            dynamicNumberSample = maxNumberSample * 1.0
        } else if (dynamicNumberSample < minNumberSample) {
            dynamicNumberSample = minNumberSample * 1.0
        }

    }

    public fun filteredResult(input: Double): Double{
        currentAcc = this.filter.filteredResult(input)
        return currentAcc
    }

    private fun isTop(): Boolean {
        val filteredOutput = filter.getFilteredOutput()
        if(filteredOutput[dynamicNumberSample.toInt()] < dynamicUpThreshold) {
            return false
        }
        for (i in 0 until dynamicNumberSample.toInt()) {
            if (filteredOutput[i] + 0.04 >= filteredOutput[i + 1]) {
                return false
            }
        }
        for (i in dynamicNumberSample.toInt() until 2*dynamicNumberSample.toInt()) {
            if (filteredOutput[i] <= filteredOutput[i + 1] + 0.04) {
                return false
            }
        }
        return true
    }

    private fun isBot(): Boolean {
        val filteredOutput = filter.getFilteredOutput()
        if(filteredOutput[dynamicNumberSample.toInt()] > dynamicDownThreshold) {
            return false
        }
        for (i in 0 until dynamicNumberSample.toInt()) {
            if (filteredOutput[i + 1] + 0.01 >= filteredOutput[i]) {
                return false
            }
        }
        for (i in dynamicNumberSample.toInt() until 2*dynamicNumberSample.toInt()) {
            if (filteredOutput[i + 1] <= filteredOutput[i] + 0.01) {
                return false
            }
        }
//        var str = ""
//        for (i in 0 until 2*minNumberSample){
//            str += filteredOutput[i].toString() + " "
//        }
        return true

    }

    public fun checkStep(): Boolean {
        onTop = onTop or isTop()
        onBottom = onBottom or isBot()
        if(onTop and onBottom) {
            onTop = false
            onBottom = false
            return true
        }
        return false
    }

}

class StepMonitor private constructor() {
    private object Holder { val INSTANCE = StepMonitor() }
    companion object {

        @JvmStatic
        fun getInstance(): StepMonitor{
            return Holder.INSTANCE
        }
    }
    private var stepCounter = StepCounter()
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

    private var useLowPassFilter = true

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
        if (useLowPassFilter) {
            val totalAcc = sqrt(this.rawAx.toDouble().pow(2.0) + this.rawAy.toDouble().pow(2.0) + this.rawAz.toDouble()
                .pow(2.0)
            )
            stepCounter.filteredResult(totalAcc)
            var isStep = false
            if (stepCounter.checkStep()) {
                SanitasApp.currentSteps += 1
                if(onStepDetected != null) {
                    onStepDetected()
                }
                isStep = true
            }
            stepCounter.updateThreshold()
            return isStep
        } else {
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

}

