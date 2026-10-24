package com.example.data.repository

import com.example.data.api.GeminiApiClient
import com.example.data.model.Content
import com.example.data.model.GenerateContentRequest
import com.example.data.model.GenerationConfig
import com.example.data.model.ImageConfig
import com.example.data.model.InlineData
import com.example.data.model.Part
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class GeminiRepository {

    private val api = GeminiApiClient.apiService
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun generateChatResponse(
        apiKey: String,
        modelId: String,
        messages: List<Content>,
        systemInstruction: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("Gemini API key is not configured. Please enter your API key in Settings or ensure GEMINI_API_KEY is set in AI Studio Secrets.")
                )
            }

            val request = GenerateContentRequest(
                contents = messages,
                generationConfig = GenerationConfig(
                    temperature = 0.7f,
                    topP = 0.95f
                ),
                systemInstruction = systemInstruction?.takeIf { it.isNotBlank() }?.let {
                    Content(parts = listOf(Part(text = it)))
                }
            )

            val response = api.generateContent(model = modelId, apiKey = apiKey, request = request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.mapNotNull { it.text }?.joinToString("\n")
            if (text != null) {
                Result.success(text)
            } else {
                val blockReason = response.promptFeedback?.blockReason
                Result.failure(Exception(if (blockReason != null) "Blocked by safety filter: $blockReason" else "No response received from model."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun streamChatResponse(
        apiKey: String,
        modelId: String,
        messages: List<Content>,
        systemInstruction: String? = null,
        onChunk: (String) -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        val fullAccumulator = StringBuilder()
        try {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(
                    IllegalStateException("Gemini API key is missing. Set it in Settings or via Secrets.")
                )
            }

            val request = GenerateContentRequest(
                contents = messages,
                generationConfig = GenerationConfig(
                    temperature = 0.7f
                ),
                systemInstruction = systemInstruction?.takeIf { it.isNotBlank() }?.let {
                    Content(parts = listOf(Part(text = it)))
                }
            )

            val responseBody = api.generateContentStream(model = modelId, apiKey = apiKey, request = request)
            responseBody.byteStream().bufferedReader().use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val rawLine = line?.trim() ?: continue
                    if (!rawLine.startsWith("data:")) continue
                    val jsonPayload = rawLine.removePrefix("data:").trim()
                    if (jsonPayload.isEmpty() || jsonPayload == "[DONE]") continue

                    try {
                        val root = json.parseToJsonElement(jsonPayload).jsonObject
                        val candidates = root["candidates"]?.jsonArray
                        val firstCandidate = candidates?.getOrNull(0)?.jsonObject
                        val contentObj = firstCandidate?.get("content")?.jsonObject
                        val partsArr = contentObj?.get("parts")?.jsonArray
                        val textChunk = partsArr?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.content
                        if (textChunk != null) {
                            fullAccumulator.append(textChunk)
                            withContext(Dispatchers.Main) {
                                onChunk(textChunk)
                            }
                        }
                    } catch (_: Exception) {
                        // ignore malformed partial chunks
                    }
                }
            }

            if (fullAccumulator.isNotEmpty()) {
                Result.success(fullAccumulator.toString())
            } else {
                // If stream didn't yield chunks, fallback to normal generation
                generateChatResponse(apiKey, modelId, messages, systemInstruction)
            }
        } catch (e: Exception) {
            // If stream failed halfway, return what we got or error
            if (fullAccumulator.isNotEmpty()) {
                Result.success(fullAccumulator.toString())
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun generateImage(
        apiKey: String,
        prompt: String,
        aspectRatio: String = "1:1",
        style: String = "Realistic"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(IllegalStateException("Gemini API key is missing."))
            }

            val styledPrompt = "$prompt. Style: $style. Highly detailed, high resolution, award-winning visual."
            val request = GenerateContentRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(text = styledPrompt))
                    )
                ),
                generationConfig = GenerationConfig(
                    imageConfig = ImageConfig(aspectRatio = aspectRatio, imageSize = "1K"),
                    responseModalities = listOf("TEXT", "IMAGE")
                )
            )

            // gemini-2.5-flash-image supports image output
            val response = api.generateContent(
                model = "gemini-2.5-flash-image",
                apiKey = apiKey,
                request = request
            )

            val imagePart = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.inlineData != null }
            val base64Data = imagePart?.inlineData?.data
            if (base64Data != null) {
                Result.success(base64Data)
            } else {
                val textPart = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.text != null }?.text
                Result.failure(Exception(textPart ?: "No image generated by model."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editImage(
        apiKey: String,
        originalBase64: String,
        editPrompt: String,
        aspectRatio: String = "1:1"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(IllegalStateException("Gemini API key is missing."))
            }

            val promptText = "Edit and transform this image according to the following instruction: $editPrompt. Maintain core composition while applying the specified edits precisely."
            val request = GenerateContentRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = promptText),
                            Part(inlineData = InlineData(mimeType = "image/jpeg", data = originalBase64))
                        )
                    )
                ),
                generationConfig = GenerationConfig(
                    imageConfig = ImageConfig(aspectRatio = aspectRatio, imageSize = "1K"),
                    responseModalities = listOf("TEXT", "IMAGE")
                )
            )

            val response = api.generateContent(
                model = "gemini-2.5-flash-image",
                apiKey = apiKey,
                request = request
            )

            val imagePart = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.inlineData != null }
            val base64Data = imagePart?.inlineData?.data
            if (base64Data != null) {
                Result.success(base64Data)
            } else {
                val textPart = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.text != null }?.text
                Result.failure(Exception(textPart ?: "Image edit failed to produce an output image."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
