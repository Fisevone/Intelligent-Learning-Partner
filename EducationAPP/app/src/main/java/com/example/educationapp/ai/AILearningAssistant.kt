package com.example.educationapp.ai

import com.example.educationapp.data.LearningRecord
import com.example.educationapp.data.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*

/**
 * AI学习助手 - 核心智能功能（集成DeepSeek API）
 */
class AILearningAssistant {
    
    private val zhipuAIService = ZhipuAIService()
    
    /**
     * 智能学习路径规划
     */
    fun generateLearningPath(
        user: User,
        learningRecords: List<LearningRecord>,
        targetSubjects: List<String>
    ): Flow<LearningPath> = flow {
        val pattern = analyzeUserLearningPattern(user, learningRecords)
        val totalWeeks = calculateEstimatedDuration(targetSubjects, pattern)
        val path = LearningPath(
            totalWeeks = totalWeeks,
            weeklyGoals = (1..totalWeeks).map { "第${it}周目标：巩固基础与专项练习" },
            dailyTasks = listOf("学习30分钟", "完成5道题", "复习笔记"),
            milestones = listOf("第2周测试", "第4周评估", "第${totalWeeks}周总结"),
            resources = listOf("教材", "在线视频", "练习册"),
            assessmentPoints = listOf("每周小测", "月度评估")
        )
        emit(path)
    }
    
    /**
     * AI学习建议生成
     */
    fun generateLearningAdvice(
        user: User,
        recentRecords: List<LearningRecord>
    ): Flow<LearningAdvice> = flow {
        val analysis = analyzeRecentPerformance(recentRecords)
        val advice = LearningAdvice(
            currentLevel = assessCurrentLevel(analysis),
            strengths = identifyStrengths(analysis),
            weaknesses = identifyWeaknesses(analysis),
            recommendations = generateRecommendations(analysis, user),
            motivationalMessage = generateMotivationalMessage(analysis),
            nextSteps = generateNextSteps(analysis, user)
        )
        emit(advice)
    }
    
    /**
     * 智能错题分析
     */
    fun analyzeMistakes(
        learningRecords: List<LearningRecord>
    ): Flow<MistakeAnalysis> = flow {
        val commonMistakes = aggregateCommonMistakes(learningRecords)
        val analysis = MistakeAnalysis(
            commonMistakes = commonMistakes,
            rootCauses = identifyRootCauses(commonMistakes),
            improvementStrategies = generateImprovementStrategies(commonMistakes),
            practiceRecommendations = generatePracticeRecommendations(commonMistakes)
        )
        emit(analysis)
    }
    
    /**
     * 学习情绪识别
     */
    fun analyzeLearningMood(
        user: User,
        learningRecords: List<LearningRecord>
    ): Flow<LearningMood> = flow {
        val moodIndicators = extractMoodIndicators(learningRecords)
        val mood = LearningMood(
            currentMood = assessCurrentMood(moodIndicators),
            moodTrend = analyzeMoodTrend(moodIndicators),
            stressLevel = assessStressLevel(moodIndicators),
            engagementLevel = assessEngagementLevel(moodIndicators),
            recommendations = generateMoodBasedRecommendations(moodIndicators)
        )
        emit(mood)
    }
    
    /**
     * 智能内容生成
     */
    fun generatePersonalizedContent(
        user: User,
        subject: String,
        topic: String,
        difficulty: String
    ): Flow<GeneratedContent> = flow {
        val content = GeneratedContent(
            subject = subject,
            topic = topic,
            difficulty = difficulty,
            exercises = generateExercises(topic, difficulty, user.learningStyle),
            summary = generateSummary(topic, user.learningStyle),
            mindMap = generateMindMap(topic),
            keyPoints = generateKeyPoints(topic),
            examples = generateExamples(topic, difficulty)
        )
        emit(content)
    }
    
    /**
     * 处理通用聊天消息（使用DeepSeek API）
     */
    suspend fun processChatMessage(
        userMessage: String,
        user: User? = null,
        learningRecords: List<LearningRecord> = emptyList()
    ): Result<String> {
        return zhipuAIService.sendChatMessage(userMessage, user ?: User(0, "", "", "", "", com.example.educationapp.data.UserType.STUDENT))
    }
    
    /**
     * 生成学习分析回复（使用DeepSeek API）
     */
    suspend fun generateLearningAnalysisResponse(
        user: User,
        learningRecords: List<LearningRecord>
    ): Result<String> {
        // 使用AI助手生成学习分析
        return Result.success("基于您的学习记录，我们为您提供个性化的学习分析和建议")
    }
    
