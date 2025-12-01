package com.example.educationapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 📊 学习进度数据模型 - 追踪用户真实学习状态
 */
@Entity(tableName = "learning_progress")
data class LearningProgress(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val subject: String,           // 学科
    val knowledgePoint: String,    // 知识点
    val masteryLevel: Float,       // 掌握程度 0.0-1.0
    val studyTime: Long,          // 学习时长(秒)
    val correctAnswers: Int,       // 正确答题数
    val totalAnswers: Int,         // 总答题数
    val lastStudyTime: Long,       // 最后学习时间
    val difficultyLevel: String,   // 当前难度等级
    val studySource: String,       // 学习来源(AI出题/课程学习/练习等)
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 📈 学习统计数据
 */
@Entity(tableName = "learning_statistics")
data class LearningStatistics(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val date: String,              // 日期 YYYY-MM-DD
    val totalStudyTime: Long,      // 总学习时长
    val questionsAnswered: Int,    // 答题总数
    val correctRate: Float,        // 正确率
    val subjectsStudied: String,   // 学习的科目列表
    val aiInteractions: Int,       // AI交互次数
    val knowledgePointsLearned: Int, // 学习的知识点数量
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 🎯 学习行为记录
 */
@Entity(tableName = "learning_behaviors")
data class LearningBehavior(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val behaviorType: BehaviorType, // 行为类型
    val content: String,           // 行为内容
    val result: String,            // 行为结果
    val duration: Long,            // 持续时间
    val context: String,           // 上下文信息
    val timestamp: Long = System.currentTimeMillis()
)

enum class BehaviorType {
    QUESTION_ANSWERED,    // 答题
    AI_CHAT,             // AI对话
    KNOWLEDGE_EXPLORED,   // 知识点探索
    VIDEO_WATCHED,       // 视频观看
    EXERCISE_COMPLETED,   // 练习完成
    TEST_TAKEN,          // 测试参与
    STUDY_SESSION_START, // 学习开始
    STUDY_SESSION_END,   // 学习结束
    DIFFICULTY_FEEDBACK, // 难度反馈
    KNOWLEDGE_GRAPH_VIEW // 知识图谱查看
}

/**
 * 🧠 知识点掌握度计算
 */
data class KnowledgeMastery(
    val knowledgePoint: String,
    val masteryLevel: Float,
    val confidence: Float,        // 置信度
    val lastUpdate: Long,
    val studyFrequency: Int,     // 学习频次
    val averageScore: Float,     // 平均得分
    val timeSpent: Long,         // 累计学习时间
    val difficultyProgression: List<String>, // 难度进阶历史
    val prerequisites: List<String>, // 前置要求
    val relatedConcepts: List<String> // 相关概念
)

