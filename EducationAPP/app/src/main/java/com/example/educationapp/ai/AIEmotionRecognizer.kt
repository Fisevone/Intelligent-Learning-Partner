package com.example.educationapp.ai

import android.util.Log
import com.example.educationapp.data.LearningRecord
import com.example.educationapp.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * AI情绪与专注度识别引擎
 * 创新功能：通过学习行为模式分析学生的情绪状态和专注度
 */
class AIEmotionRecognizer {
    
    private val zhipuAIService = ZhipuAIService()
    
    companion object {
        private const val TAG = "AIEmotionRecognizer"
    }
    
    /**
     * 行为事件数据类
     */
    data class BehaviorEvent(
        val timestamp: Long,
        val eventType: String, // "click", "scroll", "answer", "pause"
        val duration: Long = 0,
        val accuracy: Float? = null,
        val hesitationTime: Long = 0
    )
    
    /**
     * 情绪状态结果
     */
    data class EmotionalState(
        val focusLevel: Int, // 1-10
        val stressLevel: Int, // 1-10
        val confidenceLevel: Int, // 1-10
        val fatigueLevel: Int, // 1-10
        val emotionalState: String, // "积极", "中性", "消极", "焦虑", "困惑", "疲劳"
        val suggestions: List<String>,
        val interventionNeeded: Boolean,
        val analysisReason: String
    )
    
