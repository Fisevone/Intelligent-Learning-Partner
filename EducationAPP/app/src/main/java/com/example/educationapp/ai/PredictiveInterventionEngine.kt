package com.example.educationapp.ai

import android.util.Log
import com.example.educationapp.data.LearningRecord
import com.example.educationapp.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

/**
 * 🔮 预测性干预引擎 - 学习效果预测和主动干预
 * 
 * 核心功能：
 * 1. 学习效果预测 - 预测学习成果和可能的问题
 * 2. 风险识别 - 识别学习风险和潜在困难
 * 3. 主动干预 - 在问题发生前主动干预
 * 4. 个性化建议 - 提供针对性的改进建议
 */
class PredictiveInterventionEngine {
    
    private val zhipuAIService = ZhipuAIService()
    private val personalizationEngine = DeepPersonalizationEngine()
    
    companion object {
        private const val TAG = "PredictiveIntervention"
        private const val PREDICTION_WINDOW_DAYS = 7 // 预测未来7天
        private const val RISK_THRESHOLD = 0.7f // 风险阈值
    }
    
    /**
     * 🔮 学习预测结果
     */
    data class LearningPrediction(
        val userId: Long,
        val predictionPeriod: String, // "短期", "中期", "长期"
        val performancePrediction: PerformancePrediction,
        val riskAssessment: RiskAssessment,
        val interventionRecommendations: List<InterventionRecommendation>,
        val confidenceLevel: Float, // 预测置信度
        val keyFactors: List<String>, // 影响预测的关键因素
        val generatedAt: Long = System.currentTimeMillis()
    )
    
    /**
     * 📊 表现预测
     */
    data class PerformancePrediction(
        val expectedScore: Float, // 预期成绩
        val scoreRange: Pair<Float, Float>, // 成绩区间
        val improvementProbability: Float, // 提升概率
        val masteryPrediction: Map<String, Float>, // 各科目掌握度预测
        val learningEfficiency: Float, // 学习效率预测
        val motivationTrend: String, // 动机趋势：上升、稳定、下降
        val cognitiveLoadPrediction: Float, // 认知负荷预测
        val optimalLearningPath: List<String> // 最优学习路径
    )
    
    /**
     * ⚠️ 风险评估
     */
    data class RiskAssessment(
        val overallRiskLevel: String, // "低", "中", "高"
        val specificRisks: List<SpecificRisk>,
        val earlyWarningSignals: List<String>,
        val preventiveActions: List<String>,
        val criticalInterventionPoints: List<String>,
        val riskFactors: Map<String, Float> // 各风险因素的权重
    )
    
    /**
     * 🚨 具体风险
     */
    data class SpecificRisk(
        val riskType: String, // "学习倦怠", "知识遗忘", "动机下降", "认知过载"
        val probability: Float, // 发生概率
        val impact: String, // "低", "中", "高"
        val timeframe: String, // 预计发生时间
        val indicators: List<String>, // 风险指标
        val preventionStrategy: String // 预防策略
    )
    
    /**
     * 💡 干预建议
     */
    data class InterventionRecommendation(
        val interventionType: String, // "即时", "短期", "长期"
        val priority: String, // "高", "中", "低"
        val targetArea: String, // 干预目标区域
        val specificActions: List<String>,
        val expectedOutcome: String,
        val implementationSteps: List<String>,
        val successMetrics: List<String>,
        val timeline: String // 实施时间线
    )
    
    /**
     * 🎯 核心方法：生成学习预测和干预建议
     */
    suspend fun generateLearningPrediction(
        user: User,
        learningHistory: List<LearningRecord>,
        currentLearnerProfile: DeepPersonalizationEngine.LearnerProfile? = null
    ): Result<LearningPrediction> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔮 开始生成学习预测 for user: ${user.id}")
            
            if (learningHistory.isEmpty()) {
                Log.w(TAG, "⚠️ 学习历史为空，生成基础预测")
                return@withContext Result.success(generateBasicPrediction(user))
            }
            
            // 1. 获取或生成学习者画像
            val learnerProfile = currentLearnerProfile ?: run {
                val profileResult = personalizationEngine.generateLearnerProfile(user, learningHistory)
                profileResult.getOrNull() ?: return@withContext Result.failure(
                    Exception("无法生成学习者画像")
                )
            }
            
