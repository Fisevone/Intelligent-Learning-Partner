package com.example.educationapp.ui.collaboration.data

/**
 * 讨论消息数据模型
 */
data class DiscussionMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: Long,
    val type: MessageType,
    val isRead: Boolean = false,
    val reactions: List<MessageReaction> = emptyList()
)

/**
 * 消息类型
 */
enum class MessageType {
    USER_MESSAGE,    // 用户消息
    SYSTEM_MESSAGE,  // 系统消息
    AI_MESSAGE,      // AI助手消息
    TOPIC_SUGGESTION, // 话题建议
    SUMMARY         // 讨论总结
}

/**
 * 消息反应（点赞、疑问等）
 */
data class MessageReaction(
    val userId: String,
    val userName: String,
    val type: ReactionType,
    val timestamp: Long
)

enum class ReactionType {
    LIKE,      // 👍
    LOVE,      // ❤️
    QUESTION,  // ❓
    IDEA,      // 💡
    AGREE      // ✅
}

