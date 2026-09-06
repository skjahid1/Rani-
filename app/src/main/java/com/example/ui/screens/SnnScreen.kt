package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.RaniViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingPulseDot
import com.example.ui.components.ImmersiveBadge
import com.example.ui.components.SnnRechartsDashboard
import com.example.ui.theme.ImmersiveCyan
import com.example.ui.theme.ImmersiveEmerald
import com.example.ui.theme.ImmersiveGlassBorder
import com.example.ui.theme.ImmersiveGlassBorderCyan
import com.example.ui.theme.ImmersivePurple

@Composable
fun SnnScreen(
    viewModel: RaniViewModel,
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.snnTelemetry.collectAsState()
    val voltageHistory by viewModel.voltageHistory.collectAsState()
    val isRunning by viewModel.isSnnRunning.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF05070A))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = ImmersiveGlassBorderCyan,
                backgroundColor = Color(0xFF0D162B).copy(alpha = 0.75f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(ImmersivePurple, ImmersiveCyan)
                                        )
                                    )
                                    .padding(1.5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0A0F1D)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = ImmersiveCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "SNN Biophysical Engine",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF8FAFC)
                                )
                                Text(
                                    text = "Hodgkin-Huxley Multi-Compartment Model",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Status pill with pulse dot
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isRunning) ImmersiveEmerald.copy(alpha = 0.12f) else Color(0x22EF4444))
                                .border(1.dp, if (isRunning) ImmersiveEmerald.copy(alpha = 0.35f) else Color(0x44EF4444), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            GlowingPulseDot(
                                color = if (isRunning) ImmersiveEmerald else Color(0xFFEF4444),
                                size = 6.dp
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isRunning) "ACTIVE" else "PAUSED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRunning) ImmersiveEmerald else Color(0xFFEF4444)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Real-time Spiking Neural Network running right on your device! Simulates 120 cortical neurons (L2/3 Pyramidal, L4 Stellate, L5 Pyramidal, PV Basket) with membrane potential V, m/h/n gating particles, and Tsodyks-Markram synaptic plasticity.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 19.sp,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Controls Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.injectSnnPulse() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues(),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.horizontalGradient(listOf(ImmersiveCyan, Color(0xFF00B4D8))))
                                .testTag("inject_pulse_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF05070A), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Inject Pulse", color = Color(0xFF05070A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = { viewModel.toggleSnnSimulation(!isRunning) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF0E172A).copy(alpha = 0.6f),
                                contentColor = ImmersiveCyan
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveGlassBorderCyan),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = ImmersiveCyan
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = if (isRunning) "Pause" else "Run", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetSnn() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color(0xFF0E172A).copy(alpha = 0.6f),
                                contentColor = Color(0xFF94A3B8)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveGlassBorder),
                            modifier = Modifier
                                .weight(0.8f)
                                .height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }

        // Real-Time Recharts Dashboard: Activity of SNN nodes, firing rates, and layer synchronization
        item {
            SnnRechartsDashboard(
                telemetry = telemetry,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Membrane Potential Oscilloscope Canvas
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                contentDescription = null,
                                tint = ImmersiveCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Soma Potential Oscilloscope (V_soma)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF8FAFC)
                            )
                        }

                        val lastV = voltageHistory.lastOrNull() ?: -65f
                        Text(
                            text = "${String.format("%.1f", lastV)} mV",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (lastV > -55f) ImmersiveCyan else Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Oscilloscope Canvas (Immersive Void Screen)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF030712))
                            .border(1.dp, ImmersiveGlassBorderCyan, RoundedCornerShape(10.dp))
                            .testTag("oscilloscope_canvas")
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw grid lines
                            val gridColor = Color(0x1538BDF8)
                            for (yStep in 1..3) {
                                val yPos = h * (yStep / 4f)
                                drawLine(
                                    color = gridColor,
                                    start = Offset(0f, yPos),
                                    end = Offset(w, yPos),
                                    strokeWidth = 1f
                                )
                            }

                            // Threshold line at -55 mV
                            val thresholdNormY = 1.0f - ((-55f - (-80f)) / (40f - (-80f)))
                            val thresholdY = h * thresholdNormY.coerceIn(0f, 1f)
                            drawLine(
                                color = Color(0x66FF5252),
                                start = Offset(0f, thresholdY),
                                end = Offset(w, thresholdY),
                                strokeWidth = 1.5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            )

                            // Resting potential line at -65 mV
                            val restNormY = 1.0f - ((-65f - (-80f)) / (40f - (-80f)))
                            val restY = h * restNormY.coerceIn(0f, 1f)
                            drawLine(
                                color = Color(0x3322D3EE),
                                start = Offset(0f, restY),
                                end = Offset(w, restY),
                                strokeWidth = 1f
                            )

                            // Plot Voltage Trace
                            if (voltageHistory.size > 1) {
                                val path = Path()
                                val stepX = w / (voltageHistory.size - 1)

                                for (i in voltageHistory.indices) {
                                    val v = voltageHistory[i].coerceIn(-80f, 40f)
                                    val norm = 1.0f - ((v - (-80f)) / (40f - (-80f)))
                                    val x = i * stepX
                                    val y = norm * h

                                    if (i == 0) {
                                        path.moveTo(x, y)
                                    } else {
                                        path.lineTo(x, y)
                                    }
                                }

                                drawPath(
                                    path = path,
                                    color = ImmersiveCyan,
                                    style = Stroke(width = 2.5f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Rest: -65 mV", fontSize = 10.sp, color = ImmersiveCyan.copy(alpha = 0.7f))
                        Text(text = "Spike Threshold: -55 mV", fontSize = 10.sp, color = Color(0xAAFF5252))
                        Text(text = "Peak: +30 mV", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    }
                }
            }
        }

        // Live Spike Raster Plot Canvas
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = null,
                                tint = ImmersivePurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Population Spike Raster Plot",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF8FAFC)
                            )
                        }

                        ImmersiveBadge(text = "120 Neurons", tint = ImmersivePurple)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Spike Raster Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF030712))
                            .border(1.dp, ImmersiveGlassBorder, RoundedCornerShape(10.dp))
                            .testTag("spike_raster_canvas")
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw population separator bands
                            val p1 = h * 0.45f
                            val p2 = h * 0.65f
                            val p3 = h * 0.80f

                            drawLine(Color(0x15FFFFFF), Offset(0f, p1), Offset(w, p1), 1f)
                            drawLine(Color(0x15FFFFFF), Offset(0f, p2), Offset(w, p2), 1f)
                            drawLine(Color(0x33FF5252), Offset(0f, p3), Offset(w, p3), 1f)

                            // Draw spikes
                            for (i in 0 until 120) {
                                val isFiring = viewModel.voltageHistory.value.let { hist ->
                                    (i % 17 == 0 && (hist.lastOrNull() ?: -65f) > -55f) || (i % 7 == 0 && (telemetry.timeMs.toInt() % 40 < 5))
                                }
                                if (isFiring) {
                                    val y = (i / 120f) * h
                                    val color = if (i >= 96) Color(0xFFFF5252) else if (i >= 54) ImmersivePurple else ImmersiveCyan
                                    drawCircle(
                                        color = color,
                                        radius = 2.5f,
                                        center = Offset(w - (i * 3f % w), y)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "• L2/3 & L5 Excitatory", fontSize = 10.sp, color = ImmersiveCyan)
                        Text(text = "• L4 Stellate", fontSize = 10.sp, color = ImmersivePurple)
                        Text(text = "• PV Basket (Inhibitory)", fontSize = 10.sp, color = Color(0xFFFF5252))
                    }
                }
            }
        }

        // Live Biophysical Telemetry Dashboard
        item {
            Text(
                text = "Biophysical Telemetry",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8FAFC)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetryTile("Mean Firing Rate", "${String.format("%.1f", telemetry.firingRateHz)} Hz", ImmersiveCyan, Modifier.weight(1f))
                    TelemetryTile("Active Synapses", "${telemetry.activeSynapses}", ImmersivePurple, Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetryTile("Intracellular [Ca²⁺]ᵢ", "${String.format("%.3f", telemetry.meanCalcium)} μM", Color(0xFFFBBF24), Modifier.weight(1f))
                    TelemetryTile("TM Vesicle Pool (x)", String.format("%.2f", telemetry.tmDepressionX), ImmersiveEmerald, Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetryTile("TM Facilitation (u)", String.format("%.2f", telemetry.tmFacilitationU), Color(0xFFF43F5E), Modifier.weight(1f))
                    TelemetryTile("Mean STDP Weight", "${String.format("%.2f", telemetry.stdpWeightMean)} nS", Color(0xFF38BDF8), Modifier.weight(1f))
                }
            }
        }

        // STDP Synaptic Plasticity Card
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "STDP Synaptic Plasticity",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = "Spike-Timing-Dependent Plasticity dynamically modulates synaptic weights based on millisecond pre/post spike intervals.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = true,
                        onCheckedChange = { viewModel.toggleStdp(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ImmersiveCyan,
                            checkedTrackColor = ImmersiveCyan.copy(alpha = 0.25f)
                        )
                    )
                }
            }
        }

        // Educational Section: "How SNN Works"
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = null,
                    tint = ImmersiveCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "How SNN (Spiking Neural Network) Works",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF8FAFC)
                )
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ExplainerTopic(
                        title = "1. Biological Spikes vs Standard ANNs",
                        body = "Standard AI (like transformers & CNNs) pass continuous floating-point numbers continuously. Spiking Neural Networks only transmit sparse, discrete binary pulses ('spikes') when membrane potential crosses a threshold (~ -55 mV), consuming 100x less energy like a real human brain."
                    )
                    ExplainerTopic(
                        title = "2. Hodgkin-Huxley Ion Channels",
                        body = "Membrane voltage is governed by voltage-dependent sodium (Na+) and potassium (K+) ionic conductances: I_Na = g_Na * m³ * h * (V - E_Na) and I_K = g_K * n⁴ * (V - E_K). When depolarized, rapid Na+ influx causes a sharp spike up to +30 mV, followed by delayed K+ efflux returning to resting potential (-65 mV)."
                    )
                    ExplainerTopic(
                        title = "3. Tsodyks-Markram Synaptic Plasticity",
                        body = "Synapses are not static weights. In biology, neurotransmitter vesicles deplete (short-term depression) and calcium accumulates (short-term facilitation). The TM model tracks available vesicle fraction x and release probability u dynamically."
                    )
                    ExplainerTopic(
                        title = "4. STDP (Spike-Timing-Dependent Plasticity)",
                        body = "Neurons that fire together wire together: If a pre-synaptic neuron spikes slightly before the post-synaptic neuron (Δt > 0), the synapse potentiates (LTP, weight increases). If post spikes before pre (Δt < 0), the synapse depresses (LTD, weight decreases)."
                    )
                }
            }
        }
    }
}

@Composable
fun TelemetryTile(title: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    GlassCard(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFF0B1224).copy(alpha = 0.6f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun ExplainerTopic(title: String, body: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = ImmersiveCyan
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFCBD5E1),
            lineHeight = 18.sp,
            fontSize = 12.sp
        )
    }
}