            // 2. 预测学习表现
            val performancePrediction = predictPerformance(user, learningHistory, learnerProfile)
            
            // 3. 评估学习风险
            val riskAssessment = assessLearningRisks(user, learningHistory, learnerProfile)
            
            // 4. 生成干预建议
            val interventions = generateInterventionRecommendations(
                user, learnerProfile, performancePrediction, riskAssessment
            )
            
            // 5. 计算预测置信度
            val confidenceLevel = calculatePredictionConfidence(learningHistory, learnerProfile)
            
            // 6. 识别关键影响因素
            val keyFactors = identifyKeyFactors(learnerProfile, performancePrediction, riskAssessment)
            
            val prediction = LearningPrediction(
                userId = user.id,
                predictionPeriod = "短期", // 7天预测
                performancePrediction = performancePrediction,
                riskAssessment = riskAssessment,
                interventionRecommendations = interventions,
                confidenceLevel = confidenceLevel,
                keyFactors = keyFactors
            )
            
            Log.d(TAG, "✅ 学习预测生成成功，置信度: ${confidenceLevel}")
            Result.success(prediction)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ 生成学习预测失败: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * 📊 预测学习表现
     */
    private suspend fun predictPerformance(
        user: User,
        learningHistory: List<LearningRecord>,
        learnerProfile: DeepPersonalizationEngine.LearnerProfile
    ): PerformancePrediction {
        
        // 基于历史表现趋势预测
        val recentScores = learningHistory.takeLast(10).map { it.score }
        val historicalTrend = calculateTrendSlope(recentScores)
        
        // 当前平均成绩
        val currentAverage = recentScores.average().toFloat()
        
        // 预测未来成绩
        val expectedScore = (currentAverage + historicalTrend * PREDICTION_WINDOW_DAYS).coerceIn(0f, 100f)
        
        // 计算成绩区间（基于历史波动性）
        val scoreVariability = calculateScoreVariability(recentScores)
        val scoreRange = Pair(
            (expectedScore - scoreVariability).coerceAtLeast(0f),
            (expectedScore + scoreVariability).coerceAtMost(100f)
        )
        
        // 预测提升概率
        val improvementProbability = calculateImprovementProbability(
            learnerProfile.performancePattern,
            learnerProfile.motivationProfile,
            historicalTrend
        )
        
        // 预测各科目掌握度
        val masteryPrediction = predictSubjectMastery(
            learnerProfile.knowledgeMap.subjectMastery,
            learnerProfile.performancePattern.improvementRate
        )
        
        // 预测学习效率
        val learningEfficiency = predictLearningEfficiency(
            learnerProfile.cognitiveProfile,
            learnerProfile.performancePattern
        )
        
        // 预测动机趋势
        val motivationTrend = predictMotivationTrend(
            learnerProfile.motivationProfile,
            learnerProfile.performancePattern
        )
        
        // 预测认知负荷
        val cognitiveLoadPrediction = predictCognitiveLoad(
            learnerProfile.cognitiveProfile,
            expectedScore,
            learningEfficiency
        )
        
        // 生成最优学习路径
        val optimalPath = generateOptimalLearningPath(
            learnerProfile.knowledgeMap,
            masteryPrediction,
            cognitiveLoadPrediction
        )
        
        return PerformancePrediction(
            expectedScore = expectedScore,
            scoreRange = scoreRange,
            improvementProbability = improvementProbability,
            masteryPrediction = masteryPrediction,
            learningEfficiency = learningEfficiency,
            motivationTrend = motivationTrend,
            cognitiveLoadPrediction = cognitiveLoadPrediction,
            optimalLearningPath = optimalPath
        )
    }
    
