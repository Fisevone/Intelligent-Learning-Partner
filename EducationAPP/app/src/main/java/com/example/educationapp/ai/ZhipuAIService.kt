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
 * 智谱AI (BigModel) 真实AI服务
 */
class ZhipuAIService {
    
    private val TAG = "ZhipuAI"
    
    // 临时模拟模式 - 当API密钥无效时启用
    private val MOCK_MODE = false
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    
    /**
     * 发送聊天消息到智谱AI - 多模型自动尝试
     */
    suspend fun sendChatMessage(
        userMessage: String,
        user: User,
        personality: String = "鼓励型"
    ): Result<String> = withContext(Dispatchers.IO) {
        
        // 如果启用模拟模式，返回模拟响应
        if (MOCK_MODE) {
            Log.d(TAG, "🎭 模拟模式已启用，返回模拟响应")
            return@withContext generateMockResponse(userMessage, personality)
        }
        
        Log.d(TAG, "🤖 发送消息到智谱AI: ${userMessage.take(50)}...")
        
        // 多个模型配置，按优先级尝试
        val modelConfigs = listOf(
            ModelConfig(ApiConstants.DEFAULT_MODEL, "主要模型"),
            ModelConfig(ApiConstants.ALT_MODEL_1, "备用模型1"),
            ModelConfig(ApiConstants.ALT_MODEL_2, "备用模型2")
        )
        
        // 构建教学系统提示词
        val systemPrompt = buildEducationalPrompt(user, personality)
        
        // 尝试每个模型配置
        for (config in modelConfigs) {
            try {
                Log.d(TAG, "🔄 尝试模型: ${config.model} (${config.description})")
                
                val result = callZhipuAPI(systemPrompt, userMessage, config.model)
                if (result.isSuccess) {
                    Log.d(TAG, "✅ ${config.description}调用成功")
                    return@withContext result
                } else {
                    Log.w(TAG, "⚠️ ${config.description}失败: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "❌ ${config.description}异常: ${e.message}")
                continue
            }
        }
        
        // 所有模型都失败
        Log.e(TAG, "💥 所有智谱AI模型都无法响应")
        return@withContext Result.failure(Exception("所有智谱AI模型都无法响应，请检查API密钥配置"))
    }
    
    /**
     * 🎭 生成模拟AI响应
     */
    private fun generateMockResponse(userMessage: String, personality: String): Result<String> {
        val responses = when (personality) {
            "鼓励型" -> listOf(
                "很棒！你提出了一个很好的问题：「$userMessage」。让我来帮你分析一下...",
                "你做得很好！关于「$userMessage」这个问题，我建议你可以这样理解...",
                "继续加油！你的问题「$userMessage」很有思考价值，让我们一起探讨..."
            )
            "挑战型" -> listOf(
                "这是个有趣的挑战：「$userMessage」。你准备好接受更深层的思考了吗？",
                "不错的问题！但是关于「$userMessage」，你有没有考虑过更复杂的情况？",
                "让我们提高难度！针对「$userMessage」，试试这个更高级的角度..."
            )
            "幽默型" -> listOf(
                "哈哈，「$userMessage」这个问题让我想起了一个有趣的故事...",
                "你的问题「$userMessage」很棒！让我用一个轻松的方式来解释...",
                "有意思！关于「$userMessage」，我有个小笑话可以帮你记住..."
            )
            else -> listOf(
                "关于你的问题「$userMessage」，我来为你详细分析...",
                "这是一个很好的问题：「$userMessage」。让我们一步步来解答...",
                "你提到了「$userMessage」，这确实是一个重要的话题..."
            )
        }
        
        val randomResponse = responses.random()
        return Result.success(randomResponse)
    }
    
    private data class ModelConfig(
        val model: String,
        val description: String
    )
    
    private suspend fun callZhipuAPI(
        systemPrompt: String,
        userMessage: String,
        model: String
    ): Result<String> {
        return try {
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
            
            // 构建请求体 - 智谱AI格式
            val requestBody = JSONObject().apply {
                put("model", model)
                put("messages", messages)
                put("max_tokens", ApiConstants.MAX_TOKENS)
                put("temperature", ApiConstants.TEMPERATURE)
                put("stream", false)
                put("top_p", 0.7)
                put("do_sample", true)
            }
            
            Log.d(TAG, "📤 请求URL: ${ApiConstants.DEEPSEEK_BASE_URL}${ApiConstants.CHAT_ENDPOINT}")
            Log.d(TAG, "🔑 API密钥: ${ApiConstants.DEEPSEEK_API_KEY.take(20)}...")
            Log.d(TAG, "🎯 使用模型: $model")
            Log.d(TAG, "📝 请求体: ${requestBody.toString().take(200)}...")
            
            // 创建HTTP请求 - 智谱AI认证格式
            val request = Request.Builder()
                .url("${ApiConstants.DEEPSEEK_BASE_URL}${ApiConstants.CHAT_ENDPOINT}")
                .addHeader("Authorization", "Bearer ${ApiConstants.DEEPSEEK_API_KEY}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "EducationAPP/1.0")
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()
            
            // 发送请求
            val response = client.newCall(request).execute()
            
            Log.d(TAG, "📥 响应状态: ${response.code}")
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                Log.d(TAG, "📄 响应内容: ${responseBody.take(300)}...")
                
                val aiResponse = parseZhipuResponse(responseBody)
                
                if (aiResponse.isNotEmpty()) {
                    Log.d(TAG, "✅ 智谱AI回复成功: ${aiResponse.take(100)}...")
                    Result.success("🧠 $aiResponse")
                } else {
                    Log.w(TAG, "⚠️ 智谱AI回复为空")
                    Result.failure(Exception("智谱AI回复为空"))
                }
            } else {
                val errorBody = response.body?.string() ?: "未知错误"
                Log.e(TAG, "❌ 智谱AI请求失败: ${response.code} - $errorBody")
                
                // 解析具体错误
                val errorMessage = parseZhipuError(errorBody, response.code)
                Result.failure(Exception(errorMessage))
            }
            
        } catch (e: IOException) {
            Log.e(TAG, "🌐 网络异常", e)
            val errorMsg = when {
                e.message?.contains("Unable to resolve host") == true -> 
                    "无法解析域名 open.bigmodel.cn，请检查网络连接或DNS设置"
                e.message?.contains("timeout") == true -> 
                    "网络连接超时，请检查网络状况或稍后重试"
                e.message?.contains("Connection refused") == true -> 
                    "服务器拒绝连接，可能是防火墙或代理问题"
                else -> "网络连接失败: ${e.message}"
            }
            Result.failure(Exception(errorMsg))
        } catch (e: Exception) {
            Log.e(TAG, "💥 智谱AI服务异常", e)
            Result.failure(Exception("智谱AI服务异常: ${e.message}"))
        }
    }
    
    private fun parseZhipuError(errorBody: String, statusCode: Int): String {
        return try {
            val errorJson = JSONObject(errorBody)
            when {
                errorJson.has("error") -> {
                    val error = errorJson.getJSONObject("error")
                    val code = error.optString("code", "unknown")
                    val message = error.optString("message", "API请求失败")
                    when (code) {
                        "invalid_api_key" -> "API密钥无效，请检查密钥配置"
                        "insufficient_quota" -> "API额度不足，请检查账户余额"
                        "rate_limit_exceeded" -> "请求频率过高，请稍后重试"
                        "model_not_found" -> "模型不存在，请检查模型名称"
                        else -> message
                    }
                }
                statusCode == 401 -> "API密钥认证失败，请检查密钥是否正确"
                statusCode == 403 -> "API访问被拒绝，请检查密钥权限"
                statusCode == 429 -> "请求频率过高，请稍后重试"
                statusCode == 500 -> "智谱AI服务器内部错误，请稍后重试"
                else -> "API请求失败: $statusCode - ${errorBody.take(100)}"
            }
        } catch (e: Exception) {
            "API请求失败: $statusCode - ${errorBody.take(100)}"
        }
    }
    
    private fun buildEducationalPrompt(user: User, personality: String): String {
        val personalityContext = when (personality) {
            "鼓励型" -> "你是一位温暖鼓励的AI老师，总是用积极正面的语言回答学生问题，经常使用'很棒！'、'你做得很好！'、'继续加油！'等鼓励词汇，让学生在学习中充满信心和动力。"
            "挑战型" -> "你是一位善于激发潜能的AI老师，会提出有挑战性的思考问题，推动学生突破自己的极限，用'你能做得更好！'、'挑战一下自己'、'试试更高难度'等话语激励学生。"
            "幽默型" -> "你是一位幽默风趣的AI老师，善于用轻松有趣的方式、生动的比喻和适当的幽默让学习变得愉快，经常说'哈哈'、'有趣吧'、'像这样的例子'等轻松用词。"
            "严格型" -> "你是一位严谨认真的AI老师，注重细节和准确性，会详细解释每个概念，确保学生理解透彻，用'必须掌握'、'这很重要'、'不能马虎'等严肃用词。"
            "创意型" -> "你是一位富有创意的AI老师，善于用新颖独特的角度和创新方法来解释问题，经常说'换个角度看'、'有个创意想法'、'试试这种新方法'等启发用词。"
            "温和型" -> "你是一位温和耐心的AI老师，语言柔和，善于倾听，给学生足够的理解时间，用'慢慢来'、'不着急'、'我理解你的困难'等温和用词。"
            "激情型" -> "你是一位充满热情的AI老师，用饱满的情感感染学生，让学习充满动力和活力，经常使用感叹号和'太棒了！'、'amazing！'、'fantastic！'等激情用词。"
            "学者型" -> "你是一位学者型AI老师，深入浅出地分析问题，注重逻辑和理论深度，用'从学术角度来看'、'根据理论'、'研究表明'等专业用词。"
            "实用型" -> "你是一位实用型AI老师，专注于实际应用，总是告诉学生知识在现实中的用途，用'实际上'、'在生活中'、'具体应用是'等实用导向用词。"
            "启发型" -> "你是一位启发型AI老师，善于通过提问引导学生独立思考，发现答案，用'你觉得呢？'、'试着想想'、'如果是你会怎么做？'等启发用词。"
            else -> "你是一位专业的AI老师，善于因材施教。"
        }
        
        return """${personalityContext}

你正在为一位${user.grade}年级的学生提供学习帮助。这位学生的学习风格是${user.learningStyle}，姓名是${user.name}。

请严格遵循以下教学原则：
1. 完全体现${personality}的教学风格特点和用词习惯
2. 回答要适合${user.grade}学生的认知水平和理解能力
3. 充分结合${user.learningStyle}学习风格给出针对性建议
4. 回答控制在150-250字，内容丰富但简洁易懂
5. 提供实用的学习方法和具体的操作建议
6. 保持教育性、启发性和强互动性
7. 必须用中文回答，语言生动有趣，富有感染力
8. 体现智谱AI的智能水平，给出深度有价值的回答

现在请以${personality}AI老师的身份，用你独特的教学风格和语言特色来回答学生的问题。记住，你是智谱AI GLM-4模型，拥有强大的理解和生成能力！"""
    }
    
    private fun parseZhipuResponse(responseBody: String): String {
        return try {
            val jsonObject = JSONObject(responseBody)
            
            // 检查是否有错误
            if (jsonObject.has("error")) {
                val error = jsonObject.getJSONObject("error")
                val errorMessage = error.getString("message")
                Log.e(TAG, "智谱AI返回错误: $errorMessage")
                return ""
            }
            
            // 解析正常响应
            val choices = jsonObject.getJSONArray("choices")
            if (choices.length() > 0) {
                val firstChoice = choices.getJSONObject(0)
                val message = firstChoice.getJSONObject("message")
                val content = message.getString("content").trim()
                
                // 移除可能的前缀标记
                return content.removePrefix("🧠").removePrefix("🤖").trim()
            } else {
                Log.w(TAG, "智谱AI响应中没有choices")
                return ""
            }
        } catch (e: Exception) {
            Log.e(TAG, "解析智谱AI响应失败: $responseBody", e)
            return ""
        }
    }
    
    /**
     * 测试智谱AI连接
     */
    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🔧 开始测试智谱AI连接...")
            
            val testResult = sendChatMessage(
                "请简单介绍一下你自己，告诉我你是智谱AI的哪个模型，有什么特色能力。",
                User(0, "test", "test@test.com", "", "测试学生", 
                     com.example.educationapp.data.UserType.STUDENT, "大学"),
                "鼓励型"
            )
            
            if (testResult.isSuccess) {
                val response = testResult.getOrNull() ?: ""
                Result.success("✅ 智谱AI连接成功！\n🧠 使用模型: ${ApiConstants.DEFAULT_MODEL}\n🔑 API状态: 正常\n\n$response")
            } else {
                val error = testResult.exceptionOrNull()?.message ?: "未知错误"
                Result.success("❌ 智谱AI连接失败: $error\n\n💡 请检查API密钥是否正确，或者网络连接是否正常")
            }
        } catch (e: Exception) {
            Log.e(TAG, "智谱AI连接测试异常", e)
            Result.success("🔧 连接测试遇到问题: ${e.message}\n\n💡 建议：检查网络连接和API密钥配置")
        }
    }
    
    /**
     * 验证API密钥格式
     */
    fun validateApiKey(): Boolean {
        val apiKey = ApiConstants.DEEPSEEK_API_KEY
        return apiKey.isNotEmpty() && 
               apiKey.contains(".") && 
               apiKey.length > 32 &&
               !apiKey.contains("placeholder")
    }
}
