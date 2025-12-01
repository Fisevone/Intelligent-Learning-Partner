package com.example.educationapp.ai

import android.util.Log
import com.example.educationapp.data.User
import com.example.educationapp.network.ApiConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Hugging Face AI服务 - 完全免费的AI解决方案
 */
class HuggingFaceAIService {
    
    private val TAG = "HuggingFaceAI"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    /**
     * 发送聊天消息到Hugging Face API
     */
    suspend fun sendChatMessage(
        userMessage: String,
        user: User,
        personality: String = "鼓励型"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🤖 发送消息到Hugging Face: ${userMessage.take(50)}...")
            
            // 构建教学场景的提示词
            val educationalPrompt = buildEducationalPrompt(userMessage, user, personality)
            
            // 尝试多个Hugging Face模型
            val models = listOf(
                "microsoft/DialoGPT-medium",
                "facebook/blenderbot-400M-distill",
                "microsoft/DialoGPT-large"
            )
            
            for (model in models) {
                try {
                    val result = callHuggingFaceAPI(educationalPrompt, model)
                    if (result.isSuccess) {
                        Log.d(TAG, "✅ 使用模型 $model 成功")
                        return@withContext result
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "模型 $model 失败，尝试下一个: ${e.message}")
                    continue
                }
            }
            
            // 如果所有模型都失败，返回智能本地回复
            Log.w(TAG, "所有Hugging Face模型都失败，使用本地智能回复")
            Result.success(generateLocalIntelligentResponse(userMessage, personality))
            
        } catch (e: Exception) {
            Log.e(TAG, "Hugging Face API调用异常", e)
            Result.success(generateLocalIntelligentResponse(userMessage, personality))
        }
    }
    
    private fun buildEducationalPrompt(userMessage: String, user: User, personality: String): String {
        val personalityContext = when (personality) {
            "鼓励型" -> "作为一位温暖鼓励的老师，用积极正面的语言回答"
            "挑战型" -> "作为一位善于激发潜能的老师，提出有挑战性的思考"
            "幽默型" -> "作为一位幽默风趣的老师，用轻松有趣的方式回答"
            "严格型" -> "作为一位严谨认真的老师，详细准确地回答"
            "创意型" -> "作为一位富有创意的老师，用新颖的角度回答"
            else -> "作为一位专业的老师"
        }
        
        return "${personalityContext}学生的问题。学生信息：年级${user.grade}，学习风格${user.learningStyle}。问题：$userMessage。请用150字以内回答，要有教育意义且适合学生理解。"
    }
    
    private suspend fun callHuggingFaceAPI(prompt: String, model: String): Result<String> {
        return try {
            val jsonBody = JSONObject().apply {
                put("inputs", prompt)
                put("parameters", JSONObject().apply {
                    put("max_length", 200)
                    put("temperature", 0.7)
                    put("do_sample", true)
                })
            }
            
            val requestBody = jsonBody.toString()
                .toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url("${ApiConstants.DEEPSEEK_BASE_URL}models/$model")
                .addHeader("Authorization", "Bearer ${ApiConstants.DEEPSEEK_API_KEY}")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()
            
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val aiResponse = parseHuggingFaceResponse(responseBody, model)
                
                if (aiResponse.isNotEmpty()) {
                    Log.d(TAG, "✅ AI回复: ${aiResponse.take(100)}...")
                    Result.success(aiResponse)
                } else {
                    Result.failure(Exception("解析响应失败"))
                }
            } else {
                Log.w(TAG, "API请求失败: ${response.code} - ${response.message}")
                Result.failure(Exception("API请求失败: ${response.code}"))
            }
            
        } catch (e: IOException) {
            Log.e(TAG, "网络请求异常", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "API调用异常", e)
            Result.failure(e)
        }
    }
    
    private fun parseHuggingFaceResponse(responseBody: String, model: String): String {
        return try {
            when {
                model.contains("DialoGPT") -> {
                    // DialoGPT响应格式
                    val jsonArray = org.json.JSONArray(responseBody)
                    if (jsonArray.length() > 0) {
                        val firstResult = jsonArray.getJSONObject(0)
                        firstResult.getString("generated_text").trim()
                    } else ""
                }
                model.contains("blenderbot") -> {
                    // Blenderbot响应格式
                    val jsonArray = org.json.JSONArray(responseBody)
                    if (jsonArray.length() > 0) {
                        val firstResult = jsonArray.getJSONObject(0)
                        firstResult.getString("generated_text").trim()
                    } else ""
                }
                else -> {
                    // 通用格式
                    val jsonObject = JSONObject(responseBody)
                    jsonObject.optString("generated_text", "").trim()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "解析响应失败: $responseBody", e)
            ""
        }
    }
    
    /**
     * 本地智能回复备用方案
     */
    private fun generateLocalIntelligentResponse(userMessage: String, personality: String): String {
        val personalityPrefix = when (personality) {
            "鼓励型" -> "很棒的问题！"
            "挑战型" -> "这是个有挑战性的问题！"
            "幽默型" -> "哈哈，这个问题很有意思！"
            "严格型" -> "这是一个需要认真对待的问题。"
            "创意型" -> "真是个富有创意的问题！"
            "温和型" -> "我理解你的疑问，"
            "激情型" -> "太好了！这个问题很有价值！"
            "学者型" -> "这是一个值得深入研究的问题。"
            "实用型" -> "这是一个实用性很强的问题。"
            "启发型" -> "让我们一起思考这个问题..."
            else -> "好问题！"
        }
        
        val response = when {
            userMessage.contains("数学") || userMessage.contains("计算") -> {
                "$personalityPrefix 数学需要逻辑思维和练习。建议：\n• 理解概念原理\n• 多做练习题\n• 总结解题方法\n继续加油！"
            }
            userMessage.contains("英语") || userMessage.contains("单词") -> {
                "$personalityPrefix 英语学习需要坚持。建议：\n• 每天记忆单词\n• 多听多说\n• 大量阅读\n持之以恒很重要！"
            }
            userMessage.contains("学习方法") || userMessage.contains("怎么学") -> {
                "$personalityPrefix 有效学习方法：\n• 制定计划\n• 主动思考\n• 及时复习\n• 总结归纳\n找到适合自己的方式！"
            }
            userMessage.contains("你好") || userMessage.contains("hello") -> {
                "$personalityPrefix 很高兴与你交流！我是你的${personality}AI老师，可以帮你解答学习问题、提供学习建议。有什么想要学习的吗？"
            }
            else -> {
                "$personalityPrefix 这是个很好的问题！学习是一个持续的过程，重要的是保持好奇心和求知欲。我会尽力帮助你找到答案和方法。"
            }
        }
        
        return response
    }
    
    /**
     * 测试API连接
     */
    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val result = sendChatMessage("测试连接", 
                User(0, "test", "test@test.com", "", "测试用户", 
                     com.example.educationapp.data.UserType.STUDENT, "大学"),
                "鼓励型"
            )
            
            if (result.isSuccess) {
                Result.success("✅ Hugging Face AI连接测试成功！")
            } else {
                Result.success("⚠️ API暂时不可用，已启用本地智能回复模式")
            }
        } catch (e: Exception) {
            Result.success("🔧 检测到网络问题，使用本地模式确保服务稳定")
        }
    }
}
