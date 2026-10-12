package com.example.service

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    companion object {
        private const val TAG = "GeminiService"
        // Officially supported fast models
        private const val PRIMARY_MODEL = "gemini-2.5-flash"
        private const val BACKUP_MODEL = "gemini-3.5-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"
    }

    private val systemPrompt = """
        You are RAHID JARVIS AI, an advanced, highly capable, and respectful artificial intelligence assistant built for Rahid's Nothing Phone (2a) Plus (8GB RAM / 256GB Storage, Nothing OS).
        Language guidelines:
        - If the user writes or speaks in Bengali (বাংলা), respond in natural, polite, and fluent Bengali.
        - If the user writes or speaks in English, respond in fluent English.
        - Keep answers concise, direct, helpful, and suitable for voice read-aloud (avoid overly long markup, excessive markdown tables, or difficult formatting unless asked).
        - You have a futuristic JARVIS persona: intelligent, prompt, loyal, and efficient.
        - If the user asks about phone features (Flashlight, Time, Date, YouTube, Settings, Camera, Calculator), inform them that you can execute these commands directly on the device!
    """.trimIndent()

    suspend fun generateResponse(
        userPrompt: String,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("API_KEY_MISSING")
            )
        }

        // Try primary model first, fallback to backup if needed
        val firstAttempt = executeRequest(PRIMARY_MODEL, apiKey, userPrompt, conversationHistory)
        if (firstAttempt.isSuccess) {
            return@withContext firstAttempt
        }

        Log.w(TAG, "Primary model failed: ${firstAttempt.exceptionOrNull()?.message}, trying backup: $BACKUP_MODEL")
        executeRequest(BACKUP_MODEL, apiKey, userPrompt, conversationHistory)
    }

    private fun executeRequest(
        model: String,
        apiKey: String,
        prompt: String,
        history: List<Pair<String, String>>
    ): Result<String> {
        return try {
            val url = "$BASE_URL$model:generateContent?key=$apiKey"

            val jsonBody = JSONObject().apply {
                // System Instruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemPrompt)
                        })
                    })
                })

                // Generation Config
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                    put("topK", 40)
                    put("maxOutputTokens", 1024)
                })

                // Contents with conversation context (last 6 turns max)
                val contentsArray = JSONArray()
                val recentHistory = history.takeLast(6)
                for ((role, text) in recentHistory) {
                    contentsArray.put(JSONObject().apply {
                        put("role", if (role.equals("user", ignoreCase = true)) "user" else "model")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", text)
                            })
                        })
                    })
                }

                // Add current prompt
                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })

                put("contents", contentsArray)
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val errorDetail = try {
                        val errJson = JSONObject(bodyString)
                        errJson.optJSONObject("error")?.optString("message") ?: bodyString
                    } catch (e: Exception) {
                        bodyString
                    }
                    return Result.failure(Exception("HTTP ${response.code}: $errorDetail"))
                }

                val responseJson = JSONObject(bodyString)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val contentObj = firstCandidate.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val replyText = parts.getJSONObject(0).optString("text", "")
                        if (replyText.isNotBlank()) {
                            return Result.success(replyText.trim())
                        }
                    }
                }
                Result.failure(Exception("Empty candidate returned from Gemini API"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Request execution error", e)
            Result.failure(e)
        }
    }
}
