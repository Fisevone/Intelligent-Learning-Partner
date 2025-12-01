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
import com.example.educationapp.ai.LearningEffectPredictor
import com.example.educationapp.data.EducationDatabase
import com.example.educationapp.data.User
import com.example.educationapp.service.LearningProgressTracker
import com.example.educationapp.utils.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 学习效果预测界面 - 基于GLM-4的智能预测系统
 * 功能：学习成果预测、风险评估、路径优化、个性化建议
 */
class LearningPredictionActivity : AppCompatActivity() {
    
    private lateinit var predictor: LearningEffectPredictor
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var progressTracker: LearningProgressTracker
    
    // UI 组件
    private lateinit var etTargetGoal: TextInputEditText
    private lateinit var etTimeframe: TextInputEditText
    private lateinit var btnPredict: MaterialButton
    private lateinit var btnAssessRisk: MaterialButton
    private lateinit var btnOptimizePath: MaterialButton
    
    // 预测结果显示
    private lateinit var cardPredictionResult: MaterialCardView
    private lateinit var progressOverallScore: CircularProgressIndicator
    private lateinit var tvOverallScore: TextView
    private lateinit var tvExpectedImprovement: TextView
    private lateinit var tvTimeToMastery: TextView
    private lateinit var tvRiskLevel: TextView
    private lateinit var tvConfidenceLevel: TextView
    private lateinit var chipGroupFactors: ChipGroup
    private lateinit var rvRecommendations: RecyclerView
    private lateinit var tvDetailedAnalysis: TextView
    
    // 风险评估显示
    private lateinit var cardRiskAssessment: MaterialCardView
    private lateinit var progressRiskScore: LinearProgressIndicator
    private lateinit var tvRiskScore: TextView
    private lateinit var tvRiskLevelDetail: TextView
    private lateinit var chipGroupRiskFactors: ChipGroup
    private lateinit var tvEarlyWarnings: TextView
    private lateinit var tvPreventiveMeasures: TextView
    
    // 路径优化显示
    private lateinit var cardPathOptimization: MaterialCardView
    private lateinit var progressCurrentEfficiency: LinearProgressIndicator
    private lateinit var tvCurrentEfficiency: TextView
    private lateinit var tvEfficiencyGain: TextView
    private lateinit var tvTimeReduction: TextView
    private lateinit var tvDifficultyAdjustment: TextView
    private lateinit var rvOptimizedPath: RecyclerView
    
    // 数据
    private var currentUser: User? = null
    private var currentPrediction: LearningEffectPredictor.LearningPrediction? = null
    private var currentRiskAssessment: LearningEffectPredictor.LearningRiskAssessment? = null
    private var currentOptimization: LearningEffectPredictor.LearningPathOptimization? = null
    
    companion object {
        private const val TAG = "LearningPrediction"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learning_prediction)
        
