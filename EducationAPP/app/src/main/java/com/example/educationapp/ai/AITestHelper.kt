package com.example.educationapp.ai

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * AI测试助手 - 用于测试DeepSeek API集成
 */
class AITestHelper {
    
    private val aiService = ZhipuAIService()
    
    /**
     * 测试基本聊天功能
     */
    fun testBasicChat(
        message: String,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = aiService.sendChatMessage(message, com.example.educationapp.data.User(0, "test", "test@example.com", "123", "测试用户", com.example.educationapp.data.UserType.STUDENT))
            if (result.isSuccess) {
                onResult(result.getOrNull() ?: "无回复")
            } else {
                onError(result.exceptionOrNull()?.message ?: "未知错误")
            }
        }
    }
    
    /**
     * 测试学习分析功能
     */
    fun testLearningAnalysis(
        user: com.example.educationapp.data.User,
        learningRecords: List<com.example.educationapp.data.LearningRecord>,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = aiService.sendChatMessage("请分析用户的学习记录", user)
            if (result.isSuccess) {
                onResult(result.getOrNull() ?: "无分析结果")
            } else {
                onError(result.exceptionOrNull()?.message ?: "分析失败")
            }
        }
    }
}














