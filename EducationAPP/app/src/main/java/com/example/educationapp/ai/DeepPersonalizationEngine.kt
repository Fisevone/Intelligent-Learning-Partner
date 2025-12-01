package com.example.educationapp.ai

import android.util.Log
import com.example.educationapp.data.LearningRecord
import com.example.educationapp.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

/**
 * 🎯 深度个性化引擎 - 基于学习历史的智能分析
 * 
 * 核心功能：
 * 1. 学习模式识别 - 分析用户的学习习惯和偏好
 * 2. 知识掌握建模 - 构建用户的知识图谱
 * 3. 学习路径优化 - 生成个性化学习路径
 * 4. 认知负荷评估 - 评估用户的认知负荷水平
 */
class DeepPersonalizationEngine {
    
    private val zhipuAIService = ZhipuAIService()
    
    companion object {
        private const val TAG = "DeepPersonalization"
    }
    
    /**
     * 🧠 学习者画像数据类
     */
    data class LearnerProfile(
        val userId: Long,
        val learningStyle: LearningStyleProfile,
        val knowledgeMap: KnowledgeMap,
        val cognitiveProfile: CognitiveProfile,
        val motivationProfile: MotivationProfile,
        val performancePattern: PerformancePattern,
        val personalizedStrategy: PersonalizationStrategy,
        val lastUpdated: Long = System.currentTimeMillis()
    )
    
    /**
     * 🎯 学习风格画像
     */
    data class LearningStyleProfile(
        val primaryStyle: String, // "视觉型", "听觉型", "动觉型", "读写型"
        val secondaryStyle: String,
        val processingPreference: String, // "顺序型", "全局型"
        val thinkingStyle: String, // "分析型", "直觉型"
        val learningPace: String, // "快速型", "稳健型", "深思型"
        val confidenceScore: Float, // 0.0-1.0
        val evidenceBasis: List<String> // 得出结论的依据
    )
    
    /**
     * 🗺️ 知识图谱
     */
    data class KnowledgeMap(
        val subjectMastery: Map<String, SubjectMastery>,
        val conceptConnections: Map<String, List<String>>, // 概念间的关联
        val learningSequence: List<String>, // 推荐的学习顺序
        val strengthAreas: List<String>,
        val improvementAreas: List<String>,
        val nextLearningTargets: List<String>
    )
    
    /**
     * 📊 学科掌握度
     */
    data class SubjectMastery(
        val subject: String,
        val overallMastery: Float, // 0.0-1.0
        val topicMastery: Map<String, Float>,
        val skillProgression: Map<String, Float>,
        val difficultyComfort: Map<String, Float>, // 各难度级别的舒适度
        val commonMistakePatterns: List<String>,
        val strongConcepts: List<String>,
        val improvementTrends: Map<String, Float> // 各知识点的改进趋势
    )
    
    /**
     * 🧠 认知能力画像
     */
    data class CognitiveProfile(
        val workingMemoryCapacity: Float, // 工作记忆容量
        val processingSpeed: Float, // 处理速度
        val attentionSpan: Float, // 注意力持续时间
        val cognitiveLoad: Float, // 当前认知负荷
        val optimalChallengeLevel: Float, // 最佳挑战水平
        val fatiguePattern: List<TimeBasedMetric>, // 疲劳模式
        val peakPerformanceTime: List<String> // 最佳表现时间段
    )
    
    /**
     * 💪 动机画像
     */
    data class MotivationProfile(
        val intrinsicMotivation: Float, // 内在动机
        val extrinsicMotivation: Float, // 外在动机
        val goalOrientation: String, // "掌握导向", "表现导向"
        val persistenceLevel: Float, // 坚持度
        val challengePreference: Float, // 挑战偏好
        val feedbackSensitivity: Float, // 反馈敏感度
        val socialLearningPreference: Float // 社交学习偏好
    )
    
    /**
     * 📈 表现模式
     */
    data class PerformancePattern(
        val consistencyScore: Float, // 表现一致性
        val improvementRate: Float, // 改进速度
        val retentionRate: Float, // 知识保持率
        val transferAbility: Float, // 知识迁移能力
        val errorRecoveryRate: Float, // 错误恢复率
        val optimalSessionLength: Int, // 最佳学习时长(分钟)
        val performanceCycles: List<PerformanceCycle> // 表现周期
    )
    
