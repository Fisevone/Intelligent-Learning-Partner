package com.example.educationapp.ui.ai

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.educationapp.R
import com.example.educationapp.ai.AIQuestionGenerator
import com.example.educationapp.ai.AIEmotionRecognizer
import com.example.educationapp.ai.DeepPersonalizationEngine
import com.example.educationapp.ai.PredictiveInterventionEngine
import com.example.educationapp.ai.ZhipuAIService
import com.example.educationapp.data.User
import com.example.educationapp.data.UserType
import com.example.educationapp.data.LearningRecord
import com.example.educationapp.ai.PreloadedQuestionBank
import com.example.educationapp.utils.PreferenceManager
import com.example.educationapp.service.QuestionPreloadService
import com.example.educationapp.service.AIQuestionPreloadService
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import android.widget.TextView
import android.widget.RadioGroup
import android.widget.RadioButton
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellationException
import java.security.MessageDigest

/**
 * AI智能出题Activity - 展示创新的AI出题功能
 */
class AISmartQuestionActivity : AppCompatActivity() {
    
    // 🔧 开关：优化策略 - 使用真正的AI出题，后台预生成
    private val USE_AI_ONLY = true
    
    private val questionGenerator = AIQuestionGenerator()
    private val emotionRecognizer = AIEmotionRecognizer()
    private val personalizationEngine = DeepPersonalizationEngine()
    private val predictiveEngine = PredictiveInterventionEngine()
    private val zhipuAIService = com.example.educationapp.ai.ZhipuAIService() // 使用智谱GLM-4服务
    private lateinit var preferenceManager: PreferenceManager
    
    // UI组件
    private lateinit var tvQuestionTitle: TextView
    private lateinit var tvQuestionContent: TextView
    private lateinit var radioGroupOptions: RadioGroup
    private lateinit var tvExplanation: TextView
    private lateinit var tvDifficulty: TextView
    private lateinit var tvEmotionalState: TextView
    private lateinit var btnSubmitAnswer: MaterialButton
    private lateinit var btnNextQuestion: MaterialButton
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var cardEmotionStatus: MaterialCardView
    
    // 当前题目和状态
    private var currentQuestion: AIQuestionGenerator.AIGeneratedQuestion? = null
    private var currentEmotionalState: AIEmotionRecognizer.EmotionalState? = null
    private var questionStartTime: Long = 0
    private var answeredCount = 0
    private var correctCount = 0
    
    // 🎯 新增：题目池管理 - 优化速度
    private val questionPool = mutableListOf<AIQuestionGenerator.AIGeneratedQuestion>()
    private var currentQuestionIndex = 0
    private val INITIAL_SIZE = 8 // 启动时生成8道题目，避免快速重复
    private val CACHE_SIZE = 2 // 保持2道题目的缓存
    private var isGeneratingQuestion = false // 出题期间暂停情绪监测
    
    // 🎯 新增：科目和难度管理
    private var selectedSubject: String = "数学"
    private var selectedTopics: List<String> = listOf("函数与导数")
    private var currentDifficulty: String = "基础"
    private var userGrade: String = "大学"
    
    // 🎯 新增：难度反馈历史
    private val difficultyFeedbacks = mutableListOf<DifficultyFeedback>()
    
    // 🎯 新增：深度个性化数据
    private var learnerProfile: DeepPersonalizationEngine.LearnerProfile? = null
    private var currentPrediction: PredictiveInterventionEngine.LearningPrediction? = null
    
    // 行为数据收集
    private val behaviorEvents = mutableListOf<AIEmotionRecognizer.BehaviorEvent>()
    
    // 预备题队列 & AI后台缓冲
    private val preloadedQuestionQueue = ArrayDeque<AIQuestionGenerator.AIGeneratedQuestion>()
    private val aiBackgroundQueue = ArrayDeque<AIQuestionGenerator.AIGeneratedQuestion>()
    
