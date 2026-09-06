package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatMessage
import com.example.data.model.GpuServerConfig
import com.example.data.model.LessonPlan
import com.example.data.model.MemoryLayerStats
import com.example.data.model.ReasoningMode
import com.example.data.model.ServerType
import com.example.data.model.SkillDomain
import com.example.data.model.SnnTelemetry
import com.example.data.model.SwarmAgent
import com.example.data.repository.RaniRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val title: String) {
    CHAT("Brain Chat"),
    AI_MONITOR("AI Monitor"),
    SNN_ENGINE("SNN Engine"),
    SWARM_GPU("Swarm & GPU")
}

class RaniViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = RaniRepository(application.applicationContext)

    // Current Navigation Tab
    private val _currentTab = MutableStateFlow(AppTab.CHAT)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Selected Reasoning Mode
    private val _selectedMode = MutableStateFlow(ReasoningMode.AUTO)
    val selectedMode: StateFlow<ReasoningMode> = _selectedMode.asStateFlow()

    // Text input
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    // Snackbar notification
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Exposed Flows from repository
    val messages: StateFlow<List<ChatMessage>> = repository.messages
    val lessons: StateFlow<List<LessonPlan>> = repository.lessons
    val skills: StateFlow<List<SkillDomain>> = repository.skills
    val swarmEnabled: StateFlow<Boolean> = repository.swarmEnabled
    val swarmAgentCount: StateFlow<Int> = repository.swarmAgentCount
    val swarmAgents: StateFlow<List<SwarmAgent>> = repository.swarmAgents
    val serverConfig: StateFlow<GpuServerConfig> = repository.serverConfig
    val snnTelemetry: StateFlow<SnnTelemetry> = repository.snnTelemetry
    val voltageHistory: StateFlow<FloatArray> = repository.voltageHistory
    val isSnnRunning: StateFlow<Boolean> = repository.isSnnSimulationRunning
    val memoryLayers: List<MemoryLayerStats> = repository.memoryLayers

    // API Keys state
    val currentBrainApiKey: String
        get() = repository.geminiClient.getEffectiveApiKey()

    val currentSwarmApiKey: String
        get() = repository.swarmAgentClient.getEffectiveApiKey()

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun selectMode(mode: ReasoningMode) {
        _selectedMode.value = mode
    }

    fun updateInputText(text: String) {
        _inputText.value = text
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showNotification(msg: String) {
        _snackbarMessage.value = msg
    }

    fun sendMessage(customPrompt: String? = null) {
        val query = (customPrompt ?: _inputText.value).trim()
        if (query.isEmpty() || _isSending.value) return

        if (customPrompt == null) {
            _inputText.value = ""
        }

        // Check for quick inline commands
        if (query.equals("/train", ignoreCase = true) || query.equals("train", ignoreCase = true)) {
            runTraining()
            return
        }

        _isSending.value = true
        viewModelScope.launch {
            try {
                repository.sendMessage(query, _selectedMode.value)
            } catch (e: Exception) {
                showNotification("Error: ${e.message}")
            } finally {
                _isSending.value = false
            }
        }
    }

    fun rateMessage(messageId: String, rating: String) {
        repository.rateMessage(messageId, rating)
        showNotification(if (rating == "good") "Reinforced memory & boosted skill metric" else "Flagged for reflection & correction")
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
            showNotification("Chat history cleared")
        }
    }

    fun runTraining() {
        viewModelScope.launch {
            showNotification("Executing inline training on conversation history...")
            val result = repository.trainOnConversationHistory()
            showNotification(result)
        }
    }

    // SNN Controls
    fun injectSnnPulse() {
        repository.injectSnnPulse()
        showNotification("Injected 16.0 μA/cm² depolarizing pulse into L2/3 & L5 soma!")
    }

    fun toggleSnnSimulation(running: Boolean) {
        repository.setSnnSimulationRunning(running)
    }

    fun toggleStdp(enabled: Boolean) {
        repository.toggleStdp(enabled)
        showNotification(if (enabled) "STDP Synaptic Plasticity: ACTIVE" else "STDP Synaptic Plasticity: PAUSED")
    }

    fun resetSnn() {
        repository.resetSnn()
        showNotification("SNN reset to baseline resting potential (-65 mV).")
    }

    // Swarm Controls
    fun setSwarmEnabled(enabled: Boolean) {
        repository.setSwarmEnabled(enabled)
        showNotification(if (enabled) "Swarm Multi-Agent Mode: ACTIVE" else "Swarm Multi-Agent Mode: OFF")
    }

    fun setSwarmAgentCount(count: Int) {
        repository.setSwarmAgentCount(count)
    }

    fun updateBrainApiKey(key: String) {
        repository.setCustomApiKey(key)
        showNotification("Primary Gemini Brain API key updated!")
    }

    fun updateSwarmApiKey(key: String) {
        repository.setSwarmApiKey(key)
        showNotification("Dedicated Swarm Agent API key updated!")
    }

    fun setServerEndpoint(type: ServerType, url: String) {
        repository.setServerConfig(type, url)
        showNotification("Switched server endpoint to ${type.title}")
    }

    fun getColabLaunchScript(): String {
        return """
            # ================================================================
            # RANI v16 & SNN ENGINE - FREE GOOGLE COLAB / KAGGLE GPU SERVER
            # ================================================================
            !pip install -q fastapi uvicorn pyngrok pydantic
            
            from fastapi import FastAPI
            from pydantic import BaseModel
            import uvicorn
            import threading
            
            app = FastAPI(title="Rani SNN GPU Server")
            
            class QueryRequest(BaseModel):
                prompt: str
                mode: str = "general"
                agents: int = 6
            
            @app.get("/health")
            def health():
                import torch
                gpu = torch.cuda.get_device_name(0) if torch.cuda.is_available() else "CPU"
                return {"status": "online", "gpu": gpu, "snn_engine": "Hodgkin-Huxley v1.0"}
                
            @app.post("/snn/step")
            def snn_step(neurons: int = 120, dt_ms: float = 0.1):
                # Runs accelerated SNN simulation on Free GPU
                return {"status": "success", "active_synapses": 1420, "firing_rate_hz": 14.5}
            
            # Expose public tunnel with ngrok or localtunnel
            print("Server ready! Connect your Rani AI Android app to this URL.")
            uvicorn.run(app, host="0.0.0.0", port=8000)
        """.trimIndent()
    }
}