    /**
     * 🎯 个性化策略
     */
    data class PersonalizationStrategy(
        val recommendedDifficulty: String,
        val optimalQuestionTypes: List<String>,
        val suggestedTopics: List<String>,
        val learningPathAdjustments: List<String>,
        val motivationalStrategies: List<String>,
        val cognitiveSupports: List<String>,
        val nextActionRecommendations: List<String>
    )
    
    // 辅助数据类
    data class TimeBasedMetric(val timeRange: String, val value: Float)
    data class PerformanceCycle(val pattern: String, val duration: Int, val intensity: Float)
    
    /**
     * 🎯 核心方法：生成深度个性化学习者画像
     */
    suspend fun generateLearnerProfile(
        user: User,
        learningHistory: List<LearningRecord>
    ): Result<LearnerProfile> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🎯 开始生成深度个性化画像 for user: ${user.id}")
            
            if (learningHistory.isEmpty()) {
                Log.w(TAG, "⚠️ 学习历史为空，使用基础画像")
                return@withContext Result.success(generateBasicProfile(user))
            }
            
            // 1. 分析学习风格
            val learningStyle = analyzeLearningStyle(user, learningHistory)
            
            // 2. 构建知识图谱
            val knowledgeMap = buildKnowledgeMap(learningHistory)
            
            // 3. 分析认知能力
            val cognitiveProfile = analyzeCognitiveProfile(learningHistory)
            
            // 4. 分析动机模式
            val motivationProfile = analyzeMotivationProfile(user, learningHistory)
            
            // 5. 分析表现模式
            val performancePattern = analyzePerformancePattern(learningHistory)
            
            // 6. 生成个性化策略
            val strategy = generatePersonalizationStrategy(
                learningStyle, knowledgeMap, cognitiveProfile, motivationProfile, performancePattern
            )
            
            val profile = LearnerProfile(
                userId = user.id,
                learningStyle = learningStyle,
                knowledgeMap = knowledgeMap,
                cognitiveProfile = cognitiveProfile,
                motivationProfile = motivationProfile,
                performancePattern = performancePattern,
                personalizedStrategy = strategy
            )
            
            Log.d(TAG, "✅ 深度个性化画像生成成功")
            Result.success(profile)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ 生成个性化画像失败: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * 🎯 分析学习风格
     */
    private suspend fun analyzeLearningStyle(
        user: User,
        learningHistory: List<LearningRecord>
    ): LearningStyleProfile {
        
        // 分析响应时间模式
        val avgResponseTime = learningHistory.map { it.duration.toDouble() }.average()
        val responseVariability = calculateVariability(learningHistory.map { it.duration.toDouble() })
        
        // 分析正确率模式
        val avgScore = learningHistory.map { it.score.toDouble() }.average()
        val scoreConsistency = 1.0f - calculateVariability(learningHistory.map { it.score.toDouble() }).toFloat()
        
        // 分析学习时间偏好
        val timePreferences = analyzeTimePreferences(learningHistory)
        
        // 分析题目类型偏好
        val typePreferences = analyzeTypePreferences(learningHistory)
        
        // 基于数据推断学习风格
        val primaryStyle = when {
            avgResponseTime < 60 && scoreConsistency > 0.8f -> "视觉型" // 快速且稳定
            avgResponseTime > 120 && avgScore > 80.0 -> "读写型" // 慢但准确
            responseVariability > 30 && avgScore > 75.0 -> "动觉型" // 变化大但效果好
            else -> "听觉型"
        }
        
        val processingPreference = if (scoreConsistency > 0.7f) "顺序型" else "全局型"
        val thinkingStyle = if (avgResponseTime < 90.0) "直觉型" else "分析型"
        val learningPace = when {
            avgResponseTime < 60.0 -> "快速型"
            avgResponseTime > 150.0 -> "深思型"
            else -> "稳健型"
        }
        
        return LearningStyleProfile(
            primaryStyle = primaryStyle,
            secondaryStyle = determineSecondaryStyle(primaryStyle, typePreferences),
            processingPreference = processingPreference,
            thinkingStyle = thinkingStyle,
            learningPace = learningPace,
            confidenceScore = minOf(scoreConsistency, (avgScore / 100.0).toFloat()),
            evidenceBasis = listOf(
                "基于${learningHistory.size}次学习记录",
                "平均响应时间: ${avgResponseTime.toInt()}秒",
                "平均正确率: ${avgScore.toInt()}%",
                "表现一致性: ${(scoreConsistency * 100).toInt()}%"
            )
        )
    }
    
