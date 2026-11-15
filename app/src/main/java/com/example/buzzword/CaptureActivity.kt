package com.example.buzzword

import android.os.Bundle
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

// test screen: record the accelerometer, buzz a pattern, save a CSV
class CaptureActivity : ComponentActivity() {
    private lateinit var sensorHub: SensorHub
    private val samples = ArrayList<AccelerometerSample>()
    private var recordingStartNanos = 0L
    private var patternStartNanos = -1L   // -1 = not played yet

    private val testPattern = "10101100".repeat(3)
    private var bitLengthMillis by mutableStateOf(100L)
    private var isRecording by mutableStateOf(false)
    private var statusText by mutableStateOf("Phone on the table, then Start.")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorHub = SensorHub(this)
        sensorHub.onSample = { sample -> samples.add(sample) }

        setContent {
            MaterialTheme {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { if (isRecording) stopAndSave() else startRecording() }) {
                        Text(if (isRecording) "Stop and save" else "Start recording")
                    }
                    Button(onClick = { changeBitLength() }) {
                        Text("Bit length: $bitLengthMillis ms (tap to change)")
                    }
                    Button(onClick = { playTestPattern() }, enabled = isRecording) {
                        Text("Buzz test (this phone)")
                    }
                    Text(statusText)
                }
            }
        }
    }

    // cycle bit length: 100 - 150 - 200 ms
    private fun changeBitLength() {
        bitLengthMillis = if (bitLengthMillis == 200L) 100L else bitLengthMillis + 50
    }

    private fun startRecording() {
        samples.clear()
        patternStartNanos = -1L
        recordingStartNanos = SystemClock.elapsedRealtimeNanos()
        sensorHub.start()
        isRecording = true
        statusText = "Recording... wait 2 s, then Buzz test."
    }

    // buzz the pattern: 1 = motor on for 70% of the bit, 0 = off
    private fun playTestPattern() {
        val motorOnMillis = bitLengthMillis * 7 / 10
        val timings = listOf(0L) + testPattern.flatMap { bit ->
            if (bit == '1') listOf(motorOnMillis, bitLengthMillis - motorOnMillis)
            else listOf(0L, bitLengthMillis)
        }
        val vibrator = getSystemService(Vibrator::class.java)
        vibrator.vibrate(VibrationEffect.createWaveform(timings.toLongArray(), -1))
        patternStartNanos = SystemClock.elapsedRealtimeNanos()
        statusText = "Buzzing... then Stop and save."
    }

    // 1 if this phone's motor was on at that time
    private fun isMotorOn(timeNanos: Long): Int {
        if (patternStartNanos < 0 || timeNanos < patternStartNanos) return 0
        val millisSincePattern = (timeNanos - patternStartNanos) / 1_000_000
        val bitIndex = (millisSincePattern / bitLengthMillis).toInt()
        val millisInsideBit = millisSincePattern % bitLengthMillis
        val motorOn = bitIndex < testPattern.length &&
                testPattern[bitIndex] == '1' &&
                millisInsideBit < bitLengthMillis * 7 / 10
        return if (motorOn) 1 else 0
    }

    // save the csv
    private fun stopAndSave() {
        sensorHub.stop()
        isRecording = false
        val fileName = "capture_${System.currentTimeMillis() / 1000}_${bitLengthMillis}ms.csv"
        val file = File(getExternalFilesDir(null), fileName)
        file.printWriter().use { writer ->
            writer.println("time_ms,x,y,z,buzz,motor")
            for (sample in samples) {
                val millisSinceStart = (sample.timeNanos - recordingStartNanos) / 1e6
                writer.println("$millisSinceStart,${sample.x},${sample.y},${sample.z}," +
                        "${sample.buzzStrength},${isMotorOn(sample.timeNanos)}")
            }
        }
        val recordedSeconds = ((samples.lastOrNull()?.timeNanos ?: recordingStartNanos) - recordingStartNanos) / 1e9
        val samplesPerSecond = if (recordedSeconds > 0) (samples.size / recordedSeconds).toInt() else 0
        statusText = "Saved ${samples.size} samples ($samplesPerSecond per second)\n$fileName"
    }

    // stop recording when the app leaves the screen
    override fun onPause() {
        super.onPause()
        if (isRecording) stopAndSave()
    }
}
