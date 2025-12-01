package com.example.educationapp.ai

import android.util.Log
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * AI服务优化器
 * 提供重试机制、错误处理和性能优化
 */
object AIServiceOptimizer {
    
    private const val TAG = "AIServiceOptimizer"
    
    /**
     * 带重试机制的API调用
     */
    suspend fun <T> executeWithRetry(
        maxRetries: Int = 3,
        baseDelay: Long = 1000L,
        maxDelay: Long = 8000L,
        factor: Double = 2.0,
        operation: suspend () -> Result<T>
    ): Result<T> {
        var currentDelay = baseDelay
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                Log.d(TAG, "执行API调用，尝试次数: ${attempt + 1}")
                val result = operation()
                
                if (result.isSuccess) {
                    Log.d(TAG, "API调用成功")
                    return result
                } else {
                    lastException = result.exceptionOrNull() as? Exception
                    Log.w(TAG, "API调用失败，尝试次数: ${attempt + 1}, 错误: ${lastException?.message}")
                }
            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "API调用异常，尝试次数: ${attempt + 1}", e)
            }
            
            if (attempt < maxRetries - 1) {
                val jitter = Random.nextLong(0, currentDelay / 4)
                val delayTime = minOf(currentDelay + jitter, maxDelay)
                Log.d(TAG, "等待 ${delayTime}ms 后重试...")
                delay(delayTime)
                currentDelay = (currentDelay * factor).toLong()
            }
        }
        
        Log.e(TAG, "API调用最终失败，已尝试 $maxRetries 次")
        return Result.failure(lastException ?: Exception("API调用失败"))
    }
    
    /**
     * 优化AI提示词
     */
    fun optimizePrompt(basePrompt: String): String {
        return """
            $basePrompt
            
            请注意：
            1. 回答要简洁明了，重点突出
            2. 使用中文回答
            3. 如果是列表，请使用 "-" 开头的格式
            4. 避免过长的解释，直接给出核心内容
        """.trimIndent()
    }
    
    /**
     * 检查API响应质量
     */
    fun validateResponse(response: String): Boolean {
        return response.isNotBlank() && 
               response.length > 10 && 
               !response.contains("抱歉") && 
               !response.contains("无法") &&
               !response.contains("timeout", ignoreCase = true)
    }
    
    /**
     * 获取用户友好的错误消息
     */
    fun getUserFriendlyErrorMessage(error: Throwable?): String {
        return when {
            error?.message?.contains("timeout", ignoreCase = true) == true -> 
                "网络连接超时，请检查网络后重试"
            error?.message?.contains("401") == true -> 
                "API密钥验证失败，请检查配置"
            error?.message?.contains("429") == true -> 
                "请求过于频繁，请稍后重试"
            error?.message?.contains("500") == true -> 
                "服务器暂时不可用，请稍后重试"
            error is java.net.UnknownHostException -> 
                "网络连接失败，请检查网络设置"
            error is java.net.SocketTimeoutException -> 
                "请求超时，请重试"
            else -> "AI服务暂时不可用，请稍后重试"
        }
    }
    
    /**
     * 分割长文本以避免API限制
     */
    fun splitLongText(text: String, maxLength: Int = 2000): List<String> {
        if (text.length <= maxLength) return listOf(text)
        
        val chunks = mutableListOf<String>()
        var start = 0
        
        while (start < text.length) {
            val end = minOf(start + maxLength, text.length)
            var splitPoint = end
            
            // 尝试在句号处分割
            if (end < text.length) {
                val lastPeriod = text.lastIndexOf('。', end - 1)
                val lastNewline = text.lastIndexOf('\n', end - 1)
                splitPoint = maxOf(lastPeriod, lastNewline)
                if (splitPoint <= start) splitPoint = end
            }
            
            chunks.add(text.substring(start, splitPoint))
            start = splitPoint
        }
        
        return chunks
    }
}

