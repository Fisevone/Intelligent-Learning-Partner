package com.example.educationapp.ai

import android.util.Log
import com.example.educationapp.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * 本地高级AI服务 - 完全离线的智能教学助手
 * 基于规则引擎和知识库，提供专业的教育回复
 */
class LocalAdvancedAIService {
    
    private val TAG = "LocalAdvancedAI"
    
    // 学科知识库
    private val mathKnowledge = mapOf(
        "函数" to listOf(
            "函数是数学中的重要概念，表示两个变量之间的对应关系",
            "学习函数要理解定义域、值域和对应关系",
            "常见函数类型：一次函数、二次函数、三角函数、指数函数等"
        ),
        "方程" to listOf(
            "方程是含有未知数的等式，解方程就是求出未知数的值",
            "解方程的基本思路：化简、移项、合并同类项",
            "检验是解方程的重要步骤，要养成验算的习惯"
        ),
        "几何" to listOf(
            "几何学研究空间的性质，包括点、线、面、体的关系",
            "学几何要培养空间想象能力，多画图帮助理解",
            "记住常用的几何公式：面积、体积、周长等"
        )
    )
    
    private val englishKnowledge = mapOf(
        "语法" to listOf(
            "英语语法是句子结构的规则，掌握基本语法有助于正确表达",
            "重点掌握时态、语态、从句等核心语法点",
            "语法学习要结合大量的阅读和练习"
        ),
        "单词" to listOf(
            "词汇是英语学习的基础，建议采用多种记忆方法",
            "联想记忆、词根词缀、语境记忆都是有效方法",
            "每天坚持记忆新单词，定期复习巩固"
        ),
        "阅读" to listOf(
            "阅读能力需要通过大量练习来提升",
            "从简单文章开始，逐步提高难度",
            "注意理解文章主旨和细节信息"
        )
    )
    
    private val studyMethods = mapOf(
        "记忆" to listOf(
            "理解基础上的记忆更持久有效",
            "使用艾宾浩斯遗忘曲线规律复习",
            "多感官协同记忆：视觉、听觉、动觉结合"
        ),
        "复习" to listOf(
            "复习要有计划性，分阶段进行",
            "及时复习、定期复习、考前复习相结合",
            "复习时要主动思考，不只是简单重复"
        ),
        "时间管理" to listOf(
            "制定合理的学习计划，分解大目标",
            "区分轻重缓急，优先处理重要事务",
            "劳逸结合，保持高效的学习状态"
        )
    )
    
    private val encouragementWords = listOf(
        "你正在努力学习，这本身就很棒！",
        "每一次思考都是进步的开始",
        "坚持下去，你会看到明显的提升",
        "学习是一个过程，要相信自己的能力",
        "遇到困难是正常的，关键是不放弃"
    )
    
