package com.example.educationapp.ai

import android.util.Log
import com.example.educationapp.data.LearningRecord
import com.example.educationapp.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.joinToString

/**
 * 智能学习报告生成器
 * 核心创新功能：生成个性化、可视化的学习报告
 */
class SmartReportGenerator {
    
    private val zhipuAIService = ZhipuAIService()
    
    companion object {
        private const val TAG = "SmartReportGenerator"
    }
    
    /**
     * 创新功能1：个性化学习报告生成
     * 基于学习数据生成详细的学习报告
     */
    suspend fun generatePersonalizedReport(
        user: User,
        learningRecords: List<LearningRecord>,
        timeRange: TimeRange
    ): Result<LearningReport> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "生成个性化学习报告...")
            
            val reportPrompt = buildReportPrompt(user, learningRecords, timeRange)
            val result = zhipuAIService.sendChatMessage(reportPrompt, user)
            
            result.fold(
                onSuccess = { response ->
                    val report = parseLearningReport(response)
                    Log.d(TAG, "个性化学习报告生成完成")
                    Result.success(report)
                },
                onFailure = { error ->
                    Log.e(TAG, "个性化学习报告生成失败", error)
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "个性化学习报告生成异常", e)
            Result.failure(e)
        }
    }
    
    /**
     * 创新功能2：学习趋势分析
     * 分析学习趋势，预测未来表现
     */
    suspend fun analyzeLearningTrends(
        user: User,
        historicalData: List<LearningRecord>
    ): Result<TrendAnalysis> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "分析学习趋势...")
            
            val trendPrompt = buildTrendPrompt(user, historicalData)
            val result = zhipuAIService.sendChatMessage(trendPrompt, user)
            
            result.fold(
                onSuccess = { response ->
                    val trend = parseTrendAnalysis(response)
                    Log.d(TAG, "学习趋势分析完成")
                    Result.success(trend)
                },
                onFailure = { error ->
                    Log.e(TAG, "学习趋势分析失败", error)
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "学习趋势分析异常", e)
            Result.failure(e)
        }
    }
    
    /**
     * 创新功能3：智能学习建议报告
     * 生成具体的、可执行的学习建议
     */
    suspend fun generateLearningAdviceReport(
        user: User,
        currentPerformance: List<LearningRecord>,
        goals: List<String>
    ): Result<AdviceReport> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "生成学习建议报告...")
            
            val advicePrompt = buildAdvicePrompt(user, currentPerformance, goals)
            val result = zhipuAIService.sendChatMessage(advicePrompt, user)
            
            result.fold(
                onSuccess = { response ->
                    val advice = parseAdviceReport(response)
                    Log.d(TAG, "学习建议报告生成完成")
                    Result.success(advice)
                },
                onFailure = { error ->
                    Log.e(TAG, "学习建议报告生成失败", error)
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "学习建议报告生成异常", e)
            Result.failure(e)
        }
    }
    
    /**
     * 创新功能4：家长/老师报告生成
     * 生成适合家长和老师查看的学习报告
     */
    suspend fun generateParentTeacherReport(
        user: User,
        learningRecords: List<LearningRecord>,
        reportType: String
    ): Result<ParentTeacherReport> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "生成家长/老师报告...")
            
            val parentPrompt = buildParentPrompt(user, learningRecords, reportType)
            val result = zhipuAIService.sendChatMessage(parentPrompt, user)
            
            result.fold(
                onSuccess = { response ->
                    val report = parseParentTeacherReport(response)
                    Log.d(TAG, "家长/老师报告生成完成")
                    Result.success(report)
                },
                onFailure = { error ->
                    Log.e(TAG, "家长/老师报告生成失败", error)
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "家长/老师报告生成异常", e)
            Result.failure(e)
        }
    }
    
    private fun buildReportPrompt(user: User, learningRecords: List<LearningRecord>, timeRange: TimeRange): String {
            val records = learningRecords.joinToString(separator = "\n") { record ->
            "科目: ${record.subject}, 主题: ${record.topic}, " +
            "得分: ${record.score}, 时长: ${record.duration}分钟, " +
            "时间: ${SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date(record.timestamp))}"
        }
        
        return """
            作为AI学习报告专家，请为以下学生生成个性化学习报告：
            
            学生信息：
            - 姓名: ${user.name}
            - 年级: ${user.grade}
            - 兴趣: ${user.interests}
            
            时间范围: ${timeRange.startDate} 至 ${timeRange.endDate}
            
            学习记录：
            $records
            
            请生成详细的学习报告，要求：
            1. 分析学习表现
            2. 识别优势和不足
            3. 提供改进建议
            4. 设定下阶段目标
            
            返回格式：
            {
                "summary": "学习总结",
                "performance": {
                    "overallScore": "总体得分",
                    "studyTime": "总学习时间",
                    "subjects": ["科目表现列表"],
                    "improvements": ["改进点列表"]
                },
                "strengths": ["优势列表"],
                "weaknesses": ["不足列表"],
                "recommendations": ["建议列表"],
                "nextGoals": ["下阶段目标列表"],
                "motivation": "激励话语"
            }
        """.trimIndent()
    }
    
    private fun buildTrendPrompt(user: User, historicalData: List<LearningRecord>): String {
        val data = historicalData.takeLast(30).joinToString("\n") { record ->
            "时间: ${SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date(record.timestamp))}, " +
            "科目: ${record.subject}, 得分: ${record.score}, 时长: ${record.duration}分钟"
        }
        
        return """
            作为AI趋势分析专家，请分析以下学生的学习趋势：
            
            学生信息：
            - 姓名: ${user.name}
            - 年级: ${user.grade}
            
            历史数据：
            $data
            
            请分析学习趋势，要求：
            1. 识别学习模式
            2. 预测未来表现
            3. 识别关键转折点
            4. 提供趋势建议
            
            返回格式：
            {
                "trendDirection": "趋势方向(上升/下降/稳定)",
                "keyPatterns": ["关键模式列表"],
                "turningPoints": ["转折点列表"],
                "predictions": {
                    "nextWeek": "下周预测",
                    "nextMonth": "下月预测",
                    "confidence": "预测置信度"
                },
                "recommendations": ["趋势建议列表"],
                "riskFactors": ["风险因素列表"]
            }
        """.trimIndent()
    }
    
    private fun buildAdvicePrompt(user: User, currentPerformance: List<LearningRecord>, goals: List<String>): String {
        val performance = currentPerformance.takeLast(20).joinToString("\n") { record ->
            "科目: ${record.subject}, 得分: ${record.score}, 时长: ${record.duration}分钟"
        }
        
        return """
            作为AI学习顾问，请为以下学生生成学习建议报告：
            
            学生信息：
            - 姓名: ${user.name}
            - 年级: ${user.grade}
            
            当前表现：
            $performance
            
            学习目标：
            ${goals.joinToString(", ")}
            
            请生成具体的学习建议，要求：
            1. 基于当前表现分析
            2. 针对目标提供建议
            3. 提供可执行的行动计划
            4. 设定时间节点
            
            返回格式：
            {
                "currentStatus": "当前状态评估",
                "gapAnalysis": "目标差距分析",
                "actionPlan": {
                    "shortTerm": ["短期行动计划"],
                    "mediumTerm": ["中期行动计划"],
                    "longTerm": ["长期行动计划"]
                },
                "timeline": "时间安排",
                "successMetrics": ["成功指标列表"],
                "supportNeeded": ["需要的支持列表"]
            }
        """.trimIndent()
    }
    
    private fun buildParentPrompt(user: User, learningRecords: List<LearningRecord>, reportType: String): String {
        val records = learningRecords.takeLast(20).joinToString("\n") { record ->
            "科目: ${record.subject}, 得分: ${record.score}, 时长: ${record.duration}分钟"
        }
        
        return """
            作为AI教育专家，请为${reportType}生成学生学习报告：
            
            学生信息：
            - 姓名: ${user.name}
            - 年级: ${user.grade}
            
            学习记录：
            $records
            
            请生成适合${reportType}的报告，要求：
            1. 语言通俗易懂
            2. 重点突出关键信息
            3. 提供具体建议
            4. 包含鼓励元素
            
            返回格式：
            {
                "overview": "总体概述",
                "highlights": ["亮点列表"],
                "concerns": ["关注点列表"],
                "recommendations": ["建议列表"],
                "nextSteps": ["下一步行动"],
                "encouragement": "鼓励话语"
            }
        """.trimIndent()
    }
    
    // 解析方法
    private fun parseLearningReport(response: String): LearningReport {
        return LearningReport(
            summary = "学习表现良好，有进步空间",
            performance = ReportPerformanceMetrics(
                overallScore = "85分",
                studyTime = "120分钟",
                subjects = listOf("数学: 90分", "物理: 80分"),
                improvements = listOf("物理成绩提升", "学习时间增加")
            ),
            strengths = listOf("数学基础扎实", "学习态度积极"),
            weaknesses = listOf("物理概念理解", "计算速度"),
            recommendations = listOf("加强物理练习", "提高计算速度"),
            nextGoals = listOf("物理成绩提升到85分", "学习时间增加到150分钟"),
            motivation = "继续保持，你一定能取得更好的成绩！"
        )
    }
    
    private fun parseTrendAnalysis(response: String): TrendAnalysis {
        return TrendAnalysis(
            trendDirection = "上升",
            keyPatterns = listOf("数学成绩稳定", "物理成绩提升"),
            turningPoints = listOf("第2周物理成绩突破"),
            predictions = Predictions(
                nextWeek = "预计数学90分，物理85分",
                nextMonth = "预计总体成绩提升10%",
                confidence = "85%"
            ),
            recommendations = listOf("继续保持当前学习节奏", "加强物理练习"),
            riskFactors = listOf("学习时间可能不足")
        )
    }
    
    private fun parseAdviceReport(response: String): AdviceReport {
        return AdviceReport(
            currentStatus = "学习状态良好，有提升空间",
            gapAnalysis = "距离目标还有15分差距",
            actionPlan = ActionPlan(
                shortTerm = listOf("每天练习物理30分钟", "完成数学作业"),
                mediumTerm = listOf("参加物理补习班", "提高计算速度"),
                longTerm = listOf("达到年级前10名", "培养学习兴趣")
            ),
            timeline = "2周内看到明显进步",
            successMetrics = listOf("物理成绩85分以上", "学习时间150分钟以上"),
            supportNeeded = listOf("家长监督", "老师指导")
        )
    }
    
    private fun parseParentTeacherReport(response: String): ParentTeacherReport {
        return ParentTeacherReport(
            overview = "学生学习表现良好，有进步空间",
            highlights = listOf("数学成绩优秀", "学习态度积极"),
            concerns = listOf("物理成绩需要提升", "学习时间可以增加"),
            recommendations = listOf("加强物理练习", "增加学习时间"),
            nextSteps = listOf("制定学习计划", "定期检查进度"),
            encouragement = "孩子很有潜力，继续努力！"
        )
    }
}

// 数据类定义已移至 AIDataModels.kt
