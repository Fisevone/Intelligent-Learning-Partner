package com.example.educationapp.ai

import java.util.*

/**
 * AI功能相关的数据模型
 * 统一管理所有AI服务的数据类，避免重复定义
 */

// 学习模式相关
data class LearningPattern(
    val bestLearningTime: String,
    val preferredSubjects: List<String>,
    val learningStyle: String,
    val attentionSpan: String,
    val difficultyPreference: String,
    val recommendations: List<String>
)

// 学习路径相关
data class LearningPath(
    val totalWeeks: Int,
    val weeklyGoals: List<String>,
    val dailyTasks: List<String>,
    val milestones: List<String>,
    val resources: List<String>,
    val assessmentPoints: List<String>
)

// 学习会话相关
data class LearningSession(
    val subject: String,
    val topic: String,
    val duration: Int,
    val currentScore: Double,
    val attentionLevel: Int,
    val fatigueLevel: Int
)

// 学习状态相关
data class LearningState(
    val focusLevel: Int,
    val understandingLevel: Int,
    val fatigueLevel: Int,
    val recommendations: List<String>,
    val breakSuggestion: String,
    val difficultyAdjustment: String
)

// 学习建议相关
data class LearningSuggestion(
    val type: String,
    val title: String,
    val description: String,
    val priority: String,
    val estimatedTime: String
)

// 学习伙伴相关
data class LearningContext(
    val mood: String,
    val learningState: String,
    val currentSubject: String,
    val timeOfDay: String
)

data class CompanionResponse(
    val message: String,
    val suggestion: String,
    val encouragement: String,
    val nextAction: String
)

data class LearningTask(
    val title: String,
    val duration: Int,
    val priority: String,
    val subject: String
)

data class LearningReminder(
    val title: String,
    val message: String,
    val suggestion: String,
    val urgency: String,
    val estimatedTime: String
)

data class MotivationMessage(
    val title: String,
    val message: String,
    val achievements: List<String>,
    val nextGoal: String,
    val encouragement: String
)

data class LearningPlan(
    val totalGoal: String,
    val dailyTasks: List<String>,
    val estimatedTime: String
)

data class AdjustedPlan(
    val analysis: String,
    val adjustments: List<String>,
    val newPlan: LearningPlan,
    val reasoning: String
)

// 错题分析相关
data class MistakeRecord(
    val id: String,
    val subject: String,
    val question: String,
    val mistakeType: String,
    val reason: String,
    val timestamp: Long,
    val difficulty: String
)

data class MistakePattern(
    val commonMistakeTypes: List<String>,
    val weakSubjects: List<String>,
    val mistakePatterns: List<String>,
    val rootCauses: List<String>,
    val improvementSuggestions: List<String>,
    val priorityAreas: List<String>
)

data class PracticeSet(
    val totalQuestions: Int,
    val estimatedTime: String,
    val questions: List<PracticeQuestion>,
    val learningObjectives: List<String>,
    val successCriteria: String
)

data class PracticeQuestion(
    val id: String,
    val subject: String,
    val topic: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String,
    val difficulty: String,
    val targetMistakeType: String
)

data class ReviewReminder(
    val title: String,
    val message: String,
    val priorityMistakes: List<String>,
    val reviewStrategy: String,
    val suggestedTime: String,
    val estimatedDuration: String
)

data class ProgressReport(
    val improvementAreas: List<String>,
    val progressMetrics: ProgressMetrics,
    val achievements: List<String>,
    val remainingChallenges: List<String>,
    val nextSteps: List<String>,
    val overallAssessment: String
)

data class ProgressMetrics(
    val mistakeReduction: String,
    val scoreImprovement: String,
    val timeEfficiency: String
)

// 学习报告相关
data class TimeRange(
    val startDate: String,
    val endDate: String
)

data class LearningReport(
    val summary: String,
    val performance: ReportPerformanceMetrics,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val recommendations: List<String>,
    val nextGoals: List<String>,
    val motivation: String
)

data class ReportPerformanceMetrics(
    val overallScore: String,
    val studyTime: String,
    val subjects: List<String>,
    val improvements: List<String>
)

data class TrendAnalysis(
    val trendDirection: String,
    val keyPatterns: List<String>,
    val turningPoints: List<String>,
    val predictions: Predictions,
    val recommendations: List<String>,
    val riskFactors: List<String>
)

data class Predictions(
    val nextWeek: String,
    val nextMonth: String,
    val confidence: String
)

data class AdviceReport(
    val currentStatus: String,
    val gapAnalysis: String,
    val actionPlan: ActionPlan,
    val timeline: String,
    val successMetrics: List<String>,
    val supportNeeded: List<String>
)

data class ActionPlan(
    val shortTerm: List<String>,
    val mediumTerm: List<String>,
    val longTerm: List<String>
)

data class ParentTeacherReport(
    val overview: String,
    val highlights: List<String>,
    val concerns: List<String>,
    val recommendations: List<String>,
    val nextSteps: List<String>,
    val encouragement: String
)

// 追加：供 SmartAnalysis 与 AILearningAssistant 使用的模型

// 学习建议（用于 UI 展示）
data class LearningAdvice(
    val userId: Long? = null,
    val currentLevel: String,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val recommendations: List<String>,
    val motivationalMessage: String,
    val nextSteps: List<String>
)

// 常见错误条目
data class CommonMistake(
    val type: String,
    val frequency: Float
)

// 错题分析（用于 UI 展示）
data class MistakeAnalysis(
    val commonMistakes: List<CommonMistake>,
    val rootCauses: List<String>,
    val improvementStrategies: List<String>,
    val practiceRecommendations: List<String>
)

// 学习情绪（用于 UI 展示）
data class LearningMood(
    val userId: Long? = null,
    val currentMood: String,
    val moodTrend: String,
    val stressLevel: String,
    val engagementLevel: String,
    val recommendations: List<String>
)

// 个性化生成内容
data class GeneratedContent(
    val userId: Long? = null,
    val subject: String,
    val topic: String,
    val difficulty: String,
    val exercises: List<String>,
    val summary: String,
    val mindMap: String,
    val keyPoints: List<String>,
    val examples: List<String>
)