    /**
     * 🗺️ 构建知识图谱
     */
    private suspend fun buildKnowledgeMap(learningHistory: List<LearningRecord>): KnowledgeMap {
        
        // 按科目分组分析
        val subjectGroups = learningHistory.groupBy { it.subject }
        val subjectMasteryMap = mutableMapOf<String, SubjectMastery>()
        
        subjectGroups.forEach { (subject, records) ->
            val topicMastery = records.groupBy { it.topic }
                .mapValues { (_, topicRecords) ->
                    topicRecords.map { it.score }.average().toFloat() / 100f
                }
            
            val overallMastery = topicMastery.values.average().toFloat()
            
            // 分析常见错误模式
            val lowScoreRecords = records.filter { it.score < 70 }
            val commonMistakes = lowScoreRecords.groupBy { it.topic }
                .filter { it.value.size >= 2 }
                .keys.toList()
            
            // 分析强项
            val strongConcepts = topicMastery.filter { it.value > 0.85f }.keys.toList()
            
            // 计算改进趋势
            val improvementTrends = calculateImprovementTrends(records)
            
            subjectMasteryMap[subject] = SubjectMastery(
                subject = subject,
                overallMastery = overallMastery,
                topicMastery = topicMastery,
                skillProgression = calculateSkillProgression(records),
                difficultyComfort = calculateDifficultyComfort(records),
                commonMistakePatterns = commonMistakes,
                strongConcepts = strongConcepts,
                improvementTrends = improvementTrends
            )
        }
        
        // 识别强项和改进区域
        val strengthAreas = subjectMasteryMap.filter { it.value.overallMastery > 0.8f }.keys.toList()
        val improvementAreas = subjectMasteryMap.filter { it.value.overallMastery < 0.6f }.keys.toList()
        
        // 生成学习顺序建议
        val learningSequence = generateOptimalLearningSequence(subjectMasteryMap)
        
        // 推荐下一步学习目标
        val nextTargets = generateNextLearningTargets(subjectMasteryMap)
        
        return KnowledgeMap(
            subjectMastery = subjectMasteryMap,
            conceptConnections = buildConceptConnections(learningHistory),
            learningSequence = learningSequence,
            strengthAreas = strengthAreas,
            improvementAreas = improvementAreas,
            nextLearningTargets = nextTargets
        )
    }
    
    /**
     * 🧠 分析认知能力画像
     */
    private suspend fun analyzeCognitiveProfile(learningHistory: List<LearningRecord>): CognitiveProfile {
        
        // 分析工作记忆容量（基于复杂题目的表现）
        val complexQuestions = learningHistory.filter { it.difficulty == "高级" || it.difficulty == "挑战" }
        val workingMemoryCapacity = if (complexQuestions.isNotEmpty()) {
            complexQuestions.map { it.score }.average().toFloat() / 100f
        } else 0.6f
        
        // 分析处理速度
        val avgProcessingTime = learningHistory.map { it.duration }.average()
        val processingSpeed = when {
            avgProcessingTime < 60 -> 0.9f
            avgProcessingTime < 120 -> 0.7f
            avgProcessingTime < 180 -> 0.5f
            else -> 0.3f
        }
        
        // 分析注意力持续时间
        val sessionLengths = learningHistory.map { it.duration.toInt() }
        val attentionSpan = calculateAttentionSpan(sessionLengths)
        
        // 评估当前认知负荷
        val recentPerformance = learningHistory.takeLast(10)
        val cognitiveLoad = if (recentPerformance.isNotEmpty()) {
            val performanceDecline = calculatePerformanceDecline(recentPerformance)
            maxOf(0f, minOf(1f, performanceDecline))
        } else 0.5f
        
        // 计算最佳挑战水平
        val optimalChallengeLevel = calculateOptimalChallengeLevel(learningHistory)
        
        // 分析疲劳模式
        val fatiguePattern = analyzeFatiguePattern(learningHistory)
        
        // 识别最佳表现时间
        val peakTimes = identifyPeakPerformanceTimes(learningHistory)
        
        return CognitiveProfile(
            workingMemoryCapacity = workingMemoryCapacity,
            processingSpeed = processingSpeed,
            attentionSpan = attentionSpan,
            cognitiveLoad = cognitiveLoad,
            optimalChallengeLevel = optimalChallengeLevel,
            fatiguePattern = fatiguePattern,
            peakPerformanceTime = peakTimes
        )
    }
    
