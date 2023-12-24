package com.example.sanitas.dataprocessing
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random.Default.nextInt

class LowPassFilter {
    // butterworth 4th, wc = 4/(50/2) (fc/(fs/2))
    private val bCoefficient = listOf(0.00048944, 0.0024, 0.0049, 0.0049, 0.0024, 0.00048944)
    private val aCoefficient = listOf(1.0, -3.378, 4.7518, -3.4397, 1.2740, -0.1924)
    private val sizeFilter = bCoefficient.size
    private val saveLength = 100
    private val currentInput = MutableList(sizeFilter) { 9.8 }
    private val filteredOutput = MutableList(saveLength) { 9.8 }

    public fun getFilteredOutput(): MutableList<Double> {
        return this.filteredOutput
    }

    public fun filteredResult(input: Double): Double {
        filteredOutput.removeAt(saveLength - 1)
        currentInput.removeAt(sizeFilter - 1)
        currentInput.add(0, input)
        val output = (bCoefficient.zip(currentInput) { b, c -> b * c }.sum()) - (filteredOutput.subList(0, sizeFilter - 1).zip(aCoefficient.subList(1, sizeFilter)) { f, a -> f * a }.sum())
        filteredOutput.add(0, output)
        return output
    }
}

fun main() {
    val counter = StepCounter()
    val nSamples = 200
    val fs = 50
    val input = (0 until nSamples).map { i ->
        35 * sin((2 * PI * 3 * i) / fs) + nextInt(0, 100)/50
    }
    val output = mutableListOf<Double>()
    var count = 0
    for (i in 0 until nSamples) {
        output.add(counter.filteredResult(input[i]))
        if(counter.checkStep()) {
            count += 1
            println(count)
        }
    }

    print(output)
}