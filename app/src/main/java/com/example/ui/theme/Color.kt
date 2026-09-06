package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Immersive UI Neon & Accent Palette
val ImmersiveVoid = Color(0xFF05070A)
val ImmersiveGlassCard = Color(0xFF0D1527)
val ImmersiveGlassBorder = Color(0x1FFFFFFF)
val ImmersiveGlassBorderCyan = Color(0x3322D3EE)

val ImmersiveCyan = Color(0xFF22D3EE)
val ImmersiveCyanGlow = Color(0xFF00E5FF)
val ImmersivePurple = Color(0xFFA855F7)
val ImmersivePurpleDeep = Color(0xFF7C3AED)
val ImmersiveEmerald = Color(0xFF34D399)

val CyanPrimary = ImmersiveCyan
val CyanSecondary = ImmersivePurple
val CyanTertiary = ImmersiveEmerald

val DarkBackground = ImmersiveVoid
val DarkSurface = Color(0xFF0A0F1D)
val DarkSurfaceVariant = Color(0xFF131D33)
val DarkOnBackground = Color(0xFFF1F5F9)
val DarkOnSurface = Color(0xFFF8FAFC)
val DarkOnSurfaceVariant = Color(0xFF94A3B8)
val DarkPrimaryContainer = Color(0xFF0E2238)
val DarkOnPrimaryContainer = ImmersiveCyan

val LightPrimary = Color(0xFF0284C7)
val LightSecondary = Color(0xFF7C3AED)
val LightTertiary = Color(0xFF059669)
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFE2E8F0)
val LightOnBackground = Color(0xFF0F172A)
val LightOnSurface = Color(0xFF0F172A)
val LightOnSurfaceVariant = Color(0xFF475569)
val LightPrimaryContainer = Color(0xFFBAE6FD)
val LightOnPrimaryContainer = Color(0xFF001E2C)

val ImmersiveGradient = Brush.horizontalGradient(
    listOf(ImmersiveCyan, ImmersivePurple)
)
val ImmersiveCardGradient = Brush.verticalGradient(
    listOf(Color(0xFF131E35).copy(alpha = 0.75f), Color(0xFF0A1020).copy(alpha = 0.75f))
)