    // ✅ 防重复：SimHash 指纹集合（会话级）
    private val sessionQuestionFingerprints = mutableSetOf<Long>()
    private val SIMHASH_THRESHOLD = 2 // 更严格：≤2 视为重复/近重复
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_smart_question)
        
        preferenceManager = PreferenceManager(this)
        
        // 🎯 获取科目选择信息
        getSubjectSelectionInfo()
        
        initViews()
        setupClickListeners()
        
        // 初始化预备题
        preloadStaticQuestions()
        
        // 🚀 立即开始生成题目，无需等待用户操作
        if (USE_AI_ONLY) {
            // 仅用AI出题：直接进入AI出题会话
            startAIQuestionSession()
        } else {
            // 🎯 启动快速题目准备（优化速度） - 使用预备题库
            startPreloadedQuestionSystem()
        }
        // 🎯 启动深度个性化分析（两种模式都需要）
        initializeDeepPersonalization()
    }
    
    /**
     * 🎯 获取从科目选择界面传来的信息
     */
    private fun getSubjectSelectionInfo() {
        intent?.let { 
            selectedSubject = it.getStringExtra("subject_name") ?: "数学"
            selectedTopics = it.getStringArrayExtra("subject_topics")?.toList() ?: listOf("基础数学")
            currentDifficulty = it.getStringExtra("subject_difficulty") ?: "基础"
            userGrade = it.getStringExtra("user_grade") ?: "大学"
            
            android.util.Log.d("AIQuestion", "📋 科目选择信息:")
            android.util.Log.d("AIQuestion", "   选择科目: '$selectedSubject'")
            android.util.Log.d("AIQuestion", "   主题列表: ${selectedTopics.joinToString(", ")}")
            android.util.Log.d("AIQuestion", "   难度级别: '$currentDifficulty'")
            android.util.Log.d("AIQuestion", "   用户年级: '$userGrade'")
        }
        // 根据科目修正默认主题，避免英语出现数学主题
        if (selectedTopics == listOf("基础数学")) {
            selectedTopics = when (selectedSubject.lowercase()) {
                "英语", "english" -> listOf("语法", "词汇", "时态", "句型", "阅读理解")
                "语文", "chinese" -> listOf("古诗词", "现代文阅读", "语法", "修辞")
                "物理", "physics" -> listOf("力学", "电学", "光学", "热学")
                "化学", "chemistry" -> listOf("化学反应", "化学方程式", "酸碱盐")
                else -> selectedTopics
            }
        }
        
        // 更新标题显示当前科目
        supportActionBar?.title = "$selectedSubject 智能测试"
    }
    
    private fun initViews() {
        tvQuestionTitle = findViewById(R.id.tv_question_title)
        tvQuestionContent = findViewById(R.id.tv_question_content)
        radioGroupOptions = findViewById(R.id.radio_group_options)
        tvExplanation = findViewById(R.id.tv_explanation)
        tvDifficulty = findViewById(R.id.tv_difficulty)
        tvEmotionalState = findViewById(R.id.tv_emotional_state)
        btnSubmitAnswer = findViewById(R.id.btn_submit_answer)
        btnNextQuestion = findViewById(R.id.btn_next_question)
        progressIndicator = findViewById(R.id.progress_indicator)
        cardEmotionStatus = findViewById(R.id.card_emotion_status)
        
        // 初始状态
        tvExplanation.visibility = View.GONE
        btnNextQuestion.visibility = View.GONE
    }
    
    private fun setupClickListeners() {
        btnSubmitAnswer.setOnClickListener {
            submitAnswer()
        }
        
        btnNextQuestion.setOnClickListener {
            showNextQuestionFromPool()
        }
    }
    
    /**
     * 启动AI智能出题会话 - 优先使用预生成的AI题目
     */
    private fun startAIQuestionSession() {
        lifecycleScope.launch {
            try {
                showProgressDialog("🤖 准备AI题目...")
                
                // 初始化情绪状态
                initializeEmotionalState()
                
                Log.d("AIQuestion", "🤖 启动AI出题会话，优先使用预生成题目")
                
                // 优先从AI预生成服务获取题目
                val aiQuestion = AIQuestionPreloadService.getAIQuestion(selectedSubject)
                
                if (aiQuestion != null) {
                    currentQuestion = aiQuestion
                    currentQuestionIndex = 0
                    recordQuestionFingerprint(aiQuestion)
                    recordAnsweredQuestion(aiQuestion.question)
                    
                    displayQuestion(aiQuestion)
                    
                    // 开始情绪监测
                    startEmotionMonitoring()
                    
                    Log.d("AIQuestion", "✅ AI预生成题目获取成功")
                    
                    // 显示AI题目池状态
                    val poolStatus = AIQuestionPreloadService.getAIPoolStatus()
                    val statusText = poolStatus.map { "${it.key}:${it.value}" }.joinToString(", ")
                    Log.d("AIQuestion", "📊 AI题目池状态: $statusText")
                    
                } else {
                    Log.w("AIQuestion", "⚠️ AI预生成题目不可用，实时生成")
                    
                    // 开始情绪监测
                    startEmotionMonitoring()
                    
                    // 实时生成第一道题
                    generateNextQuestion()
                }
                
            } catch (e: Exception) {
                Log.e("AIQuestion", "AI出题会话启动失败", e)
                showError("启动失败：${e.message}")
            } finally {
                hideProgressDialog()
            }
        }
    }
    
    /**
     * 🚀 启动预备题库系统 - 使用全局预加载的题目，瞬间可用
     */
    private fun startPreloadedQuestionSystem() {
        lifecycleScope.launch {
            try {
                showProgressDialog("🚀 准备题库...")
                
                // 初始化情绪状态
                initializeEmotionalState()
                
                Log.d("AIQuestion", "🚀 启动预备题库系统，使用全局预加载题目")
                
                // 优先从全局预加载服务获取题目
                val preloadedQuestion = QuestionPreloadService.getQuestion(selectedSubject)
                
                if (preloadedQuestion != null) {
                    currentQuestion = preloadedQuestion
                    currentQuestionIndex = 0
                    recordQuestionFingerprint(preloadedQuestion)
                    recordAnsweredQuestion(preloadedQuestion.question)
                    
                    displayQuestion(preloadedQuestion)
                    
                    // 开始情绪监测和后台AI生成
                    startEmotionMonitoring()
                    triggerBackgroundAIGeneration()
                    
                    Log.d("AIQuestion", "✅ 全局预加载题目获取成功")
                    
                    // 显示题目池状态
                    val poolStatus = QuestionPreloadService.getPoolStatus()
                    val statusText = poolStatus.map { "${it.key}:${it.value}" }.joinToString(", ")
                    Log.d("AIQuestion", "📊 题目池状态: $statusText")
                    
                } else {
                    Log.w("AIQuestion", "⚠️ 全局预加载题目不可用，使用本地预备题库")
                    
                    // 回退到本地预备题库
                    synchronized(preloadedQuestionQueue) {
                        if (preloadedQuestionQueue.isNotEmpty()) {
                            val firstQuestion = preloadedQuestionQueue.removeFirst()
                            currentQuestion = firstQuestion
                            currentQuestionIndex = 0
                            recordQuestionFingerprint(firstQuestion)
                            recordAnsweredQuestion(firstQuestion.question)
                            
                            displayQuestion(firstQuestion)
                            
                            // 开始情绪监测和后台AI生成
                            startEmotionMonitoring()
                            triggerBackgroundAIGeneration()
                            
                            Log.d("AIQuestion", "✅ 本地预备题库启动成功，剩余题目: ${preloadedQuestionQueue.size}")
                        } else {
                            Log.w("AIQuestion", "⚠️ 所有预备题库均为空，回退到AI生成")
                            startAIQuestionSession()
                        }
                    }
                }
                
            } catch (e: Exception) {
                Log.e("AIQuestion", "预备题库系统启动失败", e)
                showError("启动失败：${e.message}")
            } finally {
                hideProgressDialog()
            }
        }
    }
    
    /**
     * 🎯 快速题目系统 - 优先使用真实题目库，显著提升速度
     */
    private fun startFastQuestionSystem() {
        lifecycleScope.launch {
            try {
                showProgressDialog("⚡ 快速准备题目...")
                
                // 初始化情绪状态
                initializeEmotionalState()
                
                val user = getCurrentUser()
                android.util.Log.d("AIQuestion", "🚀 启动快速题目系统，优先使用真实题目库")
                
                // 🎯 立即从真实题目库生成题目（0等待）
                generateFromRealQuestionBank(user)
                
                if (questionPool.isNotEmpty()) {
                    currentQuestionIndex = 0
                    displayQuestion(questionPool[currentQuestionIndex])
                    
                    // 开始后台智能缓存
                    startIntelligentCaching()
                    
                    // 开始情绪监测
                    startEmotionMonitoring()
                    
                    android.util.Log.d("AIQuestion", "✅ 快速题目系统启动成功，${questionPool.size}道题目已准备")
                } else {
                    showError("题目准备失败，请重试")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("AIQuestion", "快速题目系统启动失败", e)
                showError("启动失败：${e.message}")
            } finally {
                hideProgressDialog()
            }
        }
    }
    
    /**
     * 🎯 从真实题目库生成题目（瞬间完成）
     */
    private fun generateFromRealQuestionBank(user: User) {
        android.util.Log.d("AIQuestion", "🎯 使用175+真实题目库，瞬间生成")
        
        // 根据科目和年级智能生成
        for (i in 1..INITIAL_SIZE) {
            val topic = selectTopicIntelligently()
            
            // 🚀 直接生成本地题目库的题目（瞬间完成，无网络请求）
            val question = createLocalQuestion(selectedSubject, topic, user.grade, i)
            
            if (question != null && !isDuplicateQuestion(question)) {
                recordQuestionFingerprint(question)
                questionPool.add(question)
                android.util.Log.d("AIQuestion", "✅ 从题目库生成第 $i 道题目: ${question.question}")
            } else {
                android.util.Log.d("AIQuestion", "⏭️ 跳过重复题目，尝试替换一题")
            }
        }
    }
    
    /**
     * 🎯 直接创建本地题目（瞬间完成）
     */
    private fun createLocalQuestion(subject: String, topic: String, grade: String, index: Int): AIQuestionGenerator.AIGeneratedQuestion? {
        android.util.Log.d("AIQuestion", "🔍 创建题目 - 科目: '$subject', 主题: '$topic', 索引: $index")
        return when (subject.lowercase()) {
            "数学" -> {
                android.util.Log.d("AIQuestion", "✅ 选择数学题目")
                createMathQuestion(topic, grade, index)
            }
            "物理" -> {
                android.util.Log.d("AIQuestion", "✅ 选择物理题目")
                createPhysicsQuestion(topic, grade, index)
            }
            "计算机" -> {
                android.util.Log.d("AIQuestion", "✅ 选择计算机题目")
                createComputerQuestion(topic, grade, index)
            }
            "语文" -> {
                android.util.Log.d("AIQuestion", "✅ 选择语文题目")
                createChineseQuestion(topic, grade, index)
            }
            "英语", "english" -> {
                android.util.Log.d("AIQuestion", "✅ 选择英语题目 - 科目匹配成功!")
                createEnglishQuestion(topic, grade, index)
            }
            "生物" -> {
                android.util.Log.d("AIQuestion", "✅ 选择生物题目")
                createBiologyQuestion(index)
            }
            "地理" -> {
                android.util.Log.d("AIQuestion", "✅ 选择地理题目")
                createGeographyQuestion(index)
            }
            "历史" -> {
                android.util.Log.d("AIQuestion", "✅ 选择历史题目")
                createHistoryQuestion(index)
            }
            else -> {
                android.util.Log.w("AIQuestion", "⚠️ 未知科目 '$subject'，使用通用题目生成")
                createGeneralQuestion(subject, topic, grade, index)
            }
        }
    }
    
    /**
     * 🎯 创建数学题目
     */
    private fun createMathQuestion(topic: String, grade: String, index: Int): AIQuestionGenerator.AIGeneratedQuestion {
        val mathQuestions = when (topic) {
            "函数", "函数与导数" -> listOf(
                Triple("已知函数f(x) = x² - 2x + 1，求f(x)的最小值。", listOf("0", "1", "-1", "2"), "0"),
                Triple("函数f(x) = x³ - 3x的导数f'(x)是：", listOf("3x² - 3", "3x² + 3", "x² - 3", "3x - 3"), "3x² - 3"),
                Triple("已知f(x) = sin x，则f'(π/2)的值是：", listOf("0", "1", "-1", "π/2"), "0")
            )
            "极限", "极限理论" -> listOf(
                Triple("计算极限 lim(x→0) (sin x / x) 的值。", listOf("1", "0", "∞", "不存在"), "1"),
                Triple("计算极限 lim(x→1) (x² - 1)/(x - 1) 的值。", listOf("2", "1", "0", "不存在"), "2"),
                Triple("计算极限 lim(x→∞) (1/x) 的值。", listOf("0", "1", "∞", "不存在"), "0")
            )
            else -> listOf(
                Triple("一个正方体的棱长为3cm，求它的体积。", listOf("27 cm³", "18 cm³", "9 cm³", "36 cm³"), "27 cm³"),
                Triple("sin²x + cos²x的值等于：", listOf("1", "0", "2", "sin x"), "1"),
                Triple("log₂ 8的值是：", listOf("3", "4", "2", "8"), "3")
            )
        }
        
        val (question, options, answer) = mathQuestions[index % mathQuestions.size]
        
        return AIQuestionGenerator.AIGeneratedQuestion(
            id = "math_local_${System.currentTimeMillis()}_$index",
            subject = "数学",
            topic = topic,
            question = question,
            options = options,
            correctAnswer = answer,
            explanation = "这是一道数学基础题目，考查${topic}的核心概念。",
            difficulty = if (grade.contains("大学")) "高级" else "中级",
            questionType = "选择题",
            knowledgePoints = listOf(topic, "数学基础"),
            estimatedTime = 120,
            adaptiveReason = "根据$topic 主题智能选择",
            creativityLevel = "标准"
        )
    }
    
    
    /**
     * 🎯 创建计算机题目
     */
    private fun createComputerQuestion(topic: String, grade: String, index: Int): AIQuestionGenerator.AIGeneratedQuestion {
        val computerQuestions = listOf(
            Triple("在一个空栈中依次压入元素1、2、3、4，然后依次弹出，弹出顺序是：", listOf("1、2、3、4", "4、3、2、1", "2、1、4、3", "1、3、2、4"), "4、3、2、1"),
            Triple("快速排序算法的平均时间复杂度是：", listOf("O(n)", "O(n log n)", "O(n²)", "O(log n)"), "O(n log n)"),
            Triple("SQL中用于查询数据的关键字是：", listOf("SELECT", "INSERT", "UPDATE", "DELETE"), "SELECT")
        )
        
        val (question, options, answer) = computerQuestions[index % computerQuestions.size]
        
        return AIQuestionGenerator.AIGeneratedQuestion(
            id = "computer_local_${System.currentTimeMillis()}_$index",
            subject = "计算机",
            topic = topic,
            question = question,
            options = options,
            correctAnswer = answer,
            explanation = "这是一道计算机科学题目，考查${topic}的核心概念。",
            difficulty = "高级",
            questionType = "选择题",
            knowledgePoints = listOf(topic, "计算机基础"),
            estimatedTime = 150,
            adaptiveReason = "根据$topic 主题智能选择",
            creativityLevel = "标准"
        )
    }
    
    
    private fun createBiologyQuestion(index: Int): AIQuestionGenerator.AIGeneratedQuestion {
        val questions = listOf(
            Triple("DNA分子的双螺旋结构是由哪两位科学家发现的？", listOf("沃森和克里克", "孟德尔和达尔文", "巴斯德和弗莱明", "哈维和盖伦"), "沃森和克里克")
        )
        val (question, options, answer) = questions[index % questions.size]
        
        return AIQuestionGenerator.AIGeneratedQuestion(
            id = "biology_local_${System.currentTimeMillis()}_$index",
            subject = "生物", topic = "基础生物", question = question, options = options, correctAnswer = answer,
            explanation = "这是一道生物题目。", difficulty = "基础", questionType = "选择题",
            knowledgePoints = listOf("生物基础"), estimatedTime = 90, adaptiveReason = "生物基础", creativityLevel = "标准"
        )
    }
    
    private fun createGeographyQuestion(index: Int): AIQuestionGenerator.AIGeneratedQuestion {
        val questions = listOf(
            Triple("地球上最长的山脉是：", listOf("安第斯山脉", "喜马拉雅山脉", "阿尔卑斯山脉", "落基山脉"), "安第斯山脉")
        )
        val (question, options, answer) = questions[index % questions.size]
        
        return AIQuestionGenerator.AIGeneratedQuestion(
            id = "geography_local_${System.currentTimeMillis()}_$index",
            subject = "地理", topic = "自然地理", question = question, options = options, correctAnswer = answer,
            explanation = "这是一道地理题目。", difficulty = "基础", questionType = "选择题",
            knowledgePoints = listOf("地理基础"), estimatedTime = 90, adaptiveReason = "地理基础", creativityLevel = "标准"
        )
    }
    
    private fun createHistoryQuestion(index: Int): AIQuestionGenerator.AIGeneratedQuestion {
        val questions = listOf(
            Triple("中国古代四大发明包括：", listOf("造纸术、印刷术、指南针、火药", "造纸术、丝绸、瓷器、茶叶", "书法、绘画、诗歌、音乐", "儒学、道学、佛学、法学"), "造纸术、印刷术、指南针、火药")
        )
        val (question, options, answer) = questions[index % questions.size]
        
        return AIQuestionGenerator.AIGeneratedQuestion(
            id = "history_local_${System.currentTimeMillis()}_$index",
            subject = "历史", topic = "中国古代史", question = question, options = options, correctAnswer = answer,
            explanation = "这是一道历史题目。", difficulty = "基础", questionType = "选择题",
            knowledgePoints = listOf("历史基础"), estimatedTime = 90, adaptiveReason = "历史基础", creativityLevel = "标准"
        )
    }
    
    private fun createGeneralQuestion(subject: String, topic: String, grade: String, index: Int): AIQuestionGenerator.AIGeneratedQuestion {
        android.util.Log.d("AIQuestion", "🔄 通用题目生成 - 科目: '$subject'")
        return when (subject.lowercase()) {
            "英语", "english" -> {
                android.util.Log.d("AIQuestion", "✅ 通用路径选择英语题目")
                createEnglishQuestion(topic, grade, index)
            }
            "语文", "chinese" -> {
                android.util.Log.d("AIQuestion", "✅ 通用路径选择语文题目")
                createChineseQuestion(topic, grade, index)
            }
            "物理", "physics" -> {
                android.util.Log.d("AIQuestion", "✅ 通用路径选择物理题目")
                createPhysicsQuestion(topic, grade, index)
            }
            "化学", "chemistry" -> {
                android.util.Log.d("AIQuestion", "✅ 通用路径选择化学题目")
                createChemistryQuestion(topic, grade, index)
            }
            "生物", "biology" -> {
                android.util.Log.d("AIQuestion", "✅ 通用路径选择生物题目")
                createBiologyQuestion(index)
            }
            "历史", "history" -> {
                android.util.Log.d("AIQuestion", "✅ 通用路径选择历史题目")
                createHistoryQuestion(index)
            }
            "地理", "geography" -> {
                android.util.Log.d("AIQuestion", "✅ 通用路径选择地理题目")
                createGeographyQuestion(index)
            }
            else -> {
                android.util.Log.w("AIQuestion", "⚠️ 通用路径默认数学题目 - 科目: '$subject'")
                createMathQuestion(topic, grade, index) // 默认数学
            }
        }
    }
    
    /**
     * 🎯 创建英语题目 - 大量题库，避免重复
     */
    private fun createEnglishQuestion(topic: String, grade: String, index: Int): AIQuestionGenerator.AIGeneratedQuestion {
        val englishQuestions = when (topic.lowercase()) {
            "语法", "grammar" -> listOf(
                Triple("I _____ to school every day.", listOf("go", "goes", "going", "went"), "go"),
                Triple("She _____ her homework yesterday.", listOf("do", "does", "did", "doing"), "did"),
                Triple("They _____ playing football now.", listOf("is", "are", "was", "were"), "are"),
                Triple("He _____ to the park tomorrow.", listOf("go", "goes", "will go", "went"), "will go"),
                Triple("_____ you like coffee?", listOf("Do", "Does", "Did", "Are"), "Do"),
                Triple("She _____ speak English very well.", listOf("can", "cans", "could", "may"), "can"),
                Triple("There _____ many books on the table.", listOf("is", "are", "was", "were"), "are"),
                Triple("I have _____ apple.", listOf("a", "an", "the", "some"), "an"),
                Triple("_____ beautiful day it is!", listOf("What", "What a", "How", "How a"), "What a"),
                Triple("She is _____ than her sister.", listOf("tall", "taller", "tallest", "more tall"), "taller")
            )
            "词汇", "vocabulary" -> listOf(
                Triple("What does 'beautiful' mean?", listOf("美丽的", "聪明的", "勇敢的", "善良的"), "美丽的"),
                Triple("What does 'happy' mean?", listOf("悲伤的", "快乐的", "生气的", "害怕的"), "快乐的"),
                Triple("Choose the opposite of 'big':", listOf("large", "huge", "small", "great"), "small"),
                Triple("What does 'study' mean?", listOf("玩耍", "学习", "睡觉", "吃饭"), "学习"),
                Triple("Choose the correct spelling:", listOf("definately", "definitely", "definetly", "definitly"), "definitely"),
                Triple("What does 'library' mean?", listOf("图书馆", "医院", "学校", "公园"), "图书馆"),
                Triple("Choose the synonym of 'fast':", listOf("slow", "quick", "heavy", "light"), "quick"),
                Triple("What does 'weather' mean?", listOf("天气", "季节", "时间", "地点"), "天气"),
                Triple("Choose the correct word for '老师':", listOf("student", "teacher", "doctor", "worker"), "teacher"),
                Triple("What does 'delicious' mean?", listOf("美味的", "难吃的", "咸的", "甜的"), "美味的")
            )
            "时态", "tense" -> listOf(
                Triple("Past tense of 'go' is:", listOf("go", "goes", "went", "gone"), "went"),
                Triple("Past tense of 'eat' is:", listOf("eat", "eats", "ate", "eaten"), "ate"),
                Triple("I _____ TV yesterday.", listOf("watch", "watches", "watched", "watching"), "watched"),
                Triple("She _____ to work every day.", listOf("go", "goes", "went", "going"), "goes"),
                Triple("We _____ dinner at 7 PM tomorrow.", listOf("have", "has", "had", "will have"), "will have"),
                Triple("They _____ football when it started raining.", listOf("play", "played", "were playing", "are playing"), "were playing"),
                Triple("I _____ this book already.", listOf("read", "reads", "have read", "am reading"), "have read"),
                Triple("By next year, she _____ graduated.", listOf("will", "will have", "has", "had"), "will have"),
                Triple("He _____ here for 5 years.", listOf("live", "lives", "lived", "has lived"), "has lived"),
                Triple("What _____ you doing now?", listOf("is", "are", "was", "were"), "are")
            )
            "阅读理解", "reading" -> listOf(
                Triple("In the passage: 'Tom likes apples.' What does Tom like?", listOf("oranges", "apples", "bananas", "grapes"), "apples"),
                Triple("'She goes to school by bus.' How does she go to school?", listOf("by car", "by bike", "by bus", "on foot"), "by bus"),
                Triple("'The cat is sleeping on the bed.' Where is the cat?", listOf("on the floor", "on the bed", "under the table", "in the box"), "on the bed"),
                Triple("'It's sunny today.' What's the weather like?", listOf("rainy", "cloudy", "sunny", "snowy"), "sunny"),
                Triple("'I have two brothers.' How many brothers?", listOf("one", "two", "three", "four"), "two"),
                Triple("'We will meet at 3 o'clock.' When will they meet?", listOf("2 o'clock", "3 o'clock", "4 o'clock", "5 o'clock"), "3 o'clock"),
                Triple("'The book is on the shelf.' Where is the book?", listOf("on the desk", "on the shelf", "in the bag", "under the chair"), "on the shelf"),
                Triple("'She can swim very well.' What can she do well?", listOf("run", "swim", "jump", "dance"), "swim"),
                Triple("'There are 30 students in our class.' How many students?", listOf("20", "25", "30", "35"), "30"),
                Triple("'My favorite color is blue.' What's his favorite color?", listOf("red", "green", "blue", "yellow"), "blue")
            )
            else -> listOf(
                Triple("Choose the correct greeting:", listOf("Good morning", "Good mornings", "A good morning", "The good morning"), "Good morning"),
                Triple("How do you say '谢谢' in English?", listOf("Sorry", "Excuse me", "Thank you", "Please"), "Thank you"),
                Triple("What's the capital of England?", listOf("Manchester", "Liverpool", "Birmingham", "London"), "London"),
                Triple("How many letters in the English alphabet?", listOf("24", "25", "26", "27"), "26"),
                Triple("What comes after Monday?", listOf("Sunday", "Tuesday", "Wednesday", "Thursday"), "Tuesday"),
                Triple("'How are you?' - The correct response is:", listOf("I'm fine, thank you", "Yes, I am", "No, I'm not", "Nice to meet you"), "I'm fine, thank you"),
                Triple("What's the plural of 'child'?", listOf("childs", "childes", "children", "child"), "children"),
                Triple("Choose the correct question word: '_____ is your name?'", listOf("Who", "What", "Where", "When"), "What"),
                Triple("What time is it? 3:15 = ", listOf("three fifteen", "fifteen three", "three and fifteen", "quarter to three"), "three fifteen"),
                Triple("'See you later' means:", listOf("再见", "你好", "谢谢", "对不起"), "再见")
            )
        }
        
        val selectedQuestion = englishQuestions[index % englishQuestions.size]
        return AIQuestionGenerator.AIGeneratedQuestion(
            id = "english_local_${System.currentTimeMillis()}_$index",
            subject = "英语", topic = topic,
            question = selectedQuestion.first,
            options = selectedQuestion.second, correctAnswer = selectedQuestion.third,
            explanation = "这是一道英语基础题目，考查英语语法和词汇知识。",
            difficulty = "基础", questionType = "选择题",
            knowledgePoints = listOf("英语语法", "词汇"), estimatedTime = 120,
            adaptiveReason = "英语基础知识练习", creativityLevel = "标准"
        )
    }
    
    /**
     * 🎯 创建语文题目
     */
    private fun createChineseQuestion(topic: String, grade: String, index: Int): AIQuestionGenerator.AIGeneratedQuestion {
        val chineseQuestions = listOf(
            Triple(
                "下列词语中，读音完全正确的是：",
                listOf("载(zài)重", "载(zǎi)体", "记载(zài)", "载(zǎi)歌载舞"),
                "载(zài)重"
            ),
            Triple(
                "\"春眠不觉晓\"的下一句是：",
                listOf("处处闻啼鸟", "夜来风雨声", "花落知多少", "红掌拨清波"),
                "处处闻啼鸟"
            ),
            Triple(
                "下列句子中没有语病的是：",
                listOf(
                    "通过这次活动，使我受到了教育",
                    "这次活动使我受到了很大教育",
                    "经过这次活动，我受到了教育很大",
                    "这次活动让我受到了很大的教育"
                ),
                "这次活动使我受到了很大教育"
            )
        )
        
        val selectedQuestion = chineseQuestions[index % chineseQuestions.size]
        return AIQuestionGenerator.AIGeneratedQuestion(
            id = "chinese_local_${System.currentTimeMillis()}_$index",
            subject = "语文", topic = topic,
            question = selectedQuestion.first,
            options = selectedQuestion.second, correctAnswer = selectedQuestion.third,
            explanation = "这是一道语文题目，考查汉语言文字运用能力。",
            difficulty = "基础", questionType = "选择题",
            knowledgePoints = listOf("语言文字运用"), estimatedTime = 150,
            adaptiveReason = "语文基础知识练习", creativityLevel = "标准"
        )
    }
    
    /**
     * 🎯 创建物理题目
     */
    private fun createPhysicsQuestion(topic: String, grade: String, index: Int): AIQuestionGenerator.AIGeneratedQuestion {
        val physicsQuestions = listOf(
            Triple(
                "一个物体从静止开始，以2m/s²的加速度运动，5秒后的速度是：",
                listOf("10 m/s", "5 m/s", "2 m/s", "25 m/s"),
                "10 m/s"
            ),
            Triple(
                "重力加速度g的数值约为：",
                listOf("9.8 m/s²", "10 m/s²", "9.8 m/s", "10 m/s"),
                "9.8 m/s²"
            ),
            Triple(
                "欧姆定律的表达式是：",
                listOf("U = IR", "I = UR", "R = UI", "P = UI"),
                "U = IR"
            )
        )
        
        val selectedQuestion = physicsQuestions[index % physicsQuestions.size]
        return AIQuestionGenerator.AIGeneratedQuestion(
            id = "physics_local_${System.currentTimeMillis()}_$index",
            subject = "物理", topic = topic,
            question = selectedQuestion.first,
            options = selectedQuestion.second, correctAnswer = selectedQuestion.third,
            explanation = "这是一道物理题目，考查物理基础概念和计算。",
            difficulty = "基础", questionType = "选择题",
            knowledgePoints = listOf("物理概念"), estimatedTime = 180,
            adaptiveReason = "物理基础知识练习", creativityLevel = "标准"
        )
    }
    
    /**
     * 🎯 创建化学题目
     */
    private fun createChemistryQuestion(topic: String, grade: String, index: Int): AIQuestionGenerator.AIGeneratedQuestion {
        val chemistryQuestions = listOf(
            Triple(
                "水的化学分子式是：",
                listOf("H₂O", "CO₂", "NaCl", "CaCO₃"),
                "H₂O"
            ),
            Triple(
                "下列物质中属于酸的是：",
                listOf("HCl", "NaOH", "NaCl", "CaCO₃"),
                "HCl"
            )
        )
        
        val selectedQuestion = chemistryQuestions[index % chemistryQuestions.size]
        return AIQuestionGenerator.AIGeneratedQuestion(
            id = "chemistry_local_${System.currentTimeMillis()}_$index",
            subject = "化学", topic = topic,
            question = selectedQuestion.first,
            options = selectedQuestion.second, correctAnswer = selectedQuestion.third,
            explanation = "这是一道化学题目，考查化学基础知识。",
            difficulty = "基础", questionType = "选择题",
            knowledgePoints = listOf("化学基础"), estimatedTime = 120,
            adaptiveReason = "化学基础知识练习", creativityLevel = "标准"
        )
    }
    
    /**
     * 🎯 创建生物题目
     */
    private fun createBiologyQuestion(topic: String, grade: String, index: Int): AIQuestionGenerator.AIGeneratedQuestion {
        return AIQuestionGenerator.AIGeneratedQuestion(
            id = "biology_local_${System.currentTimeMillis()}_$index",
            subject = "生物", topic = topic,
            question = "植物进行光合作用需要的条件不包括：",
            options = listOf("阳光", "二氧化碳", "水分", "氧气"), correctAnswer = "氧气",
            explanation = "光合作用需要阳光、二氧化碳和水分，产生氧气而不是消耗氧气。",
            difficulty = "基础", questionType = "选择题",
            knowledgePoints = listOf("光合作用"), estimatedTime = 120,
            adaptiveReason = "生物基础知识练习", creativityLevel = "标准"
        )
    }
    
    /**
     * 🎯 创建历史题目
     */
    private fun createHistoryQuestion(topic: String, grade: String, index: Int): AIQuestionGenerator.AIGeneratedQuestion {
        return AIQuestionGenerator.AIGeneratedQuestion(
            id = "history_local_${System.currentTimeMillis()}_$index",
            subject = "历史", topic = topic,
            question = "中国古代四大发明不包括：",
            options = listOf("造纸术", "指南针", "火药", "算盘"), correctAnswer = "算盘",
            explanation = "中国古代四大发明是造纸术、指南针、火药和印刷术。",
            difficulty = "基础", questionType = "选择题",
            knowledgePoints = listOf("中国古代史"), estimatedTime = 120,
            adaptiveReason = "历史基础知识练习", creativityLevel = "标准"
        )
    }
    
    /**
     * 🎯 创建地理题目
     */
    private fun createGeographyQuestion(topic: String, grade: String, index: Int): AIQuestionGenerator.AIGeneratedQuestion {
        return AIQuestionGenerator.AIGeneratedQuestion(
            id = "geography_local_${System.currentTimeMillis()}_$index",
            subject = "地理", topic = topic,
            question = "世界上最大的大洲是：",
            options = listOf("亚洲", "非洲", "北美洲", "南美洲"), correctAnswer = "亚洲",
            explanation = "亚洲是世界上面积最大的大洲。",
            difficulty = "基础", questionType = "选择题",
            knowledgePoints = listOf("世界地理"), estimatedTime = 120,
            adaptiveReason = "地理基础知识练习", creativityLevel = "标准"
        )
    }
    
    /**
     * 🎯 后台智能缓存系统
     */
    private fun startIntelligentCaching() {
        lifecycleScope.launch {
            try {
                android.util.Log.d("AIQuestion", "🔄 启动后台智能缓存")
                
                // 给用户1-2道题的答题时间，然后开始缓存
                delay(10000) // 10秒后开始
                
                while (!isFinishing) {
                    // 检查是否需要补充缓存
                    val remainingQuestions = questionPool.size - currentQuestionIndex - 1
                    if (remainingQuestions <= CACHE_SIZE) {
                        android.util.Log.d("AIQuestion", "📦 触发智能缓存补充")
                        
                        // 优先从真实题目库补充（加入去重）
                        val user = getCurrentUser()
                        for (i in 1..3) { // 每次补充3道
                            val topic = selectTopicIntelligently()
                            val question = createLocalQuestion(selectedSubject, topic, user.grade, questionPool.size + i)
                            
                            if (question != null && !isDuplicateQuestion(question)) {
                                recordQuestionFingerprint(question)
                                questionPool.add(question)
                                android.util.Log.d("AIQuestion", "📦 缓存补充第 $i 道题目: ${question.question}")
                            }
                        }
                        
                        // 如果还想要一些AI生成的创新题目，可以偶尔调用AI
                        if (questionPool.size % 10 == 0) { // 每10道题生成1道AI题目
                            tryGenerateAIQuestion()
                        }
                    }
                    
                    delay(5000) // 每5秒检查一次
                }
            } catch (e: Exception) {
                android.util.Log.w("AIQuestion", "智能缓存出错: ${e.message}")
            }
        }
    }
    
    /**
     * 🎯 尝试生成AI题目（可选，增加创新性）
     */
    private suspend fun tryGenerateAIQuestion() {
        try {
            android.util.Log.d("AIQuestion", "🤖 尝试生成AI创新题目")
            
            val user = getCurrentUser()
            val learningHistory = getLearningHistory()
            val topic = selectTopicIntelligently()
            val config = AIQuestionGenerator.QuestionGenerationConfig(
                questionCount = 1,
                targetDifficulty = currentDifficulty,
                includeCreativeQuestions = true,
                focusWeakPoints = true
            )
            
            val result = questionGenerator.generateAdaptiveQuestions(
                user = user,
                subject = selectedSubject,
                topic = topic,
                learningHistory = learningHistory,
                currentEmotionalState = currentEmotionalState,
                config = config
            )
            
            result.onSuccess { questions ->
                if (questions.isNotEmpty()) {
                    val q = questions.first()
                    if (!isDuplicateQuestion(q)) {
                        recordQuestionFingerprint(q)
                        questionPool.add(q)
                        android.util.Log.d("AIQuestion", "🤖 AI题目生成成功: ${q.question}")
                    } else {
                        android.util.Log.d("AIQuestion", "🤖 AI生成题去重后被丢弃")
                    }
                }
            }.onFailure { error ->
                android.util.Log.w("AIQuestion", "AI题目生成失败，继续使用题目库: ${error.message}")
            }
        } catch (e: Exception) {
            android.util.Log.w("AIQuestion", "AI题目生成异常: ${e.message}")
        }
    }
    
    /**
     * 🎯 从题目池显示下一题（无等待时间）
     */
    private fun showNextQuestionFromPool() {
        if (USE_AI_ONLY) {
            // 🤖 AI模式：优先使用AI预生成的题目
            val aiQuestion = AIQuestionPreloadService.getAIQuestion(selectedSubject)
            
            if (aiQuestion != null) {
                currentQuestionIndex++
                currentQuestion = aiQuestion
                recordQuestionFingerprint(aiQuestion)
                recordAnsweredQuestion(aiQuestion.question)
                displayQuestion(aiQuestion)
                Log.d("AIQuestion", "✅ 使用AI预生成题目: ${aiQuestion.question.take(30)}...")
                return
            } else {
                // AI预生成题目不可用，尝试其他方式
                Log.w("AIQuestion", "⚠️ AI预生成题目不可用，尝试其他方式")
                val next = pollNextPreloadedOrBackgroundQuestion()
                if (next != null) {
                    displayQuestion(next)
                } else {
                    // 实时生成
                    showProgressDialog("🤖 AI正在生成新题目...")
                    generateNextQuestion()
                }
            }
        } else {
            // 非AI模式：使用预加载的静态题目
            val preloadedQuestion = QuestionPreloadService.getQuestion(selectedSubject)
            
            if (preloadedQuestion != null) {
                currentQuestionIndex++
                currentQuestion = preloadedQuestion
                recordQuestionFingerprint(preloadedQuestion)
                recordAnsweredQuestion(preloadedQuestion.question)
                displayQuestion(preloadedQuestion)
                Log.d("AIQuestion", "✅ 使用全局预加载题目: ${preloadedQuestion.question.take(30)}...")
                return
            }
            
            if (currentQuestionIndex + 1 < questionPool.size) {
                currentQuestionIndex++
                displayQuestion(questionPool[currentQuestionIndex])
                android.util.Log.d("AIQuestion", "显示第 ${currentQuestionIndex + 1} 道题目")
            } else {
                // 题目池用完了，需要补充
                android.util.Log.d("AIQuestion", "题目池已用完，开始补充...")
                generateMoreQuestions()
            }
        }
    }
    
    /**
     * 🎯 补充更多题目到题目池
     */
    private fun generateMoreQuestions() {
        lifecycleScope.launch {
            try {
                showProgressDialog("🎯 正在准备更多题目...")
                
                val user = getCurrentUser()
                val learningHistory = getLearningHistory()
                
                android.util.Log.d("AIQuestion", "开始补充 3 道题目")
                
                for (i in 1..3) {
                    val topic = selectTopicIntelligently()
                    val config = AIQuestionGenerator.QuestionGenerationConfig(
                        questionCount = 1,
                        targetDifficulty = currentDifficulty,
                        includeCreativeQuestions = true,
                        focusWeakPoints = true
                    )
                    
                    val result = questionGenerator.generateAdaptiveQuestions(
                        user = user,
                        subject = selectedSubject,
                        topic = topic,
                        learningHistory = learningHistory,
                        currentEmotionalState = currentEmotionalState,
                        config = config
                    )
                    
                    result.onSuccess { questions ->
                        if (questions.isNotEmpty()) {
                            questionPool.add(questions.first())
                            android.util.Log.d("AIQuestion", "补充第 $i 道题目: ${questions.first().question}")
                        }
                    }.onFailure { error ->
                        android.util.Log.w("AIQuestion", "补充第 $i 道题目失败: ${error.message}")
                    }
                    
                    delay(100)
                }
                
                android.util.Log.d("AIQuestion", "题目池补充完成，现有 ${questionPool.size} 道题目")
                
                // 显示下一题
                if (currentQuestionIndex + 1 < questionPool.size) {
                    currentQuestionIndex++
                    displayQuestion(questionPool[currentQuestionIndex])
                } else {
                    showError("无法生成更多题目")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("AIQuestion", "补充题目失败", e)
                showError("补充题目失败：${e.message}")
            } finally {
                hideProgressDialog()
            }
        }
    }
    
    /**
     * 🎯 初始化情绪状态
     */
    private fun initializeEmotionalState() {
        // 创建初始情绪状态
        currentEmotionalState = AIEmotionRecognizer.EmotionalState(
            focusLevel = 7,
            stressLevel = 3,
            confidenceLevel = 6,
            fatigueLevel = 2,
            emotionalState = "专注",
            suggestions = listOf("保持当前学习状态"),
            interventionNeeded = false,
            analysisReason = "初始化默认状态"
        )
        
        android.util.Log.d("AIQuestion", "初始化情绪状态: ${currentEmotionalState}")
    }
    
    /**
     * 🧠 创新功能：实时情绪监测
     */
    private fun startEmotionMonitoring() {
        lifecycleScope.launch {
            while (!isFinishing) {
                   delay(120000) // 每2分钟分析一次情绪，避免频繁打断做题
                
                // 出题期间暂停情绪监测，避免误判等待状态为注意力不集中
                if (!isGeneratingQuestion && behaviorEvents.size >= 2) {
                    try {
                        val user = getCurrentUser()
                        val result = emotionRecognizer.analyzeRealTimeEmotion(
                            user = user,
                            recentBehaviors = behaviorEvents.takeLast(8)
                        )
                        
                        result.onSuccess { emotionalState ->
                            currentEmotionalState = emotionalState
                            updateEmotionDisplay(emotionalState)
                        }
                    } catch (e: CancellationException) {
                        // 协程被取消，退出循环
                        break
                    } catch (e: Exception) {
                        // 其他异常静默处理
                        Log.d("EmotionMonitoring", "情绪监控异常: ${e.message}")
                    }
                }
            }
        }
    }
    
    /**
     * 🚀 优化版：预备题优先，AI后台生成
     */
    private fun generateNextQuestion() {
        lifecycleScope.launch {
            try {
                isGeneratingQuestion = true // 开始出题，暂停情绪监测
                // 🎯 策略1：优先从预备题队列获取（瞬间可用）
                val nextQuestion = pollNextPreloadedOrBackgroundQuestion()
                
                if (nextQuestion != null) {
                    // 瞬间显示预备题，无等待时间
                    currentQuestion = nextQuestion
                    currentQuestionIndex++
                    displayQuestion(nextQuestion)
                    Log.d("AIQuestion", "✅ 使用预备题：${nextQuestion.question.take(30)}...")
                    
                    // 异步触发后台AI生成，保持队列充实
                    triggerBackgroundAIGeneration()
                    return@launch
                }
                
                // 🎯 策略2：预备题用完，显示加载并尝试实时AI生成
                showProgressDialog("🤖 AI正在生成新题目...")
                
                val aiQuestion = if (USE_AI_ONLY) {
                    generateAIUniqueQuestion()
                } else {
                    generateAIUniqueQuestion() ?: generatePersonalizedQuestion()
                }
                
                hideProgressDialog()
                
                if (aiQuestion != null) {
                    currentQuestion = aiQuestion
                    currentQuestionIndex++
                    displayQuestion(aiQuestion)
                    triggerBackgroundAIGeneration()
                } else {
                    // 🎯 策略3：最后兜底，生成基础题目
                    val fallbackQuestion = generateFallbackQuestion()
                    currentQuestion = fallbackQuestion
                    currentQuestionIndex++
                    displayQuestion(fallbackQuestion)
                }
                
            } catch (e: Exception) {
                hideProgressDialog()
                Log.e("AIQuestion", "生成题目失败: ${e.message}")
                showError("生成题目失败，请重试")
            }
        }
    }
    
    /**
     * 🤖 AI大模型生成唯一题目
     */
    private suspend fun generateAIUniqueQuestion(): AIQuestionGenerator.AIGeneratedQuestion? {
        return try {
                val user = getCurrentUser()
                val learningHistory = getLearningHistory()
            val answeredQuestions = getAnsweredQuestionsHistory()

            // 若AI后台队列已有题目，优先使用
            synchronized(aiBackgroundQueue) {
                if (aiBackgroundQueue.isNotEmpty()) {
                    val candidate = aiBackgroundQueue.removeFirst()
                    if (!isDuplicateQuestion(candidate)) {
                        recordQuestionFingerprint(candidate)
                        return candidate
                    }
                }
            }

            var attempt = 0
            var topic = selectTopicIntelligently()
            while (attempt < 5) {
                val uniquePrompt = buildUniqueQuestionPrompt(
                    subject = selectedSubject,
                    topic = topic,
                    difficulty = currentDifficulty,
                    userGrade = userGrade,
                    answeredQuestions = answeredQuestions,
                    learningHistory = learningHistory
                ) + "\n附加要求: 必须生成全新的情境、不同的词汇或语法结构，不能重复 I ____ to school every day 或类似例句。#attempt=${attempt + 1}"

                val aiResult = zhipuAIService.sendChatMessage(uniquePrompt, user)
                val parsed = aiResult.fold(
                    onSuccess = { aiResponse: String ->
                        Log.d("AIQuestion", "🤖 AI响应: ${aiResponse.take(200)}...")
                        parseAIQuestionResponse(aiResponse, selectedSubject, currentDifficulty)
                    },
                    onFailure = { error: Throwable ->
                        Log.e("AIQuestion", "🤖 AI出题失败: ${error.message}")
                        null
                    }
                )

                if (parsed != null && !isDuplicateQuestion(parsed)) {
                    recordQuestionFingerprint(parsed)
                    return parsed
                }

                // 若重复或解析失败，换一个主题稍作等待后再试
                attempt++
                topic = selectTopicIntelligently()
                delay(300)
            }
            null
        } catch (e: Exception) {
            Log.e("AIQuestion", "🤖 AI出题异常: ${e.message}")
            null
        }
    }
    
    /**
     * 📝 构建防重复的AI提示词
     */
    private fun buildUniqueQuestionPrompt(
        subject: String,
        topic: String,
        difficulty: String,
        userGrade: String,
        answeredQuestions: List<String>,
        learningHistory: List<LearningRecord>
    ): String {
        
        val gradeContext = when {
            userGrade.contains("小学") -> "小学生水平，语言简单易懂，计算简单"
            userGrade.contains("初中") -> "初中生水平，注重基础概念和应用"
            userGrade.contains("高中") -> "高中生水平，可以有一定的抽象思维"
            userGrade.contains("大学") -> "大学生水平，可以进行深入分析和推理"
            else -> "适合一般学习者的水平"
        }
        
        val difficultyGuide = when (difficulty) {
            "入门" -> "非常基础，重点在概念理解"
            "基础" -> "基本知识点，简单应用"
            "中级" -> "需要一定思考，综合运用知识"
            "高级" -> "有挑战性，需要深入理解"
            "挑战" -> "高难度，需要创新思维"
            else -> "适中难度"
        }
        
        // 分析用户学习特点
            val userPattern = if (learningHistory.isNotEmpty()) {
            val avgScore = learningHistory.takeLast(5).map { it.score }.average()
            val avgTime = learningHistory.takeLast(5).map { it.duration }.average()
            "用户最近表现：平均分${avgScore.toInt()}分，平均用时${avgTime.toInt()}分钟"
        } else "新用户，首次学习"
        
        // 若为英语科目，强制英语出题要求
        val subjectConstraint = if (subject.lowercase() == "英语" || subject.lowercase() == "english") {
            "所有题目与选项必须使用英文，题干可附中文提示，不允许数学或其他学科题。"
        } else ""

        return """
你是专业的${subject}教育专家，请为${userGrade}学生生成一道全新的、高质量的题目。

【学生情况】
年级水平：${gradeContext}
学习表现：${userPattern}
目标科目：${subject}
学习主题：${topic}
难度要求：${difficulty}（${difficultyGuide}）

【重要要求 - 避免重复】
${if (answeredQuestions.isNotEmpty()) {
    "用户已经做过以下类型的题目，请生成完全不同的新题目：\n" + 
    answeredQuestions.takeLast(10).joinToString("\n") { "- $it" }
                    } else {
    "这是用户的第一道题目，请生成一道经典的入门题目。"
}}

【出题标准】
1. 题目内容必须完全原创，与已做题目不重复
2. 符合${difficulty}难度要求
3. 适合${userGrade}学生的认知水平
4. 有明确的知识点考查目标
5. 4个选择项，只有1个正确答案
6. 干扰项设计合理，有教育意义
7. ${'$'}subjectConstraint

【输出格式】严格按照JSON格式：
```json
{
  "question": "题目内容（具体、清晰、有实际意义）",
  "options": ["A选项", "B选项", "C选项", "D选项"],
  "correct_answer": "正确答案内容",
  "explanation": "详细解析（包含解题思路、知识点讲解、易错点提醒）",
  "knowledge_points": ["知识点1", "知识点2"],
  "estimated_time": 120,
  "difficulty_level": "${difficulty}"
}
```

请确保题目：
✅ 内容准确无误，符合学科规范
✅ 完全原创，不与历史题目重复
✅ 难度适合，有教育价值
✅ 选项设计合理，干扰项有效
✅ 解析详细清晰，有学习指导意义

现在请生成一道符合要求的${subject}题目：
        """.trimIndent()
    }
    
    /**
     * 🔍 解析AI题目响应
     */
    private fun parseAIQuestionResponse(
        response: String, 
        subject: String, 
        difficulty: String
    ): AIQuestionGenerator.AIGeneratedQuestion? {
        return try {
            // 提取JSON部分
            val jsonStart = response.indexOf("{")
            val jsonEnd = response.lastIndexOf("}") + 1
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonString = response.substring(jsonStart, jsonEnd)
                
                // 解析JSON内容
                val questionText = extractJsonValue(jsonString, "question")
                val optionsText = extractJsonArray(jsonString, "options")
                val correctAnswer = extractJsonValue(jsonString, "correct_answer")
                val explanation = extractJsonValue(jsonString, "explanation")
                val knowledgePoints = extractJsonArray(jsonString, "knowledge_points")
                val estimatedTime = extractJsonValue(jsonString, "estimated_time")?.toIntOrNull() ?: 120
                
                if (questionText.isNotEmpty() && optionsText.size >= 4 && correctAnswer.isNotEmpty()) {
                    // 记录此题目，避免以后重复
                    recordAnsweredQuestion(questionText)
                    
                    return AIQuestionGenerator.AIGeneratedQuestion(
                        id = "ai_unique_${System.currentTimeMillis()}",
                        subject = subject,
                        topic = selectedTopics.randomOrNull() ?: "基础知识",
                        question = questionText,
                        options = optionsText.take(4),
                        correctAnswer = correctAnswer,
                        explanation = explanation.ifEmpty { "这是一道${subject}题目，考查相关知识点。" },
                        difficulty = difficulty,
                        questionType = "选择题",
                        knowledgePoints = knowledgePoints.ifEmpty { listOf(subject) },
                        estimatedTime = estimatedTime,
                        adaptiveReason = "AI大模型原创生成",
                        creativityLevel = "原创",
                        scenarioContext = "个性化智能出题"
                    )
                }
            }
            
            null
            } catch (e: Exception) {
            Log.e("AIQuestion", "解析AI响应失败: ${e.message}")
            null
        }
    }
    
    /**
     * 📚 获取已做题目历史（避免重复）
     */
    private fun getAnsweredQuestionsHistory(): List<String> {
        val prefs = getSharedPreferences("question_history", MODE_PRIVATE)
        val historySet = prefs.getStringSet("answered_questions", emptySet()) ?: emptySet()
        return historySet.toList()
    }
    
    /**
     * 📝 记录已做题目（避免重复）
     */
    private fun recordAnsweredQuestion(questionText: String) {
        synchronized(this) {
        val prefs = getSharedPreferences("question_history", MODE_PRIVATE)
        val historySet = prefs.getStringSet("answered_questions", emptySet())?.toMutableSet() ?: mutableSetOf()
        
        // 只保存题目的关键部分，避免存储过多
        val questionKey = questionText.take(50).replace(Regex("[^\\w\\s]"), "").trim()
        historySet.add(questionKey)
        
        // 限制历史记录数量，避免存储过多
        if (historySet.size > 100) {
            val sortedList = historySet.toList()
            historySet.clear()
            historySet.addAll(sortedList.takeLast(50))
        }
        
        prefs.edit().putStringSet("answered_questions", historySet).apply()
        }
    }

    // ========= 防重复：SimHash 实现 =========
    private fun isDuplicateQuestion(question: AIQuestionGenerator.AIGeneratedQuestion): Boolean {
        val text = buildString {
            append(question.subject).append("|")
            append(question.topic).append("|")
            append(question.question).append("|")
            append(question.options.joinToString("|"))
        }
        val simhash = computeSimHash(text)
        // 会话级检查
        sessionQuestionFingerprints.forEach { fp ->
            if (hammingDistance(fp, simhash) <= SIMHASH_THRESHOLD) return true
        }
        // 持久化历史检查
        val prefs = getSharedPreferences("question_fingerprints", MODE_PRIVATE)
        val stored = prefs.getStringSet("fp", emptySet()) ?: emptySet()
        stored.forEach { saved ->
            runCatching { saved.toLong(16) }.getOrNull()?.let { fp ->
                if (hammingDistance(fp, simhash) <= SIMHASH_THRESHOLD) return true
            }
        }
        return false
    }

    private fun recordQuestionFingerprint(question: AIQuestionGenerator.AIGeneratedQuestion) {
        val text = buildString {
            append(question.subject).append("|")
            append(question.topic).append("|")
            append(question.question).append("|")
            append(question.options.joinToString("|"))
        }
        val simhash = computeSimHash(text)
        sessionQuestionFingerprints.add(simhash)
        val prefs = getSharedPreferences("question_fingerprints", MODE_PRIVATE)
        val set = prefs.getStringSet("fp", emptySet())?.toMutableSet() ?: mutableSetOf()
        set.add(java.lang.Long.toHexString(simhash))
        // 限制规模
        if (set.size > 300) {
            val trimmed = set.toList().takeLast(150).toSet()
            prefs.edit().putStringSet("fp", trimmed).apply()
        } else {
            prefs.edit().putStringSet("fp", set).apply()
        }
    }

    private fun computeSimHash(text: String): Long {
        val tokens = tokenize(text)
        val vector = IntArray(64)
        for (token in tokens) {
            val h = hash64(token)
            for (i in 0 until 64) {
                val bit = (h shr i) and 1L
                vector[i] += if (bit == 1L) 1 else -1
            }
        }
        var simhash = 0L
        for (i in 0 until 64) {
            if (vector[i] > 0) simhash = simhash or (1L shl i)
        }
        return simhash
    }

    private fun tokenize(text: String): List<String> {
        val normalized = text.lowercase().replace(Regex("[\\p{Punct}\\s]+"), " ")
        val words = normalized.split(" ").filter { it.isNotBlank() }
        // 使用3-gram增强鲁棒性
        val grams = mutableListOf<String>()
        for (i in 0 until words.size) {
            grams.add(words[i])
            if (i + 1 < words.size) grams.add(words[i] + words[i + 1])
            if (i + 2 < words.size) grams.add(words[i] + words[i + 1] + words[i + 2])
        }
        return grams.take(200)
    }

    private fun hammingDistance(a: Long, b: Long): Int {
        return java.lang.Long.bitCount(a xor b)
    }

    private fun hash64(input: String): Long {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray())
        var h = 0L
        for (i in 0 until 8) {
            h = (h shl 8) or (bytes[i].toLong() and 0xFF)
        }
        return h
    }
    
    /**
     * 🔄 生成备选题目
     */
    private suspend fun generateFallbackQuestion(): AIQuestionGenerator.AIGeneratedQuestion {
        var attempts = 0
        while (attempts < 10) {
            val randomTopic = selectTopicIntelligently()
            val randomIndex = (System.nanoTime() and 0xFFFF).toInt()
            val question = createGeneralQuestion(selectedSubject, randomTopic, userGrade, randomIndex)
            if (!isDuplicateQuestion(question)) {
                recordQuestionFingerprint(question)
                recordAnsweredQuestion(question.question)
                return question
            }
            attempts++
            delay(100)
        }
        // 兜底：返回最后一个生成的题目（即使重复）
        val defaultQuestion = createGeneralQuestion(selectedSubject, selectTopicIntelligently(), userGrade, (System.currentTimeMillis() % 1000).toInt())
        recordQuestionFingerprint(defaultQuestion)
        recordAnsweredQuestion(defaultQuestion.question)
        return defaultQuestion
    }
    
    // 辅助JSON解析方法
    private fun extractJsonValue(json: String, key: String): String {
        val pattern = "\"$key\"\\s*:\\s*\"([^\"]*)\""
        val regex = Regex(pattern)
        return regex.find(json)?.groupValues?.get(1) ?: ""
    }
    
    private fun extractJsonArray(json: String, key: String): List<String> {
        val pattern = "\"$key\"\\s*:\\s*\\[([^\\]]*)]"
        val regex = Regex(pattern)
        val match = regex.find(json)?.groupValues?.get(1) ?: return emptyList()
        return match.split(",").map { it.trim().removeSurrounding("\"") }
    }
    
    /**
     * 显示AI生成的题目
     */
    private fun displayQuestion(question: AIQuestionGenerator.AIGeneratedQuestion) {
        currentQuestion = question
        questionStartTime = System.currentTimeMillis()
        
          // 更新UI
          tvQuestionTitle.text = "📝 ${question.questionType} (${question.difficulty}) - 第${currentQuestionIndex + 1}题"
          
          // 优化题目内容显示，确保诗歌、文章等长文本完整显示
          // 处理换行符，将\n转换为实际的换行
          val formattedQuestion = question.question
              .replace("\\n", "\n")           // 将字符串\n转换为实际换行符
              .replace("\\t", "\t")           // 处理制表符
              .replace("【必须包含完整的阅读材料】", "")  // 移除提示词中的标记
              .replace("【MUST include complete reading material】", "") // 移除英文标记
              .trim()                         // 去除首尾空白
          
          tvQuestionContent.text = formattedQuestion
          tvQuestionContent.maxLines = Int.MAX_VALUE  // 允许无限行
          tvQuestionContent.setSingleLine(false)      // 允许多行显示
          tvQuestionContent.ellipsize = null          // 不截断文本
          
          tvDifficulty.text = "难度：${question.difficulty} | 预计用时：${question.estimatedTime}秒 | 题库：${questionPool.size}题已准备"
        
        // 显示选项
        radioGroupOptions.removeAllViews()
        if (question.options.isNotEmpty()) {
            // 限制最多4个选项 (A, B, C, D)
            val limitedOptions = question.options.take(4)
            limitedOptions.forEachIndexed { index, option ->
                val radioButton = RadioButton(this)
                // 清理选项文本，移除已有的A.B.C.D.前缀
                val cleanOption = option.replace(Regex("^[A-J]\\s*[.．]\\s*"), "").trim()
                radioButton.text = "${('A' + index)}. $cleanOption"
                radioButton.id = index
                radioGroupOptions.addView(radioButton)
            }
        } else {
            // 非选择题的处理
            val radioButton = RadioButton(this)
            radioButton.text = "点击提交查看答案"
            radioButton.id = 0
            radioButton.isChecked = true
            radioGroupOptions.addView(radioButton)
        }
        
        // 显示AI生成原因
        Toast.makeText(this, "💡 AI选择此题的原因：${question.adaptiveReason}", Toast.LENGTH_LONG).show()
        
        // 题目显示完成，恢复情绪监测
        isGeneratingQuestion = false
        
        // 重置UI状态
        tvExplanation.visibility = View.GONE
        btnSubmitAnswer.visibility = View.VISIBLE
        btnNextQuestion.visibility = View.GONE
        
        // 记录行为事件
        recordBehaviorEvent("question_displayed", question.difficulty)
    }
    
    /**
     * 提交答案
     */
    private fun submitAnswer() {
        val question = currentQuestion ?: return
        val responseTime = System.currentTimeMillis() - questionStartTime
        
        val selectedId = radioGroupOptions.checkedRadioButtonId
        if (selectedId == -1 && question.options.isNotEmpty()) {
            Toast.makeText(this, "请选择一个答案", Toast.LENGTH_SHORT).show()
            return
        }
        
        val userAnswer = if (question.options.isNotEmpty()) {
            question.options.getOrNull(selectedId) ?: ""
        } else {
            question.correctAnswer
        }
        
        val isCorrect = userAnswer == question.correctAnswer
        answeredCount++
        if (isCorrect) correctCount++
        
        // 显示结果和解释
        displayAnswerResult(isCorrect, question, responseTime)
        
        // 记录行为数据
        recordBehaviorEvent("answer_submitted", isCorrect.toString(), if (isCorrect) 1.0f else 0.0f)
        recordBehaviorEvent("response_time", responseTime.toString())
        
        // 🎯 创新功能：AI难度自适应调整
        adjustDifficultyBasedOnPerformance(question, isCorrect, responseTime)
        
        // 🎯 新增：实时学习监控和预测性干预
        monitorLearningProgress(question, isCorrect, responseTime)
        
        // 更新进度
        updateProgress()
        
        // 🎯 AI智能难度检测和调整（每题都检测）
        performAIIntelligentDifficultyAdjustment(question, isCorrect, responseTime)
    }
    
    /**
     * 显示答题结果
     */
    private fun displayAnswerResult(isCorrect: Boolean, question: AIQuestionGenerator.AIGeneratedQuestion, responseTime: Long) {
        val resultIcon = if (isCorrect) "✅" else "❌"
        val resultText = if (isCorrect) "正确！" else "答案错误"
        
        tvExplanation.text = """
            $resultIcon $resultText
            
            📖 正确答案：${question.correctAnswer}
            
            💡 详细解释：
            ${question.explanation}
            
            ⏱️ 用时：${responseTime / 1000}秒 (预计${question.estimatedTime}秒)
            
            🎯 涉及知识点：${question.knowledgePoints.joinToString(", ")}
        """.trimIndent()
        
        tvExplanation.visibility = View.VISIBLE
        btnSubmitAnswer.visibility = View.GONE
        btnNextQuestion.visibility = View.VISIBLE
        
        // 根据表现给予鼓励
        val encouragement = when {
            isCorrect && responseTime < question.estimatedTime * 1000 -> "🎉 太棒了！你的解题速度很快！"
            isCorrect -> "👍 回答正确，继续保持！"
            responseTime > question.estimatedTime * 2000 -> "🤔 这道题确实有挑战性，不要气馁！"
            else -> "💪 没关系，从错误中学习更有价值！"
        }
        
        Toast.makeText(this, encouragement, Toast.LENGTH_LONG).show()
    }
    
    /**
     * 🎯 创新功能：基于表现的难度调整
     */
    private fun adjustDifficultyBasedOnPerformance(
        question: AIQuestionGenerator.AIGeneratedQuestion,
        isCorrect: Boolean,
        responseTime: Long
    ) {
        lifecycleScope.launch {
            try {
                val user = getCurrentUser()
                val confidenceLevel = estimateConfidenceLevel(isCorrect, responseTime, question.estimatedTime)
                
                val result = questionGenerator.adjustDifficultyBasedOnPerformance(
                    user = user,
                    lastQuestion = question,
                    answerCorrect = isCorrect,
                    responseTime = responseTime,
                    confidenceLevel = confidenceLevel
                )
                
                result.onSuccess { newDifficulty ->
                    val adjustmentMessage = when {
                        newDifficulty != question.difficulty -> "🎯 AI已调整下题难度：${question.difficulty} → $newDifficulty"
                        else -> "📊 AI认为当前难度适合你"
                    }
                    
                    Toast.makeText(this@AISmartQuestionActivity, adjustmentMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // 静默处理
            }
        }
    }
    
    /**
     * 更新情绪状态显示
     */
    private fun updateEmotionDisplay(emotionalState: AIEmotionRecognizer.EmotionalState) {
        runOnUiThread {
            val emotionIcon = when (emotionalState.emotionalState) {
                "积极" -> "😊"
                "专注" -> "🎯"
                "疲劳" -> "😴"
                "焦虑" -> "😰"
                "困惑" -> "🤔"
                else -> "😐"
            }
            
            tvEmotionalState.text = """
                $emotionIcon 学习状态：${emotionalState.emotionalState}
                🧠 专注度：${emotionalState.focusLevel}/10
                💪 自信度：${emotionalState.confidenceLevel}/10
                ${if (emotionalState.stressLevel > 6) "⚠️ 压力较大" else ""}
            """.trimIndent()
            
            // 如果需要干预
            if (emotionalState.interventionNeeded) {
                showEmotionIntervention(emotionalState)
            }
        }
    }
    
    /**
     * 显示情绪干预建议
     */
    private fun showEmotionIntervention(emotionalState: AIEmotionRecognizer.EmotionalState) {
        val intervention = when {
            emotionalState.stressLevel > 7 -> "检测到学习压力较大，建议深呼吸放松 😌"
            emotionalState.fatigueLevel > 7 -> "检测到学习疲劳，建议休息2分钟 ☕"
            emotionalState.focusLevel < 4 -> "注意力不够集中，试试换个环境？ 🌟"
            else -> emotionalState.suggestions.firstOrNull() ?: "继续保持当前状态"
        }
        
        android.app.AlertDialog.Builder(this)
            .setTitle("🤖 AI智能干预")
            .setMessage(intervention)
            .setPositiveButton("好的") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    
    // 辅助方法
    private fun getCurrentUser(): User {
        val savedUser = preferenceManager.getUser()
        return savedUser ?: User(
            id = preferenceManager.getUserId(),
            username = "student",
            email = "student@example.com",
            password = "",
            name = preferenceManager.getUserName(),
            userType = UserType.STUDENT,
            grade = "大学",
            learningStyle = "视觉型",
            interests = "数学,物理"
        )
    }
    
    private fun getLearningHistory(): List<LearningRecord> {
        // 简化实现，返回模拟数据
        return listOf(
            LearningRecord(
                id = 1,
                userId = preferenceManager.getUserId(),
                subject = "数学",
                topic = "函数",
                duration = 45,
                score = 85.0f,
                difficulty = "medium",
                learningStyle = "visual",
                timestamp = System.currentTimeMillis()
            )
        )
    }
    
    private fun determineDifficultyByEmotion(): String {
        return when {
            currentEmotionalState?.stressLevel ?: 0 > 7 -> "基础"
            currentEmotionalState?.fatigueLevel ?: 0 > 7 -> "入门"
            currentEmotionalState?.focusLevel ?: 5 >= 8 -> "高级"
            currentEmotionalState?.confidenceLevel ?: 5 >= 8 -> "中级"
            else -> "基础"
        }
    }
    
    private fun estimateConfidenceLevel(isCorrect: Boolean, responseTime: Long, expectedTime: Int): Int {
        return when {
            isCorrect && responseTime < expectedTime * 800 -> 9
            isCorrect && responseTime < expectedTime * 1200 -> 7
            isCorrect -> 6
            responseTime < expectedTime * 1000 -> 4
            else -> 3
        }
    }
    
    private fun recordBehaviorEvent(eventType: String, context: String, accuracy: Float? = null) {
        val event = AIEmotionRecognizer.BehaviorEvent(
            timestamp = System.currentTimeMillis(),
            eventType = eventType,
            duration = 0,
            accuracy = accuracy,
            hesitationTime = 0
        )
        
        behaviorEvents.add(event)
        if (behaviorEvents.size > 20) {
            behaviorEvents.removeAt(0)
        }
    }
    
    private fun updateProgress() {
        val progress = (answeredCount * 100) / 10 // 假设10题为一轮
        progressIndicator.setProgress(progress, true)
        
        supportActionBar?.title = "AI智能出题 ($answeredCount/10) - 正确率：${(correctCount * 100 / answeredCount.coerceAtLeast(1))}%"
    }
    
    private fun showProgressDialog(message: String) {
        // 简化实现，使用Toast
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun hideProgressDialog() {
        // 简化实现
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * 🎯 根据用户年级智能确定科目和主题
     */
    private fun determineSubjectAndTopic(user: User): Pair<String, String> {
        return when (user.grade) {
            "高中", "高一", "高二", "高三" -> {
                // 高中阶段科目
                val subjects = listOf(
                    "数学" to listOf("函数与导数", "三角函数", "立体几何", "概率统计"),
                    "物理" to listOf("力学", "电磁学", "光学", "热学"),
                    "化学" to listOf("有机化学", "无机化学", "化学反应", "化学计算"),
                    "语文" to listOf("古诗词鉴赏", "现代文阅读", "文言文", "作文写作")
                )
                getRandomSubjectTopic(subjects)
            }
            "初中", "初一", "初二", "初三" -> {
                // 初中阶段科目
                val subjects = listOf(
                    "数学" to listOf("代数", "几何", "方程", "函数基础"),
                    "物理" to listOf("机械运动", "力", "声光现象", "电路基础"),
                    "化学" to listOf("基础化学", "化学反应", "酸碱盐", "气体性质"),
                    "语文" to listOf("现代文阅读", "古诗文", "语法", "写作基础")
                )
                getRandomSubjectTopic(subjects)
            }
            // 移除大学阶段，因为李老师只教中学生
            else -> {
                // 默认基础科目
                "数学" to "基础数学"
            }
        }
    }
    
    /**
     * 从科目列表中随机选择一个科目和主题
     */
    private fun getRandomSubjectTopic(subjects: List<Pair<String, List<String>>>): Pair<String, String> {
        val randomSubject = subjects.random()
        val randomTopic = randomSubject.second.random()
        return randomSubject.first to randomTopic
    }
    
    /**
     * 🎯 智能选择主题
     */
    private fun selectTopicIntelligently(): String {
        return if (selectedTopics.isNotEmpty()) {
            selectedTopics.random()
        } else {
            "基础知识"
        }
    }
    
    /**
     * 🎯 AI智能难度检测和调整
     */
    private fun performAIIntelligentDifficultyAdjustment(
        question: AIQuestionGenerator.AIGeneratedQuestion, 
        isCorrect: Boolean, 
        responseTime: Long
    ) {
        lifecycleScope.launch {
            try {
                val user = getCurrentUser()
                val recentHistory = getLearningHistory().takeLast(5)
                
                // AI智能分析当前学习状态
                val difficultyAnalysis = analyzeCurrentDifficultyNeed(
                    question, isCorrect, responseTime, recentHistory, user
                )
                
                // 根据AI分析结果调整难度
                if (difficultyAnalysis.shouldAdjust) {
                    val oldDifficulty = currentDifficulty
                    currentDifficulty = difficultyAnalysis.recommendedDifficulty
                    
                    // 显示AI调整提示
                    val adjustmentMessage = when {
                        difficultyAnalysis.adjustmentType == "increase" -> 
                            "🤖 AI检测到你表现优秀，已提高题目难度至「$currentDifficulty」"
                        difficultyAnalysis.adjustmentType == "decrease" -> 
                            "🤖 AI检测到需要巩固基础，已调整题目难度至「$currentDifficulty」"
                        else -> 
                            "🤖 AI认为当前难度「$currentDifficulty」很适合你"
                    }
                    
                    if (oldDifficulty != currentDifficulty) {
                        Toast.makeText(this@AISmartQuestionActivity, adjustmentMessage, Toast.LENGTH_LONG).show()
                        Log.d("AIQuestion", "🤖 AI难度调整: $oldDifficulty → $currentDifficulty")
                    }
                }
                
            } catch (e: Exception) {
                Log.e("AIQuestion", "AI难度检测失败: ${e.message}")
            }
        }
    }
    
    /**
     * 🧠 AI分析当前难度需求
     */
    private suspend fun analyzeCurrentDifficultyNeed(
        currentQuestion: AIQuestionGenerator.AIGeneratedQuestion,
        isCorrect: Boolean,
        responseTime: Long,
        recentHistory: List<LearningRecord>,
        user: User
    ): DifficultyAnalysis {
        
        // 计算多维度指标
        val accuracyScore = if (recentHistory.isNotEmpty()) {
            recentHistory.takeLast(5).map { if (it.score >= 70) 1.0 else 0.0 }.average()
        } else if (isCorrect) 1.0 else 0.0
        
        val speedScore = calculateSpeedScore(responseTime, currentQuestion.estimatedTime)
        val consistencyScore = calculateConsistencyScore(recentHistory)
        val confidenceScore = calculateConfidenceScore(isCorrect, responseTime, currentQuestion.estimatedTime)
        
        // AI综合评估
        val overallPerformance = (accuracyScore * 0.4 + speedScore * 0.3 + 
                                 consistencyScore * 0.2 + confidenceScore * 0.1)
        
        return when {
            overallPerformance >= 0.85 && accuracyScore >= 0.8 -> {
                DifficultyAnalysis(
                    shouldAdjust = true,
                    recommendedDifficulty = getNextDifficultyLevel(currentDifficulty, 1),
                    adjustmentType = "increase",
                    confidence = overallPerformance,
                    reason = "表现优秀，正确率${(accuracyScore*100).toInt()}%，速度适中，建议提高挑战"
                )
            }
            overallPerformance <= 0.4 || accuracyScore <= 0.4 -> {
                DifficultyAnalysis(
                    shouldAdjust = true,
                    recommendedDifficulty = getNextDifficultyLevel(currentDifficulty, -1),
                    adjustmentType = "decrease", 
                    confidence = 1.0 - overallPerformance,
                    reason = "需要巩固基础，正确率${(accuracyScore*100).toInt()}%，建议降低难度"
                )
            }
            else -> {
                DifficultyAnalysis(
                    shouldAdjust = false,
                    recommendedDifficulty = currentDifficulty,
                    adjustmentType = "maintain",
                    confidence = overallPerformance,
                    reason = "当前难度合适，表现稳定"
                )
            }
        }
    }
    
    /**
     * 计算速度得分
     */
    private fun calculateSpeedScore(actualTime: Long, expectedTime: Int): Double {
        val ratio = actualTime.toDouble() / (expectedTime * 1000)
        return when {
            ratio <= 0.7 -> 1.0  // 很快
            ratio <= 1.0 -> 0.8  // 正常
            ratio <= 1.5 -> 0.6  // 较慢
            ratio <= 2.0 -> 0.4  // 慢
            else -> 0.2           // 很慢
        }
    }
    
    /**
     * 计算一致性得分
     */
    private fun calculateConsistencyScore(recentHistory: List<LearningRecord>): Double {
        if (recentHistory.size < 3) return 0.7
        
        val scores = recentHistory.takeLast(5).map { it.score.toDouble() }
        val average = scores.average()
        val variance = scores.map { (it - average) * (it - average) }.average()
        val stdDev = kotlin.math.sqrt(variance)
        
        return maxOf(0.0, 1.0 - (stdDev / 100.0))
    }
    
    /**
     * 计算信心得分
     */
    private fun calculateConfidenceScore(isCorrect: Boolean, responseTime: Long, expectedTime: Int): Double {
        return when {
            isCorrect && responseTime < expectedTime * 800 -> 1.0   // 快速正确
            isCorrect && responseTime < expectedTime * 1200 -> 0.8  // 正常正确
            isCorrect -> 0.6                                        // 慢但正确
            responseTime < expectedTime * 1000 -> 0.3               // 快但错误
            else -> 0.1                                             // 慢且错误
        }
    }
    
    /**
     * 获取下一个难度等级
     */
    private fun getNextDifficultyLevel(currentDifficulty: String, adjustment: Int): String {
        val difficulties = listOf("入门", "基础", "中级", "高级", "挑战")
        val currentIndex = difficulties.indexOf(currentDifficulty).takeIf { it >= 0 } ?: 1
        val newIndex = (currentIndex + adjustment).coerceIn(0, difficulties.size - 1)
        return difficulties[newIndex]
    }
    
    /**
     * 难度分析结果数据类
     */
    data class DifficultyAnalysis(
        val shouldAdjust: Boolean,
        val recommendedDifficulty: String,
        val adjustmentType: String, // "increase", "decrease", "maintain"
        val confidence: Double,
        val reason: String
    )

    
    
    /**
     * 🎯 初始化深度个性化分析
     */
    private fun initializeDeepPersonalization() {
        lifecycleScope.launch {
            try {
                val user = getCurrentUser()
                val learningHistory = getLearningHistory()
                
                // 生成学习者画像
                val profileResult = personalizationEngine.generateLearnerProfile(user, learningHistory)
                profileResult.onSuccess { profile ->
                    learnerProfile = profile
                    Log.d("AIQuestion", "✅ 学习者画像生成成功: ${profile.learningStyle.primaryStyle}")
                    
                    // 生成学习预测
                    generateLearningPrediction(profile)
                    
                    // 更新UI显示个性化信息
                    updatePersonalizationDisplay(profile)
                }
                
            } catch (e: Exception) {
                Log.e("AIQuestion", "深度个性化初始化失败: ${e.message}")
            }
        }
    }
    
    /**
     * 🔮 生成学习预测
     */
    private suspend fun generateLearningPrediction(profile: DeepPersonalizationEngine.LearnerProfile) {
        try {
            val user = getCurrentUser()
            val learningHistory = getLearningHistory()
            
            val predictionResult = predictiveEngine.generateLearningPrediction(user, learningHistory, profile)
            predictionResult.onSuccess { prediction ->
                currentPrediction = prediction
                Log.d("AIQuestion", "✅ 学习预测生成成功: 预期成绩${prediction.performancePrediction.expectedScore}")
                
                // 检查是否需要即时干预
                checkForImmediateIntervention(prediction)
            }
            
        } catch (e: Exception) {
            Log.e("AIQuestion", "学习预测生成失败: ${e.message}")
        }
    }
    
    /**
     * 🚨 检查即时干预需求
     */
    private fun checkForImmediateIntervention(prediction: PredictiveInterventionEngine.LearningPrediction) {
        val highPriorityInterventions = prediction.interventionRecommendations.filter { it.priority == "高" }
        
        if (highPriorityInterventions.isNotEmpty()) {
            val intervention = highPriorityInterventions.first()
            showInterventionDialog(intervention)
        }
    }
    
    /**
     * 💡 显示干预建议对话框
     */
    private fun showInterventionDialog(intervention: PredictiveInterventionEngine.InterventionRecommendation) {
        android.app.AlertDialog.Builder(this)
            .setTitle("🤖 AI学习建议")
            .setMessage("""
                检测到需要关注的学习状况：
                
                目标区域：${intervention.targetArea}
                
                建议行动：
                ${intervention.specificActions.joinToString("\n• ", "• ")}
                
                预期效果：${intervention.expectedOutcome}
            """.trimIndent())
            .setPositiveButton("采纳建议") { _, _ ->
                // 应用干预建议
                applyInterventionRecommendation(intervention)
            }
            .setNegativeButton("稍后考虑") { dialog, _ ->
                    dialog.dismiss()
            }
            .show()
    }
    
    /**
     * 🎯 应用干预建议
     */
    private fun applyInterventionRecommendation(intervention: PredictiveInterventionEngine.InterventionRecommendation) {
        when (intervention.targetArea) {
            "学习困难" -> {
                // 降低题目难度
                currentDifficulty = "基础"
                Toast.makeText(this, "已降低题目难度，帮助您重建信心", Toast.LENGTH_LONG).show()
            }
            "疲劳管理" -> {
                // 显示休息建议
                showRestRecommendation()
            }
            "心理健康" -> {
                // 减少学习强度
                Toast.makeText(this, "建议适当休息，保持学习的可持续性", Toast.LENGTH_LONG).show()
            }
        }
        
        Log.d("AIQuestion", "✅ 已应用干预建议: ${intervention.targetArea}")
    }
    
    /**
     * 😴 显示休息建议
     */
    private fun showRestRecommendation() {
        android.app.AlertDialog.Builder(this)
            .setTitle("💤 休息建议")
            .setMessage("""
                检测到您已经学习较长时间，建议：
                
                • 休息15-20分钟
                • 进行眼部放松运动
                • 适当活动身体
                • 喝水补充水分
                
                良好的休息有助于提高后续学习效率！
            """.trimIndent())
            .setPositiveButton("开始休息") { _, _ ->
                // 可以添加休息计时器
                Toast.makeText(this, "请享受您的休息时间！", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("继续学习") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
    /**
     * 📊 更新个性化显示
     */
    private fun updatePersonalizationDisplay(profile: DeepPersonalizationEngine.LearnerProfile) {
        runOnUiThread {
            // 更新学习风格显示
            val styleInfo = "🎯 学习风格: ${profile.learningStyle.primaryStyle}"
            
            // 更新认知状态显示
            val cognitiveInfo = when {
                profile.cognitiveProfile.cognitiveLoad > 0.7f -> "🧠 认知负荷: 较高"
                profile.cognitiveProfile.cognitiveLoad > 0.4f -> "🧠 认知负荷: 适中"
                else -> "🧠 认知负荷: 较低"
            }
            
            // 更新动机状态显示
            val motivationInfo = when {
                profile.motivationProfile.intrinsicMotivation > 0.7f -> "💪 学习动机: 强"
                profile.motivationProfile.intrinsicMotivation > 0.4f -> "💪 学习动机: 中"
                else -> "💪 学习动机: 需激发"
            }
            
            // 在题目标题中显示个性化信息
            val personalizedTitle = "$styleInfo | $cognitiveInfo"
            tvDifficulty.text = personalizedTitle
        }
    }
    
    /**
     * 🎯 智能题目生成（集成深度个性化）
     */
    private suspend fun generatePersonalizedQuestion(): AIQuestionGenerator.AIGeneratedQuestion? {
        return try {
            val user = getCurrentUser()
            val learningHistory = getLearningHistory()
            
            // 使用学习者画像优化出题参数
            val config = AIQuestionGenerator.QuestionGenerationConfig(
                targetDifficulty = learnerProfile?.personalizedStrategy?.recommendedDifficulty ?: currentDifficulty,
                questionCount = 1,
                focusWeakPoints = true,
                includeCreativeQuestions = learnerProfile?.cognitiveProfile?.optimalChallengeLevel ?: 0.5f > 0.7f,
                preferredQuestionTypes = learnerProfile?.personalizedStrategy?.optimalQuestionTypes ?: emptyList(),
                learningObjective = "基于个性化分析的智能出题"
            )
            
            val result = questionGenerator.generateAdaptiveQuestions(
                user = user,
                subject = selectedSubject,
                topic = selectedTopics.randomOrNull() ?: "基础知识",
                learningHistory = learningHistory,
                currentEmotionalState = currentEmotionalState,
                config = config
            )
            
            result.getOrNull()?.firstOrNull()
            
        } catch (e: Exception) {
            Log.e("AIQuestion", "个性化题目生成失败: ${e.message}")
            null
        }
    }
    
    /**
     * 🔍 实时学习监控
     */
    private fun monitorLearningProgress(currentQuestion: AIQuestionGenerator.AIGeneratedQuestion, isCorrect: Boolean, responseTime: Long) {
        lifecycleScope.launch {
            try {
                // 创建当前学习记录
                val currentSession = LearningRecord(
                    id = System.currentTimeMillis(),
                    userId = preferenceManager.getUserId(),
                    subject = selectedSubject,
                    topic = currentQuestion.topic,
                    duration = (responseTime / 1000).toLong(),
                    score = if (isCorrect) 100f else 0f,
                    difficulty = currentQuestion.difficulty,
                    learningStyle = "visual",
                    timestamp = System.currentTimeMillis()
                )
                
                // 检查实时干预需求
                val recentHistory = getLearningHistory().takeLast(5)
                val interventionResult = predictiveEngine.checkForRealTimeIntervention(
                    getCurrentUser(), currentSession, recentHistory
                )
                
                interventionResult.onSuccess { intervention ->
                    intervention?.let {
                        showInterventionDialog(it)
                    }
                }
                
            } catch (e: Exception) {
                Log.e("AIQuestion", "学习监控失败: ${e.message}")
            }
        }
    }
    
    /**
     * 🚀 预先加载静态题库 - 每个科目50道题，瞬间可用
     */
    private fun preloadStaticQuestions() {
        try {
            Log.d("AIQuestion", "🚀 开始预加载 $selectedSubject 题库...")
            val preloaded = PreloadedQuestionBank.getQuestions(selectedSubject, selectedTopics, userGrade)
            synchronized(preloadedQuestionQueue) {
                preloadedQuestionQueue.clear()
                var addedCount = 0
                preloaded.shuffled().forEach { question ->
                    if (!isDuplicateQuestion(question)) {
                        preloadedQuestionQueue.addLast(question)
                        recordQuestionFingerprint(question)
                        addedCount++
                    }
                }
                Log.d("AIQuestion", "✅ 预加载完成：$addedCount 道 $selectedSubject 题目已准备")
            }
        } catch (e: Exception) {
            Log.e("AIQuestion", "预加载题库失败: ${e.message}", e)
        }
    }

    /**
     * 🚀 优化版：优先预备题，然后后台AI题
     */
    private fun pollNextPreloadedOrBackgroundQuestion(): AIQuestionGenerator.AIGeneratedQuestion? {
        // 🎯 优先级1：预备题库（瞬间可用，50道题）
        synchronized(preloadedQuestionQueue) {
            while (preloadedQuestionQueue.isNotEmpty()) {
                val candidate = preloadedQuestionQueue.removeFirst()
                if (!isDuplicateQuestion(candidate)) {
                    recordQuestionFingerprint(candidate)
                    recordAnsweredQuestion(candidate.question)
                    Log.d("AIQuestion", "📦 使用预备题: ${candidate.question.take(20)}...")
                    return candidate
                }
            }
        }
        
        // 🎯 优先级2：后台AI生成的题（质量更高）
        synchronized(aiBackgroundQueue) {
            while (aiBackgroundQueue.isNotEmpty()) {
                val candidate = aiBackgroundQueue.removeFirst()
                if (!isDuplicateQuestion(candidate)) {
                    recordQuestionFingerprint(candidate)
                    recordAnsweredQuestion(candidate.question)
                    Log.d("AIQuestion", "🤖 使用后台AI题: ${candidate.question.take(20)}...")
                    return candidate
                }
            }
        }
        
        return null
    }

    /**
     * 🤖 后台AI生成 - 保持5道AI题的缓存
     */
    private fun triggerBackgroundAIGeneration() {
        lifecycleScope.launch {
            try {
                // 检查是否需要补充AI题目缓存
                val currentAIQueueSize = synchronized(aiBackgroundQueue) { aiBackgroundQueue.size }
                val currentPreloadedSize = synchronized(preloadedQuestionQueue) { preloadedQuestionQueue.size }
                
                if (currentAIQueueSize < 5 && currentPreloadedSize < 10) {
                    Log.d("AIQuestion", "🤖 触发后台AI生成 (AI队列: $currentAIQueueSize, 预备队列: $currentPreloadedSize)")
                    
                    val user = getCurrentUser()
                    val learningHistory = getLearningHistory()
                    val topic = selectTopicIntelligently()
                    val config = AIQuestionGenerator.QuestionGenerationConfig(
                        targetDifficulty = currentDifficulty,
                        questionCount = 1,
                        focusWeakPoints = true,
                        includeCreativeQuestions = true
                    )
                    
                    val result = questionGenerator.generateAdaptiveQuestions(
                        user = user,
                        subject = selectedSubject,
                        topic = topic,
                        learningHistory = learningHistory,
                        currentEmotionalState = currentEmotionalState,
                        config = config
                    )
                    
                    result.onSuccess { questions ->
                        if (questions.isNotEmpty()) {
                            val candidate = questions.first()
                            if (!isDuplicateQuestion(candidate)) {
                                synchronized(aiBackgroundQueue) {
                                    aiBackgroundQueue.addLast(candidate)
                                    recordQuestionFingerprint(candidate)
                                    Log.d("AIQuestion", "✅ 后台AI生成成功: ${candidate.question.take(30)}...")
                                }
                            }
                        }
                    }.onFailure { error ->
                        Log.w("AIQuestion", "后台AI生成失败: ${error.message}")
                    }
                }
            } catch (e: Exception) {
                Log.w("AIQuestion", "后台AI生成异常: ${e.message}")
            }
        }
    }
}

/**
 * 🎯 难度反馈数据类
 */
data class DifficultyFeedback(
    val timestamp: Long,
    val previousDifficulty: String,
    val adjustmentValue: Int, // -2到2的调整值
    val feedbackText: String,
    val questionCount: Int,
    val correctCount: Int,
    val averageResponseTime: Long
)
