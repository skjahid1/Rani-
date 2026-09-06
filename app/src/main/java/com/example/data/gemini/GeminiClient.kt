package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.LessonPlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay

class GeminiClient(
    private var customApiKey: String = ""
) {
    // Generous timeouts (90s connect, 90s read/write) and connection pooling with retryOnConnectionFailure
    private val client = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    fun updateApiKey(newKey: String) {
        customApiKey = newKey.trim()
    }

    fun getEffectiveApiKey(): String {
        if (customApiKey.isNotBlank()) return customApiKey
        val buildConfigKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }
        return if (buildConfigKey.isNotBlank() && buildConfigKey != "MY_GEMINI_API_KEY") {
            buildConfigKey
        } else {
            ""
        }
    }

    suspend fun generateContent(
        prompt: String,
        systemInstruction: String? = null,
        temperature: Float = 0.7f,
        modelName: String = "gemini-3.5-flash",
        history: List<Pair<String, String>> = emptyList() // role to content
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("Free Gemini API Key is missing. Please enter your free Gemini API key in Settings or add it to Secrets.")
            )
        }

        try {
            val root = JSONObject()

            // System instruction
            if (!systemInstruction.isNullOrBlank()) {
                val sysObj = JSONObject()
                val partsArray = JSONArray()
                partsArray.put(JSONObject().put("text", systemInstruction))
                sysObj.put("parts", partsArray)
                root.put("systemInstruction", sysObj)
            }

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", temperature.toDouble())
            root.put("generationConfig", genConfig)

            // Contents array
            val contentsArray = JSONArray()
            for ((role, text) in history.takeLast(6)) {
                val contentObj = JSONObject()
                contentObj.put("role", if (role.equals("assistant", true)) "model" else "user")
                val parts = JSONArray()
                parts.put(JSONObject().put("text", text))
                contentObj.put("parts", parts)
                contentsArray.put(contentObj)
            }

            // Current user turn
            val currentContent = JSONObject()
            currentContent.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", prompt))
            currentContent.put("parts", currentParts)
            contentsArray.put(currentContent)

            root.put("contents", contentsArray)

            val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
            val requestBody = root.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            var lastException: Exception? = null
            val maxAttempts = 2

            for (attempt in 1..maxAttempts) {
                try {
                    val response = client.newCall(request).execute()
                    val responseString = response.body?.string().orEmpty()

                    if (!response.isSuccessful) {
                        val errMsg = try {
                            val errJson = JSONObject(responseString)
                            errJson.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                        } catch (e: Exception) {
                            "HTTP ${response.code}: $responseString"
                        }
                        // If 5xx error or rate limit 429, retry
                        if ((response.code == 429 || response.code in 500..599) && attempt < maxAttempts) {
                            Log.w("GeminiClient", "Transient HTTP ${response.code}, retrying attempt $attempt...")
                            delay(1000L * attempt)
                            continue
                        }
                        return@withContext Result.failure(Exception("Gemini API Error: $errMsg"))
                    }

                    val json = JSONObject(responseString)
                    val candidates = json.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val firstCandidate = candidates.getJSONObject(0)
                        val content = firstCandidate.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val text = parts.getJSONObject(0).optString("text")
                            return@withContext Result.success(text)
                        }
                    }

                    return@withContext Result.failure(Exception("No candidate text returned by Gemini API"))
                } catch (e: SocketTimeoutException) {
                    Log.w("GeminiClient", "Socket timeout on attempt $attempt: ${e.message}")
                    lastException = e
                    if (attempt < maxAttempts) {
                        delay(1200L * attempt)
                    }
                } catch (e: IOException) {
                    Log.w("GeminiClient", "IO exception on attempt $attempt: ${e.message}")
                    lastException = e
                    if (attempt < maxAttempts) {
                        delay(1000L * attempt)
                    }
                } catch (e: Exception) {
                    Log.e("GeminiClient", "Unexpected error during Gemini API call", e)
                    return@withContext Result.failure(e)
                }
            }

            val finalError = lastException ?: Exception("Network request timed out after $maxAttempts attempts")
            Log.e("GeminiClient", "Request failed after $maxAttempts attempts", finalError)
            Result.failure(finalError)
        } catch (e: Exception) {
            Log.e("GeminiClient", "Request failed", e)
            Result.failure(e)
        }
    }

    /**
     * Run inline learning extraction: asks Gemini to critique the student answer,
     * extract reasoning, produce a permanent rule, and assign a grade.
     */
    suspend fun generateLessonPlan(
        question: String,
        answer: String
    ): LessonPlan? = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank()) return@withContext null

        val teacherPrompt = """
            You are an expert AI teacher evaluating a student's answer.
            
            Question: $question
            Student's answer: $answer
            
            Evaluate and provide a structured lesson. Return ONLY valid JSON with keys:
            {
              "correct_answer": "The ideal concise answer",
              "reasoning": "Step-by-step logic the student missed or demonstrated",
              "rule": "One-sentence rule to remember forever",
              "topic": "2-3 word topic label",
              "grade": 0.85
            }
            grade: 0.0 (wrong) to 1.0 (perfect). Output ONLY JSON.
        """.trimIndent()

        val result = generateContent(
            prompt = teacherPrompt,
            temperature = 0.2f
        )

        val rawText = result.getOrNull() ?: return@withContext null
        parseLessonJson(rawText, question, answer)
    }

    private fun parseLessonJson(raw: String, question: String, answer: String): LessonPlan? {
        val clean = raw.replace("```json", "").replace("```", "").trim()
        return try {
            val start = clean.indexOf('{')
            val end = clean.lastIndexOf('}')
            if (start != -1 && end != -1) {
                val jsonStr = clean.substring(start, end + 1)
                val obj = JSONObject(jsonStr)
                LessonPlan(
                    question = question,
                    raniAnswer = answer,
                    correctAnswer = obj.optString("correct_answer", answer),
                    reasoning = obj.optString("reasoning", "Validated logical consistency."),
                    rule = obj.optString("rule", "Verify premises before deriving conclusion."),
                    topic = obj.optString("topic", "General"),
                    grade = obj.optDouble("grade", 0.85).toFloat()
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
