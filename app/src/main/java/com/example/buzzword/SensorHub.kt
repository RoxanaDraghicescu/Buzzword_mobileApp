package com.example.buzzword

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

// one reading: time, raw x/y/z, filtered buzz
data class AccelerometerSample(
    val timeNanos: Long,
    val x: Float, val y: Float, val z: Float,
    val buzzStrength: Float
)

// reads the accelerometer and extracts the buzz
class SensorHub(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    // later: mic, gyro, proximity

    var onFlip: (() -> Unit)? = null    // later
    var onLift: (() -> Unit)? = null    // later
    var onKnock: (() -> Unit)? = null   // later
    var onSample: ((AccelerometerSample) -> Unit)? = null   // every new reading

    // high-pass filter state, one slot per axis
    private val cutoffHz = 15f   // changes slower than this are ignored
    private val timeConstant = 1f / (2f * Math.PI.toFloat() * cutoffHz)
    private val previousInputs = FloatArray(3)
    private val previousOutputs = FloatArray(3)

    private var previousTimeNanos = 0L
    private var latestZ = 0f

    // sample as fast as the phone allows
    fun start() {
        previousTimeNanos = 0L
        accelerometer?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    fun stop() = sensorManager.unregisterListener(this)

    // face down: gravity on -z
    fun isFaceDown() = latestZ < -8f

    override fun onSensorChanged(event: SensorEvent) {
        val (x, y, z) = event.values
        val isFirstSample = previousTimeNanos == 0L
        val secondsSinceLastSample = (event.timestamp - previousTimeNanos) / 1e9f
        previousTimeNanos = event.timestamp
        latestZ = z

        if (isFirstSample) {
            // first sample: just store it
            previousInputs[0] = x; previousInputs[1] = y; previousInputs[2] = z
            previousOutputs.fill(0f)
            return
        }

        // remove gravity; buzz = length of the vector
        val filteredX = highPass(0, x, secondsSinceLastSample)
        val filteredY = highPass(1, y, secondsSinceLastSample)
        val filteredZ = highPass(2, z, secondsSinceLastSample)
        val buzzStrength = sqrt(filteredX * filteredX + filteredY * filteredY + filteredZ * filteredZ)

        onSample?.invoke(AccelerometerSample(event.timestamp, x, y, z, buzzStrength))
    }

    // keep fast changes only (gravity -> ~0)
    private fun highPass(axis: Int, input: Float, secondsSinceLastSample: Float): Float {
        val alpha = timeConstant / (timeConstant + secondsSinceLastSample)
        val output = alpha * (previousOutputs[axis] + input - previousInputs[axis])
        previousInputs[axis] = input
        previousOutputs[axis] = output
        return output
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
