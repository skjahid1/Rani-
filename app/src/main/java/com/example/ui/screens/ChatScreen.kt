package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.data.model.ChatMessage
import com.example.data.model.MessageRole
import com.example.data.model.ReasoningMode
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
fun ChatScreen(
    viewModel: RaniViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val selectedMode by viewModel.selectedMode.collectAsState()
    val snnTelemetry by viewModel.snnTelemetry.collectAsState()
    val swarmEnabled by viewModel.swarmEnabled.collectAsState()
    val swarmCount by viewModel.swarmAgentCount.collectAsState()

    val listState = rememberLazyListState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showClearConfirm by remember { mutableStateOf(false) }

    val showScrollToBottom by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex < (messages.size - 2).coerceAtLeast(0) && messages.isNotEmpty()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = Color(0xFF0D1527),
            titleContentColor = Color(0xFFF8FAFC),
            textContentColor = Color(0xFF94A3B8),
            title = {
                Text(
                    text = "Clear Chat History?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("This will purge locally stored interactions from the Room database and reset Rani's chat memory.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearChatHistory()
                        showClearConfirm = false
                    }
                ) {
                    Text("Clear", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = ImmersiveCyan)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF05070A))
    ) {
        // Top Immersive Header with Neural Link Status
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0F1D).copy(alpha = 0.90f))
                .drawBehind {
                    drawLine(
                        color = Color(0x1FFFFFFF),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Futuristic Avatar
                        Box(
                            modifier = Modifier
                                .size(34.dp)
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
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = ImmersiveCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Rani AI",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF8FAFC)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ImmersiveCyan.copy(alpha = 0.15f))
                                        .border(0.5.dp, ImmersiveCyan.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "v16 CORE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = ImmersiveCyan,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                            Text(
                                text = "QUANTUM NEURAL LINK ACTIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF64748B),
                                letterSpacing = 0.8.sp
                            )
                        }
                    }

                    // Actions & Live Indicators
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Room DB indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, ImmersiveGlassBorderCyan, RoundedCornerShape(14.dp))
                                .padding(horizontal = 7.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = "Room Local Database",
                                tint = ImmersiveCyan,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "ROOM DB",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ImmersiveCyan
                            )
                        }

                        Spacer(modifier = Modifier.width(5.dp))

                        // SNN Live Pulse indicator badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, ImmersiveGlassBorderCyan, RoundedCornerShape(14.dp))
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            GlowingPulseDot(color = ImmersiveEmerald, size = 6.dp)
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${String.format("%.0f", snnTelemetry.firingRateHz)}Hz",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ImmersiveCyan
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Clear history button
                        IconButton(
                            onClick = { showClearConfirm = true },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("clear_chat_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear Chat History",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Reasoning Modes Selector Carousel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ReasoningMode.values().forEach { mode ->
                        val isSelected = mode == selectedMode
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectMode(mode) },
                            label = {
                                Text(
                                    text = mode.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                val icon = when (mode) {
                                    ReasoningMode.AUTO -> Icons.Default.AutoAwesome
                                    ReasoningMode.DEEP_R1 -> Icons.Default.Psychology
                                    ReasoningMode.CODING -> Icons.Default.Code
                                    ReasoningMode.MATH -> Icons.Default.Functions
                                    ReasoningMode.SWARM -> Icons.Default.Hub
                                    ReasoningMode.CREATIVE -> Icons.Default.Lightbulb
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = if (isSelected) ImmersiveCyan else Color(0xFF94A3B8)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFF0F172A).copy(alpha = 0.6f),
                                labelColor = Color(0xFF94A3B8),
                                selectedContainerColor = ImmersiveCyan.copy(alpha = 0.16f),
                                selectedLabelColor = ImmersiveCyan
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) ImmersiveCyan.copy(alpha = 0.5f) else ImmersiveGlassBorder
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("mode_chip_${mode.name}")
                        )
                    }
                }
            }
        }

        // Messages list container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    val userQuery = if (message.role == MessageRole.ASSISTANT) {
                        val idx = messages.indexOf(message)
                        if (idx > 0 && messages[idx - 1].role == MessageRole.USER) {
                            messages[idx - 1].text
                        } else null
                    } else null

                    MessageItem(
                        message = message,
                        onRate = { rating -> viewModel.rateMessage(message.id, rating) },
                        onCopy = { text ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Rani Response", text))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        onRetry = if (userQuery != null) {
                            { viewModel.sendMessage(userQuery) }
                        } else null
                    )
                }

                if (isSending) {
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = Color(0xFF0D1527).copy(alpha = 0.5f),
                            borderColor = ImmersiveGlassBorderCyan
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = ImmersiveCyan
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (selectedMode == ReasoningMode.SWARM && swarmEnabled) {
                                        "Swarm active: coordinating $swarmCount agents in parallel..."
                                    } else {
                                        "Rani is synthesizing answer with ${selectedMode.label}..."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ImmersiveCyan,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Scroll to latest helper pill when scrolled back in history
            if (showScrollToBottom) {
                Surface(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(messages.size - 1)
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ImmersiveGlassBorderCyan),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Scroll to latest",
                            tint = ImmersiveCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Jump to latest",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ImmersiveCyan
                        )
                    }
                }
            }
        }

        // Quick suggestions bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SuggestionButton("How does SNN work?") { viewModel.sendMessage("How does the Hodgkin-Huxley Spiking Neural Network (SNN) work and learn?") }
            SuggestionButton("Run /train") { viewModel.runTraining() }
            SuggestionButton("Swarm Analysis") {
                viewModel.selectMode(ReasoningMode.SWARM)
                viewModel.sendMessage("Analyze the difference between standard artificial neural networks and spiking neural networks.")
            }
            SuggestionButton("Code HH Model") {
                viewModel.selectMode(ReasoningMode.CODING)
                viewModel.sendMessage("Write clean Python code for a Hodgkin-Huxley neuron with m, h, n gating variables.")
            }
        }

        // Input row (Immersive Glass Dock)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF090E1D).copy(alpha = 0.95f))
                .drawBehind {
                    drawLine(
                        color = Color(0x1FFFFFFF),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.updateInputText(it) },
                    placeholder = {
                        Text(
                            text = "Ask Rani, plan:, code:, or /train...",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFFF1F5F9),
                        unfocusedTextColor = Color(0xFFF1F5F9),
                        focusedContainerColor = Color(0xFF050811),
                        unfocusedContainerColor = Color(0xFF050811),
                        focusedBorderColor = ImmersiveCyan,
                        unfocusedBorderColor = Color(0x2238BDF8),
                        cursorColor = ImmersiveCyan
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { viewModel.sendMessage() },
                    enabled = inputText.isNotBlank() && !isSending,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank() && !isSending) {
                                Brush.linearGradient(listOf(ImmersiveCyan, ImmersivePurple))
                            } else {
                                Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                            }
                        )
                        .testTag("send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank() && !isSending) {
                            Color(0xFF05070A)
                        } else {
                            Color(0xFF475569)
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SuggestionButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.7f))
            .border(1.dp, ImmersiveGlassBorder, RoundedCornerShape(14.dp))
            .height(28.dp)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = onClick,
            color = Color.Transparent
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = ImmersiveCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MessageItem(
    message: ChatMessage,
    onRate: (String) -> Unit,
    onCopy: (String) -> Unit,
    onRetry: (() -> Unit)? = null
) {
    val isUser = message.role == MessageRole.USER
    val isFallback = !isUser && (message.source.contains("Local Native Brain", ignoreCase = true) || message.confidence < 0.75f)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Sender info chip
        if (!isUser) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp, bottom = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(ImmersiveCyan, ImmersivePurple)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (message.isSwarm) Icons.Default.Hub else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(11.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Rani AI • ${message.source}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isFallback) Color(0xFFFBBF24) else ImmersiveCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                if (message.confidence > 0f) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${(message.confidence * 100).toInt()}% conf",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF64748B),
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Message bubble with Glassmorphism
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(
                    RoundedCornerShape(
                        topStart = if (isUser) 18.dp else 4.dp,
                        topEnd = if (isUser) 4.dp else 18.dp,
                        bottomStart = 18.dp,
                        bottomEnd = 18.dp
                    )
                )
                .background(
                    if (isUser) {
                        Brush.horizontalGradient(
                            listOf(Color(0xFF7C3AED), Color(0xFF6D28D9))
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(Color(0xFF131D33).copy(alpha = 0.85f), Color(0xFF0D1527).copy(alpha = 0.85f))
                        )
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (isUser) Color(0x33C084FC) else ImmersiveGlassBorder,
                    shape = RoundedCornerShape(
                        topStart = if (isUser) 18.dp else 4.dp,
                        topEnd = if (isUser) 4.dp else 18.dp,
                        bottomStart = 18.dp,
                        bottomEnd = 18.dp
                    )
                )
                .testTag("message_bubble_${message.id}")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                SelectionContainer {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = if (message.text.contains("def ") || message.text.contains("class ") || message.text.contains("val ")) {
                                FontFamily.Monospace
                            } else {
                                FontFamily.Default
                            },
                            lineHeight = 22.sp
                        ),
                        color = if (isUser) Color(0xFFF8FAFC) else Color(0xFFE2E8F0)
                    )
                }

                // AI Message Actions (Copy, Rate)
                if (!isUser) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onCopy(message.text) },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                modifier = Modifier.size(15.dp),
                                tint = Color(0xFF64748B)
                            )
                        }

                        if (isFallback && onRetry != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = onRetry,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Retry Query",
                                    modifier = Modifier.size(15.dp),
                                    tint = ImmersiveCyan
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = { onRate("good") },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Good answer",
                                modifier = Modifier.size(15.dp),
                                tint = if (message.rating == "good") ImmersiveEmerald else Color(0xFF64748B)
                            )
                        }

                        IconButton(
                            onClick = { onRate("bad") },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ThumbDown,
                                contentDescription = "Bad answer",
                                modifier = Modifier.size(15.dp),
                                tint = if (message.rating == "bad") Color(0xFFEF4444) else Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }
    }
}

