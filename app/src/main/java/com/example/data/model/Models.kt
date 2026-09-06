package com.example.data.model

import java.util.UUID

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

enum class ReasoningMode(val label: String, val description: String) {
    AUTO("Auto / General", "Balanced speed and accuracy"),
    DEEP_R1("Deep Reasoning (R1)", "Step-by-step chain of thought & premise verification"),
    CODING("Code Synthesis", "Production-ready, modular syntax & test examples"),
    MATH("Math Engine", "Step-by-step rigorous calculation and verification"),
    SWARM("Swarm Consensus", "Multi-agent parallel decomposition and voting"),
    CREATIVE("Creative", "Imaginative and detailed synthesis")
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val confidence: Float = 0.85f,
    val source: String = "Gemini 3.5 Flash",
    val expertDomain: String = "General",
    val isSwarm: Boolean = false,
    val isError: Boolean = false,
    val rating: String? = null // "good", "bad", null
)

data class LessonPlan(
    val id: String = UUID.randomUUID().toString(),
    val question: String,
    val raniAnswer: String,
    val correctAnswer: String,
    val reasoning: String,
    val rule: String,
    val topic: String,
    val grade: Float = 0.85f,
    val timestamp: Long = System.currentTimeMillis()
)

data class SkillDomain(
    val name: String,
    val accuracy: Float,
    val sampleCount: Int
)

data class MemoryLayerStats(
    val layerNumber: Int,
    val name: String,
    val description: String,
    val count: Int,
    val colorHex: Long
)

enum class SwarmAgentStatus {
    IDLE,
    DECOMPOSING,
    RESEARCHING,
    SYNTHESIZING,
    VALIDATING,
    CONSENSUS_REACHED
}

data class SwarmAgent(
    val id: String,
    val name: String,
    val role: String,
    val status: SwarmAgentStatus = SwarmAgentStatus.IDLE,
    val currentTask: String = "Standby",
    val confidence: Float = 0.9f
)

enum class ServerType(val title: String) {
    ON_DEVICE_NATIVE("On-Device Native SNN + Cloud Brain"),
    COLAB_GPU("Google Colab Free T4 GPU"),
    KAGGLE_GPU("Kaggle Free Dual T4 GPU"),
    LOCAL_OLLAMA("Local Ollama / LAN GPU"),
    HUGGING_FACE("Hugging Face Spaces")
}

data class GpuServerConfig(
    val serverType: ServerType = ServerType.ON_DEVICE_NATIVE,
    val url: String = "",
    val isConnected: Boolean = true,
    val latencyMs: Long = 28L,
    val lastPing: Long = System.currentTimeMillis(),
    val gpuDevice: String = "Snapdragon NPU / Embedded ARM SIMD"
)

data class SnnSpike(
    val neuronId: Int,
    val timeMs: Float,
    val popClass: String,
    val voltagePeak: Float
)

data class LayerActivityPoint(
    val timeLabel: String = "0s",
    val overallHz: Float = 14f,
    val layer23Hz: Float = 12f,
    val layer4Hz: Float = 16f,
    val layer5Hz: Float = 14f,
    val layerPvHz: Float = 22f,
    val synchronyIndex: Float = 0.65f
)

data class SnnNodeState(
    val id: Int,
    val layer: String,
    val voltage: Float,
    val isSpiking: Boolean,
    val calcium: Float
)

data class SnnTelemetry(
    val timeMs: Float = 0f,
    val firingRateHz: Float = 14.5f,
    val layer23Hz: Float = 12.8f,
    val layer4Hz: Float = 18.2f,
    val layer5Hz: Float = 15.6f,
    val layerPvHz: Float = 24.1f,
    val activeNeurons: Int = 120,
    val activeSynapses: Int = 1420,
    val tmFacilitationU: Float = 0.42f,
    val tmDepressionX: Float = 0.78f,
    val meanCalcium: Float = 0.085f,
    val stdpWeightMean: Float = 1.65f,
    val synchronyIndex: Float = 0.68f,
    val layerSyncL2L4: Float = 0.74f,
    val layerSyncL4L5: Float = 0.69f,
    val layerSyncPv: Float = 0.81f,
    val isRunning: Boolean = true,
    val history: List<LayerActivityPoint> = emptyList(),
    val nodes: List<SnnNodeState> = emptyList()
)
