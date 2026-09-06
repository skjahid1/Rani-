package com.example.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AiMonitorScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.SnnScreen
import com.example.ui.screens.SwarmGpuScreen
import com.example.ui.theme.ImmersiveCyan
import com.example.ui.theme.ImmersivePurple

@Composable
fun RaniApp(
    viewModel: RaniViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        containerColor = Color(0xFF05070A),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                tonalElevation = 0.dp,
                containerColor = Color(0xFF090E1C).copy(alpha = 0.95f),
                modifier = Modifier
                    .testTag("main_bottom_nav")
                    .drawBehind {
                        // Futuristic top subtle border line
                        drawLine(
                            color = Color(0x2238BDF8),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.CHAT,
                    onClick = { viewModel.selectTab(AppTab.CHAT) },
                    icon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Brain Chat") },
                    label = { Text("Chat", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ImmersiveCyan,
                        selectedTextColor = ImmersiveCyan,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = ImmersiveCyan.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_chat")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.AI_MONITOR,
                    onClick = { viewModel.selectTab(AppTab.AI_MONITOR) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Monitor") },
                    label = { Text("Learn Monitor", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ImmersiveCyan,
                        selectedTextColor = ImmersiveCyan,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = ImmersiveCyan.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_monitor")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.SNN_ENGINE,
                    onClick = { viewModel.selectTab(AppTab.SNN_ENGINE) },
                    icon = { Icon(Icons.Default.Memory, contentDescription = "SNN Engine") },
                    label = { Text("SNN Engine", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ImmersiveCyan,
                        selectedTextColor = ImmersiveCyan,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = ImmersiveCyan.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_tab_snn")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.SWARM_GPU,
                    onClick = { viewModel.selectTab(AppTab.SWARM_GPU) },
                    icon = { Icon(Icons.Default.Hub, contentDescription = "Swarm & GPU") },
                    label = { Text("Swarm & GPU", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = ImmersivePurple,
                        selectedTextColor = ImmersivePurple,
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = ImmersivePurple.copy(alpha = 0.18f)
                    ),
                    modifier = Modifier.testTag("nav_tab_swarm")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.CHAT -> ChatScreen(viewModel = viewModel)
                AppTab.AI_MONITOR -> AiMonitorScreen(viewModel = viewModel)
                AppTab.SNN_ENGINE -> SnnScreen(viewModel = viewModel)
                AppTab.SWARM_GPU -> SwarmGpuScreen(viewModel = viewModel)
            }
        }
    }
}
