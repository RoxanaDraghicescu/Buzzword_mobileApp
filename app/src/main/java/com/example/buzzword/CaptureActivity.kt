package com.example.buzzword

import android.os.Bundle
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.buzzword.ui.theme.BuzzwordTheme
import java.io.File

// test screen: record the accelerometer, buzz a pattern, save a CSV
class CaptureActivity : ComponentActivity() {
    private lateinit var sensorHub: SensorHub
    private val samples = ArrayList<AccelerometerSample>()
    private var recordingStartNanos = 0L
    private var patternStartNanos = -1L   // -1 = not played yet

    private lateinit var vibrationMotor: VibrationMotor
    private val testMessage = "HELLO"   // default test: seat 1 -> seat 2
    private val testPattern = Encoder().encode(testMessage, from = 1, to = 2)
    private var bitLengthMillis by mutableStateOf(Decoder.BIT_LENGTH_MILLIS)
    private var isRecording by mutableStateOf(false)
    private var statusText by mutableStateOf("Phone on the table, then Start.")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorHub = SensorHub(this)
        vibrationMotor = VibrationMotor(this)
        sensorHub.onSample = { sample -> samples.add(sample) }

        setContent {
            BuzzwordTheme {
                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Test message: \"$testMessage\" (about ${buzzSeconds()} s at $bitLengthMillis ms)")
                        Button(onClick = { if (isRecording) stopAndSave() else startRecording() }) {
                            Text(if (isRecording) "Stop and save" else "Start recording")
                        }
                        Button(onClick = { changeBitLength() }) {
                            Text("Bit length: $bitLengthMillis ms (tap to change)")
                        }
                        Button(onClick = { playTestPattern() }, enabled = isRecording) {
                            Text("Buzz \"$testMessage\" (this phone)")
                        }
                        Text(statusText)
                    }
                }
            }
        }
    }

    // cycle bit length: 100 - 150 - 200 ms
    private fun changeBitLength() {
        bitLengthMillis = if (bitLengthMillis == 200L) 100L else bitLengthMillis + 50   // 100 -> 150 -> 200
    }

    // how long the test message buzzes, in seconds
    private fun buzzSeconds() = testPattern.length * bitLengthMillis / 1000

    private fun startRecording() {
        samples.clear()
        patternStartNanos = -1L
        recordingStartNanos = SystemClock.elapsedRealtimeNanos()
        sensorHub.start()
        isRecording = true
        statusText = "Recording... wait 2 s, then Buzz \"$testMessage\"."
    }

    // buzz the pattern: 1 = motor on for 70% of the bit, 0 = off
    private fun playTestPattern() {
        vibrationMotor.playBits(testPattern, bitLengthMillis)
        patternStartNanos = SystemClock.elapsedRealtimeNanos()
        statusText = "Buzzing \"$testMessage\"... wait ${buzzSeconds() + 1} s, then Stop and save."
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
        statusText = "Saved ${samples.size} samples ($samplesPerSecond per second)\n$fileName\n\n" +
                decodeRecording()
    }

    // Decode the message in this recording
    private fun decodeRecording(): String {
        val decoder = Decoder(bitLengthMillis)
        // 1) Header first, to learn how many letters follow
        val header = decoder.decode(samples, Encoder.HEADER_BITS)?.drop(8) ?: return "No message found."
        val from = header.substring(3, 7).toInt(2)
        val to = header.substring(7, 11).toInt(2)
        val letterCount = header.substring(11, 16).toInt(2) + 1
        // 2) Whole frame, then the text after the header
        val frame = decoder.decode(samples, Encoder.HEADER_BITS + 6 * letterCount) ?: return "No message found."
        val text = Encoder.bitsToText(frame.drop(8 + Encoder.HEADER_BITS))
        return "Received from seat $from to seat $to: \"$text\""
    }

    // stop recording when the app leaves the screen
    override fun onPause() {
        super.onPause()
        if (isRecording) stopAndSave()
    }
}
