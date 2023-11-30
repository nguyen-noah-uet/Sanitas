package com.example.sanitas.dataprocessing

import org.kotlinmath.Complex
import org.kotlinmath.complex
import org.kotlinmath.exp
import kotlin.math.pow

fun abs(x:Complex):Double {
    return kotlin.math.sqrt(x.re.pow(2) + x.im.pow(2))
}

fun dftSlow(x: DoubleArray): Array<Complex> {
    val N = x.size
    val n = (0 until N).toList()
    val k = n.map { it.toDouble() }
    val M = Array(N) { Array(N){ complex(0.0,0.0) } }
    for (i in 0 until N) {
        for (j in 0 until N) {
            M[i][j] = exp(complex(0, -2*kotlin.math.PI*i*j/N))
        }
    }
    val result = Array(N){ complex(0.0,0.0) }
    for (i in 0 until N) {
        for (j in 0 until N) {
            result[i] += M[i][j] * x[j]
        }
    }
    return result
}

fun fft(x: DoubleArray): Array<Complex> {
    var N = x.size
    if (N <= 32) {
        return dftSlow(x)
    } else {
        N = Math.pow(2.0, (Math.log(N.toDouble())/Math.log(2.0)).toInt().toDouble()).toInt()
        val X_even = fft(x.filterIndexed { index, _ -> index % 2 == 0 }.toDoubleArray())
        val X_odd = fft(x.filterIndexed { index, _ -> index % 2 != 0 }.toDoubleArray())
        val factor = Array(N) { exp(complex(0.0, -2 * kotlin.math.PI * it / N)) }
        val result = Array(N){ complex(0.0,0.0) }
        for (i in 0 until N / 2) {
            result[i] = X_even[i] + factor[i] * X_odd[i]
            result[i + N / 2] = X_even[i] + factor[i + N / 2] * X_odd[i]
        }
        return result
    }
}

fun dft(signal: DoubleArray): Array<Complex> {
    val N = signal.size
    val frequencyArray = Array<Complex>(N) {complex(0,0)}

    for (k in 0 until N){
        var temp = complex(0,0)
        for (n in 0 until N) {
            temp += exp(complex(0, -2*kotlin.math.PI*k*n/N)) * signal[n]
        }
        frequencyArray[k] = temp
    }
    return frequencyArray
}

fun heartBeatEvaluation(signal: DoubleArray, fs: Double):Double {
//    val frequencyArr = dft(signal)
    var mean = 0.0
    for (i in signal.indices) {
        mean += signal[i]
    }
    for (i in signal.indices) {
        signal[i] -= mean/signal.size
    }
    val frequencyArr = fft(signal)
    val nSamples = frequencyArr.size
    val minFrequency = 1.3
    var index = (minFrequency * nSamples /  1.0 / fs).toInt()
    var indexMax = index
    index += 1
    while (index < nSamples/2){
        if (abs(frequencyArr[index]) > abs(frequencyArr[indexMax])) {
            indexMax = index
        }
        index += 1
    }
    return indexMax * fs * 60.0/nSamples
}

fun  main() {
    // Cheek operation of `heartBeatEvaluation` function, the result must close to 'f'
    // ! Make sure fs >= 2*f (nyquist theory)
    val nSamples = 276
    val fs = 5.0
    val f = 0.9
    val signal = DoubleArray(nSamples)
    for (i in 0 until nSamples) {
        signal[i] = Math.sin(2*Math.PI*f*(i*1.0/fs))
    }
    println(heartBeatEvaluation(signal, fs))
}
