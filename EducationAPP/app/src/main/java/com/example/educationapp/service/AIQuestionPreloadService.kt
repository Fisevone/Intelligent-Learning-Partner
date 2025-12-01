package com.example.educationapp.service

import android.content.Context
import android.util.Log
import com.example.educationapp.ai.AIQuestionGenerator
import com.example.educationapp.ai.AIEmotionRecognizer
import com.example.educationapp.data.User
import com.example.educationapp.data.UserType
import com.example.educationapp.data.LearningRecord
import com.example.educationapp.utils.PreferenceManager
import kotlinx.coroutines.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap

/**
 * AI题目后台预生成服务
 * 在后台持续生成真正的AI题目，确保用户随时有新题目可用
 */
object AIQuestionPreloadService {
    
    private const val TAG = "AIQuestionPreload"
    
    // AI题目缓存队列 (科目 -> AI生成的题目队列)
    private val aiQuestionPools = ConcurrentHashMap<String, ArrayBlockingQueue<AIQuestionGenerator.AIGeneratedQuestion>>()
    
    // 预生成配置
    private const val AI_POOL_SIZE = 10 // 每个科目保持10道AI题目
    private const val MIN_AI_POOL_SIZE = 3 // 最少保持3道AI题目
    private const val GENERATION_BATCH_SIZE = 3 // 每次生成3道题目
    
    // 支持的科目列表
    private val supportedSubjects = listOf(
        "数学", "物理", "化学", "生物", 
        "语文", "英语", "历史", "地理", "计算机"
    )
    
    // 服务状态
    private var isPreloading = false
    private var preloadJob: Job? = null
    private val questionGenerator = AIQuestionGenerator()
    private val emotionRecognizer = AIEmotionRecognizer()
    
