package com.example.educationapp.service

import android.content.Context
import com.example.educationapp.data.*
import com.example.educationapp.data.dao.LearningProgressDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 📊 学习进度追踪器 - 智能收集和分析用户学习数据
 */
class LearningProgressTracker private constructor(
    private val context: Context,
    private val progressDao: LearningProgressDao
) {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    companion object {
        @Volatile
        private var INSTANCE: LearningProgressTracker? = null
        
        fun getInstance(context: Context, progressDao: LearningProgressDao): LearningProgressTracker {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LearningProgressTracker(context, progressDao).also { INSTANCE = it }
            }
        }
    }
    
    // === 数据收集入口 ===
    
    /**
     * 🎯 记录答题行为
     */
    fun recordQuestionAnswered(
        userId: Long,
        subject: String,
        knowledgePoint: String,
        isCorrect: Boolean,
        answerTime: Long,
        difficulty: String,
        questionSource: String = "练习"
    ) {
        scope.launch {
            // 更新知识点掌握度
            progressDao.updateKnowledgePointMastery(
                userId = userId,
                knowledgePoint = knowledgePoint,
                subject = subject,
                isCorrect = isCorrect,
                studyTime = answerTime,
                source = questionSource
            )
            
            // 记录学习行为
            progressDao.insertBehavior(
                LearningBehavior(
                    userId = userId,
                    behaviorType = BehaviorType.QUESTION_ANSWERED,
                    content = knowledgePoint,
                    result = if (isCorrect) "正确" else "错误",
                    duration = answerTime,
                    context = "科目:$subject,难度:$difficulty,来源:$questionSource"
                )
            )
            
            // 更新每日统计
            updateDailyStatistics(userId)
        }
    }
    
    /**
     * 💬 记录AI对话
     */
    fun recordAIInteraction(
        userId: Long,
        question: String,
        aiResponse: String,
        interactionTime: Long,
        knowledgePoints: List<String> = emptyList()
    ) {
        scope.launch {
            progressDao.insertBehavior(
                LearningBehavior(
                    userId = userId,
                    behaviorType = BehaviorType.AI_CHAT,
                    content = question,
                    result = "AI回复",
                    duration = interactionTime,
                    context = "涉及知识点:${knowledgePoints.joinToString(",")}"
                )
            )
            
            // 如果对话涉及特定知识点，增加学习时间
            knowledgePoints.forEach { point ->
                val existing = progressDao.getKnowledgePointProgress(userId, point)
                existing?.let { progress ->
                    val updated = progress.copy(
                        studyTime = progress.studyTime + interactionTime,
                        lastStudyTime = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    progressDao.updateProgress(updated)
                }
            }
        }
    }
    
    /**
     * 🕸️ 记录知识图谱探索
     */
    fun recordKnowledgeGraphExploration(
        userId: Long,
        exploredNodes: List<String>,
        sessionDuration: Long,
        interactions: Int
    ) {
        scope.launch {
            progressDao.insertBehavior(
                LearningBehavior(
                    userId = userId,
                    behaviorType = BehaviorType.KNOWLEDGE_GRAPH_VIEW,
                    content = "探索节点:${exploredNodes.joinToString(",")}",
                    result = "交互$interactions 次",
                    duration = sessionDuration,
                    context = "节点数量:${exploredNodes.size}"
                )
            )
        }
    }
    
    /**
     * 📚 记录学习会话
     */
    fun startStudySession(userId: Long, subject: String) {
        scope.launch {
            progressDao.insertBehavior(
                LearningBehavior(
                    userId = userId,
                    behaviorType = BehaviorType.STUDY_SESSION_START,
                    content = subject,
                    result = "开始学习",
                    duration = 0,
                    context = "学习科目:$subject"
                )
            )
        }
    }
    
    fun endStudySession(userId: Long, subject: String, totalTime: Long) {
        scope.launch {
            progressDao.insertBehavior(
                LearningBehavior(
                    userId = userId,
                    behaviorType = BehaviorType.STUDY_SESSION_END,
                    content = subject,
                    result = "结束学习",
                    duration = totalTime,
                    context = "总时长:${totalTime}秒"
                )
            )
            
            updateDailyStatistics(userId)
        }
    }
    
    // === 数据分析 ===
    
    /**
     * 📈 获取用户完整学习档案
     */
    suspend fun getUserLearningProfile(userId: Long): UserLearningProfile {
        val allProgress = progressDao.getUserProgress(userId)
        val recentBehaviors = progressDao.getRecentBehaviors(userId)
        val subjectSummary = progressDao.getSubjectMasteryOverview(userId)
        val recentStats = progressDao.getRecentStatistics(userId)
        
        return UserLearningProfile(
            userId = userId,
            totalKnowledgePoints = allProgress.size,
            averageMastery = allProgress.map { it.masteryLevel }.average().toFloat(),
            totalStudyTime = allProgress.sumOf { it.studyTime },
            subjectMastery = subjectSummary.associate { it.subject to it.avgMastery },
            weakPoints = progressDao.getWeakestKnowledgePoints(userId).map { 
                WeakPoint(it.knowledgePoint, it.masteryLevel, it.subject) 
            },
            masteredPoints = progressDao.getMasteredKnowledgePoints(userId).map { 
                MasteredPoint(it.knowledgePoint, it.masteryLevel, it.subject) 
            },
            recentActivity = calculateRecentActivity(recentBehaviors),
            learningStreak = calculateLearningStreak(recentStats),
            preferredSubjects = calculatePreferredSubjects(recentBehaviors),
            studyPatterns = analyzeStudyPatterns(recentBehaviors)
        )
    }
    
    /**
     * 🎯 为知识图谱生成真实进度数据
     */
    suspend fun getKnowledgeGraphData(userId: Long, subject: String): KnowledgeGraphProgressData {
        val subjectProgress = progressDao.getSubjectProgress(userId, subject)
        val recentActivity = progressDao.getRecentStudyActivity(
            userId, subject, System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
        )
        
        // 构建知识点网络
        val knowledgeNetwork = buildKnowledgeNetwork(subject, subjectProgress)
        
        return KnowledgeGraphProgressData(
            subject = subject,
            nodes = knowledgeNetwork.map { (point, progress) ->
                KnowledgeNodeData(
                    id = point,
                    name = point,
                    masteryLevel = progress?.masteryLevel ?: 0f,
                    studyTime = progress?.studyTime ?: 0L,
                    lastStudied = progress?.lastStudyTime ?: 0L,
                    difficulty = progress?.difficultyLevel ?: "未学习",
                    status = determineNodeStatus(progress?.masteryLevel ?: 0f),
                    prerequisites = getPrerequisites(point),
                    applications = getApplications(point)
                )
            },
            overallProgress = subjectProgress.map { it.masteryLevel }.average().toFloat(),
            recommendedNext = getRecommendedNextTopics(userId, subject, subjectProgress),
            learningPath = generateOptimalLearningPath(subjectProgress)
        )
    }
    
    // === 私有辅助方法 ===
    
    private suspend fun updateDailyStatistics(userId: Long) {
        val today = dateFormat.format(Date())
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val todayBehaviors = progressDao.getRecentBehaviors(userId)
            .filter { it.timestamp >= todayStart }
        
        val totalTime = todayBehaviors.sumOf { it.duration }
        val questionsAnswered = todayBehaviors.count { it.behaviorType == BehaviorType.QUESTION_ANSWERED }
        val correctAnswers = todayBehaviors.count { 
            it.behaviorType == BehaviorType.QUESTION_ANSWERED && it.result == "正确" 
        }
        val correctRate = if (questionsAnswered > 0) correctAnswers.toFloat() / questionsAnswered else 0f
        
        val statistics = LearningStatistics(
            userId = userId,
            date = today,
            totalStudyTime = totalTime,
            questionsAnswered = questionsAnswered,
            correctRate = correctRate,
            subjectsStudied = todayBehaviors.mapNotNull { 
                extractSubjectFromContext(it.context) 
            }.distinct().joinToString(","),
            aiInteractions = todayBehaviors.count { it.behaviorType == BehaviorType.AI_CHAT },
            knowledgePointsLearned = todayBehaviors.mapNotNull { 
                if (it.behaviorType == BehaviorType.QUESTION_ANSWERED) it.content else null 
            }.distinct().size
        )
        
        progressDao.insertStatistics(statistics)
    }
    
    private fun buildKnowledgeNetwork(subject: String, progress: List<LearningProgress>): Map<String, LearningProgress?> {
        // 根据学科构建知识点网络
        val baseKnowledge = when (subject) {
            "数学", "高等数学" -> listOf(
                "函数基础", "极限概念", "导数", "积分", "连续性", 
                "数列", "级数", "实际应用", "微分方程", "线性代数"
            )
            "英语" -> listOf(
                "基础语法", "词汇积累", "阅读理解", "写作技巧", "听力训练",
                "口语表达", "语法进阶", "文学赏析", "商务英语", "翻译技巧"
            )
            "物理" -> listOf(
                "力学基础", "电磁学", "热力学", "光学", "波动",
                "量子物理", "相对论", "实验技能", "工程应用", "现代物理"
            )
            else -> progress.map { it.knowledgePoint }.distinct()
        }
        
        return baseKnowledge.associateWith { point ->
            progress.find { it.knowledgePoint == point }
        }
    }
    
    private fun determineNodeStatus(masteryLevel: Float): String {
        return when {
            masteryLevel >= 0.8f -> "已掌握"
            masteryLevel >= 0.5f -> "学习中" 
            masteryLevel >= 0.2f -> "待学习"
            else -> "未开始"
        }
    }
    
    private fun getPrerequisites(knowledgePoint: String): List<String> {
        // 简化的前置关系映射
        return when (knowledgePoint) {
            "极限概念" -> listOf("函数基础")
            "导数" -> listOf("极限概念")
            "积分" -> listOf("导数")
            "连续性" -> listOf("极限概念")
            "微分方程" -> listOf("导数", "积分")
            "实际应用" -> listOf("导数", "积分")
            else -> emptyList()
        }
    }
    
    private fun getApplications(knowledgePoint: String): List<String> {
        return when (knowledgePoint) {
            "函数基础" -> listOf("极限概念", "连续性")
            "极限概念" -> listOf("导数", "连续性")
            "导数" -> listOf("积分", "实际应用")
            "积分" -> listOf("实际应用", "微分方程")
            else -> emptyList()
        }
    }
    
    private fun calculateRecentActivity(behaviors: List<LearningBehavior>): Float {
        val recentBehaviors = behaviors.filter { 
            System.currentTimeMillis() - it.timestamp < 7 * 24 * 60 * 60 * 1000L 
        }
        return recentBehaviors.size.toFloat() / 7f // 每天平均活动次数
    }
    
    private fun calculateLearningStreak(statistics: List<LearningStatistics>): Int {
        return statistics.takeWhile { it.totalStudyTime > 0 }.size
    }
    
    private fun calculatePreferredSubjects(behaviors: List<LearningBehavior>): List<String> {
        return behaviors.mapNotNull { extractSubjectFromContext(it.context) }
            .groupingBy { it }
            .eachCount()
            .toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }
    }
    
    private fun analyzeStudyPatterns(behaviors: List<LearningBehavior>): List<String> {
        val patterns = mutableListOf<String>()
        
        // 分析学习时间模式
        val hourCounts = behaviors.groupBy { 
            Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(Calendar.HOUR_OF_DAY)
        }
        val peakHour = hourCounts.maxByOrNull { it.value.size }?.key
        peakHour?.let { patterns.add("偏好在${it}点学习") }
        
        // 分析学习频率
        val avgSessionDuration = behaviors.map { it.duration }.average()
        when {
            avgSessionDuration > 30 * 60 -> patterns.add("喜欢长时间深度学习")
            avgSessionDuration > 15 * 60 -> patterns.add("适中时长专注学习")
            else -> patterns.add("碎片化时间学习")
        }
        
        return patterns
    }
    
    private fun extractSubjectFromContext(context: String): String? {
        return context.split(",").find { it.startsWith("科目:") }?.substringAfter(":")
    }
    
    private fun getRecommendedNextTopics(
        userId: Long, 
        subject: String, 
        progress: List<LearningProgress>
    ): List<String> {
        val masteredPoints = progress.filter { it.masteryLevel >= 0.7f }.map { it.knowledgePoint }
        val allKnowledge = buildKnowledgeNetwork(subject, progress).keys
        
        return allKnowledge.filter { point ->
            val prerequisites = getPrerequisites(point)
            prerequisites.isEmpty() || prerequisites.all { it in masteredPoints }
        }.filter { it !in masteredPoints }.take(3)
    }
    
    private fun generateOptimalLearningPath(progress: List<LearningProgress>): List<String> {
        // 基于掌握度和前置关系生成最优学习路径
        val sortedByMastery = progress.sortedBy { it.masteryLevel }
        return sortedByMastery.take(5).map { it.knowledgePoint }
    }
}

// === 数据类定义 ===

data class UserLearningProfile(
    val userId: Long,
    val totalKnowledgePoints: Int,
    val averageMastery: Float,
    val totalStudyTime: Long,
    val subjectMastery: Map<String, Float>,
    val weakPoints: List<WeakPoint>,
    val masteredPoints: List<MasteredPoint>,
    val recentActivity: Float,
    val learningStreak: Int,
    val preferredSubjects: List<String>,
    val studyPatterns: List<String>
)

data class WeakPoint(
    val knowledgePoint: String,
    val masteryLevel: Float,
    val subject: String
)

data class MasteredPoint(
    val knowledgePoint: String,
    val masteryLevel: Float,
    val subject: String
)

data class KnowledgeGraphProgressData(
    val subject: String,
    val nodes: List<KnowledgeNodeData>,
    val overallProgress: Float,
    val recommendedNext: List<String>,
    val learningPath: List<String>
)

data class KnowledgeNodeData(
    val id: String,
    val name: String,
    val masteryLevel: Float,
    val studyTime: Long,
    val lastStudied: Long,
    val difficulty: String,
    val status: String,
    val prerequisites: List<String>,
    val applications: List<String>
)