    /**
     * ⚠️ 评估学习风险
     */
    private suspend fun assessLearningRisks(
        user: User,
        learningHistory: List<LearningRecord>,
        learnerProfile: DeepPersonalizationEngine.LearnerProfile
    ): RiskAssessment {
        
        val specificRisks = mutableListOf<SpecificRisk>()
        val earlyWarningSignals = mutableListOf<String>()
        val preventiveActions = mutableListOf<String>()
        val criticalPoints = mutableListOf<String>()
        val riskFactors = mutableMapOf<String, Float>()
        
        // 1. 学习倦怠风险评估
        val burnoutRisk = assessBurnoutRisk(learnerProfile, learningHistory)
        if (burnoutRisk.probability > RISK_THRESHOLD) {
            specificRisks.add(burnoutRisk)
            earlyWarningSignals.add("学习时长过长且效果下降")
            preventiveActions.add("安排适当休息，调整学习强度")
        }
        riskFactors["学习倦怠"] = burnoutRisk.probability
        
        // 2. 知识遗忘风险评估
        val forgettingRisk = assessForgettingRisk(learnerProfile, learningHistory)
        if (forgettingRisk.probability > RISK_THRESHOLD) {
            specificRisks.add(forgettingRisk)
            earlyWarningSignals.add("长期未复习重要知识点")
            preventiveActions.add("安排系统性复习计划")
        }
        riskFactors["知识遗忘"] = forgettingRisk.probability
        
        // 3. 动机下降风险评估
        val motivationRisk = assessMotivationDeclineRisk(learnerProfile, learningHistory)
        if (motivationRisk.probability > RISK_THRESHOLD) {
            specificRisks.add(motivationRisk)
            earlyWarningSignals.add("学习频率和主动性下降")
            preventiveActions.add("调整学习目标，增加趣味性")
        }
        riskFactors["动机下降"] = motivationRisk.probability
        
        // 4. 认知过载风险评估
        val overloadRisk = assessCognitiveOverloadRisk(learnerProfile, learningHistory)
        if (overloadRisk.probability > RISK_THRESHOLD) {
            specificRisks.add(overloadRisk)
            earlyWarningSignals.add("学习效率明显下降")
            preventiveActions.add("降低学习难度，分解学习任务")
        }
        riskFactors["认知过载"] = overloadRisk.probability
        
        // 5. 学习停滞风险评估
        val stagnationRisk = assessLearningStagnationRisk(learnerProfile, learningHistory)
        if (stagnationRisk.probability > RISK_THRESHOLD) {
            specificRisks.add(stagnationRisk)
            earlyWarningSignals.add("成绩长期无改善")
            preventiveActions.add("调整学习策略，寻找突破点")
        }
        riskFactors["学习停滞"] = stagnationRisk.probability
        
        // 确定关键干预点
        if (specificRisks.isNotEmpty()) {
            criticalPoints.add("未来3-5天内需要关注")
            if (specificRisks.any { it.probability > 0.8f }) {
                criticalPoints.add("需要立即干预")
            }
        }
        
        // 计算整体风险等级
        val overallRiskLevel = when {
            specificRisks.any { it.probability > 0.8f } -> "高"
            specificRisks.any { it.probability > 0.6f } -> "中"
            else -> "低"
        }
        
        return RiskAssessment(
            overallRiskLevel = overallRiskLevel,
            specificRisks = specificRisks,
            earlyWarningSignals = earlyWarningSignals,
            preventiveActions = preventiveActions,
            criticalInterventionPoints = criticalPoints,
            riskFactors = riskFactors
        )
    }
    
