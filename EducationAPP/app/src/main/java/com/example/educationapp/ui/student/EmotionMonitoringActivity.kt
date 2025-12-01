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
import com.example.educationapp.R
import com.example.educationapp.ai.AIEmotionRecognizer
import com.example.educationapp.data.EducationDatabase
import com.example.educationapp.data.User
import com.example.educationapp.utils.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * 情绪监测界面 - 基于GLM-4的实时情绪识别与干预
 * 功能：实时情绪监测、行为分析、智能干预建议、学习状态优化
 */
class EmotionMonitoringActivity : AppCompatActivity() {
    
    private lateinit var emotionRecognizer: AIEmotionRecognizer
    private lateinit var preferenceManager: PreferenceManager
    
    // UI 组件
    private lateinit var tvCurrentEmotion: TextView
    private lateinit var tvFocusScore: TextView
    private lateinit var tvStressScore: TextView
    private lateinit var tvConfidenceScore: TextView
    private lateinit var tvFatigueScore: TextView
    private lateinit var progressFocus: CircularProgressIndicator
    private lateinit var progressStress: CircularProgressIndicator
    private lateinit var progressConfidence: CircularProgressIndicator
    private lateinit var progressFatigue: CircularProgressIndicator
    private lateinit var tvAnalysisReason: TextView
    private lateinit var tvSuggestions: TextView
    private lateinit var cardEmotionStatus: MaterialCardView
    private lateinit var btnStartMonitoring: MaterialButton
    private lateinit var btnStopMonitoring: MaterialButton
    private lateinit var btnGetAdvice: MaterialButton
    
    // 监测数据
    private var currentUser: User? = null
    private var isMonitoring = false
    private val behaviorEvents = mutableListOf<AIEmotionRecognizer.BehaviorEvent>()
    private var currentEmotionalState: AIEmotionRecognizer.EmotionalState? = null
    
    companion object {
        private const val TAG = "EmotionMonitoring"
        private const val MONITORING_INTERVAL = 8000L // 8秒间隔
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emotion_monitoring)
        
