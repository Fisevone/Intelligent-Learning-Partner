package com.example.educationapp.ai

import android.util.Log
import com.example.educationapp.data.LearningRecord
import com.example.educationapp.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

/**
 * AI学习效果预测器 - 基于智谱GLM-4
 * 创新功能：预测学习成果、识别学习风险、优化学习路径、生成个性化报告
 */
class LearningEffectPredictor {
    
    private val zhipuAIService = ZhipuAIService()
    
    companion object {
        private const val TAG = "LearningEffectPredictor"
    }
    
    /**
     * 学习效果预测结果
     */
    data class LearningPrediction(
        val overallScore: Float, // 综合预测分数 0-100
        val expectedImprovement: Float, // 预期提升幅度
        val timeToMastery: Int, // 预计掌握时间（天）
        val riskLevel: String, // 风险等级：低/中/高
        val confidenceLevel: Float, // 预测置信度 0-1
        val keyFactors: List<String>, // 关键影响因素
        val recommendations: List<String>, // 改进建议
        val detailedAnalysis: String // 详细分析
    )
    
    /**
     * 学习风险评估
     */
    data class LearningRiskAssessment(
        val riskLevel: String, // 低/中/高
        val riskScore: Float, // 风险分数 0-100
        val riskFactors: List<String>, // 风险因素
        val earlyWarnings: List<String>, // 预警信号
        val preventiveMeasures: List<String>, // 预防措施
        val interventionTiming: String // 干预时机
    )
    
    /**
     * 个性化学习路径优化建议
     */
    data class LearningPathOptimization(
        val currentEfficiency: Float, // 当前效率 0-1
        val optimizedPath: List<String>, // 优化后路径
        val expectedEfficiencyGain: Float, // 预期效率提升
        val timeReduction: Int, // 时间节省（天）
        val difficultyAdjustment: String, // 难度调整建议
        val studyMethodSuggestions: List<String>, // 学习方法建议
        val scheduleOptimization: String // 时间安排优化
    )
    
