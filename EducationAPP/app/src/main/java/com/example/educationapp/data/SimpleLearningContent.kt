package com.example.educationapp.data

/**
 * 简化的学习内容数据模型
 */
data class SimpleLearningContent(
    val id: String,
    val title: String,
    val description: String,
    val type: SimpleContentType,
    val subject: String,
    val duration: Int, // 预计学习时间（分钟）
    val difficulty: String,
    val rating: Float = 4.5f,
    val viewCount: Int = 0,
    val progress: Float = 0f // 学习进度 0-1
)

/**
 * 简化的内容类型
 */
enum class SimpleContentType(val displayName: String, val icon: String) {
    VIDEO("视频", "🎥"),
    ARTICLE("文章", "📖"),
    EXERCISE("练习", "✏️"),
    QUIZ("测验", "📝"),
    INTERACTIVE("互动", "🎮")
}

/**
 * 简化的学习路径
 */
data class SimpleLearningPath(
    val id: String,
    val title: String,
    val description: String,
    val subject: String,
    val contentCount: Int,
    val estimatedDuration: Int, // 预计完成时间（小时）
    val difficulty: String,
    val completionRate: Float = 0f
)
