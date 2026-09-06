package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.LayerActivityPoint
import com.example.data.model.SnnNodeState
import com.example.data.model.SnnTelemetry
import com.example.ui.theme.ImmersiveCyan
import com.example.ui.theme.ImmersiveEmerald
import com.example.ui.theme.ImmersiveGlassBorder
import com.example.ui.theme.ImmersiveGlassBorderCyan
import com.example.ui.theme.ImmersivePurple
import kotlin.math.max
import kotlin.math.min
import org.json.JSONArray
import org.json.JSONObject

enum class DashboardViewMode {
    COMPOSE_NATIVE,
    RECHARTS_WEB
}

val LayerColorL23 = Color(0xFF00F5D4)
val LayerColorL4 = Color(0xFFA855F7)
val LayerColorL5 = Color(0xFF10B981)
val LayerColorPv = Color(0xFFF43F5E)
val LayerColorOverall = Color(0xFF38BDF8)

/**
 * Real-time SNN Dashboard Component inspired by Recharts.
 * Visualizes the activity of the Spiking Neural Network nodes,
 * showing multi-layer firing rates, layer synchronization, and node states.
 */
@Composable
fun SnnRechartsDashboard(
    telemetry: SnnTelemetry,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(DashboardViewMode.COMPOSE_NATIVE) }
    var selectedNode by remember { mutableStateOf<SnnNodeState?>(null) }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("snn_recharts_dashboard"),
        borderColor = ImmersiveGlassBorderCyan,
        backgroundColor = Color(0xFF0B132B).copy(alpha = 0.85f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title & View Mode Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00F5D4).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF00F5D4).copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Recharts Dashboard",
                            tint = ImmersiveCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SNN Recharts Activity Dashboard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                        Text(
                            text = "Real-time Firing Rates & Layer Synchronization",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                GlowingPulseDot(color = ImmersiveEmerald, size = 7.dp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Switcher Tabs: Native Compose vs. Embedded Recharts Web
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = viewMode == DashboardViewMode.COMPOSE_NATIVE,
                    onClick = { viewMode = DashboardViewMode.COMPOSE_NATIVE },
                    label = { Text("Compose Recharts View", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ImmersiveCyan.copy(alpha = 0.2f),
                        selectedLabelColor = ImmersiveCyan,
                        selectedLeadingIconColor = ImmersiveCyan,
                        containerColor = Color(0xFF1E293B).copy(alpha = 0.5f),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = viewMode == DashboardViewMode.COMPOSE_NATIVE,
                        borderColor = ImmersiveGlassBorderCyan,
                        selectedBorderColor = ImmersiveCyan
                    ),
                    modifier = Modifier.testTag("tab_compose_chart")
                )

                FilterChip(
                    selected = viewMode == DashboardViewMode.RECHARTS_WEB,
                    onClick = { viewMode = DashboardViewMode.RECHARTS_WEB },
                    label = { Text("Interactive Recharts Web", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Web,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ImmersivePurple.copy(alpha = 0.25f),
                        selectedLabelColor = ImmersivePurple,
                        selectedLeadingIconColor = ImmersivePurple,
                        containerColor = Color(0xFF1E293B).copy(alpha = 0.5f),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = viewMode == DashboardViewMode.RECHARTS_WEB,
                        borderColor = ImmersiveGlassBorder,
                        selectedBorderColor = ImmersivePurple
                    ),
                    modifier = Modifier.testTag("tab_web_recharts")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick High-Level Metrics Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DashboardMetricCard(
                    title = "Mean Firing Rate",
                    value = "${String.format("%.1f", telemetry.firingRateHz)} Hz",
                    subtitle = "L4: ${String.format("%.1f", telemetry.layer4Hz)} Hz",
                    accentColor = ImmersiveCyan,
                    modifier = Modifier.weight(1f)
                )

                DashboardMetricCard(
                    title = "Layer Synchrony",
                    value = String.format("%.2f", telemetry.synchronyIndex),
                    subtitle = if (telemetry.synchronyIndex > 0.65f) "Gamma Phase Lock" else "Desynchronized",
                    accentColor = ImmersiveEmerald,
                    modifier = Modifier.weight(1f)
                )

                DashboardMetricCard(
                    title = "Active Nodes",
                    value = "${telemetry.activeNeurons} / 120",
                    subtitle = "${telemetry.activeSynapses} Synapses",
                    accentColor = ImmersivePurple,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (viewMode) {
                DashboardViewMode.COMPOSE_NATIVE -> {
                    // 1. Real-time Multi-Layer Firing Rates Chart (Recharts styled)
                    Text(
                        text = "Cortical Layer Firing Rates Over Time",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2E8F0)
                    )
                    Text(
                        text = "Interactive time scrub - drag across to inspect instantaneous layer Hz",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    RechartsFiringRateCanvas(
                        history = telemetry.history,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .testTag("compose_firing_rate_chart")
                    )

                    // Recharts Legend
                    RechartsLegendRow()

                    Spacer(modifier = Modifier.height(18.dp))

                    // 2. Layer Synchronization & Coherence Breakdown
                    Text(
                        text = "Cortical Layer Phase Synchronization & Coherence",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LayerSynchronySection(
                        syncL2L4 = telemetry.layerSyncL2L4,
                        syncL4L5 = telemetry.layerSyncL4L5,
                        syncPv = telemetry.layerSyncPv,
                        overallSync = telemetry.synchronyIndex,
                        history = telemetry.history
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 3. 120 SNN Cortical Nodes Activity Matrix
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cortical Node Activity Matrix (120 Nodes)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE2E8F0)
                        )
                        Text(
                            text = "Tap node to inspect",
                            fontSize = 11.sp,
                            color = ImmersiveCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    SnnNodesGrid(
                        nodes = telemetry.nodes,
                        selectedNodeId = selectedNode?.id,
                        onNodeClick = { node -> selectedNode = node },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Inspector popup if a node is tapped
                    AnimatedVisibility(
                        visible = selectedNode != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        selectedNode?.let { node ->
                            NodeInspectorCard(
                                node = node,
                                onClose = { selectedNode = null },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                            )
                        }
                    }
                }

                DashboardViewMode.RECHARTS_WEB -> {
                    // Embedded HTML5 Recharts Component via WebView
                    Text(
                        text = "Live Recharts Web Engine (SVG & AreaChart)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2E8F0)
                    )
                    Text(
                        text = "Rendering standalone interactive Recharts dashboard synced via JavaScript interface",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    RechartsWebView(
                        telemetry = telemetry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(440.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, ImmersiveGlassBorder, RoundedCornerShape(12.dp))
                            .testTag("webview_recharts")
                    )
                }
            }
        }
    }
}

/**
 * Metric summary tile.
 */
@Composable
fun DashboardMetricCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.65f))
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color(0xFF64748B),
                maxLines = 1
            )
        }
    }
}

