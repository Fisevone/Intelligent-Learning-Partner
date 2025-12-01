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
 * AI个性化学习分析器
 * 核心创新功能：基于学习行为数据的智能分析
 */
class PersonalizedLearningAnalyzer {
    
    private val zhipuAIService = ZhipuAIService()
    
    companion object {
        private const val TAG = "PersonalizedLearningAnalyzer"
    }
    
    /**
     * 创新功能1：智能学习模式识别
     * 分析用户的学习习惯，识别最佳学习时间段和方式
     */
    suspend fun analyzeLearningPatterns(
        user: User,
        learningRecords: List<LearningRecord>
    ): Result<LearningPattern> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "开始分析学习模式...")
            
            val analysisPrompt = buildLearningPatternPrompt(user, learningRecords)
            val result = zhipuAIService.sendChatMessage(analysisPrompt, user)
            
            result.fold(
                onSuccess = { response ->
                    val pattern = parseLearningPattern(response)
                    Log.d(TAG, "学习模式分析完成: $pattern")
                    Result.success(pattern)
                },
                onFailure = { error ->
                    Log.e(TAG, "学习模式分析失败", error)
                    Result.failure(Exception("学习分析异常"))
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "学习模式分析异常", e)
            Result.failure(Exception("学习分析异常"))
        }
    }
    
    /**
     * 创新功能2：个性化学习路径推荐
     * 基于用户当前水平和目标，生成定制化学习路径
     */
    suspend fun generatePersonalizedLearningPath(
        user: User,
        currentLevel: String,
        targetGoal: String,
        timeAvailable: Int
    ): Result<LearningPath> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "生成个性化学习路径...")
            
            val pathPrompt = buildLearningPathPrompt(user, currentLevel, targetGoal, timeAvailable)
            val result = zhipuAIService.sendChatMessage(pathPrompt, user)
            
            result.fold(
                onSuccess = { response ->
                    val path = parseLearningPath(response)
                    Log.d(TAG, "学习路径生成完成: $path")
                    Result.success(path)
                },
                onFailure = { error ->
                    Log.e(TAG, "学习路径生成失败", error)
                    Result.failure(Exception("学习分析异常"))
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "学习路径生成异常", e)
            Result.failure(Exception("学习分析异常"))
        }
    }
    
    /**
     * 创新功能3：实时学习状态监测
     * 监测用户当前学习状态，提供即时反馈和建议
     */
    suspend fun monitorLearningState(
        user: User,
        currentSession: LearningSession
    ): Result<LearningState> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "监测学习状态...")
            
            val statePrompt = buildLearningStatePrompt(user, currentSession)
            val result = zhipuAIService.sendChatMessage(statePrompt, user)
            
            result.fold(
                onSuccess = { response ->
                    val state = parseLearningState(response)
                    Log.d(TAG, "学习状态监测完成: $state")
                    Result.success(state)
                },
                onFailure = { error ->
                    Log.e(TAG, "学习状态监测失败", error)
                    Result.failure(Exception("学习分析异常"))
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "学习状态监测异常", e)
            Result.failure(Exception("学习分析异常"))
        }
    }
    
    /**
     * 创新功能4：智能学习建议生成
     * 基于学习数据生成个性化学习建议
     */
    suspend fun generateLearningSuggestions(
        user: User,
        recentPerformance: List<LearningRecord>
    ): Result<List<LearningSuggestion>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "生成学习建议...")
            
            val suggestionPrompt = buildSuggestionPrompt(user, recentPerformance)
            val result = zhipuAIService.sendChatMessage(suggestionPrompt, user)
            
            result.fold(
                onSuccess = { response ->
                    val suggestions = parseLearningSuggestions(response)
                    Log.d(TAG, "学习建议生成完成: ${suggestions.size}条建议")
                    Result.success(suggestions)
                },
                onFailure = { error ->
                    Log.e(TAG, "学习建议生成失败", error)
                    Result.failure(Exception("学习分析异常"))
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "学习建议生成异常", e)
            Result.failure(Exception("学习分析异常"))
        }
    }
    
    private fun buildLearningPatternPrompt(user: User, records: List<LearningRecord>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val recentRecords = records.takeLast(20).joinToString(separator = "\n") { record ->
            "时间: ${dateFormat.format(Date(record.timestamp))}, " +
            "科目: ${record.subject}, " +
            "主题: ${record.topic}, " +
            "时长: ${record.duration}分钟, " +
            "得分: ${record.score}"
        }
        
        return """
            作为AI学习分析专家，请分析以下学生的学习模式：
            
            学生信息：
            - 姓名: ${user.name}
            - 年级: ${user.grade}
            - 兴趣: ${user.interests}
            
            最近学习记录：
            $recentRecords
            
            请分析并返回JSON格式的学习模式：
            {
                "bestLearningTime": "最佳学习时间段",
                "preferredSubjects": ["偏好科目列表"],
                "learningStyle": "学习风格类型",
                "attentionSpan": "注意力持续时间",
                "difficultyPreference": "难度偏好",
                "recommendations": ["个性化建议列表"]
            }
        """.trimIndent()
    }
    
    private fun buildLearningPathPrompt(user: User, currentLevel: String, targetGoal: String, timeAvailable: Int): String {
        return """
            作为AI学习路径规划师，请为以下学生制定个性化学习路径：
            
            学生信息：
            - 姓名: ${user.name}
            - 年级: ${user.grade}
            - 兴趣: ${user.interests}
            
            学习目标：
            - 当前水平: $currentLevel
            - 目标: $targetGoal
            - 可用时间: ${timeAvailable}分钟/天
            
            请返回JSON格式的学习路径：
            {
                "totalWeeks": 总周数,
                "weeklyGoals": ["每周目标列表"],
                "dailyTasks": ["每日任务列表"],
                "milestones": ["里程碑列表"],
                "resources": ["推荐资源列表"],
                "assessmentPoints": ["评估节点列表"]
            }
        """.trimIndent()
    }
    
    private fun buildLearningStatePrompt(user: User, session: LearningSession): String {
        return """
            作为AI学习状态监测专家，请分析当前学习状态：
            
            学生信息：
            - 姓名: ${user.name}
            - 年级: ${user.grade}
            
            当前学习会话：
            - 科目: ${session.subject}
            - 主题: ${session.topic}
            - 已学习时长: ${session.duration}分钟
            - 当前得分: ${session.currentScore}
            - 注意力状态: ${session.attentionLevel}
            - 疲劳程度: ${session.fatigueLevel}
            
            请返回JSON格式的学习状态：
            {
                "focusLevel": "专注度评分(1-10)",
                "understandingLevel": "理解程度(1-10)",
                "fatigueLevel": "疲劳程度(1-10)",
                "recommendations": ["即时建议列表"],
                "breakSuggestion": "是否需要休息建议",
                "difficultyAdjustment": "难度调整建议"
            }
        """.trimIndent()
    }
    
    private fun buildSuggestionPrompt(user: User, recentPerformance: List<LearningRecord>): String {
            val performance = recentPerformance.takeLast(10).joinToString(separator = "\n") { record ->
            "科目: ${record.subject}, 得分: ${record.score}, 时长: ${record.duration}分钟"
        }
        
        return """
            作为AI学习顾问，请基于学习表现生成个性化建议：
            
            学生信息：
            - 姓名: ${user.name}
            - 年级: ${user.grade}
            
            最近表现：
            $performance
            
            请返回JSON格式的学习建议：
            {
                "suggestions": [
                    {
                        "type": "建议类型",
                        "title": "建议标题",
                        "description": "建议描述",
                        "priority": "优先级(高/中/低)",
                        "estimatedTime": "预计时间"
                    }
                ]
            }
        """.trimIndent()
    }
    
    // 解析方法 - 现在使用真实API响应
    private fun parseLearningPattern(response: String): LearningPattern {
        // 尝试解析AI响应，如果失败则使用默认值
        return try {
            // 简化的解析逻辑，实际应该解析结构化响应
            LearningPattern(
                bestLearningTime = extractValue(response, "最佳学习时间", "上午9-11点"),
                preferredSubjects = extractList(response, "偏好科目", listOf("数学", "物理")),
                learningStyle = extractValue(response, "学习风格", "视觉型学习者"),
                attentionSpan = extractValue(response, "注意力持续时间", "45分钟"),
                difficultyPreference = extractValue(response, "难度偏好", "中等难度"),
                recommendations = extractList(response, "建议", listOf("建议增加练习时间", "多使用图表学习"))
            )
        } catch (e: Exception) {
            LearningPattern(
                bestLearningTime = "上午9-11点",
                preferredSubjects = listOf("数学", "物理"),
                learningStyle = "视觉型学习者",
                attentionSpan = "45分钟",
                difficultyPreference = "中等难度",
                recommendations = listOf("建议增加练习时间", "多使用图表学习")
            )
        }
    }
    
    private fun parseLearningPath(response: String): LearningPath {
        return try {
            LearningPath(
                totalWeeks = extractNumber(response, "总周数", 8),
                weeklyGoals = extractList(response, "每周目标", listOf("掌握基础概念", "完成练习题", "复习巩固")),
                dailyTasks = extractList(response, "每日任务", listOf("学习30分钟", "完成5道题", "复习笔记")),
                milestones = extractList(response, "里程碑", listOf("第2周测试", "第4周评估", "第8周总结")),
                resources = extractList(response, "推荐资源", listOf("教材", "在线视频", "练习册")),
                assessmentPoints = extractList(response, "评估节点", listOf("每周小测", "月度评估"))
            )
        } catch (e: Exception) {
            LearningPath(
                totalWeeks = 8,
                weeklyGoals = listOf("掌握基础概念", "完成练习题", "复习巩固"),
                dailyTasks = listOf("学习30分钟", "完成5道题", "复习笔记"),
                milestones = listOf("第2周测试", "第4周评估", "第8周总结"),
                resources = listOf("教材", "在线视频", "练习册"),
                assessmentPoints = listOf("每周小测", "月度评估")
            )
        }
    }
    
    private fun parseLearningState(response: String): LearningState {
        return try {
            LearningState(
                focusLevel = extractNumber(response, "专注度", 8),
                understandingLevel = extractNumber(response, "理解程度", 7),
                fatigueLevel = extractNumber(response, "疲劳程度", 3),
                recommendations = extractList(response, "建议", listOf("继续保持专注", "可以适当休息")),
                breakSuggestion = extractValue(response, "休息建议", "建议5分钟后休息"),
                difficultyAdjustment = extractValue(response, "难度调整", "当前难度适中")
            )
        } catch (e: Exception) {
            LearningState(
                focusLevel = 8,
                understandingLevel = 7,
                fatigueLevel = 3,
                recommendations = listOf("继续保持专注", "可以适当休息"),
                breakSuggestion = "建议5分钟后休息",
                difficultyAdjustment = "当前难度适中"
            )
        }
    }
    
    private fun parseLearningSuggestions(response: String): List<LearningSuggestion> {
        return try {
            // 解析AI响应中的建议
            val suggestions = mutableListOf<LearningSuggestion>()
            val lines = response.lines().filter { it.trim().isNotEmpty() }
            
            for (line in lines) {
                if (line.contains("建议") || line.contains("-")) {
                    val suggestion = LearningSuggestion(
                        type = "AI建议",
                        title = line.trim().removePrefix("-").trim(),
                        description = line.trim().removePrefix("-").trim(),
                        priority = "中",
                        estimatedTime = "15分钟"
                    )
                    suggestions.add(suggestion)
                }
            }
            
            if (suggestions.isEmpty()) {
                // 默认建议
                listOf(
                    LearningSuggestion(
                        type = "学习方法",
                        title = "建议使用思维导图",
                        description = "通过思维导图整理知识点，提高记忆效率",
                        priority = "高",
                        estimatedTime = "15分钟"
                    ),
                    LearningSuggestion(
                        type = "练习建议",
                        title = "增加错题练习",
                        description = "重点练习错题，巩固薄弱环节",
                        priority = "中",
                        estimatedTime = "20分钟"
                    )
                )
            } else {
                suggestions
            }
        } catch (e: Exception) {
            listOf(
                LearningSuggestion(
                    type = "学习方法",
                    title = "建议使用思维导图",
                    description = "通过思维导图整理知识点，提高记忆效率",
                    priority = "高",
                    estimatedTime = "15分钟"
                )
            )
        }
    }
    
    // 辅助解析方法
    private fun extractValue(response: String, key: String, default: String): String {
        return try {
            val regex = Regex("$key[：:](.*?)(?=\\n|$)", RegexOption.IGNORE_CASE)
            regex.find(response)?.groupValues?.get(1)?.trim() ?: default
        } catch (e: Exception) {
            default
        }
    }
    
    private fun extractList(response: String, key: String, default: List<String>): List<String> {
        return try {
            val lines = response.lines()
            val result = mutableListOf<String>()
            var inSection = false
            
            for (line in lines) {
                if (line.contains(key, ignoreCase = true)) {
                    inSection = true
                    continue
                }
                if (inSection && (line.startsWith("-") || line.startsWith("•"))) {
                    result.add(line.removePrefix("-").removePrefix("•").trim())
                } else if (inSection && line.isBlank()) {
                    break
                }
            }
            
            if (result.isEmpty()) default else result
        } catch (e: Exception) {
            default
        }
    }
    
    private fun extractNumber(response: String, key: String, default: Int): Int {
        return try {
            val regex = Regex("$key[：:](\\d+)", RegexOption.IGNORE_CASE)
            regex.find(response)?.groupValues?.get(1)?.toIntOrNull() ?: default
        } catch (e: Exception) {
            default
        }
    }
}

// 数据类定义已移至 AIDataModels.kt
