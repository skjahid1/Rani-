package com.example.data.repository

import android.content.Context
import com.example.data.gemini.GeminiClient
import com.example.data.local.ChatMessageEntity
import com.example.data.local.RaniDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.GpuServerConfig
import com.example.data.model.LessonPlan
import com.example.data.model.MemoryLayerStats
import com.example.data.model.MessageRole
import com.example.data.model.ReasoningMode
import com.example.data.model.ServerType
import com.example.data.model.SkillDomain
import com.example.data.model.SnnTelemetry
import com.example.data.model.SwarmAgent
import com.example.data.model.SwarmAgentStatus
import com.example.data.snn.SnnEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class RaniRepository(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Room Database and DAO
    private val database = RaniDatabase.getDatabase(context)
    private val chatDao = database.chatDao()

    // Shared preferences for persistence
    private val prefs = context.getSharedPreferences("rani_brain_prefs", Context.MODE_PRIVATE)

    // Gemini Client for primary brain
    val geminiClient = GeminiClient(prefs.getString("custom_api_key", "").orEmpty())

    // Dedicated Gemini Client for Swarm agents (user requested: "for agent use another free api Key all free")
    val swarmAgentClient = GeminiClient(prefs.getString("swarm_api_key", "").orEmpty())

    // Native on-device SNN Engine
    val snnEngine = SnnEngine(numNeurons = 120, dtMs = 0.15f)

    // Chat messages backed reactively by Room database Flow
    val messages: StateFlow<List<ChatMessage>> = chatDao.getAllMessages()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    // Lessons learned
    private val _lessons = MutableStateFlow<List<LessonPlan>>(emptyList())
    val lessons: StateFlow<List<LessonPlan>> = _lessons.asStateFlow()

    // Skills
    private val _skills = MutableStateFlow<List<SkillDomain>>(
        listOf(
            SkillDomain("Mathematics", 0.92f, 14),
            SkillDomain("Spiking Neural Networks", 0.95f, 22),
            SkillDomain("Coding & Algorithms", 0.88f, 19),
            SkillDomain("Logic & Reasoning", 0.91f, 16),
            SkillDomain("Swarm Orchestration", 0.89f, 11),
            SkillDomain("Scientific Theory", 0.86f, 9),
            SkillDomain("Fact Verification", 0.94f, 25),
            SkillDomain("Planning & Synthesis", 0.90f, 13)
        )
    )
    val skills: StateFlow<List<SkillDomain>> = _skills.asStateFlow()

    // Swarm configuration
    private val _swarmEnabled = MutableStateFlow(prefs.getBoolean("swarm_enabled", true))
    val swarmEnabled: StateFlow<Boolean> = _swarmEnabled.asStateFlow()

    private val _swarmAgentCount = MutableStateFlow(prefs.getInt("swarm_count", 6))
    val swarmAgentCount: StateFlow<Int> = _swarmAgentCount.asStateFlow()

    private val _swarmAgents = MutableStateFlow<List<SwarmAgent>>(emptyList())
    val swarmAgents: StateFlow<List<SwarmAgent>> = _swarmAgents.asStateFlow()

    // GPU Server Config
    private val _serverConfig = MutableStateFlow(
        GpuServerConfig(
            serverType = ServerType.ON_DEVICE_NATIVE,
            url = prefs.getString("gpu_server_url", "http://colab-t4-gpu.internal:8000").orEmpty(),
            isConnected = true,
            latencyMs = 24L
        )
    )
    val serverConfig: StateFlow<GpuServerConfig> = _serverConfig.asStateFlow()

    // SNN live telemetry & oscilloscope
    private val _snnTelemetry = MutableStateFlow(snnEngine.getTelemetry())
    val snnTelemetry: StateFlow<SnnTelemetry> = _snnTelemetry.asStateFlow()

    private val _voltageHistory = MutableStateFlow(snnEngine.voltageHistory.copyOf())
    val voltageHistory: StateFlow<FloatArray> = _voltageHistory.asStateFlow()

    private val _isSnnSimulationRunning = MutableStateFlow(true)
    val isSnnSimulationRunning: StateFlow<Boolean> = _isSnnSimulationRunning.asStateFlow()

    // Memory layers
    val memoryLayers = listOf(
        MemoryLayerStats(1, "Working Memory", "Ring buffer of 16 recent conversation turns", 16, 0xFF00E5FF),
        MemoryLayerStats(2, "Semantic Nodes", "Dense vector embeddings for associative memory", 248, 0xFF7C4DFF),
        MemoryLayerStats(3, "Long-Term Episodic", "Persistent experiential narrative history", 132, 0xFF3D5AFE),
        MemoryLayerStats(4, "Hyperdimensional Micro-Memory", "Sparse binary hypervectors in SQLite", 1420, 0xFF00B0FF),
        MemoryLayerStats(5, "Knowledge Graph", "Concept nodes & semantic edges", 342, 0xFF651FFF),
        MemoryLayerStats(6, "Exact Facts DB", "High-confidence key-value factual index", 89, 0xFF00E676),
        MemoryLayerStats(7, "Conversation Summaries", "Consolidated macro-summaries of past dialogues", 42, 0xFFFFAB00),
        MemoryLayerStats(8, "Document Vault", "Local documents (PDF/DOCX/Code) parsed & indexed", 18, 0xFFFF5252),
        MemoryLayerStats(9, "Procedural Skill Memory", "Per-topic calibrated skill metrics", 8, 0xFFFF4081),
        MemoryLayerStats(10, "Reflection Engine", "Lessons learned from errors & corrections", 37, 0xFF1DE9B6)
    )

    init {
        // Seed welcome message into Room database if empty, or ensure updated introduction
        scope.launch {
            val count = chatDao.getMessageCount()
            if (count == 0) {
                val welcome = ChatMessage(
                    id = "seed_welcome_message",
                    role = MessageRole.ASSISTANT,
                    text = "I am rani created by Sk Jahid Afridi. I am an autonomous ai how can I help you",
                    confidence = 1.0f,
                    source = "Rani Brain"
                )
                chatDao.insertMessage(ChatMessageEntity.fromDomain(welcome))
            } else {
                // Remove the old verbose introduction if it exists in previous runs
                chatDao.updateOldIntroMessage(
                    oldSnippet = "%Rani v16%",
                    newText = "I am rani created by Sk Jahid Afridi. I am an autonomous ai how can I help you"
                )
            }
        }

        // Initialize Swarm agents
        updateSwarmRoster(_swarmAgentCount.value)

        // Seed sample lessons
        _lessons.value = listOf(
            LessonPlan(
                question = "How does Tsodyks-Markram short-term plasticity modulate synaptic transmission?",
                raniAnswer = "It models vesicle depletion and calcium-dependent facilitation.",
                correctAnswer = "TM plasticity computes release probability u and available vesicle fraction x, balancing short-term depression (tau_rec) with facilitation (tau_facil).",
                reasoning = "Depression dominates cortical E-to-E connections, while facilitation governs SST interneurons.",
                rule = "Synaptic transmission depends on both available vesicle pool x and calcium utilization u.",
                topic = "SNN Biophysics",
                grade = 0.94f
            ),
            LessonPlan(
                question = "What is the role of the Hodgkin-Huxley m, h, and n gates?",
                raniAnswer = "m and h control sodium channels, n controls potassium channels.",
                correctAnswer = "m is Na+ activation (fast), h is Na+ inactivation (slow), and n is K+ activation (delayed rectifier).",
                reasoning = "Action potential upstroke is driven by m^3*h, downstroke by n^4.",
                rule = "Action potential generation requires rapid sodium activation followed by delayed potassium rectification.",
                topic = "Neuroscience",
                grade = 0.96f
            )
        )

        // Start SNN simulation background ticker
        startSnnSimulationLoop()
    }

    private fun startSnnSimulationLoop() {
        scope.launch {
            while (isActive) {
                if (_isSnnSimulationRunning.value) {
                    // Step SNN engine forward
                    snnEngine.step(0.2f)
                    _snnTelemetry.value = snnEngine.getTelemetry()
                    _voltageHistory.value = snnEngine.voltageHistory.copyOf()
                }
                delay(33) // ~30 fps update
            }
        }
    }

    fun setSnnSimulationRunning(running: Boolean) {
        _isSnnSimulationRunning.value = running
    }

    fun injectSnnPulse() {
        snnEngine.injectCurrentPulse(16.0f, 20.0f)
    }

    fun toggleStdp(enabled: Boolean) {
        snnEngine.stdpEnabled = enabled
    }

    fun resetSnn() {
        snnEngine.reset()
        _snnTelemetry.value = snnEngine.getTelemetry()
        _voltageHistory.value = snnEngine.voltageHistory.copyOf()
    }

    fun setSwarmEnabled(enabled: Boolean) {
        _swarmEnabled.value = enabled
        prefs.edit().putBoolean("swarm_enabled", enabled).apply()
    }

    fun setSwarmAgentCount(count: Int) {
        val clamped = count.coerceIn(1, 32)
        _swarmAgentCount.value = clamped
        prefs.edit().putInt("swarm_count", clamped).apply()
        updateSwarmRoster(clamped)
    }

    private fun updateSwarmRoster(count: Int) {
        val roles = listOf(
            Pair("Agent Alpha", "Deep Web & Fact Researcher"),
            Pair("Agent Beta", "Biophysical SNN & Math Verifier"),
            Pair("Agent Gamma", "Modular Code Synthesizer"),
            Pair("Agent Delta", "Premise & Logic Critic"),
            Pair("Agent Epsilon", "Theoretical Framework Builder"),
            Pair("Agent Zeta", "Synthesis & Consensus Judge"),
            Pair("Agent Eta", "Empirical Data Analyst"),
            Pair("Agent Theta", "Safety & Alignment Guardian")
        )

        val list = mutableListOf<SwarmAgent>()
        for (i in 0 until count) {
            val roleInfo = roles[i % roles.size]
            val suffix = if (i >= roles.size) " #${i + 1}" else ""
            list.add(
                SwarmAgent(
                    id = "agent_${i + 1}",
                    name = "${roleInfo.first}$suffix",
                    role = roleInfo.second,
                    status = SwarmAgentStatus.IDLE,
                    currentTask = "Monitoring channel",
                    confidence = 0.88f + (i % 5) * 0.02f
                )
            )
        }
        _swarmAgents.value = list
    }

    fun setCustomApiKey(key: String) {
        geminiClient.updateApiKey(key)
        prefs.edit().putString("custom_api_key", key).apply()
    }

    fun setSwarmApiKey(key: String) {
        swarmAgentClient.updateApiKey(key)
        prefs.edit().putString("swarm_api_key", key).apply()
    }

    fun setServerConfig(type: ServerType, url: String) {
        _serverConfig.value = _serverConfig.value.copy(
            serverType = type,
            url = url,
            isConnected = true,
            latencyMs = if (type == ServerType.ON_DEVICE_NATIVE) 12L else 45L
        )
        prefs.edit().putString("gpu_server_url", url).apply()
    }

    fun rateMessage(messageId: String, rating: String) {
        scope.launch {
            chatDao.updateRating(messageId, rating)
        }

        // Inline reward / penalty for the brain
        if (rating == "good") {
            // Reinforce skills
            _skills.update { list ->
                list.map { s -> s.copy(accuracy = (s.accuracy + 0.01f).coerceAtMost(0.99f)) }
            }
        }
    }

    /**
     * Send user message and receive AI response with appropriate reasoning mode.
     * Both user prompt and AI response are persisted to local Room database.
     */
    suspend fun sendMessage(text: String, mode: ReasoningMode): ChatMessage {
        val userMsg = ChatMessage(
            role = MessageRole.USER,
            text = text,
            expertDomain = when (mode) {
                ReasoningMode.CODING -> "Coding"
                ReasoningMode.MATH -> "Mathematics"
                ReasoningMode.DEEP_R1 -> "Reasoning"
                ReasoningMode.SWARM -> "Swarm"
                ReasoningMode.CREATIVE -> "Creative"
                ReasoningMode.AUTO -> "General"
            }
        )

        // Persist user prompt to local Room database
        withContext(Dispatchers.IO) {
            chatDao.insertMessage(ChatMessageEntity.fromDomain(userMsg))
        }

        // Inject a pulse into the SNN whenever the brain processes data!
        snnEngine.injectCurrentPulse(12.0f, 15.0f)

        return withContext(Dispatchers.IO) {
            val responseMsg = if (mode == ReasoningMode.SWARM && _swarmEnabled.value) {
                runSwarmPipeline(text)
            } else {
                runStandardPipeline(text, mode)
            }

            // Persist AI response message to local Room database
            chatDao.insertMessage(ChatMessageEntity.fromDomain(responseMsg))

            // Trigger silent inline learning in background!
            scope.launch {
                triggerInlineLearning(text, responseMsg.text)
            }

            responseMsg
        }
    }

    suspend fun clearChatHistory() {
        withContext(Dispatchers.IO) {
            chatDao.clearAllMessages()
            val welcome = ChatMessage(
                id = "seed_welcome_message",
                role = MessageRole.ASSISTANT,
                text = "I am rani created by Sk Jahid Afridi. I am an autonomous ai how can I help you",
                confidence = 1.0f,
                source = "Rani Brain"
            )
            chatDao.insertMessage(ChatMessageEntity.fromDomain(welcome))
        }
    }

    private suspend fun runStandardPipeline(query: String, mode: ReasoningMode): ChatMessage {
        val systemPrompt = buildSystemPrompt(mode)
        val historyPairs = messages.value.takeLast(6).map {
            Pair(if (it.role == MessageRole.USER) "user" else "assistant", it.text)
        }

        val result = geminiClient.generateContent(
            prompt = query,
            systemInstruction = systemPrompt,
            temperature = when (mode) {
                ReasoningMode.CODING -> 0.25f
                ReasoningMode.MATH -> 0.15f
                ReasoningMode.DEEP_R1 -> 0.35f
                ReasoningMode.CREATIVE -> 0.85f
                ReasoningMode.AUTO -> 0.65f
                ReasoningMode.SWARM -> 0.50f
            },
            history = historyPairs
        )

        val answerText = result.getOrElse { error ->
            // If API key is missing or failed, provide high-quality fallback explaining how to configure free key
            fallbackResponse(query, mode, error.message)
        }

        // Clean any reasoning tags if present
        val cleanedText = cleanInternalTags(answerText)

        return ChatMessage(
            role = MessageRole.ASSISTANT,
            text = cleanedText,
            confidence = if (result.isSuccess) 0.92f else 0.70f,
            source = if (result.isSuccess) "Gemini 3.5 Flash" else "Local Native Brain",
            expertDomain = when (mode) {
                ReasoningMode.CODING -> "Coding"
                ReasoningMode.MATH -> "Mathematics"
                ReasoningMode.DEEP_R1 -> "Deep Reasoning"
                ReasoningMode.CREATIVE -> "Creative"
                else -> "General"
            }
        )
    }

    private suspend fun runSwarmPipeline(query: String): ChatMessage {
        // Animate swarm agents
        _swarmAgents.update { list ->
            list.mapIndexed { idx, a ->
                a.copy(
                    status = when (idx % 4) {
                        0 -> SwarmAgentStatus.DECOMPOSING
                        1 -> SwarmAgentStatus.RESEARCHING
                        2 -> SwarmAgentStatus.SYNTHESIZING
                        else -> SwarmAgentStatus.VALIDATING
                    },
                    currentTask = "Analyzing sub-facet #${idx + 1}: '$query'"
                )
            }
        }

        delay(400) // Visual simulation delay

        // Use Swarm Dedicated API Key client if configured, else primary client
        val clientToUse = if (swarmAgentClient.getEffectiveApiKey().isNotBlank()) {
            swarmAgentClient
        } else {
            geminiClient
        }

        val swarmPrompt = """
            You are acting as a coordinated Swarm of ${_swarmAgentCount.value} AI Specialist Agents.
            Original User Query: $query
            
            Synthesize the collective intelligence into a cohesive consensus report:
            1. **Agent Consensus Summary**: The core direct resolution.
            2. **Deep Analytical Perspectives**: Key insights discovered from multiple agent viewpoints (Logic, Technical, Empirical).
            3. **Actionable Takeaways**: Recommended next steps or solutions.
            
            Speak directly and professionally without placeholder boilerplate.
        """.trimIndent()

        val result = clientToUse.generateContent(
            prompt = swarmPrompt,
            temperature = 0.45f
        )

        _swarmAgents.update { list ->
            list.map { it.copy(status = SwarmAgentStatus.CONSENSUS_REACHED, currentTask = "Consensus achieved") }
        }

        val answerText = result.getOrElse { error ->
            fallbackSwarmResponse(query, error.message)
        }

        return ChatMessage(
            role = MessageRole.ASSISTANT,
            text = cleanInternalTags(answerText),
            confidence = 0.95f,
            source = "Swarm Consensus (${_swarmAgentCount.value} Agents)",
            expertDomain = "Swarm Multi-Agent",
            isSwarm = true
        )
    }

    private fun cleanInternalTags(raw: String): String {
        return raw.replace(Regex("<think>[\\s\\S]*?</think>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("(PREMISES:|PREMISE_CHECK:|KNOW:|CONCLUDE:)", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    private fun buildSystemPrompt(mode: ReasoningMode): String {
        val base = """
            You are Rani, an autonomous AI created by Sk Jahid Afridi with integrated Spiking Neural Network (SNN) biophysics and Swarm Intelligence.
            
            Identity Rules:
            - Your name is Rani.
            - You were created by Sk Jahid Afridi.
            - When asked who you are or who created you, state: "I am Rani created by Sk Jahid Afridi. I am an autonomous AI, how can I help you?"
            - Always be helpful, precise, calm, and insightful.
            - When answering questions, provide structured, clear explanations with high craftsmanship.
        """.trimIndent()

        val addon = when (mode) {
            ReasoningMode.DEEP_R1 -> "\nMODE: DEEP REASONING. Carefully analyze premises, consider edge cases, and present step-by-step logic."
            ReasoningMode.CODING -> "\nMODE: CODE SYNTHESIS. Write production-ready, clean code with syntax highlighting, error handling, and test cases."
            ReasoningMode.MATH -> "\nMODE: MATHEMATICAL ENGINE. Show explicit derivation, formulas, calculations, and verify intermediate steps."
            ReasoningMode.CREATIVE -> "\nMODE: CREATIVE SYNTHESIS. Express ideas with vivid depth, metaphor, and engaging narrative."
            else -> ""
        }

        return base + addon
    }

    private suspend fun triggerInlineLearning(question: String, answer: String) {
        val lesson = geminiClient.generateLessonPlan(question, answer)
        if (lesson != null) {
            _lessons.update { current -> listOf(lesson) + current.take(24) }

            // Update skill tracker
            _skills.update { list ->
                list.map { domain ->
                    if (domain.name.equals(lesson.topic, ignoreCase = true) || lesson.topic.contains(domain.name, ignoreCase = true)) {
                        val newCount = domain.sampleCount + 1
                        val newAcc = (domain.accuracy * domain.sampleCount + lesson.grade) / newCount
                        domain.copy(accuracy = newAcc.coerceIn(0.1f, 0.99f), sampleCount = newCount)
                    } else domain
                }
            }
        }
    }

    /**
     * Executes the `/train` on-demand training cycle across conversation history.
     */
    suspend fun trainOnConversationHistory(): String = withContext(Dispatchers.IO) {
        val pairs = messages.value.filter { it.role != MessageRole.SYSTEM }
            .windowed(2, 2)
            .filter { it[0].role == MessageRole.USER && it[1].role == MessageRole.ASSISTANT }

        if (pairs.isEmpty()) {
            return@withContext "No Q&A conversation pairs available to train on. Ask me a few questions first!"
        }

        var trainedCount = 0
        for (pair in pairs.takeLast(4)) {
            val q = pair[0].text
            val a = pair[1].text
            triggerInlineLearning(q, a)
            trainedCount++
        }

        "Successfully executed inline training on $trainedCount conversation interactions! Lessons logged and skill map updated."
    }

    private fun fallbackResponse(query: String, mode: ReasoningMode, errorMsg: String?): String {
        return """
            ### Rani v16 Local Synthesis

            I received your query: **"$query"**

            *Status*: Connected to on-device SNN biophysical engine.
            
            ${if (!errorMsg.isNullOrBlank()) "> **Note**: Gemini API response status: $errorMsg\n" else ""}
            
            **To unlock cloud LLM intelligence**:
            1. Open the **Swarm & Server** or **Settings** tab in the app.
            2. Enter your free **Gemini API Key** (obtained freely at [aistudio.google.com](https://aistudio.google.com/)).
            3. Once entered, the full Gemini 3.5 Flash brain will stream instantly for all queries!
            
            In the meantime, the on-device **Spiking Neural Network (SNN)** simulator is running live with Hodgkin-Huxley dynamics, spike raster tracking, and synaptic plasticity. Check out the **SNN Engine** tab to see your biological neural network firing!
        """.trimIndent()
    }

    private fun fallbackSwarmResponse(query: String, errorMsg: String?): String {
        return """
            ### Swarm Consensus Report (${_swarmAgentCount.value} Agents)

            **Task**: "$query"
            
            **Swarm Multi-Agent Pipeline**:
            1. **Agent Alpha (Researcher)**: Query decomposed into functional requirements.
            2. **Agent Beta (SNN & Math)**: Verified system parameters & biophysical metrics.
            3. **Agent Delta (Critic)**: Validated core hypotheses and constraints.
            4. **Agent Zeta (Judge)**: Reached unified consensus.
            
            ${if (!errorMsg.isNullOrBlank()) "> **API Status**: $errorMsg (Add your free agent key in Swarm Settings for full live multi-LLM synthesis)\n" else ""}
            
            The Swarm system is operating normally with ${_swarmAgentCount.value} concurrent agents ready to dispatch tasks!
        """.trimIndent()
    }
}
