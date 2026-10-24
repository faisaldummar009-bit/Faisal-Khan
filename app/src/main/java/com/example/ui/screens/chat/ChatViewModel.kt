package com.example.ui.screens.chat

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatSessionEntity
import com.example.data.model.AiModel
import com.example.data.model.Content
import com.example.data.model.InlineData
import com.example.data.model.Part
import com.example.data.repository.ChatRepository
import com.example.data.repository.GeminiRepository
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID

data class ChatUiState(
    val currentSessionId: String? = null,
    val isStreaming: Boolean = false,
    val streamingChunk: String = "",
    val error: String? = null,
    val attachedImageBase64: String? = null,
    val attachedImageUri: Uri? = null,
    val selectedModel: AiModel = AiModel.FLASH_3_5
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val chatRepo = ChatRepository(db.chatDao())
    private val geminiRepo = GeminiRepository()
    val prefsRepo = UserPreferencesRepository(application)

    val sessions: StateFlow<List<ChatSessionEntity>> = chatRepo.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val messages: StateFlow<List<ChatMessageEntity>> = _messages.asStateFlow()

    init {
        viewModelScope.launch {
            prefsRepo.selectedModel.collect { model ->
                _uiState.value = _uiState.value.copy(selectedModel = model)
            }
        }
        viewModelScope.launch {
            // Load latest session or create default
            val currentSessions = chatRepo.allSessions.firstOrNull()
            if (!currentSessions.isNullOrEmpty()) {
                selectSession(currentSessions.first().id)
            } else {
                createNewChat()
            }
        }
    }

    fun selectSession(sessionId: String) {
        _uiState.value = _uiState.value.copy(
            currentSessionId = sessionId,
            error = null,
            streamingChunk = "",
            isStreaming = false
        )
        viewModelScope.launch {
            chatRepo.getMessages(sessionId).collect { list ->
                _messages.value = list
            }
        }
    }

    fun createNewChat(initialTitle: String = "New Chat", systemPrompt: String? = null) {
        viewModelScope.launch {
            val modelId = _uiState.value.selectedModel.modelId
            val newId = chatRepo.createNewSession(title = initialTitle, modelId = modelId, systemInstruction = systemPrompt)
            selectSession(newId)
        }
    }

    fun attachImage(uri: Uri) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val stream = ByteArrayOutputStream()
                    // Compress to max 1024px to ensure fast transfer within token limits
                    val maxDim = 1024
                    val scaled = if (bitmap.width > maxDim || bitmap.height > maxDim) {
                        val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                        val w = if (ratio >= 1) maxDim else (maxDim * ratio).toInt()
                        val h = if (ratio >= 1) (maxDim / ratio).toInt() else maxDim
                        Bitmap.createScaledBitmap(bitmap, w, h, true)
                    } else {
                        bitmap
                    }
                    scaled.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                    val base64 = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                    _uiState.value = _uiState.value.copy(
                        attachedImageBase64 = base64,
                        attachedImageUri = uri
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to process image: ${e.message}")
            }
        }
    }

    fun removeAttachedImage() {
        _uiState.value = _uiState.value.copy(attachedImageBase64 = null, attachedImageUri = null)
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        val imageBase64 = _uiState.value.attachedImageBase64
        if (trimmed.isBlank() && imageBase64 == null) return

        val sessionId = _uiState.value.currentSessionId ?: return

        // Clear input state immediately
        _uiState.value = _uiState.value.copy(
            attachedImageBase64 = null,
            attachedImageUri = null,
            isStreaming = true,
            streamingChunk = "",
            error = null
        )

        viewModelScope.launch {
            // Save user message to DB
            chatRepo.addMessage(
                sessionId = sessionId,
                role = "user",
                content = trimmed,
                imageBase64 = imageBase64
            )
            prefsRepo.incrementUsage()

            // Fetch session details and all previous messages for multi-turn context
            val session = chatRepo.getSession(sessionId)
            val history = chatRepo.getMessagesSync(sessionId)

            // Convert to Gemini API Contents list
            val geminiContents = history.map { msg ->
                val parts = mutableListOf<Part>()
                if (msg.content.isNotBlank()) {
                    parts.add(Part(text = msg.content))
                }
                if (msg.imageBase64 != null) {
                    parts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = msg.imageBase64)))
                }
                Content(role = if (msg.role == "user") "user" else "model", parts = parts)
            }

            val customKey = prefsRepo.customApiKey.firstOrNull()
            val effectiveKey = prefsRepo.getEffectiveApiKey(customKey)
            val modelId = _uiState.value.selectedModel.modelId

            val streamingAccumulator = StringBuilder()

            val result = geminiRepo.streamChatResponse(
                apiKey = effectiveKey,
                modelId = modelId,
                messages = geminiContents,
                systemInstruction = session?.systemInstruction ?: "You are Light GPT, a helpful, brilliant, and concise AI assistant with sleek intelligence. Format responses clearly with Markdown headings, bullet points, and syntax-highlighted code blocks where helpful.",
                onChunk = { chunk ->
                    streamingAccumulator.append(chunk)
                    _uiState.value = _uiState.value.copy(streamingChunk = streamingAccumulator.toString())
                }
            )

            result.onSuccess { fullReply ->
                chatRepo.addMessage(
                    sessionId = sessionId,
                    role = "model",
                    content = fullReply
                )
                _uiState.value = _uiState.value.copy(
                    isStreaming = false,
                    streamingChunk = ""
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isStreaming = false,
                    streamingChunk = "",
                    error = err.message ?: "Failed to receive response"
                )
            }
        }
    }

    fun regenerateLastMessage() {
        val sessionId = _uiState.value.currentSessionId ?: return
        viewModelScope.launch {
            val list = chatRepo.getMessagesSync(sessionId)
            if (list.isEmpty()) return@launch

            val lastMsg = list.last()
            if (lastMsg.role == "model") {
                // Delete last model message and re-run with prior messages
                chatRepo.deleteMessagesFrom(sessionId, lastMsg.timestamp)
                val remaining = chatRepo.getMessagesSync(sessionId)
                val lastUserMsg = remaining.lastOrNull { it.role == "user" } ?: return@launch
                sendMessageInternal(sessionId, remaining, lastUserMsg)
            }
        }
    }

    private suspend fun sendMessageInternal(
        sessionId: String,
        history: List<ChatMessageEntity>,
        lastUserMsg: ChatMessageEntity
    ) {
        _uiState.value = _uiState.value.copy(
            isStreaming = true,
            streamingChunk = "",
            error = null
        )

        val session = chatRepo.getSession(sessionId)
        val geminiContents = history.map { msg ->
            val parts = mutableListOf<Part>()
            if (msg.content.isNotBlank()) parts.add(Part(text = msg.content))
            if (msg.imageBase64 != null) {
                parts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = msg.imageBase64)))
            }
            Content(role = if (msg.role == "user") "user" else "model", parts = parts)
        }

        val customKey = prefsRepo.customApiKey.firstOrNull()
        val effectiveKey = prefsRepo.getEffectiveApiKey(customKey)
        val modelId = _uiState.value.selectedModel.modelId

        val streamingAccumulator = StringBuilder()
        val result = geminiRepo.streamChatResponse(
            apiKey = effectiveKey,
            modelId = modelId,
            messages = geminiContents,
            systemInstruction = session?.systemInstruction,
            onChunk = { chunk ->
                streamingAccumulator.append(chunk)
                _uiState.value = _uiState.value.copy(streamingChunk = streamingAccumulator.toString())
            }
        )

        result.onSuccess { fullReply ->
            chatRepo.addMessage(sessionId, "model", fullReply)
            _uiState.value = _uiState.value.copy(isStreaming = false, streamingChunk = "")
        }.onFailure { err ->
            _uiState.value = _uiState.value.copy(
                isStreaming = false,
                streamingChunk = "",
                error = err.message ?: "Failed to regenerate"
            )
        }
    }

    fun setFeedback(messageId: String, isLiked: Boolean, isDisliked: Boolean) {
        viewModelScope.launch {
            chatRepo.setMessageFeedback(messageId, isLiked, isDisliked)
        }
    }

    fun renameSession(sessionId: String, newTitle: String) {
        viewModelScope.launch {
            chatRepo.updateSessionTitle(sessionId, newTitle)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            chatRepo.deleteSession(sessionId)
            val remaining = chatRepo.allSessions.firstOrNull()
            if (!remaining.isNullOrEmpty()) {
                selectSession(remaining.first().id)
            } else {
                createNewChat()
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            chatRepo.clearAll()
            createNewChat()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