    /**
     * 💪 分析动机画像
     */
    private suspend fun analyzeMotivationProfile(
        user: User,
        learningHistory: List<LearningRecord>
    ): MotivationProfile {
        
        // 分析内在动机（基于学习持续性和自主选择）
        val sessionFrequency = calculateSessionFrequency(learningHistory)
        val intrinsicMotivation = minOf(1f, sessionFrequency / 7f) // 假设每周7次为满分
        
        // 分析外在动机（基于成绩导向行为）
        val scoreImprovement = calculateScoreImprovement(learningHistory)
        val extrinsicMotivation = minOf(1f, scoreImprovement / 20f) // 20分提升为满分
        
        // 分析目标导向
        val goalOrientation = if (intrinsicMotivation > extrinsicMotivation) "掌握导向" else "表现导向"
        
        // 分析坚持度
        val persistenceLevel = calculatePersistenceLevel(learningHistory)
        
        // 分析挑战偏好
        val challengePreference = calculateChallengePreference(learningHistory)
        
        // 分析反馈敏感度
        val feedbackSensitivity = calculateFeedbackSensitivity(learningHistory)
        
        return MotivationProfile(
            intrinsicMotivation = intrinsicMotivation,
            extrinsicMotivation = extrinsicMotivation,
            goalOrientation = goalOrientation,
            persistenceLevel = persistenceLevel,
            challengePreference = challengePreference,
            feedbackSensitivity = feedbackSensitivity,
            socialLearningPreference = 0.5f // 需要更多数据支持
        )
    }
    
    /**
     * 📈 分析表现模式
     */
    private suspend fun analyzePerformancePattern(learningHistory: List<LearningRecord>): PerformancePattern {
        
        val scores = learningHistory.map { it.score }
        val times = learningHistory.map { it.duration.toInt() }
        
        // 计算一致性
        val consistencyScore = 1f - calculateVariability(scores.map { it.toDouble() }).toFloat() / 100f
        
        // 计算改进速度
        val improvementRate = calculateImprovementRate(scores)
        
        // 计算知识保持率（需要重复测试数据）
        val retentionRate = calculateRetentionRate(learningHistory)
        
        // 计算知识迁移能力
        val transferAbility = calculateTransferAbility(learningHistory)
        
        // 计算错误恢复率
        val errorRecoveryRate = calculateErrorRecoveryRate(learningHistory)
        
        // 计算最佳学习时长
        val optimalSessionLength = calculateOptimalSessionLength(times)
        
        // 识别表现周期
        val performanceCycles = identifyPerformanceCycles(scores)
        
        return PerformancePattern(
            consistencyScore = maxOf(0f, consistencyScore),
            improvementRate = improvementRate,
            retentionRate = retentionRate,
            transferAbility = transferAbility,
            errorRecoveryRate = errorRecoveryRate,
            optimalSessionLength = optimalSessionLength,
            performanceCycles = performanceCycles
        )
    }
    
    /**
     * 🎯 生成个性化策略
     */
    private suspend fun generatePersonalizationStrategy(
        learningStyle: LearningStyleProfile,
        knowledgeMap: KnowledgeMap,
        cognitiveProfile: CognitiveProfile,
        motivationProfile: MotivationProfile,
        performancePattern: PerformancePattern
    ): PersonalizationStrategy {
        
        // 推荐难度
        val recommendedDifficulty = when {
            cognitiveProfile.optimalChallengeLevel > 0.8f -> "高级"
            cognitiveProfile.optimalChallengeLevel > 0.6f -> "中级"
            cognitiveProfile.optimalChallengeLevel > 0.4f -> "基础"
            else -> "入门"
        }
        
        // 推荐题目类型
        val optimalQuestionTypes = when (learningStyle.primaryStyle) {
            "视觉型" -> listOf("图表题", "几何题", "选择题")
            "听觉型" -> listOf("概念题", "解释题", "讨论题")
            "读写型" -> listOf("文字题", "分析题", "论述题")
            "动觉型" -> listOf("实践题", "应用题", "实验题")
            else -> listOf("选择题", "填空题")
        }
        
        // 推荐学习主题
        val suggestedTopics = knowledgeMap.nextLearningTargets.take(3)
        
        // 学习路径调整
        val pathAdjustments = generatePathAdjustments(knowledgeMap, cognitiveProfile)
        
        // 动机策略
        val motivationalStrategies = generateMotivationalStrategies(motivationProfile)
        
        // 认知支持
        val cognitiveSupports = generateCognitiveSupports(cognitiveProfile)
        
        // 下一步行动建议
        val nextActions = generateNextActionRecommendations(
            learningStyle, knowledgeMap, cognitiveProfile, motivationProfile
        )
        
        return PersonalizationStrategy(
            recommendedDifficulty = recommendedDifficulty,
            optimalQuestionTypes = optimalQuestionTypes,
            suggestedTopics = suggestedTopics,
            learningPathAdjustments = pathAdjustments,
            motivationalStrategies = motivationalStrategies,
            cognitiveSupports = cognitiveSupports,
            nextActionRecommendations = nextActions
        )
    }
    