    /**
     * 核心功能：预测学习效果
     */
    suspend fun predictLearningOutcome(
        user: User,
        subject: String,
        learningHistory: List<LearningRecord>,
        targetGoal: String,
        timeframe: Int // 预测时间框架（天）
    ): Result<LearningPrediction> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "开始学习效果预测...")
            
            // 1. 分析历史学习数据
            val learningMetrics = analyzeLearningMetrics(learningHistory)
            
            // 2. 构建AI预测提示
            val predictionPrompt = buildPredictionPrompt(
                user, subject, learningHistory, targetGoal, timeframe, learningMetrics
            )
            
            // 3. 调用GLM-4进行预测
            val aiResult = zhipuAIService.sendChatMessage(predictionPrompt, user)
            
            aiResult.fold(
                onSuccess = { response ->
                    val prediction = parsePredictionResponse(response, learningMetrics)
                    Log.d(TAG, "学习效果预测完成: ${prediction.overallScore}")
                    Result.success(prediction)
                },
                onFailure = { error ->
                    Log.e(TAG, "AI预测失败", error)
                    // 使用基于规则的备用预测
                    val fallbackPrediction = generateFallbackPrediction(learningMetrics, timeframe)
                    Result.success(fallbackPrediction)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "学习效果预测异常", e)
            Result.failure(e)
        }
    }
    
    /**
     * 学习风险评估
     */
    suspend fun assessLearningRisk(
        user: User,
        learningHistory: List<LearningRecord>,
        currentPerformance: Map<String, Float>
    ): Result<LearningRiskAssessment> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "开始学习风险评估...")
            
            val riskPrompt = buildRiskAssessmentPrompt(user, learningHistory, currentPerformance)
            val aiResult = zhipuAIService.sendChatMessage(riskPrompt, user)
            
            aiResult.fold(
                onSuccess = { response ->
                    val riskAssessment = parseRiskAssessment(response)
                    Log.d(TAG, "风险评估完成: ${riskAssessment.riskLevel}")
                    Result.success(riskAssessment)
                },
                onFailure = { error ->
                    Log.e(TAG, "风险评估失败", error)
                    val fallbackRisk = generateFallbackRiskAssessment(learningHistory)
                    Result.success(fallbackRisk)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "风险评估异常", e)
            Result.failure(e)
        }
    }
    
    /**
     * 学习路径优化
     */
    suspend fun optimizeLearningPath(
        user: User,
        currentPath: List<String>,
        performanceData: Map<String, Float>,
        timeConstraints: Map<String, Int>
    ): Result<LearningPathOptimization> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "开始学习路径优化...")
            
            val optimizationPrompt = buildPathOptimizationPrompt(
                user, currentPath, performanceData, timeConstraints
            )
            val aiResult = zhipuAIService.sendChatMessage(optimizationPrompt, user)
            
            aiResult.fold(
                onSuccess = { response ->
                    val optimization = parsePathOptimization(response, currentPath)
                    Log.d(TAG, "路径优化完成: ${optimization.expectedEfficiencyGain}")
                    Result.success(optimization)
                },
                onFailure = { error ->
                    Log.e(TAG, "路径优化失败", error)
                    val fallbackOptimization = generateFallbackOptimization(currentPath, performanceData)
                    Result.success(fallbackOptimization)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "路径优化异常", e)
            Result.failure(e)
        }
    }
    
    /**
     * 分析学习指标
     */
    private fun analyzeLearningMetrics(learningHistory: List<LearningRecord>): LearningMetrics {
        if (learningHistory.isEmpty()) {
            return LearningMetrics(
                averageScore = 0f,
                scoreVariance = 0f,
                learningVelocity = 0f,
                consistencyIndex = 0f,
                difficultyProgression = 0f,
                timeEfficiency = 0f,
                retentionRate = 0f
            )
        }
        
        val scores = learningHistory.map { it.score }
        val averageScore = scores.average().toFloat()
        val scoreVariance = calculateVariance(scores)
        
        // 计算学习速度（最近10次vs前10次的改进）
        val learningVelocity = if (learningHistory.size >= 10) {
            val recentAvg = learningHistory.takeLast(10).map { it.score }.average()
            val earlyAvg = learningHistory.take(10).map { it.score }.average()
            ((recentAvg - earlyAvg) / earlyAvg * 100).toFloat()
        } else 0f
        
        // 计算一致性指数
        val consistencyIndex = calculateConsistencyIndex(learningHistory)
        
        // 计算难度进展
        val difficultyProgression = calculateDifficultyProgression(learningHistory)
        
        // 计算时间效率
        val timeEfficiency = calculateTimeEfficiency(learningHistory)
        
        // 计算保持率
        val retentionRate = calculateRetentionRate(learningHistory)
        
        return LearningMetrics(
            averageScore = averageScore,
            scoreVariance = scoreVariance,
            learningVelocity = learningVelocity,
            consistencyIndex = consistencyIndex,
            difficultyProgression = difficultyProgression,
            timeEfficiency = timeEfficiency,
            retentionRate = retentionRate
        )
    }
    
    private fun calculateVariance(scores: List<Float>): Float {
        val mean = scores.average()
        return scores.map { (it - mean).pow(2) }.average().toFloat()
    }
    
    private fun calculateConsistencyIndex(records: List<LearningRecord>): Float {
        if (records.size < 3) return 0.5f
        
        val scores = records.map { it.score }
        val mean = scores.average()
        val standardDeviation = sqrt(scores.map { (it - mean).pow(2) }.average())
        
        // 标准差越小，一致性越高
        return (1.0 / (1.0 + standardDeviation / mean)).toFloat().coerceIn(0f, 1f)
    }
    
    private fun calculateDifficultyProgression(records: List<LearningRecord>): Float {
        // 简化实现：基于题目难度的提升
        val difficulties = records.mapNotNull { record ->
            when (record.difficulty.lowercase()) {
                "简单", "easy" -> 1f
                "中等", "medium" -> 2f
                "困难", "hard" -> 3f
                else -> null
            }
        }
        
        return if (difficulties.isNotEmpty()) {
            val recent = difficulties.takeLast(5).average()
            val early = difficulties.take(5).average()
            ((recent - early) / 3.0).toFloat().coerceIn(-1f, 1f)
        } else 0f
    }
    
    private fun calculateTimeEfficiency(records: List<LearningRecord>): Float {
        if (records.isEmpty()) return 0f
        
        // 效率 = 平均分数 / 平均时长
        val avgScore = records.map { it.score }.average()
        val avgDuration = records.map { it.duration }.average()
        
        return (avgScore / (avgDuration / 60.0)).toFloat().coerceIn(0f, 10f) // 分数/小时
    }
    
    private fun calculateRetentionRate(records: List<LearningRecord>): Float {
        // 简化实现：基于重复主题的表现
        val topicPerformance = records.groupBy { it.topic }
            .mapValues { (_, topicRecords) -> 
                topicRecords.map { it.score }.average().toFloat()
            }
        
        return topicPerformance.values.average().toFloat() / 100f
    }
    
    private fun buildPredictionPrompt(
        user: User,
        subject: String,
        history: List<LearningRecord>,
        targetGoal: String,
        timeframe: Int,
        metrics: LearningMetrics
    ): String {
        val recentPerformance = history.takeLast(10).joinToString("\n") { record ->
            "主题: ${record.topic}, 得分: ${record.score}, 时长: ${record.duration}分钟, 难度: ${record.difficulty}"
        }
        
        return """
            作为智谱GLM-4驱动的AI学习效果预测专家，请基于学生的学习数据进行精准预测：
            
            学生信息：
            - 姓名: ${user.name}
            - 年级: ${user.grade}
            - 学习风格: ${user.learningStyle}
            - 兴趣领域: ${user.interests}
            
            学习目标：
            - 科目: $subject
            - 目标: $targetGoal
            - 时间框架: ${timeframe}天
            
            学习指标分析：
            - 平均分数: ${String.format("%.1f", metrics.averageScore)}
            - 学习速度: ${String.format("%.1f", metrics.learningVelocity)}%
            - 一致性指数: ${String.format("%.2f", metrics.consistencyIndex)}
            - 难度进展: ${String.format("%.2f", metrics.difficultyProgression)}
            - 时间效率: ${String.format("%.1f", metrics.timeEfficiency)}分/小时
            - 保持率: ${String.format("%.1f", metrics.retentionRate * 100)}%
            
            最近学习表现：
            $recentPerformance
            
            请基于以上数据，返回JSON格式的预测结果：
            {
                "overallScore": 预测综合分数(0-100),
                "expectedImprovement": 预期提升幅度(%),
                "timeToMastery": 预计掌握时间(天),
                "riskLevel": "风险等级(低/中/高)",
                "confidenceLevel": 预测置信度(0.0-1.0),
                "keyFactors": ["关键影响因素列表"],
                "recommendations": ["具体改进建议列表"],
                "detailedAnalysis": "详细分析说明"
            }
            
            要求：
            1. 基于真实数据进行科学预测
            2. 考虑个人学习特点和历史表现
            3. 提供可执行的改进建议
            4. 确保JSON格式正确完整
        """.trimIndent()
    }
    
    private fun buildRiskAssessmentPrompt(
        user: User,
        history: List<LearningRecord>,
        currentPerformance: Map<String, Float>
    ): String {
        val performanceText = currentPerformance.map { "${it.key}: ${it.value}" }.joinToString(", ")
        val trendAnalysis = if (history.size >= 5) {
            val recent = history.takeLast(5).map { it.score }.average()
            val earlier = history.take(5).map { it.score }.average()
            "近期表现${if (recent > earlier) "上升" else "下降"}趋势"
        } else "数据不足"
        
        return """
            作为AI学习风险评估专家，请分析学生的学习风险：
            
            学生: ${user.name} (${user.grade})
            当前表现: $performanceText
            趋势分析: $trendAnalysis
            历史记录数: ${history.size}
            
            请评估学习风险并返回JSON：
            {
                "riskLevel": "风险等级(低/中/高)",
                "riskScore": 风险分数(0-100),
                "riskFactors": ["风险因素列表"],
                "earlyWarnings": ["预警信号列表"],
                "preventiveMeasures": ["预防措施列表"],
                "interventionTiming": "建议干预时机"
            }
        """.trimIndent()
    }
    
    private fun buildPathOptimizationPrompt(
        user: User,
        currentPath: List<String>,
        performanceData: Map<String, Float>,
        timeConstraints: Map<String, Int>
    ): String {
        return """
            作为AI学习路径优化专家，请优化学生的学习路径：
            
            学生: ${user.name}
            当前路径: ${currentPath.joinToString(" → ")}
            各模块表现: ${performanceData.map { "${it.key}:${it.value}" }.joinToString(", ")}
            时间限制: ${timeConstraints.map { "${it.key}:${it.value}天" }.joinToString(", ")}
            
            请优化路径并返回JSON：
            {
                "currentEfficiency": 当前效率(0.0-1.0),
                "optimizedPath": ["优化后的学习路径"],
                "expectedEfficiencyGain": 预期效率提升(0.0-1.0),
                "timeReduction": 时间节省(天),
                "difficultyAdjustment": "难度调整建议",
                "studyMethodSuggestions": ["学习方法建议"],
                "scheduleOptimization": "时间安排优化建议"
            }
        """.trimIndent()
    }
    
    private fun parsePredictionResponse(response: String, metrics: LearningMetrics): LearningPrediction {
        return try {
            // 简化的解析逻辑，实际应该解析JSON
            val overallScore = extractFloatValue(response, "overallScore") ?: 
                (metrics.averageScore + metrics.learningVelocity * 0.1f).coerceIn(0f, 100f)
            
            LearningPrediction(
                overallScore = overallScore,
                expectedImprovement = extractFloatValue(response, "expectedImprovement") ?: 
                    metrics.learningVelocity.coerceIn(0f, 50f),
                timeToMastery = extractIntValue(response, "timeToMastery") ?: 
                    ((100 - overallScore) / 2).roundToInt().coerceIn(1, 365),
                riskLevel = extractStringValue(response, "riskLevel") ?: 
                    if (metrics.consistencyIndex > 0.7f) "低" else if (metrics.consistencyIndex > 0.4f) "中" else "高",
                confidenceLevel = extractFloatValue(response, "confidenceLevel") ?: 0.75f,
                keyFactors = extractListValue(response, "keyFactors") ?: 
                    listOf("学习一致性", "时间效率", "难度适应性"),
                recommendations = extractListValue(response, "recommendations") ?: 
                    generateDefaultRecommendations(metrics),
                detailedAnalysis = extractStringValue(response, "detailedAnalysis") ?: 
                    "基于当前学习表现和历史数据的综合分析"
            )
        } catch (e: Exception) {
            Log.w(TAG, "解析预测响应失败", e)
            generateFallbackPrediction(metrics, 30)
        }
    }
    
    private fun parseRiskAssessment(response: String): LearningRiskAssessment {
        return try {
            LearningRiskAssessment(
                riskLevel = extractStringValue(response, "riskLevel") ?: "中",
                riskScore = extractFloatValue(response, "riskScore") ?: 50f,
                riskFactors = extractListValue(response, "riskFactors") ?: 
                    listOf("学习进度不稳定", "注意力分散"),
                earlyWarnings = extractListValue(response, "earlyWarnings") ?: 
                    listOf("成绩波动较大", "学习时间不规律"),
                preventiveMeasures = extractListValue(response, "preventiveMeasures") ?: 
                    listOf("制定固定学习计划", "增加复习频率"),
                interventionTiming = extractStringValue(response, "interventionTiming") ?: "近期"
            )
        } catch (e: Exception) {
            Log.w(TAG, "解析风险评估响应失败", e)
            LearningRiskAssessment("中", 50f, listOf("需要关注"), listOf("建议监测"), listOf("加强指导"), "适时")
        }
    }
    
    private fun parsePathOptimization(response: String, currentPath: List<String>): LearningPathOptimization {
        return try {
            LearningPathOptimization(
                currentEfficiency = extractFloatValue(response, "currentEfficiency") ?: 0.6f,
                optimizedPath = extractListValue(response, "optimizedPath") ?: currentPath,
                expectedEfficiencyGain = extractFloatValue(response, "expectedEfficiencyGain") ?: 0.15f,
                timeReduction = extractIntValue(response, "timeReduction") ?: 5,
                difficultyAdjustment = extractStringValue(response, "difficultyAdjustment") ?: "适当调整",
                studyMethodSuggestions = extractListValue(response, "studyMethodSuggestions") ?: 
                    listOf("增加实践练习", "定期复习"),
                scheduleOptimization = extractStringValue(response, "scheduleOptimization") ?: 
                    "建议固定时间段学习"
            )
        } catch (e: Exception) {
            Log.w(TAG, "解析路径优化响应失败", e)
            generateFallbackOptimization(currentPath, emptyMap())
        }
    }
    
    private fun generateFallbackPrediction(metrics: LearningMetrics, timeframe: Int): LearningPrediction {
        val baseScore = metrics.averageScore
        val improvement = metrics.learningVelocity.coerceIn(-20f, 30f)
        val predictedScore = (baseScore + improvement).coerceIn(0f, 100f)
        
        return LearningPrediction(
            overallScore = predictedScore,
            expectedImprovement = improvement,
            timeToMastery = ((100 - predictedScore) / 3).roundToInt().coerceIn(1, timeframe),
            riskLevel = when {
                metrics.consistencyIndex > 0.7f && metrics.learningVelocity > 0 -> "低"
                metrics.consistencyIndex < 0.4f || metrics.learningVelocity < -10f -> "高"
                else -> "中"
            },
            confidenceLevel = metrics.consistencyIndex,
            keyFactors = listOf("历史表现", "学习一致性", "进步速度"),
            recommendations = generateDefaultRecommendations(metrics),
            detailedAnalysis = "基于历史数据的统计分析预测"
        )
    }
    
    private fun generateFallbackRiskAssessment(history: List<LearningRecord>): LearningRiskAssessment {
        val recentScores = history.takeLast(5).map { it.score }
        val variance = if (recentScores.isNotEmpty()) calculateVariance(recentScores) else 0f
        
        val riskLevel = when {
            variance > 400f -> "高"
            variance > 100f -> "中"
            else -> "低"
        }
        
        return LearningRiskAssessment(
            riskLevel = riskLevel,
            riskScore = (variance / 10f).coerceIn(0f, 100f),
            riskFactors = if (variance > 100f) listOf("成绩波动大", "学习不稳定") else listOf("表现稳定"),
            earlyWarnings = if (variance > 200f) listOf("需要关注成绩变化") else emptyList(),
            preventiveMeasures = listOf("保持学习节奏", "定期复习"),
            interventionTiming = if (variance > 300f) "立即" else "定期监测"
        )
    }
    
    private fun generateFallbackOptimization(
        currentPath: List<String>,
        performanceData: Map<String, Float>
    ): LearningPathOptimization {
        return LearningPathOptimization(
            currentEfficiency = 0.6f,
            optimizedPath = currentPath,
            expectedEfficiencyGain = 0.1f,
            timeReduction = 3,
            difficultyAdjustment = "保持当前难度",
            studyMethodSuggestions = listOf("增加练习", "定期复习"),
            scheduleOptimization = "建议每日固定时间学习"
        )
    }
    
    private fun generateDefaultRecommendations(metrics: LearningMetrics): List<String> {
        val recommendations = mutableListOf<String>()
        
        if (metrics.consistencyIndex < 0.5f) {
            recommendations.add("建议制定固定的学习计划，提高学习一致性")
        }
        
        if (metrics.timeEfficiency < 2f) {
            recommendations.add("优化学习方法，提高时间利用效率")
        }
        
        if (metrics.learningVelocity < 0) {
            recommendations.add("调整学习策略，重点关注基础知识巩固")
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("继续保持良好的学习状态")
        }
        
        return recommendations
    }
    
    // 辅助解析方法
    private fun extractFloatValue(text: String, key: String): Float? {
        return try {
            val regex = Regex("\"$key\"\\s*:\\s*([\\d.]+)")
            regex.find(text)?.groupValues?.get(1)?.toFloatOrNull()
        } catch (e: Exception) { null }
    }
    
    private fun extractIntValue(text: String, key: String): Int? {
        return try {
            val regex = Regex("\"$key\"\\s*:\\s*(\\d+)")
            regex.find(text)?.groupValues?.get(1)?.toIntOrNull()
        } catch (e: Exception) { null }
    }
    
    private fun extractStringValue(text: String, key: String): String? {
        return try {
            val regex = Regex("\"$key\"\\s*:\\s*\"([^\"]+)\"")
            regex.find(text)?.groupValues?.get(1)
        } catch (e: Exception) { null }
    }
    
    private fun extractListValue(text: String, key: String): List<String>? {
        return try {
            val regex = Regex("\"$key\"\\s*:\\s*\\[([^\\]]+)\\]")
            val match = regex.find(text)?.groupValues?.get(1)
            match?.split(",")?.map { it.trim().removeSurrounding("\"") }
        } catch (e: Exception) { null }
    }
    
    /**
     * 学习指标数据类
     */
    data class LearningMetrics(
        val averageScore: Float,
        val scoreVariance: Float,
        val learningVelocity: Float, // 学习速度（进步率）
        val consistencyIndex: Float, // 一致性指数
        val difficultyProgression: Float, // 难度进展
        val timeEfficiency: Float, // 时间效率
        val retentionRate: Float // 保持率
    )
}
