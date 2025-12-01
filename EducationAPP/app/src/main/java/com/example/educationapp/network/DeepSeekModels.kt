package com.example.educationapp.network

import com.google.gson.annotations.SerializedName

/**
 * DeepSeek API请求和响应数据模型
 */

// 聊天请求模型 - 联通算力平台版本
data class ChatRequest(
    @SerializedName("model")
    val model: String = ApiConstants.DEFAULT_MODEL,
    
    @SerializedName("messages")
    val messages: List<ChatMessage>,
    
    @SerializedName("max_tokens")
    val maxTokens: Int = ApiConstants.MAX_TOKENS,
    
    @SerializedName("temperature")
    val temperature: Float = ApiConstants.TEMPERATURE,
    
    @SerializedName("stream")
    val stream: Boolean = false,
    
    // 联通算力平台特有参数
    @SerializedName("repetition_penalty")
    val repetitionPenalty: Float? = null,
    
    @SerializedName("frequency_penalty")
    val frequencyPenalty: Float? = null,
    
    @SerializedName("do_sample")
    val doSample: Boolean? = null,
    
    @SerializedName("top_p")
    val topP: Float? = null,
    
    @SerializedName("top_k")
    val topK: Float? = null,
    
    @SerializedName("seed")
    val seed: Long? = null
)

// 聊天消息模型
data class ChatMessage(
    @SerializedName("role")
    val role: String, // "system", "user", "assistant"
    
    @SerializedName("content")
    val content: String
)

// 聊天响应模型
data class ChatResponse(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("object")
    val objectType: String,
    
    @SerializedName("created")
    val created: Long,
    
    @SerializedName("model")
    val model: String,
    
    @SerializedName("choices")
    val choices: List<Choice>,
    
    @SerializedName("usage")
    val usage: Usage
)

// 选择项模型
data class Choice(
    @SerializedName("index")
    val index: Int,
    
    @SerializedName("message")
    val message: ChatMessage,
    
    @SerializedName("finish_reason")
    val finishReason: String
)

// 使用情况模型
data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    
    @SerializedName("total_tokens")
    val totalTokens: Int
)

// API错误响应模型
data class ApiError(
    @SerializedName("error")
    val error: ErrorDetail
)

data class ErrorDetail(
    @SerializedName("message")
    val message: String,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("code")
    val code: String?
)
