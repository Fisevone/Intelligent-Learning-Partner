package com.example.educationapp.network

import android.util.Log
import org.json.JSONObject

/**
 * API密钥验证器
 */
object ApiKeyValidator {
    
    private const val TAG = "ApiKeyValidator"
    
    /**
     * 验证API密钥格式
     */
    fun validateApiKeyFormat(apiKey: String): ValidationResult {
        return when {
            apiKey.isBlank() -> {
                ValidationResult.Error("API密钥不能为空")
            }
            // 支持智谱AI格式：{hex}.{alphanumeric} 或 OpenRouter格式：sk-...
            !apiKey.matches(Regex("^[a-f0-9]{32}\\.[a-zA-Z0-9]+$")) && !apiKey.startsWith("sk-") -> {
                ValidationResult.Error("API密钥格式错误：应该是智谱AI格式（如：xxx.xxx）或OpenRouter格式（sk-开头）")
            }
            apiKey.length < 20 -> {
                ValidationResult.Error("API密钥长度不足")
            }
            else -> {
                ValidationResult.Success("API密钥格式正确")
            }
        }
    }
    
    /**
     * 解析API错误响应
     */
    fun parseApiError(errorBody: String?): ApiValidationError {
        return try {
            if (errorBody.isNullOrBlank()) {
                return ApiValidationError("未知错误", "unknown", null)
            }
            
            val json = JSONObject(errorBody)
            val error = json.optJSONObject("error")
            
            if (error != null) {
                val message = error.optString("message", "未知错误")
                val type = error.optString("type", "unknown")
                val code = error.optString("code", null)
                
                ApiValidationError(message, type, code)
            } else {
                ApiValidationError("解析错误响应失败", "parse_error", null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "解析API错误失败", e)
            ApiValidationError("解析错误响应失败: ${e.message}", "parse_error", null)
        }
    }
    
    /**
     * 获取用户友好的错误消息
     */
    fun getUserFriendlyErrorMessage(apiError: ApiValidationError): String {
        return when (apiError.type) {
            "authentication_error" -> {
                when {
                    apiError.message.contains("invalid") -> "API密钥无效，请检查密钥是否正确"
                    apiError.message.contains("expired") -> "API密钥已过期，请获取新的密钥"
                    else -> "认证失败，请检查API密钥"
                }
            }
            "insufficient_quota" -> "API配额不足，请检查账户余额"
            "rate_limit_exceeded" -> "请求频率过高，请稍后再试"
            "invalid_request_error" -> "请求参数错误，请检查请求格式"
            "server_error" -> "服务器错误，请稍后再试"
            else -> "API请求失败：${apiError.message}"
        }
    }
}

/**
 * 验证结果
 */
sealed class ValidationResult {
    data class Success(val message: String) : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

/**
 * API错误信息
 */
data class ApiValidationError(
    val message: String,
    val type: String,
    val code: String?
)
