package com.example.educationapp.ai

import android.util.Log
import com.example.educationapp.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * API测试助手 - 用于测试和调试API连接
 */
class ApiTestHelper {
    
    companion object {
        private const val TAG = "ApiTestHelper"
    }
    
    /**
     * 测试API连接和认证
     */
    suspend fun testApiConnection(): TestResult = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
            
            // 构建测试请求
            val testMessage = ChatMessage(role = "user", content = "Hello, this is a test message.")
            val requestBody = ChatRequest(
                model = ApiConstants.DEFAULT_MODEL,
                messages = listOf(testMessage),
                maxTokens = 100,
                temperature = 0.7f
            )
            
            val json = JSONObject().apply {
                put("model", requestBody.model)
                put("messages", org.json.JSONArray().apply {
                    requestBody.messages.forEach { message ->
                        put(JSONObject().apply {
                            put("role", message.role)
                            put("content", message.content)
                        })
                    }
                })
                put("max_tokens", requestBody.maxTokens)
                put("temperature", requestBody.temperature)
                put("stream", false)
            }
            
            val request = Request.Builder()
                .url("${ApiConstants.DEEPSEEK_BASE_URL}${ApiConstants.CHAT_ENDPOINT}")
                .addHeader(ApiConstants.HEADER_AUTHORIZATION, "Bearer ${ApiConstants.DEEPSEEK_API_KEY}")
                .addHeader(ApiConstants.HEADER_CONTENT_TYPE, ApiConstants.CONTENT_TYPE_JSON)
                .post(json.toString().toRequestBody("application/json".toMediaType()))
                .build()
            
            Log.d(TAG, "Testing API connection...")
            Log.d(TAG, "URL: ${request.url}")
            Log.d(TAG, "API Key: ${ApiConstants.DEEPSEEK_API_KEY.take(10)}...")
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            Log.d(TAG, "Response Code: ${response.code}")
            Log.d(TAG, "Response Body: $responseBody")
            
            return@withContext when {
                response.isSuccessful -> {
                    TestResult.Success("API连接成功！响应: $responseBody")
                }
                response.code == 401 -> {
                    TestResult.Error("认证失败: API密钥无效或已过期")
                }
                response.code == 403 -> {
                    TestResult.Error("权限不足: API密钥没有访问权限")
                }
                response.code == 429 -> {
                    TestResult.Error("请求频率限制: 请稍后再试")
                }
                else -> {
                    TestResult.Error("API请求失败: ${response.code} - $responseBody")
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "API测试失败", e)
            return@withContext TestResult.Error("网络错误: ${e.message}")
        }
    }
    
    /**
     * 测试不同的API端点
     */
    suspend fun testAlternativeEndpoints(): List<TestResult> = withContext(Dispatchers.IO) {
        val endpoints = listOf(
            "https://maas-api.ai-yuanjing.com/openapi/compatible-mode/v1/chat/completions",
            "https://maas-api.ai-yuanjing.com/openapi/compatible-mode/chat/completions"
        )
        
        val results = mutableListOf<TestResult>()
        
        endpoints.forEach { endpoint ->
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()
                
                val testMessage = ChatMessage(role = "user", content = "test")
                val json = JSONObject().apply {
                    put("model", "deepseek-chat")
                    put("messages", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", testMessage.role)
                            put("content", testMessage.content)
                        })
                    })
                    put("max_tokens", 10)
                }
                
                val request = Request.Builder()
                    .url(endpoint)
                    .addHeader("Authorization", "Bearer ${ApiConstants.DEEPSEEK_API_KEY}")
                    .addHeader("Content-Type", "application/json")
                    .post(json.toString().toRequestBody("application/json".toMediaType()))
                    .build()
                
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                
                results.add(
                    TestResult(
                        success = response.isSuccessful,
                        message = "端点: $endpoint - 状态码: ${response.code} - 响应: $responseBody"
                    )
                )
                
            } catch (e: Exception) {
                results.add(
                    TestResult(
                        success = false,
                        message = "端点: $endpoint - 错误: ${e.message}"
                    )
                )
            }
        }
        
        return@withContext results
    }
}

/**
 * 测试结果数据类
 */
data class TestResult(
    val success: Boolean,
    val message: String
) {
    companion object {
        fun Success(message: String) = TestResult(true, message)
        fun Error(message: String) = TestResult(false, message)
    }
}
