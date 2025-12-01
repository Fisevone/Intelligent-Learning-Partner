package com.example.educationapp.debug

import android.util.Log
import com.example.educationapp.network.ApiConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * 🔑 API密钥验证器
 */
class ApiKeyValidator {
    
    private val TAG = "ApiKeyValidator"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    data class ValidationResult(
        val isValid: Boolean,
        val message: String,
        val details: String = "",
        val errorCode: String = "",
        val suggestions: List<String> = emptyList()
    )
    
    /**
     * 🔍 验证API密钥有效性
     */
    suspend fun validateApiKey(apiKey: String = ApiConstants.DEEPSEEK_API_KEY): ValidationResult = withContext(Dispatchers.IO) {
        
        Log.d(TAG, "🔑 开始验证API密钥: ${apiKey.take(10)}...")
        
        // 1. 格式检查
        val formatCheck = checkApiKeyFormat(apiKey)
        if (!formatCheck.isValid) {
            return@withContext formatCheck
        }
        
        // 2. 实际API调用测试
        return@withContext testApiCall(apiKey)
    }
    
    /**
     * 📋 检查API密钥格式
     */
    private fun checkApiKeyFormat(apiKey: String): ValidationResult {
        return when {
            apiKey.isEmpty() -> ValidationResult(
                isValid = false,
                message = "API密钥为空",
                details = "请配置有效的智谱AI API密钥",
                suggestions = listOf("访问 https://open.bigmodel.cn 获取API密钥")
            )
            
            !apiKey.contains(".") -> ValidationResult(
                isValid = false,
                message = "API密钥格式错误",
                details = "智谱AI密钥应包含'.'分隔符",
                suggestions = listOf(
                    "正确格式：xxx.xxxxxxxxxxxxxxxxxx",
                    "重新从智谱AI官网复制完整密钥"
                )
            )
            
            apiKey.length < 20 -> ValidationResult(
                isValid = false,
                message = "API密钥过短",
                details = "密钥长度: ${apiKey.length}，应该更长",
                suggestions = listOf("确保复制了完整的API密钥")
            )
            
            apiKey.contains(" ") -> ValidationResult(
                isValid = false,
                message = "API密钥包含空格",
                details = "密钥中不应包含空格或换行符",
                suggestions = listOf("重新复制密钥，确保没有多余的空格")
            )
            
            else -> ValidationResult(
                isValid = true,
                message = "API密钥格式正确",
                details = "密钥长度: ${apiKey.length}"
            )
        }
    }
    
