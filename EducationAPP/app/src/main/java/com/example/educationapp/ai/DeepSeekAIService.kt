package com.example.educationapp.ai

import android.util.Log
import com.example.educationapp.data.LearningRecord
import com.example.educationapp.data.User
import com.example.educationapp.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * DeepSeek AI服务 - 集成真实的DeepSeek大模型API
 */
class DeepSeekAIService {
    
    private val apiService = NetworkClient.deepSeekApiService
    private val apiKeyValidator = ApiKeyValidator
    
    companion object {
        private const val TAG = "DeepSeekAIService"
    }
    
    /**
     * 发送聊天消息到DeepSeek API
     */
    suspend fun sendChatMessage(
        userMessage: String,
        user: User? = null,
        learningRecords: List<LearningRecord> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        // 使用优化器执行带重试机制的API调用
        AIServiceOptimizer.executeWithRetry(maxRetries = 3) {
            try {
                // 首先验证API密钥格式
                val keyValidation = apiKeyValidator.validateApiKeyFormat(ApiConstants.DEEPSEEK_API_KEY)
                if (keyValidation is ValidationResult.Error) {
                    Log.e(TAG, "API密钥验证失败: ${keyValidation.message}")
                    return@executeWithRetry Result.failure(Exception("API密钥配置错误: ${keyValidation.message}"))
                }
                
                Log.d(TAG, "发送消息到DeepSeek API: ${userMessage.take(50)}...")
                
                val systemPrompt = buildSystemPrompt(user, learningRecords)
                val optimizedUserMessage = AIServiceOptimizer.optimizePrompt(userMessage)
                
                val messages = listOf(
                    ChatMessage(role = "system", content = systemPrompt),
                    ChatMessage(role = "user", content = optimizedUserMessage)
                )
                
                val request = ChatRequest(messages = messages)
                val response = apiService.chatCompletion(
                    authorization = "Bearer ${ApiConstants.DEEPSEEK_API_KEY}",
                    contentType = ApiConstants.CONTENT_TYPE_JSON,
                    request = request
                )
                
                Log.d(TAG, "API响应状态码: ${response.code()}")
                
                if (response.isSuccessful) {
                    val chatResponse = response.body()
                    if (chatResponse != null && chatResponse.choices.isNotEmpty()) {
                        val aiMessage = chatResponse.choices[0].message.content
                        
                        // 验证响应质量
                        if (AIServiceOptimizer.validateResponse(aiMessage)) {
                            Log.d(TAG, "AI回复成功: ${aiMessage.take(100)}...")
                            Result.success(aiMessage)
                        } else {
                            Log.w(TAG, "AI回复质量不佳: $aiMessage")
                            Result.failure(Exception("AI回复质量不佳，请重试"))
                        }
                    } else {
                        Log.e(TAG, "API返回空响应")
                        Result.failure(Exception("API返回空响应"))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "API请求失败: ${response.code()} - $errorBody")
                    
                    // 解析错误信息
                    val apiError = apiKeyValidator.parseApiError(errorBody)
                    val userFriendlyMessage = apiKeyValidator.getUserFriendlyErrorMessage(apiError)
                    
                    Result.failure(Exception(userFriendlyMessage))
                }
            } catch (e: Exception) {
                Log.e(TAG, "发送消息失败", e)
                val friendlyMessage = AIServiceOptimizer.getUserFriendlyErrorMessage(e)
                Result.failure(Exception(friendlyMessage))
            }
        }
    }
    
    /**
     * 生成学习分析回复
     */
    suspend fun generateLearningAnalysis(
        user: User,
        learningRecords: List<LearningRecord>
    ): Result<String> = withContext(Dispatchers.IO) {
        val analysisPrompt = buildLearningAnalysisPrompt(user, learningRecords)
        sendChatMessage(analysisPrompt, user, learningRecords)
    }
    
    /**
     * 生成学习推荐
     */
    suspend fun generateRecommendations(
        user: User,
        learningRecords: List<LearningRecord>
    ): Result<String> = withContext(Dispatchers.IO) {
        val recommendationPrompt = buildRecommendationPrompt(user, learningRecords)
        sendChatMessage(recommendationPrompt, user, learningRecords)
    }
    
    /**
     * 生成学习计划
     */
    suspend fun generateLearningPlan(
        user: User,
        learningRecords: List<LearningRecord>
    ): Result<String> = withContext(Dispatchers.IO) {
        val planPrompt = buildLearningPlanPrompt(user, learningRecords)
        sendChatMessage(planPrompt, user, learningRecords)
    }
    
    /**
     * 分析错题
     */
    suspend fun analyzeMistakes(
        learningRecords: List<LearningRecord>
    ): Result<String> = withContext(Dispatchers.IO) {
        val mistakePrompt = buildMistakeAnalysisPrompt(learningRecords)
        sendChatMessage(mistakePrompt)
    }
    
    /**
     * 分析学习情绪
     */
    suspend fun analyzeLearningMood(
        user: User,
        learningRecords: List<LearningRecord>
    ): Result<String> = withContext(Dispatchers.IO) {
        val moodPrompt = buildMoodAnalysisPrompt(user, learningRecords)
        sendChatMessage(moodPrompt, user, learningRecords)
    }
    
