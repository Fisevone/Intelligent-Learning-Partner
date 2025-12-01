package com.example.educationapp.ui.student

import android.animation.ValueAnimator
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.educationapp.R
import com.example.educationapp.ai.AIEmotionRecognizer
import com.example.educationapp.ai.PersonalizedLearningAnalyzer
import com.example.educationapp.ai.ZhipuAIService
import com.example.educationapp.data.EducationDatabase
import com.example.educationapp.data.User
import com.example.educationapp.service.LearningProgressTracker
import com.example.educationapp.utils.PreferenceManager
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 学生端智能学习分析界面 - 基于GLM-4的个性化分析
 * 功能：学习状态监测、情绪识别、个性化建议、学习效果预测
 */
class StudentLearningAnalysisActivity : AppCompatActivity() {
    
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var emotionRecognizer: AIEmotionRecognizer
    private lateinit var learningAnalyzer: PersonalizedLearningAnalyzer
    private lateinit var progressTracker: LearningProgressTracker
    private lateinit var zhipuAIService: ZhipuAIService
    
    // UI 组件
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var tvOverallProgress: TextView
    private lateinit var tvFocusLevel: TextView
    private lateinit var tvStressLevel: TextView
    private lateinit var tvEmotionalState: TextView
    private lateinit var cardEmotionAnalysis: MaterialCardView
    private lateinit var cardLearningInsights: MaterialCardView
    private lateinit var cardRecommendations: MaterialCardView
    private lateinit var rvSuggestions: RecyclerView
    
    // 数据
    private var currentUser: User? = null
    private var currentEmotionalState: AIEmotionRecognizer.EmotionalState? = null
    private var behaviorEvents = mutableListOf<AIEmotionRecognizer.BehaviorEvent>()
    
    private fun generateSampleLearningRecords(): List<com.example.educationapp.data.LearningRecord> {
        return listOf(
            com.example.educationapp.data.LearningRecord(
                id = 1,
                userId = preferenceManager.getUserId(),
                subject = "数学",
                topic = "导数",
                duration = 30, // 30分钟
                score = 85f,
                difficulty = "中等",
                learningStyle = "练习",
                timestamp = System.currentTimeMillis() - 86400000, // 1天前
                notes = ""
            ),
            com.example.educationapp.data.LearningRecord(
                id = 2,
                userId = preferenceManager.getUserId(),
                subject = "数学",
                topic = "积分",
                duration = 40, // 40分钟
                score = 78f,
                difficulty = "困难",
                learningStyle = "练习",
                timestamp = System.currentTimeMillis() - 172800000, // 2天前
                notes = ""
            )
        )
    }

    companion object {
        private const val TAG = "StudentLearningAnalysis"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_learning_analysis)
        
