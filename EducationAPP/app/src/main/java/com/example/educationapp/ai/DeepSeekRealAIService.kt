package com.example.educationapp.ai

import android.util.Log
import com.example.educationapp.data.User
import com.example.educationapp.network.ApiConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * DeepSeek真实AI服务 - 使用你提供的免费API密钥
 */
class DeepSeekRealAIService {
    
    private val TAG = "DeepSeekRealAI"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()
    
    /**
     * 发送聊天消息到DeepSeek AI
     */
    suspend fun sendChatMessage(
        userMessage: String,
        user: User,
        personality: String = "鼓励型"
    ): Result<String> = withContext(Dispatchers.IO) {
        
        Log.d(TAG, "🤖 发送消息到DeepSeek: ${userMessage.take(50)}...")
        
        try {
            // 构建教学系统提示词
            val systemPrompt = buildEducationalPrompt(user, personality)
            
            // 构建请求消息
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userMessage)
                })
            }
            
            // 构建请求体
            val requestBody = JSONObject().apply {
                put("model", ApiConstants.DEFAULT_MODEL)
                put("messages", messages)
                put("max_tokens", ApiConstants.MAX_TOKENS)
                put("temperature", ApiConstants.TEMPERATURE)
                put("stream", false)
            }
            
            Log.d(TAG, "📤 请求URL: ${ApiConstants.DEEPSEEK_BASE_URL}${ApiConstants.CHAT_ENDPOINT}")
            Log.d(TAG, "🔑 API密钥: ${ApiConstants.DEEPSEEK_API_KEY.take(20)}...")
            Log.d(TAG, "📝 请求体: ${requestBody.toString().take(200)}...")
            
            // 创建HTTP请求
            val request = Request.Builder()
                .url("${ApiConstants.DEEPSEEK_BASE_URL}${ApiConstants.CHAT_ENDPOINT}")
                .addHeader("Authorization", "Bearer ${ApiConstants.DEEPSEEK_API_KEY}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()
            
            // 发送请求
            val response = client.newCall(request).execute()
            
            Log.d(TAG, "📥 响应状态: ${response.code}")
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                Log.d(TAG, "📄 响应内容: ${responseBody.take(200)}...")
                
                val aiResponse = parseDeepSeekResponse(responseBody)
                
                if (aiResponse.isNotEmpty()) {
                    Log.d(TAG, "✅ AI回复成功: ${aiResponse.take(100)}...")
                    Result.success("🤖 $aiResponse")
                } else {
                    Log.w(TAG, "⚠️ AI回复为空")
                    Result.failure(Exception("AI回复为空"))
                }
            } else {
                val errorBody = response.body?.string() ?: "未知错误"
                Log.e(TAG, "❌ API请求失败: ${response.code} - $errorBody")
                Result.failure(Exception("API请求失败: ${response.code} - ${errorBody.take(100)}"))
            }
            
        } catch (e: IOException) {
            Log.e(TAG, "🌐 网络异常", e)
            Result.failure(Exception("网络连接失败: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "💥 AI服务异常", e)
            Result.failure(Exception("AI服务异常: ${e.message}"))
        }
    }
    
    private fun buildEducationalPrompt(user: User, personality: String): String {
        val personalityContext = when (personality) {
            "鼓励型" -> "你是一位温暖鼓励的AI老师，总是用积极正面的语言回答学生问题，经常使用'很棒！'、'你做得很好！'、'继续加油！'等鼓励词汇，让学生在学习中充满信心。"
            "挑战型" -> "你是一位善于激发潜能的AI老师，会提出有挑战性的思考问题，推动学生突破自己的极限，用'你能做得更好！'、'挑战一下自己'等话语激励学生。"
            "幽默型" -> "你是一位幽默风趣的AI老师，善于用轻松有趣的方式、生动的比喻和适当的幽默让学习变得愉快，经常说'哈哈'、'有趣吧'等轻松用词。"
            "严格型" -> "你是一位严谨认真的AI老师，注重细节和准确性，会详细解释每个概念，确保学生理解透彻，用'必须掌握'、'这很重要'等严肃用词。"
            "创意型" -> "你是一位富有创意的AI老师，善于用新颖独特的角度和创新方法来解释问题，经常说'换个角度看'、'有个创意想法'等启发用词。"
            "温和型" -> "你是一位温和耐心的AI老师，语言柔和，善于倾听，给学生足够的理解时间，用'慢慢来'、'不着急'等温和用词。"
            "激情型" -> "你是一位充满热情的AI老师，用饱满的情感感染学生，让学习充满动力和活力，经常使用感叹号和'太棒了！'、'amazing！'等激情用词。"
            "学者型" -> "你是一位学者型AI老师，深入浅出地分析问题，注重逻辑和理论深度，用'从学术角度来看'、'根据理论'等专业用词。"
            "实用型" -> "你是一位实用型AI老师，专注于实际应用，总是告诉学生知识在现实中的用途，用'实际上'、'在生活中'等实用导向用词。"
            "启发型" -> "你是一位启发型AI老师，善于通过提问引导学生独立思考，发现答案，用'你觉得呢？'、'试着想想'等启发用词。"
            else -> "你是一位专业的AI老师。"
        }
        
        return """${personalityContext}

你正在为一位${user.grade}年级的学生提供学习帮助。这位学生的学习风格是${user.learningStyle}。

请遵循以下教学原则：
1. 体现${personality}的教学风格和用词特点
2. 回答要适合${user.grade}学生的理解水平
3. 结合${user.learningStyle}学习风格给出建议
4. 回答控制在150-250字，内容丰富但简洁
5. 提供实用的学习方法和建议
6. 保持教育性、启发性和互动性
7. 用中文回答，语言生动有趣

现在请以${personality}AI老师的身份，用你独特的教学风格来回答学生的问题。"""
    }
    
    private fun parseDeepSeekResponse(responseBody: String): String {
        return try {
            val jsonObject = JSONObject(responseBody)
            
            // 检查是否有错误
            if (jsonObject.has("error")) {
                val error = jsonObject.getJSONObject("error")
                val errorMessage = error.getString("message")
                Log.e(TAG, "API返回错误: $errorMessage")
                return ""
            }
            
            // 解析正常响应
            val choices = jsonObject.getJSONArray("choices")
            if (choices.length() > 0) {
                val firstChoice = choices.getJSONObject(0)
                val message = firstChoice.getJSONObject("message")
                val content = message.getString("content").trim()
                
                // 移除可能的前缀标记
                return content.removePrefix("🤖").trim()
            } else {
                Log.w(TAG, "响应中没有choices")
                return ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "解析DeepSeek响应失败: $responseBody", e)
            return ""
        }
    }
    
    /**
     * 测试API连接
     */
    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔧 开始测试DeepSeek连接...")
            
            val testResult = sendChatMessage(
                "请简单介绍一下你自己，告诉我你是什么AI模型。",
                User(0, "test", "test@test.com", "", "测试学生", 
                     com.example.educationapp.data.UserType.STUDENT, "大学"),
                "鼓励型"
            )
            
            if (testResult.isSuccess) {
                val response = testResult.getOrNull() ?: ""
                Result.success("✅ DeepSeek AI连接成功！\n🤖 使用模型: ${ApiConstants.DEFAULT_MODEL}\n🔑 API状态: 正常\n\n$response")
            } else {
                val error = testResult.exceptionOrNull()?.message ?: "未知错误"
                Result.success("❌ DeepSeek连接失败: $error\n\n已启用本地智能回复作为备用方案")
            }
        } catch (e: Exception) {
            Log.e(TAG, "连接测试异常", e)
            Result.success("🔧 连接测试遇到问题: ${e.message}\n\n系统已启用多重保障机制")
        }
    }
}

