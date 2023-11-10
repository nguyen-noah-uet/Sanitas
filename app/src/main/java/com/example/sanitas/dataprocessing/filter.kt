package com.example.sanitas.dataprocessing
import android.util.Log
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random.Default.nextInt

// butterworth 4th, wc = 4/(50/2) (fc/(fs/2))
val bCoefficient = listOf(0.00048944, 0.0024, 0.0049, 0.0049, 0.0024, 0.00048944)
val aCoefficient = listOf(1.0, -3.378, 4.7518, -3.4397, 1.2740, -0.1924)
val sizeFilter = bCoefficient.size
const val saveLength = 100
val filteredOutput = MutableList(saveLength) { 9.8 }
val currentInput = MutableList(sizeFilter) { 9.8 }

const val minNumberSample = 6
var onTop = false
var onBottom = false
val minTop = 11
val maxBot = 8.7

fun filteredResult(input: Double): Double {
    filteredOutput.removeAt(saveLength - 1)
    currentInput.removeAt(sizeFilter - 1)
    currentInput.add(0, input)
    val output = (bCoefficient.zip(currentInput) { b, c -> b * c }.sum()) - (filteredOutput.subList(0, sizeFilter - 1).zip(aCoefficient.subList(1, sizeFilter)) { f, a -> f * a }.sum())
    filteredOutput.add(0, output)
    return output
}

fun isTop(): Boolean {
    if(filteredOutput[minNumberSample] < minTop) {
        return false
    }
    for (i in 0 until minNumberSample) {
        if (filteredOutput[i] + 0.04 >= filteredOutput[i + 1]) {
            return false
        }
    }
    for (i in minNumberSample until 2*minNumberSample) {
        if (filteredOutput[i] <= filteredOutput[i + 1] + 0.04) {
            return false
        }
    }
    return true
}

fun isBot(): Boolean {
    if(filteredOutput[minNumberSample] > maxBot) {
        return false
    }
    for (i in 0 until minNumberSample) {
        if (filteredOutput[i + 1] + 0.01 >= filteredOutput[i]) {
            return false
        }
    }
    for (i in minNumberSample until 2*minNumberSample) {
        if (filteredOutput[i + 1] <= filteredOutput[i] + 0.01) {
            return false
        }
    }
    var str = ""
    for (i in 0 until 2*minNumberSample){
        str += filteredOutput[i].toString() + " "
    }
    return true

}

fun checkStep(): Boolean {
    onTop = onTop or isTop()
    onBottom = onBottom or isBot()
    if(onTop and onBottom) {
        onTop = false
        onBottom = false
        return true
    }
    return false
}

fun main() {
    val nSamples = 200
    val fs = 50
    val input = (0 until nSamples).map { i ->
        5 * sin((2 * PI * 5 * i) / fs) + nextInt(0, 100)/50
    }
    val output = mutableListOf<Double>()
    var count = 0
    for (i in 0 until nSamples) {
//        output.add(filteredResult(filteredOutput, input[i]))
        filteredResult(input[i])
        if(checkStep()) {
            count += 1
            println(count)
        }
    }

    print(output)
}