    /**
     * 🧪 测试API调用
     */
    private suspend fun testApiCall(apiKey: String): ValidationResult {
        return try {
            // 构建最简单的测试请求
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", "测试")
                })
            }
            
            val requestBody = JSONObject().apply {
                put("model", "glm-4-flash")
                put("messages", messages)
                put("max_tokens", 5)
                put("temperature", 0.1)
            }
            
            Log.d(TAG, "📤 发送测试请求到智谱AI...")
            
            val request = Request.Builder()
                .url("${ApiConstants.DEEPSEEK_BASE_URL}${ApiConstants.CHAT_ENDPOINT}")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "EducationAPP/1.0")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            
            Log.d(TAG, "📥 响应状态码: ${response.code}")
            Log.d(TAG, "📄 响应内容: ${responseBody.take(200)}...")
            
            when (response.code) {
                200 -> {
                    // 解析响应确认API正常工作
                    val responseJson = JSONObject(responseBody)
                    if (responseJson.has("choices")) {
                        ValidationResult(
                            isValid = true,
                            message = "✅ API密钥有效！",
                            details = "成功调用智谱AI接口",
                            suggestions = listOf("API配置正确，可以正常使用")
                        )
                    } else {
                        ValidationResult(
                            isValid = false,
                            message = "API响应格式异常",
                            details = "收到响应但格式不正确: $responseBody",
                            suggestions = listOf("联系智谱AI技术支持")
                        )
                    }
                }
                
                401 -> ValidationResult(
                    isValid = false,
                    message = "❌ API密钥无效",
                    details = "HTTP 401: 认证失败",
                    errorCode = "INVALID_API_KEY",
                    suggestions = listOf(
                        "检查API密钥是否正确",
                        "确认密钥没有过期",
                        "重新生成新的API密钥"
                    )
                )
                
                403 -> ValidationResult(
                    isValid = false,
                    message = "❌ 访问被拒绝",
                    details = "HTTP 403: 权限不足或账户受限",
                    errorCode = "ACCESS_DENIED",
                    suggestions = listOf(
                        "完成智谱AI账户实名认证",
                        "检查账户状态是否正常",
                        "联系智谱AI客服"
                    )
                )
                
                429 -> ValidationResult(
                    isValid = false,
                    message = "⚠️ 请求频率过高",
                    details = "HTTP 429: 触发限流",
                    errorCode = "RATE_LIMITED",
                    suggestions = listOf(
                        "等待1分钟后重试",
                        "降低API调用频率",
                        "考虑升级账户套餐"
                    )
                )
                
                402, 400 -> {
                    // 解析具体错误信息
                    val errorMsg = try {
                        val errorJson = JSONObject(responseBody)
                        val error = errorJson.optJSONObject("error")
                        error?.optString("message") ?: "账户余额不足或参数错误"
                    } catch (e: Exception) {
                        "账户余额不足或参数错误"
                    }
                    
                    ValidationResult(
                        isValid = false,
                        message = "❌ $errorMsg",
                        details = "HTTP ${response.code}: $responseBody",
                        errorCode = "INSUFFICIENT_QUOTA",
                        suggestions = listOf(
                            "检查智谱AI账户余额",
                            "充值或等待免费额度重置",
                            "确认API调用参数正确"
                        )
                    )
                }
                
                500, 502, 503, 504 -> ValidationResult(
                    isValid = false,
                    message = "⚠️ 智谱AI服务暂时不可用",
                    details = "HTTP ${response.code}: 服务器错误",
                    errorCode = "SERVER_ERROR",
                    suggestions = listOf(
                        "稍后重试",
                        "检查智谱AI服务状态",
                        "如果问题持续，联系技术支持"
                    )
                )
                
                else -> ValidationResult(
                    isValid = false,
                    message = "❌ 未知错误",
                    details = "HTTP ${response.code}: $responseBody",
                    errorCode = "UNKNOWN_ERROR",
                    suggestions = listOf(
                        "检查网络连接",
                        "稍后重试",
                        "联系技术支持"
                    )
                )
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "🚨 API验证异常", e)
            
            val errorMsg = when {
                e.message?.contains("Unable to resolve host") == true -> 
                    "❌ 无法连接到智谱AI服务器"
                e.message?.contains("timeout") == true -> 
                    "⏱️ 连接超时"
                e.message?.contains("Connection refused") == true -> 
                    "🚫 连接被拒绝"
                else -> "❌ 网络错误"
            }
            
            ValidationResult(
                isValid = false,
                message = errorMsg,
                details = e.message ?: "未知网络错误",
                errorCode = "NETWORK_ERROR",
                suggestions = listOf(
                    "检查网络连接",
                    "尝试使用VPN或更换网络",
                    "检查防火墙设置",
                    "稍后重试"
                )
            )
        }
    }
    
    /**
     * 📊 生成验证报告
     */
    fun generateValidationReport(result: ValidationResult): String {
        val report = StringBuilder()
        report.appendLine("🔑 API密钥验证报告")
        report.appendLine("=".repeat(40))
        report.appendLine()
        
        report.appendLine("状态: ${result.message}")
        if (result.details.isNotEmpty()) {
            report.appendLine("详情: ${result.details}")
        }
        if (result.errorCode.isNotEmpty()) {
            report.appendLine("错误代码: ${result.errorCode}")
        }
        
        if (result.suggestions.isNotEmpty()) {
            report.appendLine()
            report.appendLine("💡 建议:")
            result.suggestions.forEach { suggestion ->
                report.appendLine("• $suggestion")
            }
        }
        
        if (result.isValid) {
            report.appendLine()
            report.appendLine("🎉 恭喜！你的API配置完全正确，可以正常使用智谱AI功能！")
        }
        
        return report.toString()
    }
}

