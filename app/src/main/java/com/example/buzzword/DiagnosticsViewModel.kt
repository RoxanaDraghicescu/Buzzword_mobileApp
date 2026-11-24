package com.example.buzzword

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

/**
 * Gives the buzz strength from Person 1's SensorHub as a Flow, so the graph shows exactly
 * what her Decoder reads (gravity removed with her high-pass filter). Silence ≈ 0, a buzz = a spike.
 * SensorHub only has a callback (onSample), so callbackFlow turns it into a Flow.
 */
fun sensorHubFlow(context: Context): Flow<Float> = callbackFlow {
    // SensorHub does nothing if there is no accelerometer, so we check first and end the flow.
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) {
        close()
        return@callbackFlow
    }
    val sensorHub = SensorHub(context)
    sensorHub.onSample = { sample -> trySend(sample.buzzStrength) }
    sensorHub.start()
    awaitClose { sensorHub.stop() } // stop reading when nobody listens
}

/** Collects the sensor values and gives the screen a snapshot ~30 times a second. */
class DiagnosticsViewModel(samples: Flow<Float>) : ViewModel() {

    data class State(
        val samples: List<Float> = emptyList(),
        val current: Float = 0f,
        val peak: Float = 0f,
        val sampleRate: Int = 0, // values per second
        val sensorMissing: Boolean = false,
    )

    private val buffer = ArrayDeque<Float>() // only the last MAX_SAMPLES values
    private var peak = 0f
    private var countThisSecond = 0
    private var sampleRate = 0
    private var sensorMissing = false

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        // 1. Read every value from the sensor (hundreds per second).
        viewModelScope.launch {
            var gotAny = false
            samples.collect { value ->
                gotAny = true
                buffer.addLast(value)
                if (buffer.size > MAX_SAMPLES) buffer.removeFirst()
                if (value > peak) peak = value
                countThisSecond++
            }
            if (!gotAny) sensorMissing = true // the flow ended without values
        }
        // 2. Count values per second.
        viewModelScope.launch {
            while (true) {
                delay(1_000)
                sampleRate = countThisSecond
                countThisSecond = 0
            }
        }
        // 3. Update the screen ~30 times a second (redrawing on every value would be too slow).
        viewModelScope.launch {
            while (true) {
                delay(33)
                _state.value = State(
                    samples = buffer.toList(),
                    current = buffer.lastOrNull() ?: 0f,
                    peak = peak,
                    sampleRate = sampleRate,
                    sensorMissing = sensorMissing,
                )
            }
        }
    }

    fun resetPeak() {
        peak = 0f
    }

    companion object {
        const val MAX_SAMPLES = 300

        val Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY])
                DiagnosticsViewModel(sensorHubFlow(app))
            }
        }
    }
}