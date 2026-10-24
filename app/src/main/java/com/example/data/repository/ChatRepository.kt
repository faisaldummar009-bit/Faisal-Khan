package com.example.data.repository

import com.example.data.local.dao.ChatDao
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatSessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ChatRepository(private val chatDao: ChatDao) {

    val allSessions: Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    fun getMessages(sessionId: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForSession(sessionId)

    suspend fun getMessagesSync(sessionId: String): List<ChatMessageEntity> =
        chatDao.getMessagesForSessionSync(sessionId)

    suspend fun createNewSession(
        title: String = "New Chat",
        modelId: String = "gemini-3.5-flash",
        systemInstruction: String? = null
    ): String {
        val id = UUID.randomUUID().toString()
        val session = ChatSessionEntity(
            id = id,
            title = title,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            modelId = modelId,
            systemInstruction = systemInstruction
        )
        chatDao.insertSession(session)
        return id
    }

    suspend fun getSession(id: String): ChatSessionEntity? =
        chatDao.getSessionById(id)

    suspend fun updateSessionTitle(id: String, title: String) {
        chatDao.updateSessionTitle(id, title)
    }

    suspend fun deleteSession(id: String) {
        chatDao.deleteSessionById(id)
    }

    suspend fun clearAll() {
        chatDao.clearAllSessions()
    }

    suspend fun addMessage(
        sessionId: String,
        role: String,
        content: String,
        imageBase64: String? = null
    ): String {
        val id = UUID.randomUUID().toString()
        val message = ChatMessageEntity(
            id = id,
            sessionId = sessionId,
            role = role,
            content = content,
            imageBase64 = imageBase64,
            timestamp = System.currentTimeMillis()
        )
        chatDao.insertMessage(message)
        chatDao.updateSessionTitle(sessionId, getAutoTitle(content, sessionId))
        return id
    }

    suspend fun updateMessage(message: ChatMessageEntity) {
        chatDao.updateMessage(message)
    }

    suspend fun deleteMessagesFrom(sessionId: String, timestamp: Long) {
        chatDao.deleteMessagesFrom(sessionId, timestamp)
    }

    suspend fun setMessageFeedback(messageId: String, isLiked: Boolean, isDisliked: Boolean) {
        chatDao.setMessageFeedback(messageId, isLiked, isDisliked)
    }

    private suspend fun getAutoTitle(firstUserText: String, sessionId: String): String {
        val session = chatDao.getSessionById(sessionId) ?: return "New Chat"
        if (session.title != "New Chat" && session.title.isNotBlank()) {
            return session.title
        }
        val clean = firstUserText.lines().firstOrNull()?.trim() ?: "Conversation"
        return if (clean.length > 36) clean.take(33) + "..." else clean
    }
}