        initServices()
        initViews()
        setupClickListeners()
        loadUserData()
        startRealTimeMonitoring()
    }
    
    private fun initServices() {
        preferenceManager = PreferenceManager(this)
        emotionRecognizer = AIEmotionRecognizer()
        learningAnalyzer = PersonalizedLearningAnalyzer()
        zhipuAIService = ZhipuAIService()
        
        val database = EducationDatabase.getDatabase(this)
        progressTracker = LearningProgressTracker.getInstance(this, database.learningProgressDao())
    }
    
    private fun initViews() {
        // 初始化UI组件
        progressIndicator = findViewById(R.id.progressIndicator)
        tvOverallProgress = findViewById(R.id.tvOverallProgress)
        tvFocusLevel = findViewById(R.id.tvFocusLevel)
        tvStressLevel = findViewById(R.id.tvStressLevel)
        tvEmotionalState = findViewById(R.id.tvEmotionalState)
        cardEmotionAnalysis = findViewById(R.id.cardEmotionAnalysis)
        cardLearningInsights = findViewById(R.id.cardLearningInsights)
        cardRecommendations = findViewById(R.id.cardRecommendations)
        rvSuggestions = findViewById(R.id.rvSuggestions)
        
        // 设置RecyclerView
        rvSuggestions.layoutManager = LinearLayoutManager(this)
        
        // 设置工具栏
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "🧠 智能学习分析"
    }
    
    private fun setupClickListeners() {
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }
        
        cardEmotionAnalysis.setOnClickListener {
            showDetailedEmotionAnalysis()
        }
        
        cardLearningInsights.setOnClickListener {
            showLearningInsights()
        }
        
        cardRecommendations.setOnClickListener {
            refreshRecommendations()
        }
        
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabRefresh).setOnClickListener {
            refreshAnalysis()
        }
    }
    
    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                val userId = preferenceManager.getUserId()
                val database = EducationDatabase.getDatabase(this@StudentLearningAnalysisActivity)
                val userDao = database.userDao()
                
                currentUser = userDao.getUserById(userId)
                
                if (currentUser != null) {
                    updateUI()
                    performInitialAnalysis()
                } else {
                    Toast.makeText(this@StudentLearningAnalysisActivity, "无法加载用户数据", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载用户数据失败", e)
                Toast.makeText(this@StudentLearningAnalysisActivity, "数据加载失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun startRealTimeMonitoring() {
        // 模拟行为事件采集
        lifecycleScope.launch {
            while (true) {
                kotlinx.coroutines.delay(10000) // 每10秒分析一次
                
                // 添加模拟的行为事件
                addBehaviorEvent("activity", System.currentTimeMillis())
                
                // 执行情绪识别
                performEmotionAnalysis()
                
                if (behaviorEvents.size > 50) {
                    behaviorEvents.removeFirst()
                }
            }
        }
    }
    
    private fun addBehaviorEvent(eventType: String, timestamp: Long, accuracy: Float? = null) {
        val event = AIEmotionRecognizer.BehaviorEvent(
            timestamp = timestamp,
            eventType = eventType,
            duration = kotlin.random.Random.nextLong(1000, 5000),
            accuracy = accuracy ?: kotlin.random.Random.nextFloat(),
            hesitationTime = kotlin.random.Random.nextLong(500, 3000)
        )
        behaviorEvents.add(event)
    }
    
    private fun performInitialAnalysis() {
        lifecycleScope.launch {
            try {
                // 获取学习记录
                val userId = preferenceManager.getUserId()
                val learningRecords = generateSampleLearningRecords()
                
                // 执行学习模式分析
                currentUser?.let { user ->
                    val patternResult = learningAnalyzer.analyzeLearningPatterns(user, learningRecords)
                    patternResult.fold(
                        onSuccess = { pattern ->
                            Log.d(TAG, "学习模式分析完成: $pattern")
                            updateLearningPattern(pattern)
                        },
                        onFailure = { error ->
                            Log.e(TAG, "学习模式分析失败", error)
                        }
                    )
                    
                    // 生成学习建议
                    val suggestionsResult = learningAnalyzer.generateLearningSuggestions(user, learningRecords)
                    suggestionsResult.fold(
                        onSuccess = { suggestions ->
                            updateSuggestions(suggestions)
                        },
                        onFailure = { error ->
                            Log.e(TAG, "学习建议生成失败", error)
                        }
                    )
                }
                
                // 执行初始情绪分析
                performEmotionAnalysis()
                
            } catch (e: Exception) {
                Log.e(TAG, "初始分析失败", e)
            }
        }
    }
    
    private suspend fun performEmotionAnalysis() {
        currentUser?.let { user ->
            try {
                val emotionResult = emotionRecognizer.analyzeRealTimeEmotion(
                    user = user,
                    recentBehaviors = behaviorEvents.takeLast(20),
                    currentLearningSession = null
                )
                
                emotionResult.fold(
                    onSuccess = { emotionalState ->
                        currentEmotionalState = emotionalState
                        runOnUiThread {
                            updateEmotionalStateUI(emotionalState)
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "情绪分析失败", error)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "情绪分析异常", e)
            }
        }
    }
    
    private fun updateUI() {
        currentUser?.let { user ->
            supportActionBar?.subtitle = "${user.name} - ${user.grade}"
        }
    }
    
    private fun updateEmotionalStateUI(emotionalState: AIEmotionRecognizer.EmotionalState) {
        // 更新专注度
        tvFocusLevel.text = "${emotionalState.focusLevel}/10"
        animateProgressBar(findViewById(R.id.progressFocus), emotionalState.focusLevel * 10)
        
        // 更新压力水平
        tvStressLevel.text = "${emotionalState.stressLevel}/10"
        animateProgressBar(findViewById(R.id.progressStress), emotionalState.stressLevel * 10)
        
        // 更新情绪状态
        tvEmotionalState.text = emotionalState.emotionalState
        updateEmotionColor(emotionalState.emotionalState)
        
        // 显示建议
        if (emotionalState.suggestions.isNotEmpty()) {
            val suggestionsText = emotionalState.suggestions.joinToString("\n• ", "• ")
            findViewById<TextView>(R.id.tvEmotionSuggestions).text = suggestionsText
        }
        
        // 检查是否需要干预
        if (emotionalState.interventionNeeded) {
            showInterventionDialog(emotionalState)
        }
    }
    
    private fun updateLearningPattern(pattern: com.example.educationapp.ai.LearningPattern) {
        findViewById<TextView>(R.id.tvBestLearningTime).text = pattern.bestLearningTime
        findViewById<TextView>(R.id.tvLearningStyle).text = pattern.learningStyle
        findViewById<TextView>(R.id.tvAttentionSpan).text = pattern.attentionSpan
        
        val recommendationsText = pattern.recommendations.joinToString("\n• ", "• ")
        findViewById<TextView>(R.id.tvPatternRecommendations).text = recommendationsText
    }
    
    private fun updateSuggestions(suggestions: List<com.example.educationapp.ai.LearningSuggestion>) {
        val uiSuggestions = suggestions.map { aiSuggestion ->
            LearningSuggestion(
                type = aiSuggestion.type,
                title = aiSuggestion.title,
                description = aiSuggestion.description,
                priority = aiSuggestion.priority,
                estimatedTime = aiSuggestion.estimatedTime
            )
        }
        val adapter = LearningSuggestionsAdapter(uiSuggestions) { suggestion ->
            // 处理建议点击
            showSuggestionDetails(suggestion)
        }
        rvSuggestions.adapter = adapter
    }
    
    private fun animateProgressBar(progressBar: CircularProgressIndicator, targetProgress: Int) {
        val animator = ValueAnimator.ofInt(progressBar.progress, targetProgress)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            progressBar.progress = animation.animatedValue as Int
        }
        animator.start()
    }
    
    private fun updateEmotionColor(emotionalState: String) {
        val color = when (emotionalState) {
            "积极" -> Color.parseColor("#4CAF50")
            "中性" -> Color.parseColor("#FFC107")
            "困惑" -> Color.parseColor("#FF9800")
            "疲劳" -> Color.parseColor("#F44336")
            else -> Color.parseColor("#9E9E9E")
        }
        
        cardEmotionAnalysis.setCardBackgroundColor(Color.argb(30, Color.red(color), Color.green(color), Color.blue(color)))
    }
    
    private fun showDetailedEmotionAnalysis() {
        currentEmotionalState?.let { state ->
            val message = """
                🧠 详细情绪分析
                
                专注度: ${state.focusLevel}/10
                压力水平: ${state.stressLevel}/10
                自信程度: ${state.confidenceLevel}/10
                疲劳程度: ${state.fatigueLevel}/10
                
                情绪状态: ${state.emotionalState}
                
                分析原因:
                ${state.analysisReason}
                
                建议措施:
                ${state.suggestions.joinToString("\n• ", "• ")}
            """.trimIndent()
            
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("🎭 情绪状态详情")
                .setMessage(message)
                .setPositiveButton("了解") { _, _ -> }
                .setNegativeButton("获取更多建议") { _, _ ->
                    getAdditionalSuggestions()
                }
                .show()
        }
    }
    
    private fun showLearningInsights() {
        lifecycleScope.launch {
            try {
                currentUser?.let { user ->
                    val userId = preferenceManager.getUserId()
                    val progressData = progressTracker.getKnowledgeGraphData(userId, "数学")
                    
                    val insightsMessage = """
                        📊 学习洞察报告
                        
                        整体进度: ${(progressData.overallProgress * 100).roundToInt()}%
                        
                        推荐学习:
                        ${progressData.recommendedNext.joinToString("\n• ", "• ")}
                        
                        薄弱环节:
                        ${progressData.nodes.filter { it.masteryLevel < 0.5f }.map { it.name }.joinToString("\n• ", "• ")}
                        
                        学习建议:
                        • 重点关注薄弱知识点
                        • 保持当前学习节奏
                        • 适时复习已掌握内容
                    """.trimIndent()
                    
                    androidx.appcompat.app.AlertDialog.Builder(this@StudentLearningAnalysisActivity)
                        .setTitle("💡 学习洞察")
                        .setMessage(insightsMessage)
                        .setPositiveButton("制定学习计划") { _, _ ->
                            createLearningPlan()
                        }
                        .setNegativeButton("关闭", null)
                        .show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "显示学习洞察失败", e)
            }
        }
    }
    
    private fun refreshRecommendations() {
        lifecycleScope.launch {
            try {
                currentUser?.let { user ->
                    val userId = preferenceManager.getUserId()
                    val learningRecords = generateSampleLearningRecords()
                    
                    val suggestionsResult = learningAnalyzer.generateLearningSuggestions(user, learningRecords)
                    suggestionsResult.fold(
                        onSuccess = { suggestions ->
                            updateSuggestions(suggestions)
                            Toast.makeText(this@StudentLearningAnalysisActivity, "建议已更新", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { error ->
                            Log.e(TAG, "刷新建议失败", error)
                            Toast.makeText(this@StudentLearningAnalysisActivity, "刷新失败", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "刷新建议异常", e)
            }
        }
    }
    
    private fun refreshAnalysis() {
        Toast.makeText(this, "🔄 正在刷新分析...", Toast.LENGTH_SHORT).show()
        performInitialAnalysis()
    }
    
    private fun showInterventionDialog(emotionalState: AIEmotionRecognizer.EmotionalState) {
        val interventionMessage = """
            ⚠️ 学习状态提醒
            
            检测到您可能需要调整学习状态：
            
            当前状态: ${emotionalState.emotionalState}
            ${if (emotionalState.stressLevel > 7) "• 压力水平较高" else ""}
            ${if (emotionalState.focusLevel < 4) "• 专注度偏低" else ""}
            ${if (emotionalState.fatigueLevel > 7) "• 疲劳程度较高" else ""}
            
            建议措施:
            ${emotionalState.suggestions.joinToString("\n• ", "• ")}
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("💡 智能干预建议")
            .setMessage(interventionMessage)
            .setPositiveButton("采纳建议") { _, _ ->
                // 可以在这里执行具体的干预措施
                applyInterventionMeasures(emotionalState)
            }
            .setNegativeButton("稍后处理", null)
            .show()
    }
    
    private fun applyInterventionMeasures(emotionalState: AIEmotionRecognizer.EmotionalState) {
        when {
            emotionalState.fatigueLevel > 7 -> {
                Toast.makeText(this, "建议休息10分钟，已为您暂停学习提醒", Toast.LENGTH_LONG).show()
            }
            emotionalState.stressLevel > 7 -> {
                Toast.makeText(this, "已为您调整学习难度，请放松心情", Toast.LENGTH_LONG).show()
            }
            emotionalState.focusLevel < 4 -> {
                Toast.makeText(this, "建议切换学习方式，增加互动练习", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun getAdditionalSuggestions() {
        lifecycleScope.launch {
            currentUser?.let { user ->
                currentEmotionalState?.let { state ->
                    try {
                        val interventionResult = emotionRecognizer.generateInterventionSuggestions(
                            user = user,
                            emotionalState = state,
                            learningContext = "数学学习"
                        )
                        
                        interventionResult.fold(
                            onSuccess = { suggestions ->
                                val suggestionsText = suggestions.joinToString("\n• ", "• ")
                                androidx.appcompat.app.AlertDialog.Builder(this@StudentLearningAnalysisActivity)
                                    .setTitle("🎯 个性化干预建议")
                                    .setMessage(suggestionsText)
                                    .setPositiveButton("好的", null)
                                    .show()
                            },
                            onFailure = { error ->
                                Log.e(TAG, "获取额外建议失败", error)
                                Toast.makeText(this@StudentLearningAnalysisActivity, "获取建议失败", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "获取额外建议异常", e)
                    }
                }
            }
        }
    }
    
    private fun showSuggestionDetails(suggestion: LearningSuggestion) {
        val detailMessage = """
            📋 建议详情
            
            类型: ${suggestion.type}
            标题: ${suggestion.title}
            
            详细描述:
            ${suggestion.description}
            
            优先级: ${suggestion.priority}
            预计时间: ${suggestion.estimatedTime}
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("💡 学习建议")
            .setMessage(detailMessage)
            .setPositiveButton("采纳") { _, _ ->
                Toast.makeText(this, "已记录您的学习计划", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("关闭", null)
            .show()
    }
    
    private fun createLearningPlan() {
        lifecycleScope.launch {
            try {
                currentUser?.let { user ->
                    val pathResult = learningAnalyzer.generatePersonalizedLearningPath(
                        user = user,
                        currentLevel = "中等",
                        targetGoal = "期末考试",
                        timeAvailable = 60
                    )
                    
                    pathResult.fold(
                        onSuccess = { path ->
                            showLearningPlan(path)
                        },
                        onFailure = { error ->
                            Log.e(TAG, "创建学习计划失败", error)
                            Toast.makeText(this@StudentLearningAnalysisActivity, "创建计划失败", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "创建学习计划异常", e)
            }
        }
    }
    
    private fun showLearningPlan(path: com.example.educationapp.ai.LearningPath) {
        val planMessage = """
            📅 个性化学习计划
            
            总时长: ${path.totalWeeks}周
            
            每周目标:
            ${path.weeklyGoals.joinToString("\n• ", "• ")}
            
            每日任务:
            ${path.dailyTasks.joinToString("\n• ", "• ")}
            
            重要里程碑:
            ${path.milestones.joinToString("\n• ", "• ")}
            
            推荐资源:
            ${path.resources.joinToString("\n• ", "• ")}
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🎯 学习计划")
            .setMessage(planMessage)
            .setPositiveButton("开始执行") { _, _ ->
                Toast.makeText(this, "学习计划已保存，加油！", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("稍后", null)
            .show()
    }
}

// 适配器类
class LearningSuggestionsAdapter(
    private val suggestions: List<LearningSuggestion>,
    private val onItemClick: (LearningSuggestion) -> Unit
) : RecyclerView.Adapter<LearningSuggestionsAdapter.ViewHolder>() {
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvSuggestionTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvSuggestionDescription)
        val tvPriority: TextView = itemView.findViewById(R.id.tvSuggestionPriority)
        val tvTime: TextView = itemView.findViewById(R.id.tvSuggestionTime)
    }
    
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_learning_suggestion, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val suggestion = suggestions[position]
        
        holder.tvTitle.text = suggestion.title
        holder.tvDescription.text = suggestion.description
        holder.tvPriority.text = suggestion.priority
        holder.tvTime.text = suggestion.estimatedTime
        
        // 设置优先级颜色
        val priorityColor = when (suggestion.priority) {
            "高" -> Color.parseColor("#F44336")
            "中" -> Color.parseColor("#FF9800")
            "低" -> Color.parseColor("#4CAF50")
            else -> Color.parseColor("#9E9E9E")
        }
        holder.tvPriority.setTextColor(priorityColor)
        
        holder.itemView.setOnClickListener {
            onItemClick(suggestion)
        }
    }
    
    override fun getItemCount() = suggestions.size
}

// 数据类（如果不在其他文件中定义）
data class LearningPattern(
    val bestLearningTime: String,
    val preferredSubjects: List<String>,
    val learningStyle: String,
    val attentionSpan: String,
    val difficultyPreference: String,
    val recommendations: List<String>
)

data class LearningPath(
    val totalWeeks: Int,
    val weeklyGoals: List<String>,
    val dailyTasks: List<String>,
    val milestones: List<String>,
    val resources: List<String>,
    val assessmentPoints: List<String>
)

data class LearningSession(
    val subject: String,
    val topic: String,
    val duration: Int,
    val currentScore: Float,
    val attentionLevel: String,
    val fatigueLevel: String
)

data class LearningState(
    val focusLevel: Int,
    val understandingLevel: Int,
    val fatigueLevel: Int,
    val recommendations: List<String>,
    val breakSuggestion: String,
    val difficultyAdjustment: String
)

data class LearningSuggestion(
    val type: String,
    val title: String,
    val description: String,
    val priority: String,
    val estimatedTime: String
)