    // ==================== 辅助计算方法 ====================
    
    private fun calculateVariability(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        val mean = values.average()
        val variance = values.map { (it - mean).pow(2) }.average()
        return sqrt(variance)
    }
    
    private fun analyzeTimePreferences(learningHistory: List<LearningRecord>): Map<String, Float> {
        // 简化实现：分析学习时间分布
        return mapOf(
            "morning" to 0.3f,
            "afternoon" to 0.4f,
            "evening" to 0.3f
        )
    }
    
    private fun analyzeTypePreferences(learningHistory: List<LearningRecord>): Map<String, Float> {
        // 简化实现：分析学习风格偏好
        return mapOf(
            "visual" to 0.4f,
            "auditory" to 0.3f,
            "kinesthetic" to 0.3f
        )
    }
    
    private fun determineSecondaryStyle(primaryStyle: String, preferences: Map<String, Float>): String {
        return when (primaryStyle) {
            "视觉型" -> "读写型"
            "听觉型" -> "视觉型"
            "读写型" -> "听觉型"
            "动觉型" -> "视觉型"
            else -> "视觉型"
        }
    }
    
    private fun calculateSkillProgression(records: List<LearningRecord>): Map<String, Float> {
        // 计算各技能的进步情况
        return records.groupBy { it.topic }.mapValues { (_, topicRecords) ->
            if (topicRecords.size < 2) return@mapValues 0.5f
            val sorted = topicRecords.sortedBy { it.timestamp }
            val improvement = sorted.last().score - sorted.first().score
            minOf(1f, maxOf(0f, improvement / 100f))
        }
    }
    
    private fun calculateDifficultyComfort(records: List<LearningRecord>): Map<String, Float> {
        return records.groupBy { it.difficulty }.mapValues { (_, difficultyRecords) ->
            difficultyRecords.map { it.score }.average().toFloat() / 100f
        }
    }
    
    private fun calculateImprovementTrends(records: List<LearningRecord>): Map<String, Float> {
        return records.groupBy { it.topic }.mapValues { (_, topicRecords) ->
            if (topicRecords.size < 3) return@mapValues 0f
            val sorted = topicRecords.sortedBy { it.timestamp }
            val recent = sorted.takeLast(3).map { it.score }.average()
            val earlier = sorted.take(3).map { it.score }.average()
            ((recent - earlier) / 100f).toFloat()
        }
    }
    
    private fun buildConceptConnections(learningHistory: List<LearningRecord>): Map<String, List<String>> {
        // 简化实现：基于学习顺序建立概念连接
        val subjects = learningHistory.map { it.subject }.distinct()
        return subjects.associateWith { subject ->
            learningHistory.filter { it.subject == subject }
                .map { it.topic }
                .distinct()
                .take(5)
        }
    }
    
    private fun generateOptimalLearningSequence(subjectMastery: Map<String, SubjectMastery>): List<String> {
        // 基于掌握度排序，优先学习基础较好的科目
        return subjectMastery.entries
            .sortedByDescending { it.value.overallMastery }
            .map { it.key }
    }
    