    /**
     * 构建系统提示词
     */
    private fun buildSystemPrompt(user: User?, learningRecords: List<LearningRecord>): String {
        val userInfo = if (user != null) {
            """
            用户信息：
            - 姓名：${user.name}
            - 学习风格：${user.learningStyle}
            - 年级：${user.grade}
            - 兴趣：${user.interests}
            """.trimIndent()
        } else ""
        
        val learningData = if (learningRecords.isNotEmpty()) {
            val recentRecords = learningRecords.takeLast(10)
            val subjectPerformance = recentRecords.groupBy { it.subject }
                .mapValues { (_, records) -> records.map { it.score }.average() }
            
            """
            最近学习数据：
            ${subjectPerformance.entries.joinToString("\n") { (subject, avgScore) ->
                "- $subject: 平均分 ${String.format("%.1f", avgScore)}"
            }}
            """.trimIndent()
        } else ""
        
        return """
        你是一个专业的AI学习助手，专门帮助中国学生进行个性化学习指导。你的特点是：
        
        1. 专业性强：具备教育学、心理学知识，能够提供科学的学习建议
        2. 个性化：根据学生的具体情况制定针对性的学习方案
        3. 鼓励性：用积极正面的语言激励学生，增强学习动力
        4. 实用性：提供具体可操作的学习方法和建议
        5. 中文回复：所有回复都使用简体中文
        
        $userInfo
        
        $learningData
        
        请根据用户的问题和上述信息，提供专业、个性化、实用的学习建议。回复要简洁明了，重点突出，使用emoji增加亲和力。
        """.trimIndent()
    }
    
    /**
     * 构建学习分析提示词
     */
    private fun buildLearningAnalysisPrompt(user: User, learningRecords: List<LearningRecord>): String {
        val subjectStats = learningRecords.groupBy { it.subject }
            .mapValues { (_, records) -> 
                val avgScore = records.map { it.score }.average()
                val count = records.size
                "平均分：${String.format("%.1f", avgScore)}，练习次数：$count"
            }
        
        return """
        请分析我的学习情况，并给出专业建议：
        
        各科目表现：
        ${subjectStats.entries.joinToString("\n") { (subject, stats) ->
            "- $subject: $stats"
        }}
        
        请从以下角度进行分析：
        1. 优势科目和薄弱科目
        2. 学习效果评估
        3. 具体改进建议
        4. 学习策略调整
        
        请用鼓励性的语言，并提供具体可操作的建议。
        """.trimIndent()
    }
    
    /**
     * 构建推荐提示词
     */
    private fun buildRecommendationPrompt(user: User, learningRecords: List<LearningRecord>): String {
        val weakSubjects = learningRecords.groupBy { it.subject }
            .filter { (_, records) -> records.map { it.score }.average() < 80 }
            .keys.toList()
        
        return """
        请为我推荐学习资源：
        
        我的学习风格：${user.learningStyle}
        需要加强的科目：${weakSubjects.joinToString("、")}
        兴趣：${user.interests}
        
        请推荐：
        1. 适合我学习风格的资源类型
        2. 针对薄弱科目的具体资源
        3. 学习方法和工具建议
        4. 时间安排建议
        
        请提供具体、实用的推荐，并说明推荐理由。
        """.trimIndent()
    }
    
    /**
     * 构建学习计划提示词
     */
    private fun buildLearningPlanPrompt(user: User, learningRecords: List<LearningRecord>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        
        return """
        请为我制定一个个性化的学习计划：
        
        当前日期：$currentDate
        学习风格：${user.learningStyle}
        兴趣：${user.interests}
        年级：${user.grade}
        
        请制定：
        1. 本周学习计划（具体到每天）
        2. 本月学习目标
        3. 学习时间安排建议
        4. 重点学习内容
        5. 复习和练习安排
        
        计划要具体可行，符合我的学习风格，并包含进度检查点。
        """.trimIndent()
    }
    
    /**
     * 构建错题分析提示词
     */
    private fun buildMistakeAnalysisPrompt(learningRecords: List<LearningRecord>): String {
        val lowScoreRecords = learningRecords.filter { it.score < 70 }
        val mistakePatterns = lowScoreRecords.groupBy { it.subject }
            .mapValues { (_, records) -> records.size }
        
        return """
        请分析我的错题情况：
        
        低分练习记录：
        ${mistakePatterns.entries.joinToString("\n") { (subject, count) ->
            "- $subject: $count 次低分"
        }}
        
        请分析：
        1. 主要错误类型和原因
        2. 知识薄弱点识别
        3. 改进策略建议
        4. 针对性练习推荐
        5. 学习方法调整
        
        请提供具体的改进方案和练习建议。
        """.trimIndent()
    }
    
    /**
     * 构建情绪分析提示词
     */
    private fun buildMoodAnalysisPrompt(user: User, learningRecords: List<LearningRecord>): String {
        val recentRecords = learningRecords.takeLast(5)
        val avgScore = if (recentRecords.isNotEmpty()) {
            recentRecords.map { it.score }.average()
        } else 0.0
        
        return """
        请分析我的学习情绪状态：
        
        最近5次练习平均分：${String.format("%.1f", avgScore)}
        学习风格：${user.learningStyle}
        
        请从以下角度分析：
        1. 当前学习情绪状态
        2. 学习动力评估
        3. 压力水平分析
        4. 学习兴趣变化
        5. 情绪调节建议
        
        请用温暖、鼓励的语言，并提供实用的情绪管理建议。
        """.trimIndent()
    }
}