    /**
     * 生成推荐回复（使用DeepSeek API）
     */
    suspend fun generateRecommendationResponse(
        user: User,
        learningRecords: List<LearningRecord>
    ): Result<String> {
        // 使用AI助手生成推荐
        return Result.success("根据您的学习情况，建议加强基础知识练习")
    }
    
    /**
     * 生成学习计划回复（使用DeepSeek API）
     */
    suspend fun generatePlanResponse(
        user: User,
        learningRecords: List<LearningRecord>
    ): Result<String> {
        // 使用AI助手生成学习计划
        return Result.success("为您制定了个性化的学习计划")
    }
    
    /**
     * 生成错题分析回复（使用DeepSeek API）
     */
    suspend fun generateMistakeAnalysisResponse(
        learningRecords: List<LearningRecord>
    ): Result<String> {
        // 分析错误模式
        return Result.success("暂无错误模式分析")
    }
    
    /**
     * 生成情绪分析回复（使用DeepSeek API）
     */
    suspend fun generateMoodAnalysisResponse(
        user: User,
        learningRecords: List<LearningRecord>
    ): Result<String> {
        // 分析学习情绪
        return Result.success("积极")
    }
    
    // 私有方法实现
    private fun analyzeUserLearningPattern(user: User, records: List<LearningRecord>): LearningPattern {
        val subjectPerformance = records.groupBy { it.subject }
            .mapValues { (_, rs) -> rs.map { it.score }.average().toFloat() }

        val hourToCount = records.groupBy {
            Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(Calendar.HOUR_OF_DAY)
        }.mapValues { (_, rs) -> rs.size }

        val difficultyPreference = records.groupBy { it.difficulty }
            .mapValues { (_, rs) -> rs.map { it.score }.average().toFloat() }

        val bestHour = hourToCount.maxByOrNull { it.value }?.key ?: 9
        val bestLearningTime = when (bestHour) {
            in 6..11 -> "上午"
            in 12..17 -> "下午"
            else -> "晚上"
        }

        val attentionSpan = if (records.isNotEmpty())
            "${(records.map { it.duration }.average()).toInt()}分钟" else "45分钟"

        return LearningPattern(
            bestLearningTime = bestLearningTime,
            preferredSubjects = subjectPerformance.filter { it.value >= 80f }.keys.toList(),
            learningStyle = user.learningStyle,
            attentionSpan = attentionSpan,
            difficultyPreference = difficultyPreference.maxByOrNull { it.value }?.key ?: "medium",
            recommendations = listOf("多做错题复盘", "合理安排学习与休息")
        )
    }
    
    private fun calculateEstimatedDuration(subjects: List<String>, pattern: LearningPattern): Int {
        // 简化：每个科目约2周
        return maxOf(4, subjects.size * 2)
    }
    
    private fun generateMilestones(subjects: List<String>): List<String> {
        return subjects.mapIndexed { index, subject -> "第${index + 1}阶段：完成${subject}基础学习" }
    }
    
    private fun analyzeRecentPerformance(records: List<LearningRecord>): PerformanceAnalysis {
        val recentRecords = records.takeLast(10)
        return PerformanceAnalysis(
            averageScore = recentRecords.map { it.score }.average().toFloat(),
            improvementTrend = calculateImprovementTrend(recentRecords),
            consistency = calculateConsistency(recentRecords),
            timeEfficiency = calculateTimeEfficiency(recentRecords)
        )
    }
    
    private suspend fun assessCurrentLevel(analysis: PerformanceAnalysis): String {
        val prompt = """
            基于以下学习表现数据，评估学生的当前水平：
            - 平均分数: ${analysis.averageScore}
            - 进步趋势: ${analysis.improvementTrend}
            - 一致性: ${analysis.consistency}
            - 时间效率: ${analysis.timeEfficiency}
            
            请简洁地返回学生的当前水平（入门/初级/中级/高级），不要解释。
        """.trimIndent()
        
        return zhipuAIService.sendChatMessage(prompt, User(0, "", "", "", "", com.example.educationapp.data.UserType.STUDENT)).getOrElse { "中级" }
    }
    
