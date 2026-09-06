package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ChatMessage
import com.example.data.model.MessageRole

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val role: String,
    val text: String,
    val timestamp: Long,
    val confidence: Float,
    val source: String,
    val expertDomain: String,
    val isSwarm: Boolean,
    val isError: Boolean,
    val rating: String?
) {
    fun toDomain(): ChatMessage {
        return ChatMessage(
            id = id,
            role = try {
                MessageRole.valueOf(role)
            } catch (e: Exception) {
                MessageRole.ASSISTANT
            },
            text = text,
            timestamp = timestamp,
            confidence = confidence,
            source = source,
            expertDomain = expertDomain,
            isSwarm = isSwarm,
            isError = isError,
            rating = rating
        )
    }

    companion object {
        fun fromDomain(msg: ChatMessage): ChatMessageEntity {
            return ChatMessageEntity(
                id = msg.id,
                role = msg.role.name,
                text = msg.text,
                timestamp = msg.timestamp,
                confidence = msg.confidence,
                source = msg.source,
                expertDomain = msg.expertDomain,
                isSwarm = msg.isSwarm,
                isError = msg.isError,
                rating = msg.rating
            )
        }
    }
}