    private fun generateNextLearningTargets(subjectMastery: Map<String, SubjectMastery>): List<String> {
        val targets = mutableListOf<String>()
        
        subjectMastery.forEach { (subject, mastery) ->
            // 找出掌握度中等的主题作为下一步目标
            val nextTopics = mastery.topicMastery
                .filter { it.value in 0.4f..0.8f }
                .keys.take(2)
            targets.addAll(nextTopics)
        }
        
        return targets.take(5)
    }
    
    private fun calculateAttentionSpan(sessionLengths: List<Int>): Float {
        if (sessionLengths.isEmpty()) return 0.5f
        val avgLength = sessionLengths.average()
        return when {
            avgLength > 180 -> 0.9f
            avgLength > 120 -> 0.7f
            avgLength > 60 -> 0.5f
            else -> 0.3f
        }
    }
    
    private fun calculatePerformanceDecline(recentRecords: List<LearningRecord>): Float {
        if (recentRecords.size < 3) return 0.3f
        
        val scores = recentRecords.map { it.score }
        val firstHalf = scores.take(scores.size / 2).average()
        val secondHalf = scores.takeLast(scores.size / 2).average()
        
        return maxOf(0f, (firstHalf - secondHalf).toFloat() / 100f)
    }
    
    private fun calculateOptimalChallengeLevel(learningHistory: List<LearningRecord>): Float {
        // 分析在不同难度下的表现，找出最佳挑战水平
        val difficultyPerformance = learningHistory.groupBy { it.difficulty }
            .mapValues { (_, records) -> records.map { it.score }.average() }
        
        return when {
            difficultyPerformance.getOrDefault("挑战", 0.0) > 70 -> 0.9f
            difficultyPerformance.getOrDefault("高级", 0.0) > 75 -> 0.8f
            difficultyPerformance.getOrDefault("中级", 0.0) > 80 -> 0.6f
            else -> 0.4f
        }
    }
    
    private fun analyzeFatiguePattern(learningHistory: List<LearningRecord>): List<TimeBasedMetric> {
        // 简化实现：分析不同时间段的疲劳模式
        return listOf(
            TimeBasedMetric("早晨", 0.2f),
            TimeBasedMetric("下午", 0.5f),
            TimeBasedMetric("晚上", 0.7f)
        )
    }
    
    private fun identifyPeakPerformanceTimes(learningHistory: List<LearningRecord>): List<String> {
        // 简化实现：识别最佳表现时间段
        return listOf("上午9-11点", "下午2-4点")
    }
    
    private fun calculateSessionFrequency(learningHistory: List<LearningRecord>): Float {
        if (learningHistory.isEmpty()) return 0f
        
        val daySpan = (learningHistory.maxOf { it.timestamp } - learningHistory.minOf { it.timestamp }) / (24 * 60 * 60 * 1000L)
        return if (daySpan > 0) learningHistory.size.toFloat() / daySpan.toFloat() else 0f
    }
    
    private fun calculateScoreImprovement(learningHistory: List<LearningRecord>): Float {
        if (learningHistory.size < 2) return 0f
        
        val sorted = learningHistory.sortedBy { it.timestamp }
        val recentAvg = sorted.takeLast(5).map { it.score.toDouble() }.average()
        val earlyAvg = sorted.take(5).map { it.score.toDouble() }.average()
        
        return (recentAvg - earlyAvg).toFloat()
    }
    
    private fun calculatePersistenceLevel(learningHistory: List<LearningRecord>): Float {
        // 基于学习连续性和困难情况下的坚持程度
        val difficultSessions = learningHistory.filter { it.score < 60 }
        val continuedAfterDifficult = difficultSessions.count { record ->
            learningHistory.any { it.timestamp > record.timestamp && it.timestamp < record.timestamp + 24 * 60 * 60 * 1000L }
        }
        
        return if (difficultSessions.isNotEmpty()) {
            continuedAfterDifficult.toFloat() / difficultSessions.size
        } else 0.7f
    }
    
    private fun calculateChallengePreference(learningHistory: List<LearningRecord>): Float {
        val challengingQuestions = learningHistory.filter { it.difficulty in listOf("高级", "挑战") }
        return challengingQuestions.size.toFloat() / learningHistory.size.coerceAtLeast(1)
    }
    
    private fun calculateFeedbackSensitivity(learningHistory: List<LearningRecord>): Float {
        // 简化实现：基于成绩波动性评估反馈敏感度
        val scores = learningHistory.map { it.score.toDouble() }
        val variability = calculateVariability(scores)
        return minOf(1f, variability.toFloat() / 50f)
    }
    