    /**
     * 🤖 启动AI题目预生成服务
     */
    fun startAIPreloading(context: Context) {
        if (isPreloading) return
        
        Log.d(TAG, "🤖 启动AI题目预生成服务...")
        isPreloading = true
        
        // 初始化AI题目池
        supportedSubjects.forEach { subject ->
            aiQuestionPools[subject] = ArrayBlockingQueue(AI_POOL_SIZE * 2)
        }
        
        // 启动后台AI生成协程
        preloadJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                // 立即为所有科目生成首批题目
                generateInitialAIQuestions(context)
                
                // 启动持续生成服务
                startContinuousAIGeneration(context)
                
            } catch (e: Exception) {
                Log.e(TAG, "AI预生成服务异常", e)
            }
        }
    }
    
    /**
     * 🚀 立即为所有科目生成首批AI题目
     */
    private suspend fun generateInitialAIQuestions(context: Context) {
        Log.d(TAG, "🚀 开始为所有科目生成首批AI题目...")
        
        val preferenceManager = PreferenceManager(context)
        val user = getCurrentUser(preferenceManager)
        
        // 并行为所有科目生成题目
        val generateJobs = supportedSubjects.map { subject ->
            CoroutineScope(Dispatchers.IO).async {
                generateAIQuestionsForSubject(subject, user, GENERATION_BATCH_SIZE)
            }
        }
        
        generateJobs.forEach { 
            try {
                it.await()
            } catch (e: Exception) {
                Log.w(TAG, "科目题目生成失败", e)
            }
        }
        
        Log.d(TAG, "✅ 首批AI题目生成完成！")
    }
    
    /**
     * 🔄 持续AI题目生成服务
     */
    private suspend fun startContinuousAIGeneration(context: Context) {
        Log.d(TAG, "🔄 启动持续AI题目生成服务...")
        
        val preferenceManager = PreferenceManager(context)
        val user = getCurrentUser(preferenceManager)
        
        while (isPreloading) {
            try {
                // 每30秒检查一次AI题目池状态
                delay(30 * 1000)
                
                supportedSubjects.forEach { subject ->
                    val pool = aiQuestionPools[subject]
                    if (pool != null && pool.size < MIN_AI_POOL_SIZE) {
                        Log.d(TAG, "🔄 ${subject}AI题目池不足(${pool.size}/${MIN_AI_POOL_SIZE})，开始补充...")
                        
                        // 异步补充AI题目
                        CoroutineScope(Dispatchers.IO).launch {
                            generateAIQuestionsForSubject(subject, user, GENERATION_BATCH_SIZE)
                        }
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "持续AI生成异常", e)
            }
        }
    }
    
    /**
     * 🤖 为指定科目生成AI题目
     */
    private suspend fun generateAIQuestionsForSubject(subject: String, user: User, count: Int) {
        val pool = aiQuestionPools[subject] ?: return
        
        Log.d(TAG, "🤖 为${subject}生成${count}道AI题目...")
        
        try {
            // 模拟学习历史
            val learningHistory = generateSampleLearningHistory(user, subject)
            
            // 模拟情绪状态
            val emotionalState = AIEmotionRecognizer.EmotionalState(
                focusLevel = 8,
                stressLevel = 3,
                confidenceLevel = 8,
                fatigueLevel = 2,
                emotionalState = "专注",
                suggestions = listOf("保持当前学习状态"),
                interventionNeeded = false,
                analysisReason = "学习状态良好，专注度较高"
            )
            
            // 配置生成参数
            val config = AIQuestionGenerator.QuestionGenerationConfig(
                targetDifficulty = "中级",
                questionCount = count,
                focusWeakPoints = true,
                includeCreativeQuestions = true,
                timeLimit = null,
                preferredQuestionTypes = listOf("选择题"),
                learningObjective = "巩固${subject}基础知识"
            )
            
            // 调用AI生成题目
            val result = questionGenerator.generateAdaptiveQuestions(
                user = user,
                subject = subject,
                topic = getRandomTopicForSubject(subject),
                learningHistory = learningHistory,
                currentEmotionalState = emotionalState,
                config = config
            )
            
            result.onSuccess { questions ->
                var addedCount = 0
                questions.forEach { question ->
                    if (pool.offer(question)) {
                        addedCount++
                        Log.d(TAG, "✅ ${subject}AI题目添加成功: ${question.question.take(30)}...")
                    } else {
                        Log.w(TAG, "⚠️ ${subject}AI题目池已满，跳过")
                    }
                }
                Log.d(TAG, "🎉 ${subject}AI题目生成完成，新增${addedCount}道，当前池大小: ${pool.size}")
                
            }.onFailure { error ->
                Log.w(TAG, "${subject}AI题目生成失败: ${error.message}")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "${subject}AI题目生成异常", e)
        }
    }
    
    /**
     * 📊 获取AI生成的题目（用户使用时调用）
     */
    fun getAIQuestion(subject: String): AIQuestionGenerator.AIGeneratedQuestion? {
        val pool = aiQuestionPools[subject]
        val question = pool?.poll()
        
        if (question != null) {
            Log.d(TAG, "✅ 获取${subject}AI题目成功，剩余: ${pool.size}")
            
            // 如果AI题目池不足，触发异步补充
            if (pool.size < MIN_AI_POOL_SIZE) {
                Log.d(TAG, "🔄 ${subject}AI题目池不足，触发异步补充")
                // 这里可以触发立即补充，但为了避免阻塞，让持续生成服务处理
            }
        } else {
            Log.w(TAG, "⚠️ ${subject}AI题目池为空，需要等待生成")
        }
        
        return question
    }
    
    /**
     * 📈 获取AI题目池状态
     */
    fun getAIPoolStatus(): Map<String, Int> {
        return aiQuestionPools.mapValues { it.value.size }
    }
    
    /**
     * 🛑 停止AI预生成服务
     */
    fun stopAIPreloading() {
        Log.d(TAG, "🛑 停止AI题目预生成服务")
        isPreloading = false
        preloadJob?.cancel()
        preloadJob = null
    }
    
    // ==================== 辅助方法 ====================
    
    private fun getCurrentUser(preferenceManager: PreferenceManager): User {
        return preferenceManager.getUser() ?: User(
            id = 1,
            username = "student",
            email = "student@example.com",
            password = "",
            name = "张小明",
            userType = UserType.STUDENT,
            grade = "七年级",
            learningStyle = "视觉型",
            interests = "数学,物理"
        )
    }
    
    private fun generateSampleLearningHistory(user: User, subject: String): List<LearningRecord> {
        return listOf(
            LearningRecord(
                id = 1,
                userId = user.id,
                subject = subject,
                topic = getRandomTopicForSubject(subject),
                duration = 30,
                score = 85f,
                difficulty = "中等",
                learningStyle = "练习",
                timestamp = System.currentTimeMillis() - 86400000, // 1天前
                notes = ""
            ),
            LearningRecord(
                id = 2,
                userId = user.id,
                subject = subject,
                topic = getRandomTopicForSubject(subject),
                duration = 40,
                score = 78f,
                difficulty = "困难",
                learningStyle = "练习",
                timestamp = System.currentTimeMillis() - 172800000, // 2天前
                notes = ""
            )
        )
    }
    
    private fun getRandomTopicForSubject(subject: String): String {
        return when (subject) {
            "数学" -> listOf("函数", "方程", "几何", "概率", "统计").random()
            "物理" -> listOf("力学", "电学", "光学", "热学", "原子物理").random()
            "化学" -> listOf("有机化学", "无机化学", "物理化学", "分析化学").random()
            "生物" -> listOf("细胞生物学", "遗传学", "生态学", "进化论").random()
            "语文" -> listOf("现代文阅读", "古文阅读", "作文", "诗词鉴赏").random()
            "英语" -> listOf("语法", "阅读理解", "写作", "听力").random()
            "历史" -> listOf("中国古代史", "中国近代史", "世界史", "文化史").random()
            "地理" -> listOf("自然地理", "人文地理", "区域地理", "地图").random()
            "计算机" -> listOf("编程基础", "数据结构", "算法", "网络").random()
            else -> "基础知识"
        }
    }
}
