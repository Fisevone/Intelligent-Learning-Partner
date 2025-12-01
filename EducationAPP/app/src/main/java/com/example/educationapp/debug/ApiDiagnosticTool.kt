package com.example.educationapp.debug

import android.content.Context
import android.util.Log
import com.example.educationapp.network.ApiConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.InetAddress
import java.util.concurrent.TimeUnit

/**
 * 🔍 API诊断工具 - 检查API连接和配置问题
 */
class ApiDiagnosticTool(private val context: Context) {
    
    private val TAG = "ApiDiagnostic"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()
    
    data class DiagnosticResult(
        val step: String,
        val success: Boolean,
        val message: String,
        val details: String = ""
    )
    
    /**
     * 🔍 完整诊断流程
     */
    suspend fun runFullDiagnostic(): List<DiagnosticResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<DiagnosticResult>()
        
        // 1. 检查网络连接
        results.add(checkNetworkConnection())
        
        // 2. 检查DNS解析
        results.add(checkDnsResolution())
        
        // 3. 检查API密钥格式
        results.add(checkApiKeyFormat())
        
        // 4. 测试API连接
        results.add(testApiConnection())
        
        // 5. 测试简单API调用
        results.add(testSimpleApiCall())
        
        return@withContext results
    }
    
    /**
     * 📱 检查网络连接
     */
    private suspend fun checkNetworkConnection(): DiagnosticResult {
        return try {
            val request = Request.Builder()
                .url("https://www.baidu.com")
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                DiagnosticResult(
                    step = "网络连接检查",
                    success = true,
                    message = "网络连接正常",
                    details = "HTTP状态码: ${response.code}"
                )
            } else {
                DiagnosticResult(
                    step = "网络连接检查",
                    success = false,
                    message = "网络连接异常",
                    details = "HTTP状态码: ${response.code}"
                )
            }
        } catch (e: Exception) {
            DiagnosticResult(
                step = "网络连接检查",
                success = false,
                message = "网络连接失败",
                details = e.message ?: "未知网络错误"
            )
        }
    }
    
    /**
     * 🌐 检查DNS解析
     */
    private suspend fun checkDnsResolution(): DiagnosticResult {
        return try {
            val address = InetAddress.getByName("open.bigmodel.cn")
            DiagnosticResult(
                step = "DNS解析检查",
                success = true,
                message = "DNS解析成功",
                details = "IP地址: ${address.hostAddress}"
            )
        } catch (e: Exception) {
            DiagnosticResult(
                step = "DNS解析检查",
                success = false,
                message = "DNS解析失败",
                details = e.message ?: "无法解析open.bigmodel.cn"
            )
        }
    }
    
    /**
     * 🔑 检查API密钥格式
     */
    private fun checkApiKeyFormat(): DiagnosticResult {
        val apiKey = ApiConstants.DEEPSEEK_API_KEY
        
        return when {
            apiKey.isEmpty() -> DiagnosticResult(
                step = "API密钥格式检查",
                success = false,
                message = "API密钥为空",
                details = "请配置有效的智谱AI API密钥"
            )
            
            !apiKey.contains(".") -> DiagnosticResult(
                step = "API密钥格式检查",
                success = false,
                message = "API密钥格式错误",
                details = "智谱AI密钥应包含'.'分隔符，格式：xxx.xxxxxxxxxx"
            )
            
            apiKey.length < 20 -> DiagnosticResult(
                step = "API密钥格式检查",
                success = false,
                message = "API密钥过短",
                details = "密钥长度: ${apiKey.length}，应该更长"
            )
            
            else -> DiagnosticResult(
                step = "API密钥格式检查",
                success = true,
                message = "API密钥格式正确",
                details = "密钥长度: ${apiKey.length}，格式: ${apiKey.take(10)}...${apiKey.takeLast(10)}"
            )
        }
    }
    
    /**
     * 🔗 测试API连接
     */
    private suspend fun testApiConnection(): DiagnosticResult {
        return try {
            val request = Request.Builder()
                .url("${ApiConstants.DEEPSEEK_BASE_URL}${ApiConstants.CHAT_ENDPOINT}")
                .addHeader("Authorization", "Bearer ${ApiConstants.DEEPSEEK_API_KEY}")
                .addHeader("Content-Type", "application/json")
                .build()
            
            val response = client.newCall(request).execute()
            
            DiagnosticResult(
                step = "API连接测试",
                success = response.code != 404,
                message = if (response.code != 404) "API端点可访问" else "API端点不存在",
                details = "HTTP状态码: ${response.code}, URL: ${request.url}"
            )
            
        } catch (e: Exception) {
            DiagnosticResult(
                step = "API连接测试",
                success = false,
                message = "API连接失败",
                details = e.message ?: "连接超时或网络错误"
            )
        }
    }
    
    /**
     * 📤 测试简单API调用
     */
    private suspend fun testSimpleApiCall(): DiagnosticResult {
        return try {
            // 构建最简单的测试请求
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", "Hello")
                })
            }
            
            val requestBody = JSONObject().apply {
                put("model", "glm-4-flash")
                put("messages", messages)
                put("max_tokens", 10)
                put("temperature", 0.7)
            }
            
            val request = Request.Builder()
                .url("${ApiConstants.DEEPSEEK_BASE_URL}${ApiConstants.CHAT_ENDPOINT}")
                .addHeader("Authorization", "Bearer ${ApiConstants.DEEPSEEK_API_KEY}")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            
            when (response.code) {
                200 -> DiagnosticResult(
                    step = "API调用测试",
                    success = true,
                    message = "API调用成功",
                    details = "响应: ${responseBody.take(100)}..."
                )
                
                401 -> DiagnosticResult(
                    step = "API调用测试",
                    success = false,
                    message = "API密钥无效",
                    details = "HTTP 401: 请检查API密钥是否正确或已过期"
                )
                
                429 -> DiagnosticResult(
                    step = "API调用测试",
                    success = false,
                    message = "请求频率过高",
                    details = "HTTP 429: 请稍后重试"
                )
                
                500, 502, 503 -> DiagnosticResult(
                    step = "API调用测试",
                    success = false,
                    message = "服务器错误",
                    details = "HTTP ${response.code}: 智谱AI服务暂时不可用"
                )
                
                else -> DiagnosticResult(
                    step = "API调用测试",
                    success = false,
                    message = "API调用失败",
                    details = "HTTP ${response.code}: $responseBody"
                )
            }
            
        } catch (e: IOException) {
            DiagnosticResult(
                step = "API调用测试",
                success = false,
                message = "网络连接错误",
                details = e.message ?: "网络超时或连接被拒绝"
            )
        } catch (e: Exception) {
            DiagnosticResult(
                step = "API调用测试",
                success = false,
                message = "API调用异常",
                details = e.message ?: "未知错误"
            )
        }
    }
    
    /**
     * 📊 生成诊断报告
     */
    fun generateReport(results: List<DiagnosticResult>): String {
        val report = StringBuilder()
        report.appendLine("🔍 API诊断报告")
        report.appendLine("=".repeat(50))
        report.appendLine()
        
        results.forEachIndexed { index, result ->
            val status = if (result.success) "✅" else "❌"
            report.appendLine("${index + 1}. $status ${result.step}")
            report.appendLine("   ${result.message}")
            if (result.details.isNotEmpty()) {
                report.appendLine("   详情: ${result.details}")
            }
            report.appendLine()
        }
        
        // 生成建议
        val failedSteps = results.filter { !it.success }
        if (failedSteps.isNotEmpty()) {
            report.appendLine("💡 修复建议:")
            failedSteps.forEach { result ->
                when (result.step) {
                    "网络连接检查" -> report.appendLine("• 检查设备网络连接")
                    "DNS解析检查" -> report.appendLine("• 检查DNS设置或使用VPN")
                    "API密钥格式检查" -> report.appendLine("• 重新获取正确的智谱AI API密钥")
                    "API连接测试" -> report.appendLine("• 检查API端点URL配置")
                    "API调用测试" -> report.appendLine("• 验证API密钥有效性和账户状态")
                }
            }
        } else {
            report.appendLine("🎉 所有检查都通过！API配置正确。")
        }
        
        return report.toString()
    }
}
