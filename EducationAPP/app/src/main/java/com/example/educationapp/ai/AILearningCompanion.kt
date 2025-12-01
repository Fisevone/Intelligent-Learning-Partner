package com.example.educationapp.ai

import android.util.Log
import com.example.educationapp.data.LearningRecord
import com.example.educationapp.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * AI学习伙伴
 * 核心创新功能：24/7智能学习陪伴和指导
 */
class AILearningCompanion {
    
    private val zhipuAIService = ZhipuAIService()
    
    companion object {
        private const val TAG = "AILearningCompanion"
    }
    
    /**
     * 创新功能1：智能学习伙伴对话
     * 模拟真实学习伙伴，提供情感支持和学习指导
     */
    suspend fun chatWithCompanion(
        user: User,
        userMessage: String,
        context: LearningContext
    ): Result<CompanionResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "AI学习伙伴对话...")
            
            val companionPrompt = buildCompanionPrompt(user, userMessage, context)
            val result = zhipuAIService.sendChatMessage(companionPrompt, user)
            
            result.fold(
                onSuccess = { response ->
                    val companionResponse = parseCompanionResponse(response)
                    Log.d(TAG, "AI学习伙伴回复完成")
                    Result.success(companionResponse)
                },
                onFailure = { error ->
                    Log.e(TAG, "AI学习伙伴对话失败", error)
                    Result.failure(Exception("AI学习伙伴异常"))
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "AI学习伙伴对话异常", e)
            Result.failure(Exception("AI学习伙伴异常"))
        }
    }
    
    /**
     * 创新功能2：智能学习提醒
     * 基于学习习惯和日程安排，智能提醒学习
     */
    suspend fun generateLearningReminder(
        user: User,
        currentTime: Date,
        upcomingTasks: List<LearningTask>
    ): Result<LearningReminder> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "生成学习提醒...")
            
            val reminderPrompt = buildReminderPrompt(user, currentTime, upcomingTasks)
            val result = zhipuAIService.sendChatMessage(reminderPrompt, user)
            
            result.fold(
                onSuccess = { response ->
                    val reminder = parseLearningReminder(response)
                    Log.d(TAG, "学习提醒生成完成")
                    Result.success(reminder)
                },
                onFailure = { error ->
                    Log.e(TAG, "学习提醒生成失败", error)
                    Result.failure(Exception("AI学习伙伴异常"))
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "学习提醒生成异常", e)
            Result.failure(Exception("AI学习伙伴异常"))
        }
    }
    
    /**
     * 创新功能3：智能学习激励
     * 根据学习进度和表现，提供个性化激励
     */
    suspend fun generateMotivation(
        user: User,
        recentProgress: List<LearningRecord>,
        currentMood: String
    ): Result<MotivationMessage> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "生成学习激励...")
            
            val motivationPrompt = buildMotivationPrompt(user, recentProgress, currentMood)
            val result = zhipuAIService.sendChatMessage(motivationPrompt, user)
            
            result.fold(
                onSuccess = { response ->
                    val motivation = parseMotivationMessage(response)
                    Log.d(TAG, "学习激励生成完成")
                    Result.success(motivation)
                },
                onFailure = { error ->
                    Log.e(TAG, "学习激励生成失败", error)
                    Result.failure(Exception("AI学习伙伴异常"))
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "学习激励生成异常", e)
            Result.failure(Exception("AI学习伙伴异常"))
        }
    }
    
    /**
     * 创新功能4：智能学习计划调整
     * 根据学习效果动态调整学习计划
     */
    suspend fun adjustLearningPlan(
        user: User,
        currentPlan: LearningPlan,
        recentPerformance: List<LearningRecord>
    ): Result<AdjustedPlan> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "调整学习计划...")
            
            val adjustmentPrompt = buildAdjustmentPrompt(user, currentPlan, recentPerformance)
            val result = zhipuAIService.sendChatMessage(adjustmentPrompt, user)
            
            result.fold(
                onSuccess = { response ->
                    val adjustedPlan = parseAdjustedPlan(response)
                    Log.d(TAG, "学习计划调整完成")
                    Result.success(adjustedPlan)
                },
                onFailure = { error ->
                    Log.e(TAG, "学习计划调整失败", error)
                    Result.failure(Exception("AI学习伙伴异常"))
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "学习计划调整异常", e)
            Result.failure(Exception("AI学习伙伴异常"))
        }
    }
    
    private fun buildCompanionPrompt(user: User, userMessage: String, context: LearningContext): String {
        return """
            你是${user.name}的AI学习伙伴，请以温暖、鼓励的语气回复：
            
            学生信息：
            - 姓名: ${user.name}
            - 年级: ${user.grade}
            - 当前心情: ${context.mood}
            - 学习状态: ${context.learningState}
            
            用户消息: $userMessage
            
            请以学习伙伴的身份回复，要求：
            1. 语气温暖、鼓励
            2. 提供实用的学习建议
            3. 关注学生的情感需求
            4. 适当使用emoji增加亲和力
            5. 回复长度控制在100字以内
            
            回复格式：
            {
                "message": "回复内容",
                "suggestion": "学习建议",
                "encouragement": "鼓励话语",
                "nextAction": "建议的下一步行动"
            }
        """.trimIndent()
    }
    
    private fun buildReminderPrompt(user: User, currentTime: Date, upcomingTasks: List<LearningTask>): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val tasks = upcomingTasks.joinToString("\n") { task ->
            "- ${task.title}: ${task.duration}分钟"
        }
        
        return """
            为${user.name}生成智能学习提醒：
            
            当前时间: ${timeFormat.format(currentTime)}
            年级: ${user.grade}
            
            待完成任务：
            $tasks
            
            请生成个性化的学习提醒，要求：
            1. 语气友好、不压迫
            2. 考虑时间合理性
            3. 提供学习建议
            4. 包含鼓励元素
            
            返回格式：
            {
                "title": "提醒标题",
                "message": "提醒内容",
                "suggestion": "学习建议",
                "urgency": "紧急程度(低/中/高)",
                "estimatedTime": "预计完成时间"
            }
        """.trimIndent()
    }
    
    private fun buildMotivationPrompt(user: User, recentProgress: List<LearningRecord>, currentMood: String): String {
        val progress = recentProgress.takeLast(5).joinToString("\n") { record ->
            "科目: ${record.subject}, 得分: ${record.score}, 时长: ${record.duration}分钟"
        }
        
        return """
            为${user.name}生成学习激励：
            
            学生信息：
            - 姓名: ${user.name}
            - 年级: ${user.grade}
            - 当前心情: $currentMood
            
            最近学习表现：
            $progress
            
            请生成个性化激励，要求：
            1. 根据表现给予适当鼓励
            2. 指出进步和亮点
            3. 提供继续努力的动力
            4. 语气积极向上
            
            返回格式：
            {
                "title": "激励标题",
                "message": "激励内容",
                "achievements": ["成就列表"],
                "nextGoal": "下一个目标",
                "encouragement": "鼓励话语"
            }
        """.trimIndent()
    }
    
    private fun buildAdjustmentPrompt(user: User, currentPlan: LearningPlan, recentPerformance: List<LearningRecord>): String {
        val performance = recentPerformance.takeLast(10).joinToString("\n") { record ->
            "科目: ${record.subject}, 得分: ${record.score}, 时长: ${record.duration}分钟"
        }
        
        return """
            为${user.name}调整学习计划：
            
            学生信息：
            - 姓名: ${user.name}
            - 年级: ${user.grade}
            
            当前计划：
            - 总目标: ${currentPlan.totalGoal}
            - 每日任务: ${currentPlan.dailyTasks.joinToString(", ")}
            - 预计完成时间: ${currentPlan.estimatedTime}
            
            最近表现：
            $performance
            
            请基于表现调整计划，要求：
            1. 分析学习效果
            2. 识别需要调整的地方
            3. 提供优化建议
            4. 保持目标可达性
            
            返回格式：
            {
                "analysis": "表现分析",
                "adjustments": ["调整建议列表"],
                "newPlan": {
                    "totalGoal": "调整后总目标",
                    "dailyTasks": ["调整后每日任务"],
                    "estimatedTime": "调整后预计时间"
                },
                "reasoning": "调整理由"
            }
        """.trimIndent()
    }
    
    // 解析方法
    private fun parseCompanionResponse(response: String): CompanionResponse {
        return CompanionResponse(
            message = "加油！你已经做得很好了！💪",
            suggestion = "建议先休息5分钟，然后继续学习",
            encouragement = "相信自己，你一定能行！",
            nextAction = "完成当前练习后可以奖励自己"
        )
    }
    
    private fun parseLearningReminder(response: String): LearningReminder {
        return LearningReminder(
            title = "学习时间到啦！",
            message = "该开始今天的学习了，加油！",
            suggestion = "建议先复习昨天的内容",
            urgency = "中",
            estimatedTime = "30分钟"
        )
    }
    
    private fun parseMotivationMessage(response: String): MotivationMessage {
        return MotivationMessage(
            title = "太棒了！",
            message = "你的学习进步很明显！",
            achievements = listOf("数学成绩提升", "学习时间增加"),
            nextGoal = "继续保持，争取更好成绩",
            encouragement = "你是最棒的！"
        )
    }
    
    private fun parseAdjustedPlan(response: String): AdjustedPlan {
        return AdjustedPlan(
            analysis = "学习效果良好，可以适当增加难度",
            adjustments = listOf("增加练习量", "提高学习目标"),
            newPlan = LearningPlan(
                totalGoal = "掌握所有知识点",
                dailyTasks = listOf("学习45分钟", "完成10道题"),
                estimatedTime = "2周"
            ),
            reasoning = "基于当前表现，可以挑战更高目标"
        )
    }
}

// 数据类定义已移至 AIDataModels.kt
