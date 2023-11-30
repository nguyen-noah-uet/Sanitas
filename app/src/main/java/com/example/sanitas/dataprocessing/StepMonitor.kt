package com.example.sanitas.dataprocessing

class StepMonitor {
    private var dynamicThreshold = 0.06
    private var defaultThreshold = 0.06
    private var reductionRateValue = 0.01
    private var frameCountMin = 9
    private var frameCountMax = 15

    // Initialize variables
    private var stepCountFlag = true
    private var frameCount = 0
    private var preVa = 0.0
    private var va = 0.0
    
    private var rawAx = 0.0f
    private var rawAy = 0.0f
    private var rawAz = 0.0f
    public var rawRoll = 0.0f
    public var rawPitch = 0.0f
    private fun updateThreshold() {
        if (va > dynamicThreshold) {
            // Update threshold if va is greater than the current threshold
            dynamicThreshold = va
            reductionRateValue = (va - defaultThreshold) / frameCountMax
        } else if (dynamicThreshold > defaultThreshold) {
            // Reduce threshold if it's greater than the default threshold
            dynamicThreshold -= reductionRateValue
        } else {
            // Reset threshold to default value
            dynamicThreshold = defaultThreshold
        }
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

    public fun detectStep() : Boolean{
        // Convert raw rotation to degrees
        val phiD = rawRoll * 180 / Math.PI
        val thetaD = rawPitch * 180 / Math.PI

        // Rotation rate normalization
        val phi90 = if (phiD > 90) 90.0 - (phiD - 90.0)
        else if (phiD > -90) -90.0 - (phiD + 90.0)
        else phiD

        val theta90 = thetaD

        // Rotation rate normalization (-1.0 to 1.0)
        val phi = phi90 / 90
        val theta = theta90 / 90

        // Calculate the weight of components Ax, Ay, Az
        val xw = phi * (1.0 - kotlin.math.abs(theta))
        val yw = -1.0 * theta
        val zw = if (kotlin.math.abs(phiD) > 90) 1.0 - (kotlin.math.abs(xw) + kotlin.math.abs(yw))
        else -1.0 * (1.0 - (kotlin.math.abs(xw) + kotlin.math.abs(yw)))

        // Calculate vertical acceleration
        preVa = rawAx * xw + rawAy * yw + rawAz * zw

        // Smooth vertical acceleration
        va = preVa * 0.1 + va * 0.9

        // Step detection
        if (va > dynamicThreshold && stepCountFlag && frameCount > frameCountMin) {
            stepCountFlag = false
            frameCount = 0
            updateThreshold()
            return true
        } else if (va < 0) {
            stepCountFlag = true
            frameCount++
            return false
        }
        return false
    }

}