    /**
     * 💡 生成干预建议
     */
    private suspend fun generateInterventionRecommendations(
        user: User,
        learnerProfile: DeepPersonalizationEngine.LearnerProfile,
        performancePrediction: PerformancePrediction,
        riskAssessment: RiskAssessment
    ): List<InterventionRecommendation> {
        
        val recommendations = mutableListOf<InterventionRecommendation>()
        
        // 1. 基于风险的即时干预
        riskAssessment.specificRisks.forEach { risk ->
            when (risk.riskType) {
                "学习倦怠" -> {
                    recommendations.add(
                        InterventionRecommendation(
                            interventionType = "即时",
                            priority = "高",
                            targetArea = "心理健康",
                            specificActions = listOf(
                                "立即减少50%学习强度",
                                "安排2小时放松活动",
                                "调整学习环境"
                            ),
                            expectedOutcome = "恢复学习动力和效率",
                            implementationSteps = listOf(
                                "暂停当前学习任务",
                                "进行身心放松",
                                "重新制定学习计划"
                            ),
                            successMetrics = listOf("学习效率提升", "压力水平下降"),
                            timeline = "24小时内实施"
                        )
                    )
                }
                "认知过载" -> {
                    recommendations.add(
                        InterventionRecommendation(
                            interventionType = "短期",
                            priority = "高",
                            targetArea = "学习策略",
                            specificActions = listOf(
                                "将复杂任务分解为小步骤",
                                "使用记忆辅助工具",
                                "延长学习间隔"
                            ),
                            expectedOutcome = "提高学习效率和理解深度",
                            implementationSteps = listOf(
                                "重新设计学习任务",
                                "引入辅助工具",
                                "调整学习节奏"
                            ),
                            successMetrics = listOf("任务完成率提升", "理解准确度提高"),
                            timeline = "3-5天内调整"
                        )
                    )
                }
                "动机下降" -> {
                    recommendations.add(
                        InterventionRecommendation(
                            interventionType = "中期",
                            priority = "中",
                            targetArea = "动机激发",
                            specificActions = listOf(
                                "设置更具挑战性的目标",
                                "引入游戏化元素",
                                "建立学习伙伴关系"
                            ),
                            expectedOutcome = "重新激发学习热情",
                            implementationSteps = listOf(
                                "重新设定学习目标",
                                "设计奖励机制",
                                "寻找学习伙伴"
                            ),
                            successMetrics = listOf("学习频率增加", "主动性提升"),
                            timeline = "1-2周内实施"
                        )
                    )
                }
            }
        }
        
        // 2. 基于表现预测的优化建议
        if (performancePrediction.improvementProbability < 0.5f) {
            recommendations.add(
                InterventionRecommendation(
                    interventionType = "长期",
                    priority = "中",
                    targetArea = "学习方法",
                    specificActions = listOf(
                        "调整学习策略",
                        "加强薄弱环节训练",
                        "优化学习时间分配"
                    ),
                    expectedOutcome = "提升整体学习效果",
                    implementationSteps = listOf(
                        "分析当前学习方法",
                        "制定改进计划",
                        "持续监控效果"
                    ),
                    successMetrics = listOf("成绩稳步提升", "学习效率改善"),
                    timeline = "2-4周持续改进"
                )
            )
        }
        
        // 3. 基于个性化特征的定制建议
        when (learnerProfile.learningStyle.primaryStyle) {
            "视觉型" -> {
                recommendations.add(
                    InterventionRecommendation(
                        interventionType = "长期",
                        priority = "低",
                        targetArea = "学习工具",
                        specificActions = listOf(
                            "增加图表和可视化材料",
                            "使用思维导图工具",
                            "创建视觉学习笔记"
                        ),
                        expectedOutcome = "更好地利用视觉学习优势",
                        implementationSteps = listOf(
                            "准备可视化学习资源",
                            "学习思维导图技巧",
                            "建立视觉笔记系统"
                        ),
                        successMetrics = listOf("理解速度提升", "记忆效果改善"),
                        timeline = "逐步实施，持续优化"
                    )
                )
            }
        }
        
        // 4. 预防性建议
        if (riskAssessment.overallRiskLevel == "低") {
            recommendations.add(
                InterventionRecommendation(
                    interventionType = "预防",
                    priority = "低",
                    targetArea = "持续优化",
                    specificActions = listOf(
                        "保持当前学习节奏",
                        "适当增加挑战性",
                        "建立长期学习规划"
                    ),
                    expectedOutcome = "维持良好学习状态",
                    implementationSteps = listOf(
                        "定期评估学习状态",
                        "适时调整学习目标",
                        "建立学习反馈机制"
                    ),
                    successMetrics = listOf("持续稳定进步", "学习满意度高"),
                    timeline = "持续关注和优化"
                )
            )
        }
        
        // 按优先级排序
        return recommendations.sortedByDescending { 
            when (it.priority) {
                "高" -> 3
                "中" -> 2
                "低" -> 1
                else -> 0
            }
        }
    }
    
