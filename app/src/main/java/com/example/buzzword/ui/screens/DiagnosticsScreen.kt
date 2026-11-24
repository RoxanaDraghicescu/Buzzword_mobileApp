package com.example.buzzword.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.buzzword.DiagnosticsViewModel
import java.util.Locale

/** Diagnostics screen ("View diagnostics" use case): a live graph of the accelerometer. */
@Composable
fun DiagnosticsScreen(
    viewModel: DiagnosticsViewModel = viewModel(factory = DiagnosticsViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.sensorMissing) {
        EmptyState(
            icon = Icons.Outlined.Warning,
            title = "No accelerometer",
            description = "This phone or emulator has no accelerometer, so there is nothing to show.",
        )
        return
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column {
            Text("Accelerometer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                "How strongly this phone shakes, without gravity. Put another phone next to it on the table and make it buzz.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth(),
        ) {
            SignalGraph(state.samples, Modifier.fillMaxWidth().height(220.dp).padding(16.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("Now", "%.2f".format(Locale.US, state.current), "m/s²", Modifier.weight(1f))
            StatTile("Peak", "%.2f".format(Locale.US, state.peak), "m/s²", Modifier.weight(1f))
            StatTile("Rate", "${state.sampleRate}", "Hz", Modifier.weight(1f))
        }

        OutlinedButton(onClick = viewModel::resetPeak, shape = MaterialTheme.shapes.medium) {
            Text("Reset peak")
        }
    }
}

/** Draws the last values as a line: newest on the right, older ones move to the left. */
@Composable
private fun SignalGraph(samples: List<Float>, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Canvas(modifier) {
        for (i in 0..4) { // horizontal grid lines
            val y = size.height * i / 4
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
        }
        if (samples.size < 2) return@Canvas

        // The top of the graph is the biggest value, but at least 0.1 so that noise stays small
        // (Person 1's Decoder treats anything under 0.05 as "no buzz").
        val top = maxOf(samples.max(), 0.1f)
        val stepX = size.width / (DiagnosticsViewModel.MAX_SAMPLES - 1)
        val path = Path()
        samples.forEachIndexed { i, value ->
            val x = size.width - (samples.size - 1 - i) * stepX
            val y = size.height - (value / top).coerceIn(0f, 1f) * size.height
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
    }
}

@Composable
private fun StatTile(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainer, modifier = modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(4.dp))
                Text(
                    unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp),
                )
            }
        }
    }
}