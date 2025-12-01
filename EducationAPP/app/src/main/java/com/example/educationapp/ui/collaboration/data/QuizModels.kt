package com.example.educationapp.ui.collaboration.data

/**
 * 竞赛题目数据模型
 */
data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: String, // A, B, C, D
    val difficulty: String, // easy, medium, hard
    val subject: String,
    val explanation: String = "",
    val timeLimit: Int = 15 // 秒
)

/**
 * 答题记录
 */
data class QuizAnswer(
    val questionId: String,
    val selectedOption: String,
    val isCorrect: Boolean,
    val responseTime: Long, // 毫秒
    val timestamp: Long
)

/**
 * 小组分数
 */
data class GroupScore(
    val groupName: String,
    var score: Int,
    val isMyGroup: Boolean = false,
    var rank: Int = 0,
    var correctAnswers: Int = 0,
    var totalAnswers: Int = 0
) {
    val accuracy: Float
        get() = if (totalAnswers > 0) correctAnswers.toFloat() / totalAnswers else 0f
}

/**
 * 竞赛统计
 */
data class QuizStatistics(
    val totalQuestions: Int,
    val correctAnswers: Int,
    val totalScore: Int,
    val averageResponseTime: Long,
    val accuracy: Float,
    val rank: Int,
    val participantCount: Int
)

/**
 * 竞赛排行榜项目
 */
data class LeaderboardItem(
    val rank: Int,
    val groupName: String,
    val score: Int,
    val accuracy: Float,
    val isMyGroup: Boolean = false
)