        initServices()
        initViews()
        setupClickListeners()
        loadUserData()
    }
    
    private fun initServices() {
        predictor = LearningEffectPredictor()
        preferenceManager = PreferenceManager(this)
        
        val database = EducationDatabase.getDatabase(this)
        progressTracker = LearningProgressTracker.getInstance(this, database.learningProgressDao())
    }
    
    private fun initViews() {
        // 输入组件
        etTargetGoal = findViewById(R.id.etTargetGoal)
        etTimeframe = findViewById(R.id.etTimeframe)
        btnPredict = findViewById(R.id.btnPredict)
        btnAssessRisk = findViewById(R.id.btnAssessRisk)
        btnOptimizePath = findViewById(R.id.btnOptimizePath)
        
        // 预测结果
        cardPredictionResult = findViewById(R.id.cardPredictionResult)
        progressOverallScore = findViewById(R.id.progressOverallScore)
        tvOverallScore = findViewById(R.id.tvOverallScore)
        tvExpectedImprovement = findViewById(R.id.tvExpectedImprovement)
        tvTimeToMastery = findViewById(R.id.tvTimeToMastery)
        tvRiskLevel = findViewById(R.id.tvRiskLevel)
        tvConfidenceLevel = findViewById(R.id.tvConfidenceLevel)
        chipGroupFactors = findViewById(R.id.chipGroupFactors)
        rvRecommendations = findViewById(R.id.rvRecommendations)
        tvDetailedAnalysis = findViewById(R.id.tvDetailedAnalysis)
        
        // 风险评估
        cardRiskAssessment = findViewById(R.id.cardRiskAssessment)
        progressRiskScore = findViewById(R.id.progressRiskScore)
        tvRiskScore = findViewById(R.id.tvRiskScore)
        tvRiskLevelDetail = findViewById(R.id.tvRiskLevelDetail)
        chipGroupRiskFactors = findViewById(R.id.chipGroupRiskFactors)
        tvEarlyWarnings = findViewById(R.id.tvEarlyWarnings)
        tvPreventiveMeasures = findViewById(R.id.tvPreventiveMeasures)
        
        // 路径优化
        cardPathOptimization = findViewById(R.id.cardPathOptimization)
        progressCurrentEfficiency = findViewById(R.id.progressCurrentEfficiency)
        tvCurrentEfficiency = findViewById(R.id.tvCurrentEfficiency)
        tvEfficiencyGain = findViewById(R.id.tvEfficiencyGain)
        tvTimeReduction = findViewById(R.id.tvTimeReduction)
        tvDifficultyAdjustment = findViewById(R.id.tvDifficultyAdjustment)
        rvOptimizedPath = findViewById(R.id.rvOptimizedPath)
        
        // 设置RecyclerView
        rvRecommendations.layoutManager = LinearLayoutManager(this)
        rvOptimizedPath.layoutManager = LinearLayoutManager(this)
        
        // 设置工具栏
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "🔮 学习效果预测"
        
        // 初始隐藏结果卡片
        hideAllResultCards()
    }
    
    private fun setupClickListeners() {
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }
        
        btnPredict.setOnClickListener {
            performLearningPrediction()
        }
        
        btnAssessRisk.setOnClickListener {
            performRiskAssessment()
        }
        
        btnOptimizePath.setOnClickListener {
            performPathOptimization()
        }
        
        cardPredictionResult.setOnClickListener {
            showDetailedPrediction()
        }
        
        cardRiskAssessment.setOnClickListener {
            showDetailedRiskAnalysis()
        }
        
        cardPathOptimization.setOnClickListener {
            showDetailedOptimization()
        }
    }
    
    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                val userId = preferenceManager.getUserId()
                val database = EducationDatabase.getDatabase(this@LearningPredictionActivity)
                val userDao = database.userDao()
                
                currentUser = userDao.getUserById(userId)
                
                if (currentUser != null) {
                    supportActionBar?.subtitle = "${currentUser?.name} - 智能预测"
                    
                    // 设置默认值
                    etTargetGoal.setText("期末考试80分以上")
                    etTimeframe.setText("30")
                } else {
                    Toast.makeText(this@LearningPredictionActivity, "无法加载用户数据", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载用户数据失败", e)
            }
        }
    }
    
    private fun performLearningPrediction() {
        val targetGoal = etTargetGoal.text.toString().trim()
        val timeframeText = etTimeframe.text.toString().trim()
        
        if (targetGoal.isEmpty() || timeframeText.isEmpty()) {
            Toast.makeText(this, "请填写学习目标和时间框架", Toast.LENGTH_SHORT).show()
            return
        }
        
        val timeframe = timeframeText.toIntOrNull()
        if (timeframe == null || timeframe <= 0) {
            Toast.makeText(this, "请输入有效的时间框架", Toast.LENGTH_SHORT).show()
            return
        }
        
        currentUser?.let { user ->
            lifecycleScope.launch {
                try {
                    btnPredict.isEnabled = false
                    btnPredict.text = "预测中..."
                    
                    val userId = preferenceManager.getUserId()
                    val learningHistory = generateSampleLearningRecords()
                    
                    val predictionResult = predictor.predictLearningOutcome(
                        user = user,
                        subject = "数学",
                        learningHistory = learningHistory,
                        targetGoal = targetGoal,
                        timeframe = timeframe
                    )
                    
                    predictionResult.fold(
                        onSuccess = { prediction ->
                            currentPrediction = prediction
                            displayPredictionResult(prediction)
                            Toast.makeText(this@LearningPredictionActivity, "预测完成", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { error ->
                            Log.e(TAG, "预测失败", error)
                            Toast.makeText(this@LearningPredictionActivity, "预测失败，请重试", Toast.LENGTH_SHORT).show()
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "预测异常", e)
                    Toast.makeText(this@LearningPredictionActivity, "预测出现异常", Toast.LENGTH_SHORT).show()
                } finally {
                    btnPredict.isEnabled = true
                    btnPredict.text = "开始预测"
                }
            }
        }
    }
    
    private fun performRiskAssessment() {
        currentUser?.let { user ->
            lifecycleScope.launch {
                try {
                    btnAssessRisk.isEnabled = false
                    btnAssessRisk.text = "评估中..."
                    
                    val userId = preferenceManager.getUserId()
                    val learningHistory = generateSampleLearningRecords()
                    
                    // 模拟当前表现数据
                    val currentPerformance = mapOf(
                        "数学" to 75f,
                        "物理" to 68f,
                        "化学" to 82f
                    )
                    
                    val riskResult = predictor.assessLearningRisk(
                        user = user,
                        learningHistory = learningHistory,
                        currentPerformance = currentPerformance
                    )
                    
                    riskResult.fold(
                        onSuccess = { riskAssessment ->
                            currentRiskAssessment = riskAssessment
                            displayRiskAssessment(riskAssessment)
                            Toast.makeText(this@LearningPredictionActivity, "风险评估完成", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { error ->
                            Log.e(TAG, "风险评估失败", error)
                            Toast.makeText(this@LearningPredictionActivity, "评估失败，请重试", Toast.LENGTH_SHORT).show()
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "风险评估异常", e)
                } finally {
                    btnAssessRisk.isEnabled = true
                    btnAssessRisk.text = "风险评估"
                }
            }
        }
    }
    
    private fun performPathOptimization() {
        currentUser?.let { user ->
            lifecycleScope.launch {
                try {
                    btnOptimizePath.isEnabled = false
                    btnOptimizePath.text = "优化中..."
                    
                    val currentPath = listOf("基础概念", "基本运算", "应用题", "综合练习", "模拟考试")
                    val performanceData = mapOf(
                        "基础概念" to 85f,
                        "基本运算" to 78f,
                        "应用题" to 65f,
                        "综合练习" to 72f,
                        "模拟考试" to 70f
                    )
                    val timeConstraints = mapOf(
                        "总时长" to 30,
                        "每日时长" to 2
                    )
                    
                    val optimizationResult = predictor.optimizeLearningPath(
                        user = user,
                        currentPath = currentPath,
                        performanceData = performanceData,
                        timeConstraints = timeConstraints
                    )
                    
                    optimizationResult.fold(
                        onSuccess = { optimization ->
                            currentOptimization = optimization
                            displayPathOptimization(optimization)
                            Toast.makeText(this@LearningPredictionActivity, "路径优化完成", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { error ->
                            Log.e(TAG, "路径优化失败", error)
                            Toast.makeText(this@LearningPredictionActivity, "优化失败，请重试", Toast.LENGTH_SHORT).show()
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "路径优化异常", e)
                } finally {
                    btnOptimizePath.isEnabled = true
                    btnOptimizePath.text = "路径优化"
                }
            }
        }
    }
    
    private fun displayPredictionResult(prediction: LearningEffectPredictor.LearningPrediction) {
        cardPredictionResult.visibility = View.VISIBLE
        
        // 动画更新整体分数
        animateProgress(progressOverallScore, prediction.overallScore.roundToInt())
        tvOverallScore.text = "${prediction.overallScore.roundToInt()}/100"
        
        // 更新其他指标
        tvExpectedImprovement.text = "+${String.format("%.1f", prediction.expectedImprovement)}%"
        tvTimeToMastery.text = "${prediction.timeToMastery}天"
        tvRiskLevel.text = prediction.riskLevel
        tvConfidenceLevel.text = "${(prediction.confidenceLevel * 100).roundToInt()}%"
        
        // 设置风险等级颜色
        val riskColor = when (prediction.riskLevel) {
            "低" -> Color.parseColor("#4CAF50")
            "中" -> Color.parseColor("#FF9800")
            "高" -> Color.parseColor("#F44336")
            else -> Color.parseColor("#9E9E9E")
        }
        tvRiskLevel.setTextColor(riskColor)
        
        // 添加关键因素芯片
        chipGroupFactors.removeAllViews()
        prediction.keyFactors.forEach { factor ->
            val chip = Chip(this)
            chip.text = factor
            chip.isClickable = false
            chipGroupFactors.addView(chip)
        }
        
        // 设置建议列表
        val recommendationAdapter = RecommendationAdapter(prediction.recommendations)
        rvRecommendations.adapter = recommendationAdapter
        
        // 设置详细分析
        tvDetailedAnalysis.text = prediction.detailedAnalysis
    }
    
    private fun displayRiskAssessment(risk: LearningEffectPredictor.LearningRiskAssessment) {
        cardRiskAssessment.visibility = View.VISIBLE
        
        // 动画更新风险分数
        animateLinearProgress(progressRiskScore, risk.riskScore.roundToInt())
        tvRiskScore.text = "${risk.riskScore.roundToInt()}/100"
        tvRiskLevelDetail.text = risk.riskLevel
        
        // 设置风险等级颜色
        val riskColor = when (risk.riskLevel) {
            "低" -> Color.parseColor("#4CAF50")
            "中" -> Color.parseColor("#FF9800")
            "高" -> Color.parseColor("#F44336")
            else -> Color.parseColor("#9E9E9E")
        }
        tvRiskLevelDetail.setTextColor(riskColor)
        progressRiskScore.setIndicatorColor(riskColor)
        
        // 添加风险因素芯片
        chipGroupRiskFactors.removeAllViews()
        risk.riskFactors.forEach { factor ->
            val chip = Chip(this)
            chip.text = factor
            chip.isClickable = false
            chip.setChipBackgroundColorResource(R.color.risk_chip_background)
            chipGroupRiskFactors.addView(chip)
        }
        
        // 设置预警信号和预防措施
        tvEarlyWarnings.text = risk.earlyWarnings.joinToString("\n• ", "⚠️ 预警信号：\n• ")
        tvPreventiveMeasures.text = risk.preventiveMeasures.joinToString("\n• ", "🛡️ 预防措施：\n• ")
    }
    
    private fun displayPathOptimization(optimization: LearningEffectPredictor.LearningPathOptimization) {
        cardPathOptimization.visibility = View.VISIBLE
        
        // 动画更新当前效率
        animateLinearProgress(progressCurrentEfficiency, (optimization.currentEfficiency * 100).roundToInt())
        tvCurrentEfficiency.text = "${(optimization.currentEfficiency * 100).roundToInt()}%"
        
        // 更新其他指标
        tvEfficiencyGain.text = "+${(optimization.expectedEfficiencyGain * 100).roundToInt()}%"
        tvTimeReduction.text = "${optimization.timeReduction}天"
        tvDifficultyAdjustment.text = optimization.difficultyAdjustment
        
        // 设置优化路径列表
        val pathAdapter = OptimizedPathAdapter(optimization.optimizedPath, optimization.studyMethodSuggestions)
        rvOptimizedPath.adapter = pathAdapter
    }
    
    private fun animateProgress(progressBar: CircularProgressIndicator, targetProgress: Int) {
        val animator = ValueAnimator.ofInt(0, targetProgress)
        animator.duration = 1500
        animator.addUpdateListener { animation ->
            progressBar.progress = animation.animatedValue as Int
        }
        animator.start()
    }
    
    private fun animateLinearProgress(progressBar: LinearProgressIndicator, targetProgress: Int) {
        val animator = ValueAnimator.ofInt(0, targetProgress)
        animator.duration = 1200
        animator.addUpdateListener { animation ->
            progressBar.progress = animation.animatedValue as Int
        }
        animator.start()
    }
    
    private fun hideAllResultCards() {
        cardPredictionResult.visibility = View.GONE
        cardRiskAssessment.visibility = View.GONE
        cardPathOptimization.visibility = View.GONE
    }
    
    private fun showDetailedPrediction() {
        currentPrediction?.let { prediction ->
            val detailMessage = """
                📊 详细预测报告
                
                🎯 综合预测分数：${prediction.overallScore.roundToInt()}/100
                📈 预期提升幅度：+${String.format("%.1f", prediction.expectedImprovement)}%
                ⏰ 预计掌握时间：${prediction.timeToMastery}天
                ⚠️ 风险等级：${prediction.riskLevel}
                🔍 预测置信度：${(prediction.confidenceLevel * 100).roundToInt()}%
                
                🔑 关键影响因素：
                ${prediction.keyFactors.joinToString("\n• ", "• ")}
                
                💡 改进建议：
                ${prediction.recommendations.joinToString("\n• ", "• ")}
                
                📋 详细分析：
                ${prediction.detailedAnalysis}
            """.trimIndent()
            
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("🔮 学习效果预测详情")
                .setMessage(detailMessage)
                .setPositiveButton("制定计划") { _, _ ->
                    createLearningPlan(prediction)
                }
                .setNegativeButton("关闭", null)
                .show()
        }
    }
    
    private fun showDetailedRiskAnalysis() {
        currentRiskAssessment?.let { risk ->
            val riskMessage = """
                ⚠️ 学习风险详细分析
                
                🎯 风险等级：${risk.riskLevel}
                📊 风险分数：${risk.riskScore.roundToInt()}/100
                
                🔍 风险因素：
                ${risk.riskFactors.joinToString("\n• ", "• ")}
                
                ⚡ 预警信号：
                ${risk.earlyWarnings.joinToString("\n• ", "• ")}
                
                🛡️ 预防措施：
                ${risk.preventiveMeasures.joinToString("\n• ", "• ")}
                
                ⏰ 建议干预时机：${risk.interventionTiming}
            """.trimIndent()
            
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("🚨 风险评估详情")
                .setMessage(riskMessage)
                .setPositiveButton("制定预防计划") { _, _ ->
                    createRiskPreventionPlan(risk)
                }
                .setNegativeButton("关闭", null)
                .show()
        }
    }
    
    private fun showDetailedOptimization() {
        currentOptimization?.let { optimization ->
            val optimizationMessage = """
                🚀 学习路径优化详情
                
                📊 当前效率：${(optimization.currentEfficiency * 100).roundToInt()}%
                📈 预期效率提升：+${(optimization.expectedEfficiencyGain * 100).roundToInt()}%
                ⏰ 时间节省：${optimization.timeReduction}天
                
                🎯 难度调整建议：
                ${optimization.difficultyAdjustment}
                
                📚 学习方法建议：
                ${optimization.studyMethodSuggestions.joinToString("\n• ", "• ")}
                
                📅 时间安排优化：
                ${optimization.scheduleOptimization}
                
                🛤️ 优化后路径：
                ${optimization.optimizedPath.joinToString(" → ")}
            """.trimIndent()
            
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("⚡ 路径优化详情")
                .setMessage(optimizationMessage)
                .setPositiveButton("应用优化") { _, _ ->
                    applyOptimization(optimization)
                }
                .setNegativeButton("关闭", null)
                .show()
        }
    }
    
    private fun createLearningPlan(prediction: LearningEffectPredictor.LearningPrediction) {
        Toast.makeText(this, "根据预测结果制定学习计划功能开发中...", Toast.LENGTH_SHORT).show()
        // 这里可以跳转到学习计划制定界面
    }
    
    private fun createRiskPreventionPlan(risk: LearningEffectPredictor.LearningRiskAssessment) {
        Toast.makeText(this, "风险预防计划已保存到学习提醒中", Toast.LENGTH_LONG).show()
        // 这里可以设置风险预防提醒
    }
    
    private fun applyOptimization(optimization: LearningEffectPredictor.LearningPathOptimization) {
        Toast.makeText(this, "学习路径优化已应用，请查看学习计划", Toast.LENGTH_LONG).show()
        // 这里可以更新用户的学习路径
    }
    
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
}

// 推荐建议适配器
class RecommendationAdapter(
    private val recommendations: List<String>
) : RecyclerView.Adapter<RecommendationAdapter.ViewHolder>() {
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRecommendation: TextView = itemView.findViewById(R.id.tvRecommendation)
    }
    
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommendation, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvRecommendation.text = "• ${recommendations[position]}"
    }
    
    override fun getItemCount() = recommendations.size
}

// 优化路径适配器
class OptimizedPathAdapter(
    private val path: List<String>,
    private val methods: List<String>
) : RecyclerView.Adapter<OptimizedPathAdapter.ViewHolder>() {
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPathStep: TextView = itemView.findViewById(R.id.tvPathStep)
        val tvMethod: TextView = itemView.findViewById(R.id.tvMethod)
    }
    
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_optimized_path, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvPathStep.text = "${position + 1}. ${path[position]}"
        if (position < methods.size) {
            holder.tvMethod.text = methods[position]
            holder.tvMethod.visibility = View.VISIBLE
        } else {
            holder.tvMethod.visibility = View.GONE
        }
    }
    
    override fun getItemCount() = path.size
}
