package com.example.educationapp.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * API密钥管理工具 - 方便动态更新API配置
 */
object ApiKeyManager {
    
    private const val PREFS_NAME = "api_config"
    private const val KEY_API_KEY = "deepseek_api_key"
    private const val KEY_MODEL_NAME = "model_name"
    private const val KEY_BASE_URL = "base_url"
    
    // 默认配置
    private const val DEFAULT_API_KEY = "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"
    private const val DEFAULT_MODEL = "google/gemini-flash-1.5"
    private const val DEFAULT_BASE_URL = "https://openrouter.ai/api/"
    
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * 获取当前API密钥
     */
    fun getApiKey(context: Context): String {
        return getPrefs(context).getString(KEY_API_KEY, DEFAULT_API_KEY) ?: DEFAULT_API_KEY
    }
    
    /**
     * 设置新的API密钥
     */
    fun setApiKey(context: Context, apiKey: String) {
        getPrefs(context).edit()
            .putString(KEY_API_KEY, apiKey)
            .apply()
    }
    
    /**
     * 获取当前模型名称
     */
    fun getModelName(context: Context): String {
        return getPrefs(context).getString(KEY_MODEL_NAME, DEFAULT_MODEL) ?: DEFAULT_MODEL
    }
    
    /**
     * 设置模型名称
     */
    fun setModelName(context: Context, modelName: String) {
        getPrefs(context).edit()
            .putString(KEY_MODEL_NAME, modelName)
            .apply()
    }
    
    /**
     * 获取API基础URL
     */
    fun getBaseUrl(context: Context): String {
        return getPrefs(context).getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
    }
    
    /**
     * 设置API基础URL
     */
    fun setBaseUrl(context: Context, baseUrl: String) {
        getPrefs(context).edit()
            .putString(KEY_BASE_URL, baseUrl)
            .apply()
    }
    
    /**
     * 重置为默认配置
     */
    fun resetToDefault(context: Context) {
        getPrefs(context).edit()
            .putString(KEY_API_KEY, DEFAULT_API_KEY)
            .putString(KEY_MODEL_NAME, DEFAULT_MODEL)
            .putString(KEY_BASE_URL, DEFAULT_BASE_URL)
            .apply()
    }
    
    /**
     * 验证API密钥格式
     */
    fun validateApiKey(apiKey: String): Boolean {
        // 支持智谱AI格式：{hex}.{alphanumeric} 或 OpenRouter格式：sk-...
        return apiKey.isNotBlank() && 
               apiKey.length >= 20 &&
               (apiKey.matches(Regex("^[a-f0-9]{32}\\.[a-zA-Z0-9]+$")) || apiKey.startsWith("sk-"))
    }
    
    /**
     * 获取API配置信息
     */
    fun getConfigInfo(context: Context): String {
        return """
            当前API配置：
            • 密钥：${getApiKey(context).take(10)}...
            • 模型：${getModelName(context)}
            • 端点：${getBaseUrl(context)}
        """.trimIndent()
    }
}