        initServices()
        initViews()
        setupClickListeners()
        loadUserData()
    }
    
    private fun initServices() {
        emotionRecognizer = AIEmotionRecognizer()
        preferenceManager = PreferenceManager(this)
    }
    
    private fun initViews() {
        // 初始化UI组件
        tvCurrentEmotion = findViewById(R.id.tvCurrentEmotion)
        tvFocusScore = findViewById(R.id.tvFocusScore)
        tvStressScore = findViewById(R.id.tvStressScore)
        tvConfidenceScore = findViewById(R.id.tvConfidenceScore)
        tvFatigueScore = findViewById(R.id.tvFatigueScore)
        
        progressFocus = findViewById(R.id.progressFocus)
        progressStress = findViewById(R.id.progressStress)
        progressConfidence = findViewById(R.id.progressConfidence)
        progressFatigue = findViewById(R.id.progressFatigue)
        
        tvAnalysisReason = findViewById(R.id.tvAnalysisReason)
        tvSuggestions = findViewById(R.id.tvSuggestions)
        cardEmotionStatus = findViewById(R.id.cardEmotionStatus)
        
        btnStartMonitoring = findViewById(R.id.btnStartMonitoring)
        btnStopMonitoring = findViewById(R.id.btnStopMonitoring)
        btnGetAdvice = findViewById(R.id.btnGetAdvice)
        
        // 设置工具栏
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "🎭 情绪监测中心"
        
        // 初始状态
        updateMonitoringButtonsState(false)
    }
    
    private fun setupClickListeners() {
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }
        
        btnStartMonitoring.setOnClickListener {
            startEmotionMonitoring()
        }
        
        btnStopMonitoring.setOnClickListener {
            stopEmotionMonitoring()
        }
        
        btnGetAdvice.setOnClickListener {
            getPersonalizedAdvice()
        }
        
        cardEmotionStatus.setOnClickListener {
            showDetailedAnalysis()
        }
        
        // 模拟用户行为事件
        findViewById<MaterialButton>(R.id.btnSimulateClick).setOnClickListener {
            simulateUserBehavior("click")
        }
        
        findViewById<MaterialButton>(R.id.btnSimulateAnswer).setOnClickListener {
            simulateUserBehavior("answer")
        }
        
        findViewById<MaterialButton>(R.id.btnSimulatePause).setOnClickListener {
            simulateUserBehavior("pause")
        }
    }
    
    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                val userId = preferenceManager.getUserId()
                val database = EducationDatabase.getDatabase(this@EmotionMonitoringActivity)
                val userDao = database.userDao()
                
                currentUser = userDao.getUserById(userId)
                
                if (currentUser != null) {
                    supportActionBar?.subtitle = "${currentUser?.name} - 情绪监测"
                } else {
                    Toast.makeText(this@EmotionMonitoringActivity, "无法加载用户数据", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载用户数据失败", e)
            }
        }
    }
    
    private fun startEmotionMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        updateMonitoringButtonsState(true)
        
        Toast.makeText(this, "🎯 开始情绪监测...", Toast.LENGTH_SHORT).show()
        
        // 启动监测循环
        lifecycleScope.launch {
            while (isMonitoring) {
                try {
                    // 执行情绪分析
                    performEmotionAnalysis()
                    
                    // 等待下一次监测
                    delay(MONITORING_INTERVAL)
                    
                    // 清理过多的历史数据
                    if (behaviorEvents.size > 100) {
                        behaviorEvents.removeAt(0)
                    }
                    
                } catch (e: CancellationException) {
                    // 协程被正常取消，不需要记录错误
                    Log.d(TAG, "监测协程已取消")
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "监测过程中出现异常", e)
                    break
                }
            }
        }
    }
    
    private fun stopEmotionMonitoring() {
        isMonitoring = false
        updateMonitoringButtonsState(false)
        Toast.makeText(this, "⏹️ 情绪监测已停止", Toast.LENGTH_SHORT).show()
    }
    
    private fun updateMonitoringButtonsState(monitoring: Boolean) {
        btnStartMonitoring.isEnabled = !monitoring
        btnStopMonitoring.isEnabled = monitoring
        
        if (monitoring) {
            btnStartMonitoring.text = "监测中..."
            btnStartMonitoring.setIconResource(R.drawable.ic_monitoring)
        } else {
            btnStartMonitoring.text = "开始监测"
            btnStartMonitoring.setIconResource(R.drawable.ic_play)
        }
    }
    
    private suspend fun performEmotionAnalysis() {
        currentUser?.let { user ->
            try {
                Log.d(TAG, "执行情绪分析，当前行为事件数：${behaviorEvents.size}")
                
                val emotionResult = emotionRecognizer.analyzeRealTimeEmotion(
                    user = user,
                    recentBehaviors = behaviorEvents.takeLast(30),
                    currentLearningSession = null
                )
                
                emotionResult.fold(
                    onSuccess = { emotionalState ->
                        currentEmotionalState = emotionalState
                        runOnUiThread {
                            updateEmotionUI(emotionalState)
                        }
                        
                        // 检查是否需要干预
                        if (emotionalState.interventionNeeded) {
                            runOnUiThread {
                                showInterventionAlert(emotionalState)
                            }
                        }
                        
                        Log.d(TAG, "情绪分析完成：${emotionalState.emotionalState}")
                    },
                    onFailure = { error ->
                        Log.e(TAG, "情绪分析失败", error)
                        runOnUiThread {
                            Toast.makeText(this@EmotionMonitoringActivity, "分析失败，请检查网络", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } catch (e: CancellationException) {
                // 协程被取消，正常情况
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "情绪分析异常", e)
            }
        }
    }
    
    private fun updateEmotionUI(emotionalState: AIEmotionRecognizer.EmotionalState) {
        // 更新情绪状态文本和颜色
        tvCurrentEmotion.text = emotionalState.emotionalState
        updateEmotionCardColor(emotionalState.emotionalState)
        
        // 更新各项指标
        updateScoreWithAnimation(tvFocusScore, progressFocus, emotionalState.focusLevel)
        updateScoreWithAnimation(tvStressScore, progressStress, emotionalState.stressLevel)
        updateScoreWithAnimation(tvConfidenceScore, progressConfidence, emotionalState.confidenceLevel)
        updateScoreWithAnimation(tvFatigueScore, progressFatigue, emotionalState.fatigueLevel)
        
        // 更新分析原因
        tvAnalysisReason.text = emotionalState.analysisReason
        
        // 更新建议
        val suggestionsText = emotionalState.suggestions.joinToString("\n• ", "💡 当前建议：\n• ")
        tvSuggestions.text = suggestionsText
        
        // 启用获取建议按钮
        btnGetAdvice.isEnabled = true
    }
    
    private fun updateScoreWithAnimation(textView: TextView, progressBar: CircularProgressIndicator, score: Int) {
        // 更新文本
        textView.text = "$score/10"
        
        // 动画更新进度条
        val animator = ValueAnimator.ofInt(progressBar.progress, score * 10)
        animator.duration = 800
        animator.addUpdateListener { animation ->
            progressBar.progress = animation.animatedValue as Int
        }
        animator.start()
        
        // 根据分数设置颜色
        val color = when {
            score >= 8 -> Color.parseColor("#4CAF50") // 绿色
            score >= 6 -> Color.parseColor("#FFC107") // 黄色
            score >= 4 -> Color.parseColor("#FF9800") // 橙色
            else -> Color.parseColor("#F44336") // 红色
        }
        textView.setTextColor(color)
    }
    
    private fun updateEmotionCardColor(emotionalState: String) {
        val color = when (emotionalState) {
            "积极" -> Color.parseColor("#E8F5E8")
            "中性" -> Color.parseColor("#FFF8E1")
            "困惑" -> Color.parseColor("#FFF3E0")
            "疲劳" -> Color.parseColor("#FFEBEE")
            "焦虑" -> Color.parseColor("#FCE4EC")
            else -> Color.parseColor("#F5F5F5")
        }
        cardEmotionStatus.setCardBackgroundColor(color)
    }
    
    private fun simulateUserBehavior(eventType: String) {
        val currentTime = System.currentTimeMillis()
        val accuracy = when (eventType) {
            "answer" -> Random.nextFloat()
            else -> null
        }
        
        val event = AIEmotionRecognizer.BehaviorEvent(
            timestamp = currentTime,
            eventType = eventType,
            duration = Random.nextLong(500, 3000),
            accuracy = accuracy,
            hesitationTime = Random.nextLong(200, 2000)
        )
        
        behaviorEvents.add(event)
        
        Toast.makeText(this, "模拟${eventType}事件已记录", Toast.LENGTH_SHORT).show()
        
        Log.d(TAG, "模拟行为事件：$eventType")
    }
    
    private fun showInterventionAlert(emotionalState: AIEmotionRecognizer.EmotionalState) {
        val alertMessage = """
            ⚠️ 检测到需要关注的学习状态
            
            当前状态：${emotionalState.emotionalState}
            专注度：${emotionalState.focusLevel}/10
            压力水平：${emotionalState.stressLevel}/10
            疲劳程度：${emotionalState.fatigueLevel}/10
            
            建议措施：
            ${emotionalState.suggestions.joinToString("\n• ", "• ")}
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🚨 智能干预提醒")
            .setMessage(alertMessage)
            .setPositiveButton("立即采纳") { _, _ ->
                applyInterventionMeasures(emotionalState)
            }
            .setNegativeButton("稍后处理", null)
            .setNeutralButton("关闭提醒") { _, _ ->
                // 可以在这里设置暂时关闭干预提醒
            }
            .show()
    }
    
    private fun applyInterventionMeasures(emotionalState: AIEmotionRecognizer.EmotionalState) {
        when {
            emotionalState.fatigueLevel > 7 -> {
                showRestSuggestion()
            }
            emotionalState.stressLevel > 7 -> {
                showStressReliefSuggestion()
            }
            emotionalState.focusLevel < 4 -> {
                showFocusImprovementSuggestion()
            }
            else -> {
                Toast.makeText(this, "已记录您的状态，建议按提示调整学习方式", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun showRestSuggestion() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("😴 休息建议")
            .setMessage("检测到您的疲劳程度较高，建议：\n\n• 休息10-15分钟\n• 做简单的眼保健操\n• 喝水补充水分\n• 到窗边看看远处\n\n是否现在开始休息？")
            .setPositiveButton("开始休息") { _, _ ->
                startRestTimer()
            }
            .setNegativeButton("继续学习", null)
            .show()
    }
    
    private fun showStressReliefSuggestion() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("😌 压力缓解")
            .setMessage("检测到您的压力水平较高，建议：\n\n• 深呼吸3-5次\n• 降低当前学习难度\n• 听轻松的背景音乐\n• 与朋友或家人聊聊\n\n选择一个适合的方式：")
            .setPositiveButton("深呼吸练习") { _, _ ->
                startBreathingExercise()
            }
            .setNegativeButton("降低难度", null)
            .show()
    }
    
    private fun showFocusImprovementSuggestion() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🎯 专注度提升")
            .setMessage("检测到您的专注度偏低，建议：\n\n• 切换学习环境\n• 使用番茄工作法\n• 增加互动练习\n• 设定小目标\n\n选择改进方式：")
            .setPositiveButton("番茄工作法") { _, _ ->
                startPomodoroTimer()
            }
            .setNegativeButton("增加互动", null)
            .show()
    }
    
    private fun startRestTimer() {
        Toast.makeText(this, "⏰ 休息计时器已启动，15分钟后提醒您", Toast.LENGTH_LONG).show()
        // 这里可以实现一个实际的计时器
    }
    
    private fun startBreathingExercise() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🧘 深呼吸练习")
            .setMessage("请跟随节奏进行深呼吸：\n\n1. 慢慢吸气4秒\n2. 屏住呼吸4秒\n3. 慢慢呼气6秒\n4. 重复3-5次\n\n准备好了吗？")
            .setPositiveButton("开始") { _, _ ->
                Toast.makeText(this, "🌸 请开始深呼吸练习...", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun startPomodoroTimer() {
        Toast.makeText(this, "🍅 番茄工作法：25分钟专注学习已开始", Toast.LENGTH_LONG).show()
        // 这里可以实现番茄工作法计时器
    }
    
    private fun getPersonalizedAdvice() {
        currentEmotionalState?.let { state ->
            currentUser?.let { user ->
                lifecycleScope.launch {
                    try {
                        val adviceResult = emotionRecognizer.generateInterventionSuggestions(
                            user = user,
                            emotionalState = state,
                            learningContext = "当前学习会话"
                        )
                        
                        adviceResult.fold(
                            onSuccess = { suggestions ->
                                showPersonalizedAdvice(suggestions)
                            },
                            onFailure = { error ->
                                Log.e(TAG, "获取个性化建议失败", error)
                                Toast.makeText(this@EmotionMonitoringActivity, "获取建议失败", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "获取建议异常", e)
                    }
                }
            }
        }
    }
    
    private fun showPersonalizedAdvice(suggestions: List<String>) {
        val adviceText = suggestions.joinToString("\n• ", "🎯 个性化建议：\n• ")
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("💡 AI个性化建议")
            .setMessage(adviceText)
            .setPositiveButton("采纳建议") { _, _ ->
                Toast.makeText(this, "建议已记录，祝您学习愉快！", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("关闭", null)
            .show()
    }
    
    private fun showDetailedAnalysis() {
        currentEmotionalState?.let { state ->
            val detailedMessage = """
                📊 详细情绪分析报告
                
                🎭 情绪状态：${state.emotionalState}
                🧠 专注度：${state.focusLevel}/10
                😰 压力水平：${state.stressLevel}/10
                💪 自信程度：${state.confidenceLevel}/10
                😴 疲劳程度：${state.fatigueLevel}/10
                
                📋 分析依据：
                ${state.analysisReason}
                
                💡 建议措施：
                ${state.suggestions.joinToString("\n• ", "• ")}
                
                ⚠️ 是否需要干预：${if (state.interventionNeeded) "是" else "否"}
            """.trimIndent()
            
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("📈 情绪分析详情")
                .setMessage(detailedMessage)
                .setPositiveButton("导出报告") { _, _ ->
                    // 可以实现导出功能
                    Toast.makeText(this, "报告导出功能开发中...", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("关闭", null)
                .show()
        } ?: run {
            Toast.makeText(this, "暂无分析数据，请先开始监测", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopEmotionMonitoring()
    }
}
