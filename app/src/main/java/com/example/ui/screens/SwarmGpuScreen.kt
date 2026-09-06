package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServerType
import com.example.data.model.SwarmAgent
import com.example.data.model.SwarmAgentStatus
import com.example.ui.RaniViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingPulseDot
import com.example.ui.components.ImmersiveBadge
import com.example.ui.theme.ImmersiveCyan
import com.example.ui.theme.ImmersiveEmerald
import com.example.ui.theme.ImmersiveGlassBorder
import com.example.ui.theme.ImmersiveGlassBorderCyan
import com.example.ui.theme.ImmersivePurple

@Composable
fun SwarmGpuScreen(
    viewModel: RaniViewModel,
    modifier: Modifier = Modifier
) {
    val swarmEnabled by viewModel.swarmEnabled.collectAsState()
    val swarmCount by viewModel.swarmAgentCount.collectAsState()
    val swarmAgents by viewModel.swarmAgents.collectAsState()
    val serverConfig by viewModel.serverConfig.collectAsState()

    var primaryKeyInput by remember { mutableStateOf(viewModel.currentBrainApiKey) }
    var swarmKeyInput by remember { mutableStateOf(viewModel.currentSwarmApiKey) }
    var serverUrlInput by remember { mutableStateOf(serverConfig.url) }

    var sliderValue by remember(swarmCount) { mutableFloatStateOf(swarmCount.toFloat()) }
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF05070A))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Swarm Intelligence Controller
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = ImmersivePurple.copy(alpha = 0.4f),
                backgroundColor = Color(0xFF0D1226).copy(alpha = 0.75f)
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
                                    imageVector = Icons.Default.Hub,
                                    contentDescription = null,
                                    tint = ImmersivePurple,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Swarm Multi-Agent Controller",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF8FAFC)
                                )
                                Text(
                                    text = if (swarmEnabled) "$swarmCount Active Parallel Agents" else "Swarm Mode Disabled",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (swarmEnabled) ImmersiveEmerald else Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Swarm ON/OFF Toggle Switch
                        Switch(
                            checked = swarmEnabled,
                            onCheckedChange = { viewModel.setSwarmEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ImmersivePurple,
                                checkedTrackColor = ImmersivePurple.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier.testTag("swarm_toggle_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "When Swarm is ON, complex user questions are decomposed across multiple specialized AI agents executing in parallel. Results are synthesized into an authoritative consensus.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 19.sp,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Agent Count Slider
                    Text(
                        text = "Configured Swarm Workers: ${sliderValue.toInt()} Agents",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFF1F5F9)
                    )
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        onValueChangeFinished = { viewModel.setSwarmAgentCount(sliderValue.toInt()) },
                        valueRange = 1f..32f,
                        steps = 30,
                        enabled = swarmEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = ImmersivePurple,
                            activeTrackColor = ImmersivePurple,
                            inactiveTrackColor = Color(0x337C4DFF)
                        ),
                        modifier = Modifier.testTag("swarm_count_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dedicated Swarm Agent Free API Key Input
                    Text(
                        text = "Dedicated Free API Key for Agents (Optional):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE2E8F0)
                    )
                    Text(
                        text = "Use a secondary free Gemini API Key for swarm workers to distribute rate limits!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = swarmKeyInput,
                            onValueChange = { swarmKeyInput = it },
                            placeholder = { Text("Enter secondary free API key (optional)", fontSize = 12.sp, color = Color(0xFF64748B)) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("swarm_api_key_field"),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF070B14),
                                unfocusedContainerColor = Color(0xFF070B14),
                                focusedBorderColor = ImmersivePurple,
                                unfocusedBorderColor = ImmersiveGlassBorder
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.updateSwarmApiKey(swarmKeyInput) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ImmersivePurple)
                        ) {
                            Text("Save", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Active Swarm Agents List
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = ImmersivePurple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Swarm Agent Roster (${swarmAgents.size} Agents)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF8FAFC)
                )
            }
        }

        items(swarmAgents, key = { it.id }) { agent ->
            AgentRowCard(agent)
        }

        // Section 2: Free GPU Server Integration
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = ImmersiveGlassBorderCyan,
                backgroundColor = Color(0xFF0A1428).copy(alpha = 0.75f)
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
                                            listOf(ImmersiveCyan, ImmersivePurple)
                                        )
                                    )
                                    .padding(1.5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0A0F1D)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = ImmersiveCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Free GPU Server Connector",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF8FAFC)
                                )
                                Text(
                                    text = serverConfig.serverType.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ImmersiveCyan,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Ping latency badge
                        ImmersiveBadge(
                            text = "${serverConfig.latencyMs} ms",
                            tint = ImmersiveCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "You can run your SNN & AI models either on-device natively or link to a Free Cloud GPU server (Google Colab T4, Kaggle Dual T4, or Local Ollama).",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Server Type Selection Chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ServerType.values().forEach { type ->
                            val isSelected = serverConfig.serverType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setServerEndpoint(type, serverUrlInput) },
                                label = { Text(type.title, fontSize = 12.sp, color = if (isSelected) ImmersiveCyan else Color(0xFFCBD5E1)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.CloudDone else Icons.Default.Cloud,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSelected) ImmersiveCyan else Color(0xFF94A3B8)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = Color(0xFF0B1325).copy(alpha = 0.5f),
                                    selectedContainerColor = ImmersiveCyan.copy(alpha = 0.15f)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) ImmersiveCyan else ImmersiveGlassBorder,
                                    enabled = true,
                                    selected = isSelected
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Server URL Input
                    if (serverConfig.serverType != ServerType.ON_DEVICE_NATIVE) {
                        OutlinedTextField(
                            value = serverUrlInput,
                            onValueChange = { serverUrlInput = it },
                            label = { Text("Server Endpoint URL (e.g. ngrok / Colab tunnel)", fontSize = 12.sp) },
                            placeholder = { Text("https://xxxx.ngrok-free.app", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF070B14),
                                unfocusedContainerColor = Color(0xFF070B14),
                                focusedBorderColor = ImmersiveCyan,
                                unfocusedBorderColor = ImmersiveGlassBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    viewModel.setServerEndpoint(serverConfig.serverType, serverUrlInput)
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ImmersiveCyan)
                            ) {
                                Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = Color(0xFF05070A))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Connect & Test Ping", color = Color(0xFF05070A), fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Copy One-Click Colab / Kaggle Launch Script
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF070E1C))
                            .border(1.dp, ImmersiveGlassBorderCyan, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "One-Click Colab / Kaggle GPU Python Script",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFF1F5F9)
                                )

                                IconButton(
                                    onClick = {
                                        val script = viewModel.getColabLaunchScript()
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Colab Launch Script", script))
                                        Toast.makeText(context, "Copied Python script to clipboard! Paste into Google Colab.", Toast.LENGTH_LONG).show()
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy script", tint = ImmersiveCyan)
                                }
                            }
                            Text(
                                text = "Run this in a free Google Colab (T4 GPU) notebook to host your SNN C++ & Python AI brain server in the cloud!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Primary Free Gemini API Key Settings
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = ImmersiveCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Primary Brain Free API Key",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF8FAFC)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Powers Rani's main conversational brain with Gemini 3.5 Flash. You can obtain a free key at aistudio.google.com with no credit card required.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = primaryKeyInput,
                        onValueChange = { primaryKeyInput = it },
                        placeholder = { Text("Paste free Gemini API key here...", color = Color(0xFF64748B), fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("primary_api_key_field"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF070B14),
                            unfocusedContainerColor = Color(0xFF070B14),
                            focusedBorderColor = ImmersiveCyan,
                            unfocusedBorderColor = ImmersiveGlassBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.updateBrainApiKey(primaryKeyInput) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ImmersiveCyan),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Save Key", color = Color(0xFF05070A), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AgentRowCard(agent: SwarmAgent) {
    GlassCard(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFF0B1224).copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(ImmersivePurple.copy(alpha = 0.18f))
                        .border(1.dp, ImmersivePurple.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = ImmersivePurple,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = agent.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF1F5F9)
                    )
                    Text(
                        text = agent.role,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (agent.status) {
                            SwarmAgentStatus.IDLE -> Color(0xFF1E293B).copy(alpha = 0.6f)
                            SwarmAgentStatus.CONSENSUS_REACHED -> ImmersiveEmerald.copy(alpha = 0.15f)
                            else -> ImmersiveCyan.copy(alpha = 0.15f)
                        }
                    )
                    .border(
                        1.dp,
                        when (agent.status) {
                            SwarmAgentStatus.IDLE -> ImmersiveGlassBorder
                            SwarmAgentStatus.CONSENSUS_REACHED -> ImmersiveEmerald.copy(alpha = 0.4f)
                            else -> ImmersiveCyan.copy(alpha = 0.4f)
                        },
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = agent.status.name.replace("_", " "),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (agent.status) {
                        SwarmAgentStatus.IDLE -> Color(0xFF94A3B8)
                        SwarmAgentStatus.CONSENSUS_REACHED -> ImmersiveEmerald
                        else -> ImmersiveCyan
                    }
                )
            }
        }
    }
}

