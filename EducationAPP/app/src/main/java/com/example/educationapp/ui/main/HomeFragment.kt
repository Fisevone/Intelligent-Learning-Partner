package com.example.educationapp.ui.main

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.educationapp.R
import com.example.educationapp.ui.ai.SubjectSelectionActivity
import com.example.educationapp.ui.knowledge.KnowledgeGraphActivity
import com.example.educationapp.ui.learning.LearningZoneActivity
import com.example.educationapp.ui.student.EmotionMonitoringActivity
import com.example.educationapp.utils.SimpleDemoDataManager
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    
    private lateinit var tvUserName: TextView
    private lateinit var tvCurrentCourse: TextView
    private lateinit var tvCurrentProgressTopic: TextView
    private lateinit var tvNextGoal: TextView
    private lateinit var tvCurrentEmotion: TextView
    private lateinit var tvFocusLevel: TextView
    private lateinit var tvStressLevel: TextView
    private lateinit var tvAiSuggestion: TextView
    private lateinit var tvAiRecommendation: TextView
    private lateinit var switchEmotionMonitoring: Switch
    private lateinit var progressClassroomEngagement: ProgressBar
    private lateinit var cardEmotionStatus: CardView
    private lateinit var cardAiQuestion: CardView
    private lateinit var cardKnowledgeGraph: CardView
    private lateinit var cardLearningZone: CardView
    
    private lateinit var demoDataManager: SimpleDemoDataManager
    private val handler = Handler(Looper.getMainLooper())
    private val emotionUpdateRunnable = object : Runnable {
        override fun run() {
            if (switchEmotionMonitoring.isChecked) {
                updateEmotionStatus()
                updateAiRecommendation()
                animateProgressBar()
            }
            handler.postDelayed(this, 20000) // 每20秒更新一次
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupClickListeners()
        setupDynamicContent()
        startEmotionMonitoring()
    }
    
    private fun initViews(view: View) {
        tvUserName = view.findViewById(R.id.tvUserName)
        tvCurrentCourse = view.findViewById(R.id.tv_current_course)
        tvCurrentProgressTopic = view.findViewById(R.id.tv_current_progress_topic)
        tvNextGoal = view.findViewById(R.id.tv_next_goal)
        tvCurrentEmotion = view.findViewById(R.id.tv_current_emotion)
        tvFocusLevel = view.findViewById(R.id.tv_focus_level)
        tvStressLevel = view.findViewById(R.id.tv_stress_level)
        tvAiSuggestion = view.findViewById(R.id.tv_ai_suggestion)
        tvAiRecommendation = view.findViewById(R.id.tv_ai_recommendation)
        switchEmotionMonitoring = view.findViewById(R.id.switch_emotion_monitoring)
        progressClassroomEngagement = view.findViewById(R.id.progress_classroom_engagement)
        cardEmotionStatus = view.findViewById(R.id.card_emotion_status)
        cardAiQuestion = view.findViewById(R.id.card_ai_question)
        cardKnowledgeGraph = view.findViewById(R.id.card_knowledge_graph)
        cardLearningZone = view.findViewById(R.id.card_learning_zone)
        
        // 初始化数据管理器
        demoDataManager = SimpleDemoDataManager(requireContext())
    }
    
    private fun setupClickListeners() {
        // AI智能出题卡片点击
        cardAiQuestion.setOnClickListener {
            animateCardClick(cardAiQuestion) {
                val intent = Intent(requireContext(), SubjectSelectionActivity::class.java)
                startActivity(intent)
            }
        }
        
        // 知识图谱卡片点击
        cardKnowledgeGraph.setOnClickListener {
            animateCardClick(cardKnowledgeGraph) {
                val intent = Intent(requireContext(), KnowledgeGraphActivity::class.java)
                startActivity(intent)
            }
        }
        
        // 情绪监控卡片点击
        cardEmotionStatus.setOnClickListener {
            animateCardClick(cardEmotionStatus) {
                val intent = Intent(requireContext(), EmotionMonitoringActivity::class.java)
            startActivity(intent)
            }
        }

        // 学习专区卡片点击
        cardLearningZone.setOnClickListener {
            animateCardClick(cardLearningZone) {
                val intent = Intent(requireContext(), LearningZoneActivity::class.java)
                startActivity(intent)
            }
        }
        
        // 情绪监控开关
        switchEmotionMonitoring.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startEmotionMonitoring()
            } else {
                stopEmotionMonitoring()
            }
        }
    }
    
    private fun setupDynamicContent() {
        // 设置动态问候语
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 6 -> "深夜好！🌙"
            hour < 12 -> "早上好！☀️"
            hour < 14 -> "中午好！🌞"
            hour < 18 -> "下午好！🌅"
            hour < 22 -> "晚上好！🌆"
            else -> "夜晚好！🌙"
        }
        
        // 获取真实用户数据
        val userProfile = demoDataManager.getUserProfile()
        val userName = userProfile["name"] as? String ?: "同学"
        val userGrade = userProfile["grade"] as? String ?: "七年级"
        tvUserName.text = userName
        
        // 根据年级设置课程内容
        setupGradeSpecificContent(userGrade)
        
        // 设置初始状态
        updateEmotionStatus()
        updateAiRecommendation()
        updateLearningProgress()
        
        // 添加进度条动画
        animateProgressBar()
    }
    
    private fun getUserName(): String {
        // 这里应该从实际的用户数据源获取
        return "小明同学"
    }
    
    private fun startEmotionMonitoring() {
        handler.removeCallbacks(emotionUpdateRunnable)
        handler.post(emotionUpdateRunnable)
    }
    
    private fun stopEmotionMonitoring() {
        handler.removeCallbacks(emotionUpdateRunnable)
        tvAiRecommendation.text = "🔴 情绪监控已暂停"
    }
    
    private fun updateEmotionStatus() {
        // 模拟AI情绪识别结果（实际应用中应该调用真实的AI服务）
        val emotions = listOf("😊 专注", "🤔 思考中", "😅 轻松", "💪 积极", "🎯 集中")
        val focusLevels = (6..10).toList()
        val stressLevels = (1..4).toList()
        
        val currentEmotion = emotions.random()
        val focusLevel = focusLevels.random()
        val stressLevel = stressLevels.random()
        
        tvCurrentEmotion.text = "当前状态: $currentEmotion"
        tvFocusLevel.text = "🎯 专注度: $focusLevel/10"
        tvStressLevel.text = "😰 压力: $stressLevel/10"
        
        // 根据状态更新AI建议
        val suggestion = when {
            focusLevel >= 8 && stressLevel <= 3 -> "💡 学习状态极佳，继续保持！"
            focusLevel >= 6 && stressLevel <= 5 -> "✨ 状态良好，可以适当提高学习强度"
            focusLevel < 6 -> "⚡ 建议休息一下，做些放松运动"
            stressLevel > 5 -> "🌸 压力较大，建议深呼吸放松"
            else -> "📚 保持当前学习节奏"
        }
        tvAiSuggestion.text = suggestion
    }
    
    private fun updateAiRecommendation() {
        // 使用真实的AI推荐数据
        val learningStats = demoDataManager.getLearningStats()
        val subjectProgress = demoDataManager.getSubjectProgress()
        
        // 找出最需要提升的科目
        val weakestSubject = subjectProgress.minByOrNull { it.value }?.key ?: "数学"
        val strongestSubject = subjectProgress.maxByOrNull { it.value }?.key ?: "物理"
        
        val recommendations = listOf(
            "💡 建议加强${weakestSubject}学习，当前掌握度${String.format("%.0f", (subjectProgress[weakestSubject] ?: 0f) * 100)}%",
            "📚 你在${strongestSubject}方面表现出色，掌握度${String.format("%.0f", (subjectProgress[strongestSubject] ?: 0f) * 100)}%！",
            "🎯 本周目标：完成${learningStats["weekly_goal"]}小时学习，已完成${String.format("%.1f", (learningStats["this_week_time"] as Long) / (60 * 60 * 1000.0))}小时",
            "⚡ 连续学习${learningStats["current_streak"]}天，继续保持！",
            "🔍 平均成绩${String.format("%.1f", learningStats["average_score"] as Float)}分，继续努力！"
        )
        
        tvAiRecommendation.text = recommendations.random()
    }
    
    private fun updateLearningProgress() {
        val learningStats = demoDataManager.getLearningStats()
        val emotionData = demoDataManager.getEmotionData()
        
        // 更新情绪状态
        tvCurrentEmotion.text = "😊 当前状态: ${emotionData["current_emotion"]}"
        tvFocusLevel.text = "🎯 专注度: ${String.format("%.1f", emotionData["current_focus"])}/10"
        tvStressLevel.text = "😰 压力: ${String.format("%.1f", emotionData["current_stress"])}/10"
        
        // 更新课堂参与度
        val engagementProgress = ((emotionData["current_focus"] as Float) * 10).toInt()
        progressClassroomEngagement.progress = engagementProgress
        
        // 更新AI建议
        val completedRate = (learningStats["completed_contents"] as Int * 100) / (learningStats["total_contents"] as Int)
        tvAiSuggestion.text = "已完成 ${learningStats["completed_contents"]}/${learningStats["total_contents"]} 个内容 (${completedRate}%)"
    }
    
    private fun animateProgressBar() {
        val currentProgress = progressClassroomEngagement.progress
        val targetProgress = (75..95).random()
        
        val animator = ObjectAnimator.ofInt(progressClassroomEngagement, "progress", currentProgress, targetProgress)
        animator.duration = 1500
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }
    
    private fun animateCardClick(card: CardView, action: () -> Unit) {
        // 点击动画效果
        val scaleDown = ObjectAnimator.ofFloat(card, "scaleX", 1f, 0.95f).apply {
            duration = 100
        }
        val scaleUp = ObjectAnimator.ofFloat(card, "scaleX", 0.95f, 1f).apply {
            duration = 100
        }
        
        scaleDown.addUpdateListener { 
            card.scaleY = card.scaleX
        }
        scaleUp.addUpdateListener { 
            card.scaleY = card.scaleX
        }
        
        scaleDown.start()
        scaleDown.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                scaleUp.start()
                scaleUp.addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        action()
                    }
                })
            }
        })
    }
    
    /**
     * 根据学生年级设置对应的课程内容
     */
    private fun setupGradeSpecificContent(userGrade: String) {
        when {
            userGrade.contains("七年级") || userGrade.contains("初一") -> {
                tvCurrentCourse.text = "七年级数学 • 有理数运算"
                tvCurrentProgressTopic.text = "有理数运算 • 整式基础"
                tvNextGoal.text = "🎯 下一个目标: 整式加减"
            }
            userGrade.contains("八年级") || userGrade.contains("初二") -> {
                tvCurrentCourse.text = "八年级数学 • 三角形性质"
                tvCurrentProgressTopic.text = "全等三角形 • 轴对称"
                tvNextGoal.text = "🎯 下一个目标: 勾股定理"
            }
            userGrade.contains("九年级") || userGrade.contains("初三") -> {
                tvCurrentCourse.text = "九年级数学 • 二次函数"
                tvCurrentProgressTopic.text = "一元二次方程 • 函数图像"
                tvNextGoal.text = "🎯 下一个目标: 圆的性质"
            }
            userGrade.contains("高一") -> {
                tvCurrentCourse.text = "高一数学 • 函数概念"
                tvCurrentProgressTopic.text = "集合运算 • 基本初等函数"
                tvNextGoal.text = "🎯 下一个目标: 函数应用"
            }
            userGrade.contains("高二") -> {
                tvCurrentCourse.text = "高二数学 • 三角函数"
                tvCurrentProgressTopic.text = "平面向量 • 三角恒等变换"
                tvNextGoal.text = "🎯 下一个目标: 解三角形"
            }
            userGrade.contains("高三") -> {
                tvCurrentCourse.text = "高三数学 • 导数应用"
                tvCurrentProgressTopic.text = "导数概念 • 函数单调性"
                tvNextGoal.text = "🎯 下一个目标: 概率统计"
            }
            userGrade.contains("教师") -> {
                tvCurrentCourse.text = "教师课程 • 多年级教学"
                tvCurrentProgressTopic.text = "课程设计 • 学生管理"
                tvNextGoal.text = "🎯 下一个目标: AI辅助教学"
            }
            else -> {
                // 默认七年级
                tvCurrentCourse.text = "七年级数学 • 有理数运算"
                tvCurrentProgressTopic.text = "有理数运算 • 整式基础"
                tvNextGoal.text = "🎯 下一个目标: 整式加减"
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(emotionUpdateRunnable)
    }
}