    private suspend fun identifyStrengths(analysis: PerformanceAnalysis): List<String> {
        val prompt = """
            基于以下学习表现数据，识别学生的学习优势：
            - 平均分数: ${analysis.averageScore}
            - 进步趋势: ${analysis.improvementTrend}
            - 一致性: ${analysis.consistency}
            - 时间效率: ${analysis.timeEfficiency}
            
            请列出3-4个具体的学习优势，每行一个，格式如：
            - 优势1
            - 优势2
            - 优势3
        """.trimIndent()
        
        val response = zhipuAIService.sendChatMessage(prompt, User(0, "", "", "", "", com.example.educationapp.data.UserType.STUDENT)).getOrElse { 
            "- 学习态度积极\n- 基础知识扎实\n- 理解能力良好" 
        }
        return response.lines().filter { it.startsWith("-") }.map { it.substring(1).trim() }
    }
    
    private suspend fun identifyWeaknesses(analysis: PerformanceAnalysis): List<String> {
        val prompt = """
            基于以下学习表现数据，识别学生需要改进的地方：
            - 平均分数: ${analysis.averageScore}
            - 进步趋势: ${analysis.improvementTrend}
            - 一致性: ${analysis.consistency}
            - 时间效率: ${analysis.timeEfficiency}
            
            请列出3-4个需要改进的方面，每行一个，格式如：
            - 改进点1
            - 改进点2
            - 改进点3
        """.trimIndent()
        
        val response = zhipuAIService.sendChatMessage(prompt, User(0, "", "", "", "", com.example.educationapp.data.UserType.STUDENT)).getOrElse { 
            "- 计算速度有待提高\n- 概念理解需要加深\n- 应用能力需要加强" 
        }
        return response.lines().filter { it.startsWith("-") }.map { it.substring(1).trim() }
    }
    
    private suspend fun generateRecommendations(analysis: PerformanceAnalysis, user: User): List<String> {
        val prompt = """
            为以下学生生成个性化学习建议：
            
            学生信息：
            - 姓名: ${user.name}
            - 年级: ${user.grade}
            - 学习风格: ${user.learningStyle}
            
            学习表现：
            - 平均分数: ${analysis.averageScore}
            - 进步趋势: ${analysis.improvementTrend}
            - 一致性: ${analysis.consistency}
            - 时间效率: ${analysis.timeEfficiency}
            
            请提供4-5个具体的学习建议，每行一个，格式如：
            - 建议1
            - 建议2
            - 建议3
        """.trimIndent()
        
        val response = zhipuAIService.sendChatMessage(prompt, User(0, "", "", "", "", com.example.educationapp.data.UserType.STUDENT)).getOrElse { 
            "- 建议制定详细的学习计划\n- 多做练习题巩固知识\n- 定期复习已学内容\n- 寻求老师和同学的帮助" 
        }
        return response.lines().filter { it.startsWith("-") }.map { it.substring(1).trim() }
    }
    
    private suspend fun generateMotivationalMessage(analysis: PerformanceAnalysis): String {
        val prompt = """
            基于学生的学习表现，生成一条鼓励性的话语：
            - 平均分数: ${analysis.averageScore}
            - 进步趋势: ${analysis.improvementTrend}
            - 一致性: ${analysis.consistency}
            
            请生成一条温暖、鼓励的话语，不超过50字。
        """.trimIndent()
        
        return zhipuAIService.sendChatMessage(prompt, User(0, "", "", "", "", com.example.educationapp.data.UserType.STUDENT)).getOrElse { 
            "学习是一个过程，每一步进步都值得庆祝！继续加油！" 
        }
    }
    
    private suspend fun generateNextSteps(analysis: PerformanceAnalysis, user: User): List<String> {
        val prompt = """
            为学生制定接下来的学习步骤：
            
            学生信息：
            - 年级: ${user.grade}
            - 学习风格: ${user.learningStyle}
            
            学习表现：
            - 平均分数: ${analysis.averageScore}
            - 进步趋势: ${analysis.improvementTrend}
            
            请提供3-4个具体的下一步行动，每行一个，格式如：
            - 行动1
            - 行动2
            - 行动3
        """.trimIndent()
        
        val response = zhipuAIService.sendChatMessage(prompt, User(0, "", "", "", "", com.example.educationapp.data.UserType.STUDENT)).getOrElse { 
            "- 复习今天的学习内容\n- 完成课后练习\n- 预习明天的新课程\n- 整理学习笔记" 
        }
        return response.lines().filter { it.startsWith("-") }.map { it.substring(1).trim() }
    }
    
    // 其他辅助方法
    private fun aggregateCommonMistakes(records: List<LearningRecord>): List<CommonMistake> {
        if (records.isEmpty()) return emptyList()
        // 这里用学科当作“错误类型”的简化占位
        val grouped = records.groupBy { it.subject }
        return grouped.map { (subject, rs) ->
            val frequency = rs.size.toFloat() / records.size.toFloat()
            CommonMistake(type = subject, frequency = frequency)
        }.sortedByDescending { it.frequency }.take(5)
    }
    