/**
 * Native Jetpack Compose Canvas implementation of the Recharts Area & Line Chart.
 * Includes bezier smoothing, gradient fills, cartesian grid lines, and interactive scrub.
 */
@Composable
fun RechartsFiringRateCanvas(
    history: List<LayerActivityPoint>,
    modifier: Modifier = Modifier
) {
    var scrubIndex by remember { mutableStateOf<Int?>(null) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF040711).copy(alpha = 0.9f))
            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .pointerInput(history) {
                detectTapGestures(
                    onTap = { offset ->
                        if (history.isNotEmpty()) {
                            val idx = ((offset.x / size.width) * (history.size - 1))
                                .toInt()
                                .coerceIn(0, history.size - 1)
                            scrubIndex = idx
                        }
                    }
                )
            }
            .pointerInput(history) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (history.isNotEmpty()) {
                            val idx = ((offset.x / size.width) * (history.size - 1))
                                .toInt()
                                .coerceIn(0, history.size - 1)
                            scrubIndex = idx
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        if (history.isNotEmpty()) {
                            val idx = ((change.position.x / size.width) * (history.size - 1))
                                .toInt()
                                .coerceIn(0, history.size - 1)
                            scrubIndex = idx
                        }
                    },
                    onDragEnd = { /* keep last scrubbed */ },
                    onDragCancel = { /* keep */ }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(170.dp)) {
            val width = size.width
            val height = size.height
            val leftPad = 36.dp.toPx()
            val rightPad = 12.dp.toPx()
            val topPad = 14.dp.toPx()
            val bottomPad = 24.dp.toPx()

            val chartWidth = width - leftPad - rightPad
            val chartHeight = height - topPad - bottomPad
            val maxHz = 50f

            // Cartesian Grid Lines (Recharts style)
            val gridSteps = 4
            for (i in 0..gridSteps) {
                val y = topPad + (i.toFloat() / gridSteps) * chartHeight
                drawLine(
                    color = Color.White.copy(alpha = 0.06f),
                    start = Offset(leftPad, y),
                    end = Offset(width - rightPad, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                )
            }

            // Left & Bottom Axis lines
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(leftPad, topPad),
                end = Offset(leftPad, topPad + chartHeight),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(leftPad, topPad + chartHeight),
                end = Offset(width - rightPad, topPad + chartHeight),
                strokeWidth = 1.dp.toPx()
            )

            if (history.size < 2) return@Canvas

            val numPoints = history.size
            fun getX(index: Int): Float {
                return leftPad + (index.toFloat() / (numPoints - 1)) * chartWidth
            }
            fun getY(hz: Float): Float {
                val clamped = hz.coerceIn(0f, maxHz)
                return topPad + chartHeight - (clamped / maxHz) * chartHeight
            }

            // Draw Area Gradients & Smooth Lines for each layer
            val layers = listOf(
                LayerSeries(name = "PV Basket", color = LayerColorPv, points = history.map { it.layerPvHz }),
                LayerSeries(name = "L4 Stellate", color = LayerColorL4, points = history.map { it.layer4Hz }),
                LayerSeries(name = "L2/3 Pyramidal", color = LayerColorL23, points = history.map { it.layer23Hz }),
                LayerSeries(name = "L5 Pyramidal", color = LayerColorL5, points = history.map { it.layer5Hz })
            )

            for (series in layers) {
                val linePath = Path()
                val areaPath = Path()

                val pts = series.points.mapIndexed { idx, hz -> Offset(getX(idx), getY(hz)) }
                if (pts.isNotEmpty()) {
                    linePath.moveTo(pts[0].x, pts[0].y)
                    areaPath.moveTo(pts[0].x, topPad + chartHeight)
                    areaPath.lineTo(pts[0].x, pts[0].y)

                    for (i in 0 until pts.size - 1) {
                        val p0 = if (i > 0) pts[i - 1] else pts[i]
                        val p1 = pts[i]
                        val p2 = pts[i + 1]
                        val p3 = if (i + 2 < pts.size) pts[i + 2] else p2

                        val cp1x = p1.x + (p2.x - p0.x) / 6f
                        val cp1y = p1.y + (p2.y - p0.y) / 6f
                        val cp2x = p2.x - (p3.x - p1.x) / 6f
                        val cp2y = p2.y - (p3.y - p1.y) / 6f

                        linePath.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
                        areaPath.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
                    }

                    areaPath.lineTo(pts.last().x, topPad + chartHeight)
                    areaPath.close()

                    // Draw Gradient Fill (Area)
                    drawPath(
                        path = areaPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                series.color.copy(alpha = 0.22f),
                                series.color.copy(alpha = 0.0f)
                            ),
                            startY = topPad,
                            endY = topPad + chartHeight
                        )
                    )

                    // Draw Smooth Line
                    drawPath(
                        path = linePath,
                        color = series.color,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Overall Mean rate dashed line
            val meanPts = history.mapIndexed { idx, pt -> Offset(getX(idx), getY(pt.overallHz)) }
            val meanPath = Path()
            meanPts.forEachIndexed { i, pt ->
                if (i == 0) meanPath.moveTo(pt.x, pt.y) else meanPath.lineTo(pt.x, pt.y)
            }
            drawPath(
                path = meanPath,
                color = LayerColorOverall,
                style = Stroke(
                    width = 1.8.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            )

            // Draw Interactive Scrub Line
            scrubIndex?.let { idx ->
                val validIdx = idx.coerceIn(0, numPoints - 1)
                val scrubX = getX(validIdx)
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(scrubX, topPad),
                    end = Offset(scrubX, topPad + chartHeight),
                    strokeWidth = 1.2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                )

                // Highlight dot on each series
                for (series in layers) {
                    val y = getY(series.points[validIdx])
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(scrubX, y)
                    )
                    drawCircle(
                        color = series.color,
                        radius = 2.5.dp.toPx(),
                        center = Offset(scrubX, y)
                    )
                }
            }
        }

        // Scrub Tooltip Floating Card (Recharts <Tooltip />)
        scrubIndex?.let { idx ->
            if (idx in history.indices) {
                val pt = history[idx]
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.92f))
                        .border(1.dp, ImmersiveGlassBorderCyan, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Column {
                        Text(
                            text = "T: ${pt.timeLabel} • Mean: ${String.format("%.1f", pt.overallHz)}Hz",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF1F5F9)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("L2/3: ${String.format("%.0f", pt.layer23Hz)}", color = LayerColorL23, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text("L4: ${String.format("%.0f", pt.layer4Hz)}", color = LayerColorL4, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text("L5: ${String.format("%.0f", pt.layer5Hz)}", color = LayerColorL5, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text("PV: ${String.format("%.0f", pt.layerPvHz)}", color = LayerColorPv, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

private data class LayerSeries(
    val name: String,
    val color: Color,
    val points: List<Float>
)

/**
 * Recharts Legend row.
 */
@Composable
fun RechartsLegendRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(color = LayerColorL23, label = "L2/3 Pyramidal")
        LegendItem(color = LayerColorL4, label = "L4 Stellate")
        LegendItem(color = LayerColorL5, label = "L5 Pyramidal")
        LegendItem(color = LayerColorPv, label = "PV Basket (Inh)")
        LegendItem(color = LayerColorOverall, label = "Mean Rate")
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, color = Color(0xFF94A3B8))
    }
}

/**
 * Layer Synchronization breakdown with cross-layer phase coherence bars and synchrony wave.
 */
@Composable
fun LayerSynchronySection(
    syncL2L4: Float,
    syncL4L5: Float,
    syncPv: Float,
    overallSync: Float,
    history: List<LayerActivityPoint>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF070D1E).copy(alpha = 0.7f))
            .border(1.dp, ImmersiveGlassBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Sync row 1
        SyncProgressBar(
            title = "L2/3 ↔ L4 Feedforward Sync",
            value = syncL2L4,
            barGradient = Brush.horizontalGradient(listOf(LayerColorL23, LayerColorL4))
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Sync row 2
        SyncProgressBar(
            title = "L4 ↔ L5 Deep Feedback Sync",
            value = syncL4L5,
            barGradient = Brush.horizontalGradient(listOf(LayerColorL4, LayerColorL5))
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Sync row 3
        SyncProgressBar(
            title = "Pyramidal ↔ PV Interneuron",
            value = syncPv,
            barGradient = Brush.horizontalGradient(listOf(LayerColorPv, ImmersiveCyan))
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Mini Synchrony Wave Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF030712).copy(alpha = 0.8f))
                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(44.dp)) {
                if (history.size > 1) {
                    val w = size.width
                    val h = size.height
                    val pts = history.mapIndexed { idx, pt ->
                        val x = (idx.toFloat() / (history.size - 1)) * w
                        val y = h - (pt.synchronyIndex.coerceIn(0f, 1f)) * (h - 6.dp.toPx())
                        Offset(x, y)
                    }

                    val path = Path()
                    val area = Path()
                    path.moveTo(pts[0].x, pts[0].y)
                    area.moveTo(pts[0].x, h)
                    area.lineTo(pts[0].x, pts[0].y)

                    for (i in 0 until pts.size - 1) {
                        val midX = (pts[i].x + pts[i + 1].x) / 2f
                        val midY = (pts[i].y + pts[i + 1].y) / 2f
                        path.quadraticTo(pts[i].x, pts[i].y, midX, midY)
                        area.quadraticTo(pts[i].x, pts[i].y, midX, midY)
                    }
                    path.lineTo(pts.last().x, pts.last().y)
                    area.lineTo(pts.last().x, pts.last().y)
                    area.lineTo(w, h)
                    area.close()

                    drawPath(
                        path = area,
                        brush = Brush.verticalGradient(
                            listOf(ImmersiveEmerald.copy(alpha = 0.35f), Color.Transparent),
                            startY = 0f,
                            endY = h
                        )
                    )
                    drawPath(
                        path = path,
                        color = ImmersiveEmerald,
                        style = Stroke(width = 1.8.dp.toPx())
                    )
                }
            }
            Text(
                text = "Kuramoto Coherence Wave",
                fontSize = 9.sp,
                color = ImmersiveEmerald.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 6.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun SyncProgressBar(
    title: String,
    value: Float,
    barGradient: Brush
) {
    val animatedProgress by animateFloatAsState(
        targetValue = value.coerceIn(0f, 1f),
        label = "syncProgress"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            color = Color(0xFFCBD5E1),
            modifier = Modifier.width(150.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .padding(horizontal = 8.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(barGradient)
            )
        }

        Text(
            text = String.format("%.2f", value),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF1F5F9),
            modifier = Modifier.width(36.dp)
        )
    }
}

/**
 * 120 SNN Cortical Nodes Grid.
 * Renders all 120 neurons arranged by cortical layers:
 * - 0 until 54: L2/3 Pyramidal
 * - 54 until 78: L4 Stellate
 * - 78 until 96: L5 Pyramidal
 * - 96 until 120: PV Basket (Inhibitory)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SnnNodesGrid(
    nodes: List<SnnNodeState>,
    selectedNodeId: Int?,
    onNodeClick: (SnnNodeState) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF030712).copy(alpha = 0.85f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            // Legend of Node Layers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("L2/3 (54)", fontSize = 9.sp, color = LayerColorL23)
                Text("L4 (24)", fontSize = 9.sp, color = LayerColorL4)
                Text("L5 (18)", fontSize = 9.sp, color = LayerColorL5)
                Text("PV Inh (24)", fontSize = 9.sp, color = LayerColorPv)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Grid layout of nodes (12 columns x 10 rows)
            val totalNodes = if (nodes.size >= 120) nodes else List(120) { idx ->
                val layer = when {
                    idx < 54 -> "L2/3 Pyramidal"
                    idx < 78 -> "L4 Stellate"
                    idx < 96 -> "L5 Pyramidal"
                    else -> "PV Basket (Inh)"
                }
                SnnNodeState(id = idx, layer = layer, voltage = -65f, isSpiking = false, calcium = 0.05f)
            }

            val columns = 15
            val rows = 8 // 15 x 8 = 120 nodes

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                for (row in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        for (col in 0 until columns) {
                            val idx = row * columns + col
                            if (idx < totalNodes.size) {
                                val node = totalNodes[idx]
                                val isSelected = selectedNodeId == node.id
                                val nodeColor = when {
                                    node.isSpiking -> Color.White
                                    node.voltage > -55f -> ImmersiveCyan
                                    node.id >= 96 -> LayerColorPv.copy(alpha = 0.4f + (node.voltage + 75f) / 50f)
                                    node.id >= 78 -> LayerColorL5.copy(alpha = 0.35f + (node.voltage + 75f) / 50f)
                                    node.id >= 54 -> LayerColorL4.copy(alpha = 0.35f + (node.voltage + 75f) / 50f)
                                    else -> LayerColorL23.copy(alpha = 0.35f + (node.voltage + 75f) / 50f)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(nodeColor)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 0.5.dp,
                                            color = if (isSelected) Color.White else Color.Transparent,
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                        .clickable { onNodeClick(node) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Inspector Card shown when user clicks on a node in the 120-node matrix.
 */
@Composable
fun NodeInspectorCard(
    node: SnnNodeState,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, ImmersiveCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (node.isSpiking) Color.White else ImmersiveCyan)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Node #${node.id}: ${node.layer}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFF1F5F9)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "V: ${String.format("%.1f", node.voltage)} mV",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = if (node.voltage > -55f) ImmersiveCyan else Color(0xFF94A3B8)
                    )
                    Text(
                        text = "Ca²⁺: ${String.format("%.3f", node.calcium)}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Text(
                        text = if (node.isSpiking) "ACTION POTENTIAL" else "Polarized",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (node.isSpiking) ImmersiveEmerald else Color(0xFF64748B)
                    )
                }
            }

            IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Embedded Recharts Web View running snn_recharts.html from assets,
 * with real-time JSON updates streamed from Kotlin via evaluateJavascript.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RechartsWebView(
    telemetry: SnnTelemetry,
    modifier: Modifier = Modifier
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Stream telemetry updates into WebView
    LaunchedEffect(telemetry) {
        val web = webViewRef ?: return@LaunchedEffect
        try {
            val json = JSONObject().apply {
                put("timeMs", telemetry.timeMs)
                put("firingRateHz", telemetry.firingRateHz)
                put("layer23Hz", telemetry.layer23Hz)
                put("layer4Hz", telemetry.layer4Hz)
                put("layer5Hz", telemetry.layer5Hz)
                put("layerPvHz", telemetry.layerPvHz)
                put("synchronyIndex", telemetry.synchronyIndex)
                put("layerSyncL2L4", telemetry.layerSyncL2L4)
                put("layerSyncL4L5", telemetry.layerSyncL4L5)
                put("layerSyncPv", telemetry.layerSyncPv)

                val historyArr = JSONArray()
                telemetry.history.forEach { pt ->
                    historyArr.put(JSONObject().apply {
                        put("timeLabel", pt.timeLabel)
                        put("overallHz", pt.overallHz)
                        put("layer23Hz", pt.layer23Hz)
                        put("layer4Hz", pt.layer4Hz)
                        put("layer5Hz", pt.layer5Hz)
                        put("layerPvHz", pt.layerPvHz)
                        put("synchronyIndex", pt.synchronyIndex)
                    })
                }
                put("history", historyArr)

                val nodesArr = JSONArray()
                telemetry.nodes.forEach { node ->
                    nodesArr.put(JSONObject().apply {
                        put("id", node.id)
                        put("layer", node.layer)
                        put("voltage", node.voltage)
                        put("isSpiking", node.isSpiking)
                        put("calcium", node.calcium)
                    })
                }
                put("nodes", nodesArr)
            }

            val script = "window.updateSnnData && window.updateSnnData($json);"
            web.evaluateJavascript(script, null)
        } catch (_: Exception) {
            // Graceful silent ignore if WebView is destroyed or detached
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.destroy()
            webViewRef = null
        }
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(AndroidColor.parseColor("#05070a"))
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    cacheMode = WebSettings.LOAD_NO_CACHE
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // Trigger initial paint
                    }
                }
                loadUrl("file:///android_asset/snn_recharts.html")
                webViewRef = this
            }
        },
        modifier = modifier
    )
}