    private fun calculateImprovementRate(scores: List<Float>): Float {
        if (scores.size < 2) return 0f
        
        val improvements = scores.zipWithNext { a, b -> b - a }
        val positiveImprovements = improvements.filter { it > 0 }
        return if (positiveImprovements.isNotEmpty()) {
            positiveImprovements.average().toFloat() / 100f
        } else 0f
    }
    
    private fun calculateRetentionRate(learningHistory: List<LearningRecord>): Float {
        // 简化实现：基于重复主题的表现评估保持率
        val repeatedTopics = learningHistory.groupBy { it.topic }
            .filter { it.value.size > 1 }
        
        if (repeatedTopics.isEmpty()) return 0.7f
        
        val retentionScores = repeatedTopics.values.map { records ->
            val sorted = records.sortedBy { it.timestamp }
            if (sorted.size < 2) return@map 0.7f
            val retention = sorted.last().score / sorted.first().score.coerceAtLeast(1f)
            minOf(1f, retention)
        }
        
        return if (retentionScores.isNotEmpty()) {
            retentionScores.average().toFloat()
        } else 0.7f
    }
    
    private fun calculateTransferAbility(learningHistory: List<LearningRecord>): Float {
        // 评估跨主题的知识迁移能力
        val subjects = learningHistory.groupBy { it.subject }
        if (subjects.size < 2) return 0.5f
        
        val crossSubjectPerformance = subjects.values.map { records ->
            records.map { it.score.toDouble() }.average()
        }
        
        val consistencyAcrossSubjects = 1f - calculateVariability(crossSubjectPerformance).toFloat() / 100f
        return maxOf(0f, consistencyAcrossSubjects)
    }
    
    private fun calculateErrorRecoveryRate(learningHistory: List<LearningRecord>): Float {
        // 计算错误后的恢复能力
        val lowScoreSessions = learningHistory.filter { it.score < 60 }
        if (lowScoreSessions.isEmpty()) return 0.8f
        
        val recoveries = lowScoreSessions.count { lowScore ->
            val nextSession = learningHistory
                .filter { it.timestamp > lowScore.timestamp }
                .minByOrNull { it.timestamp }
            (nextSession?.score ?: 0f) > lowScore.score + 10f
        }
        
        return recoveries.toFloat() / lowScoreSessions.size
    }
    
    private fun calculateOptimalSessionLength(sessionLengths: List<Int>): Int {
        if (sessionLengths.isEmpty()) return 120
        
        // 找出表现最好时的会话长度
        return sessionLengths.sorted()[sessionLengths.size / 2] // 中位数
    }
    
    private fun identifyPerformanceCycles(scores: List<Float>): List<PerformanceCycle> {
        // 简化实现：识别表现周期模式
        return listOf(
            PerformanceCycle("周期性波动", 7, 0.6f),
            PerformanceCycle("渐进提升", 14, 0.8f)
        )
    }
    
    private fun generatePathAdjustments(knowledgeMap: KnowledgeMap, cognitiveProfile: CognitiveProfile): List<String> {
        val adjustments = mutableListOf<String>()
        
        if (cognitiveProfile.cognitiveLoad > 0.7f) {
            adjustments.add("降低学习强度，增加休息时间")
        }
        
        if (cognitiveProfile.attentionSpan < 0.5f) {
            adjustments.add("缩短单次学习时间，增加学习频次")
        }
        
        if (knowledgeMap.improvementAreas.isNotEmpty()) {
            adjustments.add("重点关注薄弱科目：${knowledgeMap.improvementAreas.joinToString("、")}")
        }
        
        return adjustments
    }
    
    private fun generateMotivationalStrategies(motivationProfile: MotivationProfile): List<String> {
        val strategies = mutableListOf<String>()
        
        if (motivationProfile.intrinsicMotivation > 0.6f) {
            strategies.add("提供更多探索性学习机会")
            strategies.add("设置个人兴趣相关的学习目标")
        }
        
        if (motivationProfile.extrinsicMotivation > 0.6f) {
            strategies.add("设置明确的成就目标和奖励")
            strategies.add("提供及时的进度反馈")
        }
        
        if (motivationProfile.challengePreference > 0.7f) {
            strategies.add("逐步提高题目难度")
            strategies.add("引入竞争性学习元素")
        }
        
        return strategies
    }
    