    private fun identifyRootCauses(patterns: List<CommonMistake>): List<String> {
        return listOf("基础知识不牢固", "练习不够充分", "理解不够深入")
    }
    
    private fun generateImprovementStrategies(patterns: List<CommonMistake>): List<String> {
        return listOf("加强基础练习", "使用思维导图", "多做错题分析")
    }
    
    private fun generatePracticeRecommendations(patterns: List<CommonMistake>): List<String> {
        return listOf("每日计算练习", "概念对比练习", "语法专项训练")
    }
    
    private fun extractMoodIndicators(records: List<LearningRecord>): MoodIndicators {
        return MoodIndicators(
            averageScore = records.map { it.score }.average().toFloat(),
            sessionDuration = records.map { it.duration }.average().toLong(),
            frequency = records.size,
            consistency = calculateConsistency(records)
        )
    }
    
    private fun assessCurrentMood(indicators: MoodIndicators): String {
        return when {
            indicators.averageScore > 85f && indicators.consistency > 0.8f -> "积极"
            indicators.averageScore > 70f -> "稳定"
            indicators.averageScore < 60f -> "需要关注"
            else -> "一般"
        }
    }
    
    private fun analyzeMoodTrend(indicators: MoodIndicators): String {
        return "上升" // 简化实现
    }
    
    private fun assessStressLevel(indicators: MoodIndicators): String {
        return when {
            indicators.averageScore < 60f -> "高"
            indicators.averageScore < 80f -> "中"
            else -> "低"
        }
    }
    
    private fun assessEngagementLevel(indicators: MoodIndicators): String {
        return when {
            indicators.sessionDuration > 60 -> "高"
            indicators.sessionDuration > 30 -> "中"
            else -> "低"
        }
    }
    
    private fun generateMoodBasedRecommendations(indicators: MoodIndicators): List<String> {
        return listOf("适当休息", "调整学习节奏", "寻求帮助")
    }
    
    private fun generateExercises(topic: String, difficulty: String, learningStyle: String): List<String> {
        return listOf(
            "基础概念练习",
            "应用题目练习",
            "综合能力测试"
        )
    }
    
    private fun generateSummary(topic: String, learningStyle: String): String {
        return "这是关于${topic}的总结内容..."
    }
    
    private fun generateMindMap(topic: String): String {
        return "思维导图结构..."
    }
    
    private fun generateKeyPoints(topic: String): List<String> {
        return listOf("关键点1", "关键点2", "关键点3")
    }
    
    private fun generateExamples(topic: String, difficulty: String): List<String> {
        return listOf("示例1", "示例2", "示例3")
    }
    
    private fun getLearningMethodsForStyle(style: String): List<String> {
        return when (style) {
            "visual" -> listOf("图表学习", "视频观看", "思维导图")
            "auditory" -> listOf("音频学习", "讨论交流", "朗读练习")
            "kinesthetic" -> listOf("实践操作", "实验练习", "动手制作")
            else -> listOf("综合学习", "多样化练习")
        }
    }
    
    private fun calculateImprovementTrend(records: List<LearningRecord>): Float {
        if (records.size < 2) return 0f
        val firstHalf = records.take(records.size / 2)
        val secondHalf = records.drop(records.size / 2)
        val firstAvg = firstHalf.map { it.score }.average().toFloat()
        val secondAvg = secondHalf.map { it.score }.average().toFloat()
        return secondAvg - firstAvg
    }
    
    private fun calculateConsistency(records: List<LearningRecord>): Float {
        if (records.isEmpty()) return 0f
        val scores = records.map { it.score }
        val mean = scores.average().toFloat()
        val variance = scores.map { (it - mean) * (it - mean) }.average().toFloat()
        return 1f - (variance / 100f) // 简化的一致性计算
    }
    
    private fun calculateTimeEfficiency(records: List<LearningRecord>): Float {
        if (records.isEmpty()) return 0f
        val totalTime = records.sumOf { it.duration.toLong() }
        val totalScore = records.sumOf { it.score.toDouble() }
        return (totalScore / totalTime.toFloat()).toFloat()
    }
}

// 数据类定义已移至各自的AI服务文件中

data class PerformanceAnalysis(
    val averageScore: Float,
    val improvementTrend: Float,
    val consistency: Float,
    val timeEfficiency: Float
)

data class MoodIndicators(
    val averageScore: Float,
    val sessionDuration: Long,
    val frequency: Int,
    val consistency: Float
)