    /**
     * 🎯 实时干预触发器
     */
    suspend fun checkForRealTimeIntervention(
        user: User,
        currentSession: LearningRecord,
        recentHistory: List<LearningRecord>
    ): Result<InterventionRecommendation?> = withContext(Dispatchers.IO) {
        try {
            // 检查即时干预条件
            val interventionNeeded = when {
                // 连续答错3题以上
                recentHistory.takeLast(3).all { it.score < 50 } -> {
                    InterventionRecommendation(
                        interventionType = "即时",
                        priority = "高",
                        targetArea = "学习困难",
                        specificActions = listOf(
                            "暂停当前学习",
                            "回顾基础知识",
                            "降低题目难度"
                        ),
                        expectedOutcome = "重建学习信心",
                        implementationSteps = listOf(
                            "停止当前练习",
                            "提供基础知识复习",
                            "重新开始简单题目"
                        ),
                        successMetrics = listOf("答题正确率回升"),
                        timeline = "立即执行"
                    )
                }
                
                // 学习时间过长（超过2小时）
                currentSession.duration > 120 -> {
                    InterventionRecommendation(
                        interventionType = "即时",
                        priority = "中",
                        targetArea = "疲劳管理",
                        specificActions = listOf(
                            "建议休息15分钟",
                            "进行眼部放松",
                            "适当活动身体"
                        ),
                        expectedOutcome = "恢复注意力和学习效率",
                        implementationSteps = listOf(
                            "显示休息提醒",
                            "提供放松指导",
                            "设置休息计时器"
                        ),
                        successMetrics = listOf("后续学习效率提升"),
                        timeline = "立即建议"
                    )
                }
                
                // 答题速度异常（过快或过慢）
                currentSession.duration < 30 && recentHistory.takeLast(3).all { it.duration < 30 } -> {
                    InterventionRecommendation(
                        interventionType = "即时",
                        priority = "中",
                        targetArea = "学习态度",
                        specificActions = listOf(
                            "提醒仔细思考",
                            "强调学习质量",
                            "提供解题指导"
                        ),
                        expectedOutcome = "提高学习质量",
                        implementationSteps = listOf(
                            "显示提醒消息",
                            "提供解题提示",
                            "鼓励深入思考"
                        ),
                        successMetrics = listOf("答题质量提升"),
                        timeline = "即时提醒"
                    )
                }
                
                else -> null
            }
            
            Result.success(interventionNeeded)
            
        } catch (e: Exception) {
            Log.e(TAG, "实时干预检查失败: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    // ==================== 辅助计算方法 ====================
    
    private fun calculateTrendSlope(scores: List<Float>): Float {
        if (scores.size < 2) return 0f
        
        val n = scores.size
        val x = (1..n).map { it.toFloat() }
        val y = scores
        
        val sumX = x.sum()
        val sumY = y.sum()
        val sumXY = x.zip(y) { xi, yi -> xi * yi }.sum()
        val sumX2 = x.map { it * it }.sum()
        
        return (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
    }
    
    private fun calculateScoreVariability(scores: List<Float>): Float {
        if (scores.isEmpty()) return 10f
        val mean = scores.average()
        val variance = scores.map { (it - mean).pow(2) }.average()
        return sqrt(variance).toFloat()
    }
    
    private fun calculateImprovementProbability(
        performancePattern: DeepPersonalizationEngine.PerformancePattern,
        motivationProfile: DeepPersonalizationEngine.MotivationProfile,
        historicalTrend: Float
    ): Float {
        val baseProb = when {
            historicalTrend > 1f -> 0.8f
            historicalTrend > 0f -> 0.6f
            historicalTrend > -1f -> 0.4f
            else -> 0.2f
        }
        
        val motivationBonus = motivationProfile.intrinsicMotivation * 0.2f
        val consistencyBonus = performancePattern.consistencyScore * 0.1f
        
        return (baseProb + motivationBonus + consistencyBonus).coerceIn(0f, 1f)
    }
    
    private fun predictSubjectMastery(
        currentMastery: Map<String, DeepPersonalizationEngine.SubjectMastery>,
        improvementRate: Float
    ): Map<String, Float> {
        return currentMastery.mapValues { (_, mastery) ->
            val predictedImprovement = improvementRate * PREDICTION_WINDOW_DAYS
            (mastery.overallMastery + predictedImprovement).coerceIn(0f, 1f)
        }
    }
    
    private fun predictLearningEfficiency(
        cognitiveProfile: DeepPersonalizationEngine.CognitiveProfile,
        performancePattern: DeepPersonalizationEngine.PerformancePattern
    ): Float {
        val cognitiveEfficiency = (cognitiveProfile.processingSpeed + cognitiveProfile.workingMemoryCapacity) / 2f
        val performanceEfficiency = performancePattern.consistencyScore
        return (cognitiveEfficiency + performanceEfficiency) / 2f
    }
    
    private fun predictMotivationTrend(
        motivationProfile: DeepPersonalizationEngine.MotivationProfile,
        performancePattern: DeepPersonalizationEngine.PerformancePattern
    ): String {
        val motivationScore = (motivationProfile.intrinsicMotivation + motivationProfile.persistenceLevel) / 2f
        val performanceScore = performancePattern.improvementRate
        
        return when {
            motivationScore > 0.7f && performanceScore > 0.1f -> "上升"
            motivationScore < 0.4f || performanceScore < -0.1f -> "下降"
            else -> "稳定"
        }
    }
    
    private fun predictCognitiveLoad(
        cognitiveProfile: DeepPersonalizationEngine.CognitiveProfile,
        expectedScore: Float,
        learningEfficiency: Float
    ): Float {
        val baseLoad = cognitiveProfile.cognitiveLoad
        val difficultyAdjustment = (100f - expectedScore) / 100f * 0.2f
        val efficiencyAdjustment = (1f - learningEfficiency) * 0.1f
        
        return (baseLoad + difficultyAdjustment + efficiencyAdjustment).coerceIn(0f, 1f)
    }
    
    private fun generateOptimalLearningPath(
        knowledgeMap: DeepPersonalizationEngine.KnowledgeMap,
        masteryPrediction: Map<String, Float>,
        cognitiveLoadPrediction: Float
    ): List<String> {
        val path = mutableListOf<String>()
        
        // 如果认知负荷高，优先选择简单的主题
        if (cognitiveLoadPrediction > 0.7f) {
            path.addAll(knowledgeMap.strengthAreas.take(2))
        }
        
        // 添加改进区域
        path.addAll(knowledgeMap.improvementAreas.take(2))
        
        // 添加下一步学习目标
        path.addAll(knowledgeMap.nextLearningTargets.take(2))
        
        return path.distinct()
    }
    
    private fun assessBurnoutRisk(
        learnerProfile: DeepPersonalizationEngine.LearnerProfile,
        learningHistory: List<LearningRecord>
    ): SpecificRisk {
        // 评估学习倦怠风险
        val recentSessions = learningHistory.takeLast(10)
        val avgDuration = recentSessions.map { it.duration }.average()
        val performanceDecline = recentSessions.take(5).map { it.score }.average() - 
                                recentSessions.takeLast(5).map { it.score }.average()
        
        val riskProbability = when {
            avgDuration > 150 && performanceDecline > 10 -> 0.9f
            avgDuration > 120 && performanceDecline > 5 -> 0.7f
            avgDuration > 90 && performanceDecline > 0 -> 0.5f
            else -> 0.2f
        }
        
        return SpecificRisk(
            riskType = "学习倦怠",
            probability = riskProbability,
            impact = if (riskProbability > 0.7f) "高" else "中",
            timeframe = "未来3-5天",
            indicators = listOf("学习时长过长", "成绩下降", "效率降低"),
            preventionStrategy = "调整学习强度，增加休息时间"
        )
    }
    
    private fun assessForgettingRisk(
        learnerProfile: DeepPersonalizationEngine.LearnerProfile,
        learningHistory: List<LearningRecord>
    ): SpecificRisk {
        val retentionRate = learnerProfile.performancePattern.retentionRate
        val riskProbability = 1f - retentionRate
        
        return SpecificRisk(
            riskType = "知识遗忘",
            probability = riskProbability,
            impact = "中",
            timeframe = "未来1-2周",
            indicators = listOf("长期未复习", "知识保持率低"),
            preventionStrategy = "建立系统复习计划"
        )
    }
    
    private fun assessMotivationDeclineRisk(
        learnerProfile: DeepPersonalizationEngine.LearnerProfile,
        learningHistory: List<LearningRecord>
    ): SpecificRisk {
        val motivationLevel = learnerProfile.motivationProfile.intrinsicMotivation
        val persistenceLevel = learnerProfile.motivationProfile.persistenceLevel
        
        val riskProbability = 1f - (motivationLevel + persistenceLevel) / 2f
        
        return SpecificRisk(
            riskType = "动机下降",
            probability = riskProbability,
            impact = "高",
            timeframe = "未来1周",
            indicators = listOf("学习频率下降", "主动性降低"),
            preventionStrategy = "调整学习目标，增加激励机制"
        )
    }
    
    private fun assessCognitiveOverloadRisk(
        learnerProfile: DeepPersonalizationEngine.LearnerProfile,
        learningHistory: List<LearningRecord>
    ): SpecificRisk {
        val cognitiveLoad = learnerProfile.cognitiveProfile.cognitiveLoad
        val workingMemoryCapacity = learnerProfile.cognitiveProfile.workingMemoryCapacity
        
        val riskProbability = cognitiveLoad / workingMemoryCapacity
        
        return SpecificRisk(
            riskType = "认知过载",
            probability = riskProbability.coerceIn(0f, 1f),
            impact = "高",
            timeframe = "当前",
            indicators = listOf("学习效率下降", "理解困难"),
            preventionStrategy = "降低学习难度，分解学习任务"
        )
    }
    
    private fun assessLearningStagnationRisk(
        learnerProfile: DeepPersonalizationEngine.LearnerProfile,
        learningHistory: List<LearningRecord>
    ): SpecificRisk {
        val improvementRate = learnerProfile.performancePattern.improvementRate
        val riskProbability = if (improvementRate < 0.05f) 0.8f else 0.3f
        
        return SpecificRisk(
            riskType = "学习停滞",
            probability = riskProbability,
            impact = "中",
            timeframe = "未来2-3周",
            indicators = listOf("成绩无改善", "学习方法单一"),
            preventionStrategy = "调整学习策略，寻找新的突破点"
        )
    }
    
    private fun calculatePredictionConfidence(
        learningHistory: List<LearningRecord>,
        learnerProfile: DeepPersonalizationEngine.LearnerProfile
    ): Float {
        val dataQuality = minOf(1f, learningHistory.size / 20f) // 20条记录为满分
        val consistencyScore = learnerProfile.performancePattern.consistencyScore
        val profileCompleteness = 0.8f // 假设画像完整度
        
        return (dataQuality * 0.4f + consistencyScore * 0.3f + profileCompleteness * 0.3f)
    }
    
    private fun identifyKeyFactors(
        learnerProfile: DeepPersonalizationEngine.LearnerProfile,
        performancePrediction: PerformancePrediction,
        riskAssessment: RiskAssessment
    ): List<String> {
        val factors = mutableListOf<String>()
        
        // 基于学习风格
        factors.add("学习风格: ${learnerProfile.learningStyle.primaryStyle}")
        
        // 基于认知能力
        if (learnerProfile.cognitiveProfile.cognitiveLoad > 0.7f) {
            factors.add("认知负荷较高")
        }
        
        // 基于动机水平
        if (learnerProfile.motivationProfile.intrinsicMotivation > 0.7f) {
            factors.add("内在动机强")
        }
        
        // 基于表现模式
        if (learnerProfile.performancePattern.consistencyScore > 0.8f) {
            factors.add("表现稳定")
        }
        
        // 基于风险因素
        riskAssessment.specificRisks.forEach { risk ->
            if (risk.probability > 0.6f) {
                factors.add("${risk.riskType}风险")
            }
        }
        
        return factors.take(5)
    }
    
    private fun generateBasicPrediction(user: User): LearningPrediction {
        return LearningPrediction(
            userId = user.id,
            predictionPeriod = "短期",
            performancePrediction = PerformancePrediction(
                expectedScore = 75f,
                scoreRange = Pair(65f, 85f),
                improvementProbability = 0.6f,
                masteryPrediction = emptyMap(),
                learningEfficiency = 0.6f,
                motivationTrend = "稳定",
                cognitiveLoadPrediction = 0.5f,
                optimalLearningPath = listOf("基础数学", "基础物理")
            ),
            riskAssessment = RiskAssessment(
                overallRiskLevel = "低",
                specificRisks = emptyList(),
                earlyWarningSignals = emptyList(),
                preventiveActions = listOf("保持当前学习节奏"),
                criticalInterventionPoints = emptyList(),
                riskFactors = emptyMap()
            ),
            interventionRecommendations = listOf(
                InterventionRecommendation(
                    interventionType = "基础",
                    priority = "中",
                    targetArea = "学习建立",
                    specificActions = listOf("建立规律学习习惯", "设置学习目标"),
                    expectedOutcome = "建立良好学习基础",
                    implementationSteps = listOf("制定学习计划", "开始基础练习"),
                    successMetrics = listOf("学习频率稳定"),
                    timeline = "持续实施"
                )
            ),
            confidenceLevel = 0.4f,
            keyFactors = listOf("新用户", "基础设置")
        )
    }
}