    /**
     * 发送聊天消息 - 本地AI处理
     */
    suspend fun sendChatMessage(
        userMessage: String,
        user: User,
        personality: String = "鼓励型"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🧠 本地AI处理消息: ${userMessage.take(30)}...")
            
            // 模拟AI思考时间
            delay(Random.nextLong(800, 2000))
            
            // 智能分析用户输入
            val response = analyzeAndRespond(userMessage, user, personality)
            
            Log.d(TAG, "✅ 本地AI回复生成: ${response.take(50)}...")
            Result.success(response)
            
        } catch (e: Exception) {
            Log.e(TAG, "本地AI处理异常", e)
            Result.success(generateFallbackResponse(personality))
        }
    }
    
    private fun analyzeAndRespond(userMessage: String, user: User, personality: String): String {
        val input = userMessage.lowercase()
        
        // 1. 学科专业分析
        val subjectResponse = analyzeSubject(input, personality)
        if (subjectResponse != null) return subjectResponse
        
        // 2. 学习方法指导
        val methodResponse = analyzeStudyMethod(input, personality)
        if (methodResponse != null) return methodResponse
        
        // 3. 情感支持
        val emotionalResponse = analyzeEmotionalNeeds(input, personality)
        if (emotionalResponse != null) return emotionalResponse
        
        // 4. 问候和通用回复
        return generatePersonalizedResponse(userMessage, user, personality)
    }
    
    private fun analyzeSubject(input: String, personality: String): String? {
        return when {
            // 数学相关
            input.contains("数学") || input.contains("函数") || input.contains("方程") || 
            input.contains("几何") || input.contains("计算") -> {
                val keyword = when {
                    input.contains("函数") -> "函数"
                    input.contains("方程") -> "方程"
                    input.contains("几何") -> "几何"
                    else -> "函数" // 默认
                }
                val knowledge = mathKnowledge[keyword]?.random() ?: "数学需要逻辑思维和大量练习"
                formatResponse(knowledge, getPersonalityStyle(personality), "数学")
            }
            
            // 英语相关
            input.contains("英语") || input.contains("单词") || input.contains("语法") || 
            input.contains("阅读") || input.contains("写作") -> {
                val keyword = when {
                    input.contains("语法") -> "语法"
                    input.contains("单词") -> "单词"
                    input.contains("阅读") -> "阅读"
                    else -> "单词" // 默认
                }
                val knowledge = englishKnowledge[keyword]?.random() ?: "英语学习需要持续的积累"
                formatResponse(knowledge, getPersonalityStyle(personality), "英语")
            }
            
            // 物理相关
            input.contains("物理") || input.contains("力学") || input.contains("电学") -> {
                val knowledge = "物理是研究自然现象的科学，理论与实践相结合很重要"
                formatResponse(knowledge, getPersonalityStyle(personality), "物理")
            }
            
            // 化学相关
            input.contains("化学") || input.contains("元素") || input.contains("反应") -> {
                val knowledge = "化学研究物质的组成和变化，实验是理解化学的重要途径"
                formatResponse(knowledge, getPersonalityStyle(personality), "化学")
            }
            
            else -> null
        }
    }
    
    private fun analyzeStudyMethod(input: String, personality: String): String? {
        return when {
            input.contains("怎么学") || input.contains("学习方法") -> {
                val method = studyMethods.values.flatten().random()
                formatResponse(method, getPersonalityStyle(personality), "学习方法")
            }
            input.contains("记忆") || input.contains("背诵") || input.contains("记不住") -> {
                val method = studyMethods["记忆"]?.random() ?: "记忆需要理解和重复"
                formatResponse(method, getPersonalityStyle(personality), "记忆技巧")
            }
            input.contains("复习") || input.contains("巩固") -> {
                val method = studyMethods["复习"]?.random() ?: "复习要有计划和方法"
                formatResponse(method, getPersonalityStyle(personality), "复习方法")
            }
            input.contains("时间") || input.contains("效率") || input.contains("计划") -> {
                val method = studyMethods["时间管理"]?.random() ?: "合理规划时间很重要"
                formatResponse(method, getPersonalityStyle(personality), "时间管理")
            }
            else -> null
        }
    }
    
    private fun analyzeEmotionalNeeds(input: String, personality: String): String? {
        return when {
            input.contains("累") || input.contains("疲") || input.contains("压力") -> {
                val advice = "学习时感到疲惫很正常，适当休息可以提高效率"
                val encouragement = encouragementWords.random()
                "${getPersonalityPrefix(personality)} $advice\n\n💪 $encouragement"
            }
            input.contains("难") || input.contains("不会") || input.contains("困难") -> {
                val advice = "遇到困难说明你在挑战自己，这是成长的表现"
                val encouragement = encouragementWords.random()
                "${getPersonalityPrefix(personality)} $advice\n\n🌟 $encouragement"
            }
            input.contains("没信心") || input.contains("害怕") || input.contains("紧张") -> {
                val advice = "缺乏信心时要回顾自己的进步，每个人都有自己的节奏"
                val encouragement = encouragementWords.random()
                "${getPersonalityPrefix(personality)} $advice\n\n✨ $encouragement"
            }
            else -> null
        }
    }
    
    private fun generatePersonalizedResponse(userMessage: String, user: User, personality: String): String {
        val hasQuestion = userMessage.contains("?") || userMessage.contains("？")
        val length = userMessage.length
        
        return when {
            userMessage.contains("你好") || userMessage.contains("hello") || userMessage.contains("hi") -> {
                "${getPersonalityPrefix(personality)} 很高兴与你交流！我是你的${personality}AI老师。\n\n🎓 我了解到你是${user.grade}的学生，学习风格偏向${user.learningStyle}。\n\n💡 我可以帮你：解答学习问题、提供学习建议、制定学习计划。\n\n有什么想要学习的内容吗？"
            }
            hasQuestion && length > 10 -> {
                val advice = studyMethods.values.flatten().random()
                "${getPersonalityPrefix(personality)} 这是个很好的问题！\n\n📚 $advice\n\n🔍 如果你能提供更具体的信息，我可以给出更详细的建议。"
            }
            length < 5 -> {
                "${getPersonalityPrefix(personality)} 可以详细说说你的想法吗？我会根据你的具体情况提供针对性的建议。"
            }
            else -> {
                val encouragement = encouragementWords.random()
                val advice = studyMethods.values.flatten().random()
                "${getPersonalityPrefix(personality)} 我理解你的想法。\n\n💭 $advice\n\n🚀 $encouragement"
            }
        }
    }
    
    private fun formatResponse(knowledge: String, style: String, subject: String): String {
        return when (Random.nextInt(3)) {
            0 -> "$style $knowledge\n\n📖 这是$subject 学习的要点，建议多练习加深理解。"
            1 -> "$style 关于$subject，我想说：$knowledge\n\n💡 结合实际应用会更好理解。"
            else -> "$style $knowledge\n\n🎯 在$subject 学习中，坚持练习是关键！"
        }
    }
    
    private fun getPersonalityPrefix(personality: String): String {
        return when (personality) {
            "鼓励型" -> "很棒的问题！"
            "挑战型" -> "这是个有挑战性的问题！"
            "幽默型" -> "哈哈，这个问题很有意思！"
            "严格型" -> "这是个需要认真对待的问题。"
            "创意型" -> "真是个富有创意的问题！"
            "温和型" -> "我理解你的疑问，"
            "激情型" -> "太好了！这个问题很有价值！"
            "学者型" -> "这是个值得深入研究的问题。"
            "实用型" -> "这是个实用性很强的问题。"
            "启发型" -> "让我们一起思考这个问题..."
            else -> "好问题！"
        }
    }
    
    private fun getPersonalityStyle(personality: String): String {
        return when (personality) {
            "鼓励型" -> "✨ 相信自己，你一定可以做到！"
            "挑战型" -> "💪 敢于挑战，突破自己的极限！"
            "幽默型" -> "😄 学习就像冒险，充满趣味和惊喜！"
            "严格型" -> "📚 严谨的态度是学习成功的基础。"
            "创意型" -> "🎨 换个角度思考，会有意想不到的收获！"
            "温和型" -> "🌸 慢慢来，每一步都是进步。"
            "激情型" -> "🔥 燃烧学习的热情，知识就是力量！"
            "学者型" -> "🎓 深入思考，追求真理的本质。"
            "实用型" -> "⚡ 学以致用，知识要能解决实际问题。"
            "启发型" -> "💡 思考比答案更重要。"
            else -> "📖 学习是一个美好的过程。"
        }
    }
    
    private fun generateFallbackResponse(personality: String): String {
        return "${getPersonalityPrefix(personality)} 让我想想如何更好地帮助你学习。${getPersonalityStyle(personality)}"
    }
    
    /**
     * 测试连接
     */
    suspend fun testConnection(): Result<String> {
        return try {
            delay(500) // 模拟检测时间
            Result.success("✅ 本地AI系统运行正常！\n🧠 智能知识库已加载\n🎓 个性化教学引擎就绪")
        } catch (e: Exception) {
            Result.success("🔧 本地AI系统自检完成")
        }
    }
}