    private fun generateCognitiveSupports(cognitiveProfile: CognitiveProfile): List<String> {
        val supports = mutableListOf<String>()
        
        if (cognitiveProfile.workingMemoryCapacity < 0.5f) {
            supports.add("提供记忆辅助工具和策略")
            supports.add("分解复杂问题为简单步骤")
        }
        
        if (cognitiveProfile.processingSpeed < 0.5f) {
            supports.add("给予充分的思考时间")
            supports.add("提供解题步骤提示")
        }
        
        if (cognitiveProfile.attentionSpan < 0.5f) {
            supports.add("使用多媒体和互动元素")
            supports.add("设置注意力提醒机制")
        }
        
        return supports
    }
    
    private fun generateNextActionRecommendations(
        learningStyle: LearningStyleProfile,
        knowledgeMap: KnowledgeMap,
        cognitiveProfile: CognitiveProfile,
        motivationProfile: MotivationProfile
    ): List<String> {
        val recommendations = mutableListOf<String>()
        
        // 基于知识图谱的建议
        if (knowledgeMap.nextLearningTargets.isNotEmpty()) {
            recommendations.add("开始学习：${knowledgeMap.nextLearningTargets.first()}")
        }
        
        // 基于认知状态的建议
        if (cognitiveProfile.cognitiveLoad > 0.7f) {
            recommendations.add("建议休息15分钟后继续学习")
        } else {
            recommendations.add("当前状态良好，可以继续挑战性学习")
        }
        
        // 基于学习风格的建议
        when (learningStyle.primaryStyle) {
            "视觉型" -> recommendations.add("尝试使用图表和思维导图学习")
            "听觉型" -> recommendations.add("考虑使用音频材料或讨论学习")
            "动觉型" -> recommendations.add("寻找实践性强的学习活动")
            "读写型" -> recommendations.add("多做笔记和文字总结")
        }
        
        return recommendations.take(5)
    }
    
    /**
     * 生成基础画像（用于新用户）
     */
    private fun generateBasicProfile(user: User): LearnerProfile {
        return LearnerProfile(
            userId = user.id,
            learningStyle = LearningStyleProfile(
                primaryStyle = user.learningStyle,
                secondaryStyle = "视觉型",
                processingPreference = "顺序型",
                thinkingStyle = "分析型",
                learningPace = "稳健型",
                confidenceScore = 0.5f,
                evidenceBasis = listOf("基于用户注册信息的初始设置")
            ),
            knowledgeMap = KnowledgeMap(
                subjectMastery = emptyMap(),
                conceptConnections = emptyMap(),
                learningSequence = emptyList(),
                strengthAreas = emptyList(),
                improvementAreas = emptyList(),
                nextLearningTargets = listOf("基础数学", "基础物理", "基础语文")
            ),
            cognitiveProfile = CognitiveProfile(
                workingMemoryCapacity = 0.6f,
                processingSpeed = 0.6f,
                attentionSpan = 0.6f,
                cognitiveLoad = 0.3f,
                optimalChallengeLevel = 0.5f,
                fatiguePattern = emptyList(),
                peakPerformanceTime = listOf("上午", "下午")
            ),
            motivationProfile = MotivationProfile(
                intrinsicMotivation = 0.6f,
                extrinsicMotivation = 0.5f,
                goalOrientation = "掌握导向",
                persistenceLevel = 0.6f,
                challengePreference = 0.5f,
                feedbackSensitivity = 0.5f,
                socialLearningPreference = 0.5f
            ),
            performancePattern = PerformancePattern(
                consistencyScore = 0.6f,
                improvementRate = 0.1f,
                retentionRate = 0.7f,
                transferAbility = 0.5f,
                errorRecoveryRate = 0.6f,
                optimalSessionLength = 120,
                performanceCycles = emptyList()
            ),
            personalizedStrategy = PersonalizationStrategy(
                recommendedDifficulty = "基础",
                optimalQuestionTypes = listOf("选择题", "填空题"),
                suggestedTopics = listOf("基础数学"),
                learningPathAdjustments = listOf("从基础开始，循序渐进"),
                motivationalStrategies = listOf("设置小目标，及时鼓励"),
                cognitiveSupports = listOf("提供充分指导和解释"),
                nextActionRecommendations = listOf("开始基础学习，建立信心")
            )
        )
    }
}