    /**
     * 核心创新功能：实时分析学习情绪状态
     */
    suspend fun analyzeRealTimeEmotion(
        user: User,
        recentBehaviors: List<BehaviorEvent>,
        currentLearningSession: LearningRecord? = null
    ): Result<EmotionalState> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "开始AI情绪识别分析...")
            
            // 1. 基础行为模式分析
            val behaviorAnalysis = analyzeBehaviorPatterns(recentBehaviors)
            
            // 2. 构建AI分析提示
            val analysisPrompt = buildEmotionAnalysisPrompt(user, behaviorAnalysis, currentLearningSession)
            
            // 3. 调用AI进行深度分析
            val aiAnalysisResult = zhipuAIService.sendChatMessage(analysisPrompt, user)
            
            aiAnalysisResult.fold(
                onSuccess = { aiResponse ->
                    val emotionalState = parseEmotionalState(aiResponse, behaviorAnalysis)
                    Log.d(TAG, "AI情绪识别完成: ${emotionalState.emotionalState}")
                    Result.success(emotionalState)
                },
                onFailure = { error ->
                    Log.e(TAG, "AI情绪识别失败", error)
                    // 降级到基础分析
                    val fallbackState = createFallbackEmotionalState(behaviorAnalysis)
                    Result.success(fallbackState)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "情绪识别异常", e)
            Result.failure(e)
        }
    }
    
    /**
     * 创新功能：预测学习状态变化趋势
     */
    suspend fun predictEmotionalTrend(
        user: User,
        historicalStates: List<EmotionalState>,
        currentState: EmotionalState
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val trendPrompt = buildTrendPredictionPrompt(user, historicalStates, currentState)
            val result = zhipuAIService.sendChatMessage(trendPrompt, user)
            
            result.fold(
                onSuccess = { prediction ->
                    Log.d(TAG, "情绪趋势预测完成")
                    Result.success(prediction)
                },
                onFailure = { error ->
                    Log.w(TAG, "趋势预测失败，使用默认分析", error)
                    Result.success("基于当前状态，建议保持当前学习节奏")
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "趋势预测异常", e)
            Result.failure(e)
        }
    }
    
    /**
     * 创新功能：智能学习干预建议
     */
    suspend fun generateInterventionSuggestions(
        user: User,
        emotionalState: EmotionalState,
        learningContext: String
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val interventionPrompt = buildInterventionPrompt(user, emotionalState, learningContext)
            val result = zhipuAIService.sendChatMessage(interventionPrompt, user)
            
            result.fold(
                onSuccess = { suggestions ->
                    val interventionList = parseInterventionSuggestions(suggestions)
                    Log.d(TAG, "干预建议生成完成: ${interventionList.size}条")
                    Result.success(interventionList)
                },
                onFailure = { error ->
                    Log.w(TAG, "干预建议生成失败", error)
                    Result.success(getDefaultInterventions(emotionalState))
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "干预建议异常", e)
            Result.failure(e)
        }
    }
    
    // 私有方法实现
    private fun analyzeBehaviorPatterns(behaviors: List<BehaviorEvent>): BehaviorAnalysis {
        if (behaviors.isEmpty()) {
            return BehaviorAnalysis(
                clickFrequency = 0.0,
                averageHesitation = 0L,
                accuracyTrend = 0.0,
                activityLevel = 0.0,
                consistencyScore = 0.0
            )
        }
        
        val timeSpan = behaviors.maxOf { it.timestamp } - behaviors.minOf { it.timestamp }
        val clickEvents = behaviors.filter { it.eventType == "click" }
        val answerEvents = behaviors.filter { it.eventType == "answer" && it.accuracy != null }
        
        return BehaviorAnalysis(
            clickFrequency = if (timeSpan > 0) clickEvents.size.toDouble() / (timeSpan / 1000.0) else 0.0,
            averageHesitation = behaviors.mapNotNull { if (it.hesitationTime > 0) it.hesitationTime else null }.average().toLong(),
            accuracyTrend = if (answerEvents.isNotEmpty()) {
                val recent = answerEvents.takeLast(5).mapNotNull { it.accuracy }.average()
                val earlier = answerEvents.take(5).mapNotNull { it.accuracy }.average()
                recent - earlier
            } else 0.0,
            activityLevel = calculateActivityLevel(behaviors),
            consistencyScore = calculateConsistencyScore(behaviors)
        )
    }
    
    private fun calculateActivityLevel(behaviors: List<BehaviorEvent>): Double {
        if (behaviors.isEmpty()) return 0.0
        
        val totalTime = behaviors.maxOf { it.timestamp } - behaviors.minOf { it.timestamp }
        val activeTime = behaviors.sumOf { it.duration }
        
        return if (totalTime > 0) activeTime.toDouble() / totalTime.toDouble() else 0.0
    }
    
    private fun calculateConsistencyScore(behaviors: List<BehaviorEvent>): Double {
        if (behaviors.size < 3) return 0.5
        
        val intervals = mutableListOf<Long>()
        for (i in 1 until behaviors.size) {
            intervals.add(behaviors[i].timestamp - behaviors[i-1].timestamp)
        }
        
        val mean = intervals.average()
        val variance = intervals.map { (it - mean) * (it - mean) }.average()
        val standardDeviation = sqrt(variance)
        
        // 标准差越小，一致性越高
        return (1.0 / (1.0 + standardDeviation / mean)).coerceIn(0.0, 1.0)
    }
    
    private fun buildEmotionAnalysisPrompt(
        user: User,
        behaviorAnalysis: BehaviorAnalysis,
        currentSession: LearningRecord?
    ): String {
        return """
            作为AI情绪识别专家，请分析以下学生的实时学习状态：
            
            学生信息：
            - 姓名: ${user.name}
            - 年级: ${user.grade}
            - 学习风格: ${user.learningStyle}
            
            行为数据分析：
            - 点击频率: ${String.format("%.2f", behaviorAnalysis.clickFrequency)} 次/秒
            - 平均犹豫时间: ${behaviorAnalysis.averageHesitation} 毫秒
            - 准确率趋势: ${String.format("%.2f", behaviorAnalysis.accuracyTrend * 100)}%
            - 活跃度: ${String.format("%.2f", behaviorAnalysis.activityLevel * 100)}%
            - 行为一致性: ${String.format("%.2f", behaviorAnalysis.consistencyScore * 100)}%
            
            ${currentSession?.let { 
                "当前学习会话：科目=${it.subject}, 主题=${it.topic}, 当前得分=${it.score}"
            } ?: ""}
            
            请基于这些数据分析学生的情绪状态，返回以下格式：
            专注度：[1-10]
            压力水平：[1-10] 
            自信程度：[1-10]
            疲劳程度：[1-10]
            情绪状态：[积极/中性/消极/焦虑/困惑/疲劳]
            分析原因：[简要说明判断依据]
            建议：[3-4条具体建议]
            是否需要干预：[是/否]
        """.trimIndent()
    }
    
    private fun buildTrendPredictionPrompt(
        user: User,
        historicalStates: List<EmotionalState>,
        currentState: EmotionalState
    ): String {
        val stateHistory = historicalStates.takeLast(5).joinToString("\n") { state ->
            "情绪：${state.emotionalState}, 专注度：${state.focusLevel}, 压力：${state.stressLevel}"
        }
        
        return """
            作为AI学习状态预测专家，请分析学生的情绪变化趋势：
            
            学生：${user.name}
            
            历史状态：
            $stateHistory
            
            当前状态：
            情绪：${currentState.emotionalState}
            专注度：${currentState.focusLevel}
            压力水平：${currentState.stressLevel}
            疲劳程度：${currentState.fatigueLevel}
            
            请预测接下来15分钟内的学习状态变化趋势，并给出建议。
            回答要简洁，不超过100字。
        """.trimIndent()
    }
    
    private fun buildInterventionPrompt(
        user: User,
        emotionalState: EmotionalState,
        learningContext: String
    ): String {
        return """
            作为AI学习干预专家，请为以下情况提供智能干预建议：
            
            学生：${user.name} (${user.grade})
            学习场景：$learningContext
            
            当前状态：
            - 情绪：${emotionalState.emotionalState}
            - 专注度：${emotionalState.focusLevel}/10
            - 压力水平：${emotionalState.stressLevel}/10
            - 疲劳程度：${emotionalState.fatigueLevel}/10
            
            请提供3-5个具体的干预建议，格式为：
            - 建议1
            - 建议2
            - 建议3
            
            要求：建议要具体、可执行、适合当前情绪状态。
        """.trimIndent()
    }
    
    private fun parseEmotionalState(aiResponse: String, behaviorAnalysis: BehaviorAnalysis): EmotionalState {
        return try {
            val lines = aiResponse.lines()
            val focusLevel = extractNumber(lines, "专注度", 5)
            val stressLevel = extractNumber(lines, "压力水平", 5)
            val confidenceLevel = extractNumber(lines, "自信程度", 5)
            val fatigueLevel = extractNumber(lines, "疲劳程度", 5)
            val emotionalState = extractValue(lines, "情绪状态", "中性")
            val analysisReason = extractValue(lines, "分析原因", "基于行为数据分析")
            val suggestions = extractSuggestions(lines)
            val interventionNeeded = extractValue(lines, "是否需要干预", "否").contains("是")
            
            EmotionalState(
                focusLevel = focusLevel,
                stressLevel = stressLevel,
                confidenceLevel = confidenceLevel,
                fatigueLevel = fatigueLevel,
                emotionalState = emotionalState,
                suggestions = suggestions,
                interventionNeeded = interventionNeeded,
                analysisReason = analysisReason
            )
        } catch (e: Exception) {
            Log.w(TAG, "解析AI响应失败，使用默认值", e)
            createFallbackEmotionalState(behaviorAnalysis)
        }
    }
    
    private fun createFallbackEmotionalState(behaviorAnalysis: BehaviorAnalysis): EmotionalState {
        val focusLevel = when {
            behaviorAnalysis.consistencyScore > 0.8 -> 8
            behaviorAnalysis.consistencyScore > 0.6 -> 6
            else -> 4
        }
        
        val stressLevel = when {
            behaviorAnalysis.averageHesitation > 5000 -> 7
            behaviorAnalysis.averageHesitation > 3000 -> 5
            else -> 3
        }
        
        return EmotionalState(
            focusLevel = focusLevel,
            stressLevel = stressLevel,
            confidenceLevel = 6,
            fatigueLevel = 4,
            emotionalState = "中性",
            suggestions = listOf("保持当前学习节奏", "适当休息", "多做互动练习"),
            interventionNeeded = stressLevel > 6 || focusLevel < 4,
            analysisReason = "基于行为模式的基础分析"
        )
    }
    
    private fun parseInterventionSuggestions(response: String): List<String> {
        return response.lines()
            .filter { it.startsWith("-") || it.startsWith("•") }
            .map { it.removePrefix("-").removePrefix("•").trim() }
            .filter { it.isNotBlank() }
            .take(5)
    }
    
    private fun getDefaultInterventions(emotionalState: EmotionalState): List<String> {
        return when {
            emotionalState.stressLevel > 7 -> listOf("深呼吸放松", "暂停5分钟", "降低学习难度")
            emotionalState.fatigueLevel > 7 -> listOf("休息10分钟", "做眼保健操", "喝水补充水分")
            emotionalState.focusLevel < 4 -> listOf("切换学习方式", "增加互动练习", "设定小目标")
            else -> listOf("保持当前状态", "继续努力", "适时奖励自己")
        }
    }
    
    // 辅助解析方法
    private fun extractNumber(lines: List<String>, key: String, default: Int): Int {
        return lines.find { it.contains(key, ignoreCase = true) }
            ?.let { line ->
                Regex("\\d+").find(line)?.value?.toIntOrNull()
            } ?: default
    }
    
    private fun extractValue(lines: List<String>, key: String, default: String): String {
        return lines.find { it.contains(key, ignoreCase = true) }
            ?.substringAfter("：")
            ?.substringAfter(":")
            ?.trim()
            ?: default
    }
    
    private fun extractSuggestions(lines: List<String>): List<String> {
        val suggestions = mutableListOf<String>()
        var inSuggestionSection = false
        
        for (line in lines) {
            if (line.contains("建议", ignoreCase = true)) {
                inSuggestionSection = true
                continue
            }
            if (inSuggestionSection && (line.startsWith("-") || line.startsWith("•"))) {
                suggestions.add(line.removePrefix("-").removePrefix("•").trim())
            } else if (inSuggestionSection && line.isBlank()) {
                break
            }
        }
        
        return suggestions.ifEmpty { listOf("继续保持学习状态", "适当调整学习节奏") }
    }
    
    /**
     * 行为分析结果数据类
     */
    data class BehaviorAnalysis(
        val clickFrequency: Double,
        val averageHesitation: Long,
        val accuracyTrend: Double,
        val activityLevel: Double,
        val consistencyScore: Double
    )
    
    // 课堂氛围分析数据类
    data class ClassroomAtmosphere(
        val overallMood: String,
        val atmosphereScore: Float,
        val recommendedMood: String,
        val suggestions: List<String>,
        val interventionNeeded: Boolean
    )
    
    /**
     * 🎭 创新功能：分析整体课堂氛围
     */
    suspend fun analyzeClassroomAtmosphere(
        studentEmotions: List<EmotionalState>,
        currentKnowledgeNodes: List<String>,
        groupCollaborationScore: Float
    ): ClassroomAtmosphere {
        return try {
            // 计算整体情绪倾向
            val moodCounts = studentEmotions.groupingBy { it.emotionalState }.eachCount()
            val dominantMood = moodCounts.maxByOrNull { it.value }?.key ?: "中性"
            
            // 计算整体氛围评分
            val avgFocus = studentEmotions.map { it.focusLevel }.average().toFloat() / 10
            val avgStress = studentEmotions.map { it.stressLevel }.average().toFloat() / 10
            val atmosphereScore = (avgFocus + groupCollaborationScore + (1 - avgStress)) / 3
            
            // 决定推荐的氛围调整
            val recommendedMood = when {
                avgFocus < 0.4f -> "活跃"
                avgStress > 0.7f -> "放松"
                groupCollaborationScore < 0.5f -> "协作"
                else -> "专注"
            }
            
            val suggestions = mutableListOf<String>()
            if (avgFocus < 0.5f) suggestions.add("增加互动环节")
            if (avgStress > 0.6f) suggestions.add("安排休息时间")
            if (groupCollaborationScore < 0.6f) suggestions.add("促进小组合作")
            
            ClassroomAtmosphere(
                overallMood = dominantMood,
                atmosphereScore = atmosphereScore,
                recommendedMood = recommendedMood,
                suggestions = suggestions.ifEmpty { listOf("保持当前氛围") },
                interventionNeeded = avgStress > 0.7f || avgFocus < 0.3f
            )
        } catch (e: Exception) {
            ClassroomAtmosphere(
                overallMood = "平静",
                atmosphereScore = 0.7f,
                recommendedMood = "专注",
                suggestions = listOf("继续当前活动"),
                interventionNeeded = false
            )
        }
    }
}

