package com.example.educationapp.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.educationapp.databinding.FragmentCollaborationBinding
import com.example.educationapp.utils.PreferenceManager
import com.example.educationapp.ai.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class CollaborationFragment : Fragment() {
    private var _binding: FragmentCollaborationBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var smartGroupMatcher: AISmartGroupMatcher
    private lateinit var emotionRecognizer: AIEmotionRecognizer
    
    // 协作状态
    private var currentGroups = listOf<AISmartGroupMatcher.StudentGroup>()
    private var collaborationScore = 0.85f
    private var isGroupingActive = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCollaborationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeComponents()
        setupSmartGrouping()
        setupCollaborationMonitoring()
        setupGroupInteractions()
        loadInitialData()
    }
    
    private fun initializeComponents() {
        preferenceManager = PreferenceManager(requireContext())
        smartGroupMatcher = AISmartGroupMatcher()
        emotionRecognizer = AIEmotionRecognizer()
    }
    
    private fun setupSmartGrouping() {
        binding.btnSmartGrouping.setOnClickListener {
            performIntelligentGrouping()
        }
        
        binding.btnManualAdjust.setOnClickListener {
            showManualGroupingOptions()
        }
    }
    
    private fun setupCollaborationMonitoring() {
        binding.switchCollaborationMonitoring.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                startCollaborationTracking()
            } else {
                stopCollaborationTracking()
            }
        }
    }
    
    private fun setupGroupInteractions() {
        binding.btnStartDiscussion.setOnClickListener {
            startGroupDiscussion()
        }
        
        binding.btnGroupPoll.setOnClickListener {
            startGroupPoll()
        }
        
        binding.btnPeerReview.setOnClickListener {
            startPeerReview()
        }
    }
    
    private fun loadInitialData() {
        // 显示当前分组信息
        displayCurrentGrouping()
        
        // 启动协作监控
        binding.switchCollaborationMonitoring.isChecked = true
        startCollaborationTracking()
    }
    
    private fun performIntelligentGrouping() {
        lifecycleScope.launch {
            try {
                binding.tvGroupingStatus.text = "🤖 AI正在分析最佳分组方案..."
                binding.btnSmartGrouping.isEnabled = false
                
                val allStudents = getAllClassroomStudents()
                val groupingResult = smartGroupMatcher.performIntelligentGrouping(
                    students = allStudents,
                    groupSize = 4,
                    criteria = listOf("知识互补", "性格平衡", "协作历史")
                )
                
                currentGroups = groupingResult.groups
                displayGroupingResults(groupingResult)
                
                // 开始实时协作追踪
                isGroupingActive = true
                startRealTimeCollaborationTracking()
                
            } catch (e: Exception) {
                binding.tvGroupingStatus.text = "分组匹配失败，请重试"
                Toast.makeText(context, "智能分组失败: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnSmartGrouping.isEnabled = true
            }
        }
    }
    
    private fun getAllClassroomStudents(): List<AISmartGroupMatcher.StudentProfile> {
        // 模拟获取课堂上所有学生的信息
        return listOf(
            AISmartGroupMatcher.StudentProfile("张三", "数学强", "外向", listOf("李四")),
            AISmartGroupMatcher.StudentProfile("李四", "物理强", "内向", listOf("张三")),
            AISmartGroupMatcher.StudentProfile("王五", "语言强", "外向", listOf("赵六")),
            AISmartGroupMatcher.StudentProfile("赵六", "逻辑强", "中性", listOf("王五")),
            AISmartGroupMatcher.StudentProfile("钱七", "创意强", "外向", emptyList()),
            AISmartGroupMatcher.StudentProfile("孙八", "分析强", "内向", listOf("钱七")),
            AISmartGroupMatcher.StudentProfile("周九", "综合平衡", "中性", emptyList()),
            AISmartGroupMatcher.StudentProfile("吴十", "表达强", "外向", listOf("周九"))
        )
    }
    
    private fun displayGroupingResults(result: AISmartGroupMatcher.GroupingResult) {
        binding.tvGroupingStatus.text = "✅ 智能分组完成！匹配度: ${String.format("%.1f", result.matchingScore * 100)}%"
        
        val groupsText = result.groups.mapIndexed { index, group ->
            val memberInfo = group.members.joinToString(", ") { member ->
                "${member.name}(${group.recommendedRole[member.name] ?: "成员"})"
            }
            "第${index + 1}组: $memberInfo\n" +
            "  • 能力均衡: ${String.format("%.1f", group.strengthsBalance * 100)}%\n" +
            "  • 性格和谐: ${String.format("%.1f", group.personalityHarmony * 100)}%\n" +
            "  • 协作潜力: ${String.format("%.1f", group.collaborationPotential * 100)}%"
        }.joinToString("\n\n")
        
        binding.tvGroupingResults.text = groupsText
        binding.tvGroupingReason.text = "📋 分组理由: ${result.reasoning}"
        
        // 显示预期成果
        binding.tvExpectedOutcomes.text = "🎯 预期成果:\n${result.expectedOutcomes.joinToString("\n") { "• $it" }}"
        
        // 显示潜在挑战
        binding.tvPotentialChallenges.text = "⚠️ 潜在挑战:\n${result.potentialChallenges.joinToString("\n") { "• $it" }}"
    }
    
    private fun displayCurrentGrouping() {
        // 显示默认分组信息
        binding.tvGroupingStatus.text = "📊 当前分组状态"
        binding.tvGroupingResults.text = "等待智能分组..."
        binding.tvGroupingReason.text = "💡 点击上方按钮开始AI智能分组"
    }
    
    private fun startCollaborationTracking() {
        _binding?.tvMonitoringStatus?.text = "👥 协作监控已启动"
        
        lifecycleScope.launch {
            while (_binding?.switchCollaborationMonitoring?.isChecked == true) {
                try {
                    updateCollaborationMetrics()
                    delay(10000) // 每10秒更新一次
                } catch (e: Exception) {
                    // 继续监控，如果binding为null则退出循环
                    if (_binding == null) break
                }
            }
        }
    }
    
    private fun stopCollaborationTracking() {
        _binding?.tvMonitoringStatus?.text = "⏸️ 协作监控已停止"
    }
    
    private fun startRealTimeCollaborationTracking() {
        lifecycleScope.launch {
            while (isGroupingActive && _binding?.switchCollaborationMonitoring?.isChecked == true) {
                try {
                    // 检查binding是否仍然有效
                    val currentBinding = _binding ?: break
                    
                    currentGroups.forEachIndexed { index, group ->
                        val score = smartGroupMatcher.calculateCollaborationScore(group)
                        
                        // 更新UI显示
                        when (index) {
                            0 -> {
                                currentBinding.progressGroup1.progress = (score * 100).toInt()
                                currentBinding.tvGroup1Score.text = "${String.format("%.1f", score * 100)}/100"
                            }
                            1 -> {
                                currentBinding.progressGroup2.progress = (score * 100).toInt()
                                currentBinding.tvGroup2Score.text = "${String.format("%.1f", score * 100)}/100"
                            }
                        }
                    }
                    
                    // 分析整体协作质量
                    if (currentGroups.isNotEmpty()) {
                        analyzeOverallCollaboration()
                    }
                    
                } catch (e: Exception) {
                    // 如果binding为null则退出循环
                    if (_binding == null) break
                }
                
                delay(15000) // 每15秒更新一次
            }
        }
    }
    
    private fun updateCollaborationMetrics() {
        // 检查binding是否有效
        val currentBinding = _binding ?: return
        
        // 模拟协作指标更新
        val communicationLevel = (70..95).random()
        val participationBalance = (60..90).random()
        val taskProgress = (50..85).random()
        
        currentBinding.tvCommunicationLevel.text = "💬 沟通活跃度: $communicationLevel%"
        currentBinding.tvParticipationBalance.text = "⚖️ 参与均衡度: $participationBalance%"
        currentBinding.tvTaskProgress.text = "📈 任务进度: $taskProgress%"
        
        // 更新整体协作评分
        collaborationScore = (communicationLevel + participationBalance + taskProgress) / 300.0f
        currentBinding.progressCollaboration.progress = (collaborationScore * 100).toInt()
        currentBinding.tvCollaborationScore.text = "协作总评: ${String.format("%.1f", collaborationScore * 100)}/100"
    }
    
    private fun analyzeOverallCollaboration() {
        lifecycleScope.launch {
            try {
                val currentBinding = _binding ?: return@launch
                
                if (currentGroups.isNotEmpty()) {
                    val analysis = smartGroupMatcher.analyzeCollaborationQuality(currentGroups.first())
                    
                    currentBinding.tvCollaborationAnalysis.text = """
                        📊 协作质量分析:
                        • 沟通质量: ${String.format("%.1f", analysis.communicationQuality * 100)}%
                        • 任务分配: ${String.format("%.1f", analysis.taskDistribution * 100)}%
                        • 冲突水平: ${String.format("%.1f", analysis.conflictLevel * 100)}%
                        • 整体效果: ${String.format("%.1f", analysis.overallEffectiveness * 100)}%
                        
                        💡 改进建议:
                        ${analysis.improvementSuggestions.joinToString("\n") { "• $it" }}
                    """.trimIndent()
                }
            } catch (e: Exception) {
                _binding?.tvCollaborationAnalysis?.text = "分析暂时不可用"
            }
        }
    }
    
    private fun showManualGroupingOptions() {
        Toast.makeText(context, "手动调整功能开发中...", Toast.LENGTH_SHORT).show()
        // 这里可以实现手动调整分组的功能
    }
    
    private fun startGroupDiscussion() {
        lifecycleScope.launch {
            try {
                val currentBinding = _binding ?: return@launch
                
                currentBinding.tvDiscussionStatus.text = "🗣️ 启动小组讨论..."
                
                val topics = listOf(
                    "极限理论在实际生活中的应用",
                    "如何理解函数的连续性概念",
                    "导数的几何意义是什么",
                    "微积分基本定理的证明思路"
                )
                
                val selectedTopic = topics.random()
                currentBinding.tvDiscussionTopic.text = "📝 讨论主题: $selectedTopic"
                
                currentBinding.tvDiscussionStatus.text = "✅ 讨论进行中"
                currentBinding.tvDiscussionProgress.text = "🕒 已进行: 0分钟"
                
                // 模拟讨论进度
                var minutes = 0
                while (minutes < 10 && _binding != null) {
                    delay(6000) // 每6秒代表1分钟
                    minutes++
                    _binding?.tvDiscussionProgress?.text = "🕒 已进行: ${minutes}分钟"
                }
                
                _binding?.tvDiscussionStatus?.text = "⏰ 讨论时间到"
                
            } catch (e: Exception) {
                _binding?.tvDiscussionStatus?.text = "讨论启动失败"
            }
        }
    }
    
    private fun startGroupPoll() {
        lifecycleScope.launch {
            try {
                val currentBinding = _binding ?: return@launch
                
                currentBinding.tvPollStatus.text = "📊 发起小组投票..."
                
                val questions = listOf(
                    "你认为当前学习进度如何？",
                    "哪个知识点最需要加强？",
                    "小组协作效果怎么样？",
                    "是否需要调整学习计划？"
                )
                
                val options = listOf(
                    listOf("很好", "一般", "需要改进"),
                    listOf("极限理论", "连续性", "导数概念"),
                    listOf("非常好", "还可以", "有待提升"),
                    listOf("是", "否", "部分调整")
                )
                
                val questionIndex = (questions.indices).random()
                currentBinding.tvPollQuestion.text = "❓ ${questions[questionIndex]}"
                currentBinding.tvPollOptions.text = "选项: ${options[questionIndex].joinToString(" | ")}"
                
                currentBinding.tvPollStatus.text = "✅ 投票进行中"
                
                // 模拟投票结果
                delay(5000)
                val results = options[questionIndex].map { "${it}: ${(10..30).random()}票" }
                _binding?.tvPollResults?.text = "📈 投票结果:\n${results.joinToString("\n")}"
                
            } catch (e: Exception) {
                _binding?.tvPollStatus?.text = "投票启动失败"
            }
        }
    }
    
    private fun startPeerReview() {
        lifecycleScope.launch {
            try {
                val currentBinding = _binding ?: return@launch
                
                currentBinding.tvReviewStatus.text = "👥 启动同伴评价..."
                
                val criteria = listOf(
                    "参与积极性",
                    "知识贡献度", 
                    "团队合作精神",
                    "解决问题能力"
                )
                
                currentBinding.tvReviewCriteria.text = "📋 评价维度:\n${criteria.joinToString("\n") { "• $it" }}"
                
                delay(2000)
                _binding?.tvReviewStatus?.text = "✅ 评价系统已启动"
                
                // 模拟评价结果
                delay(8000)
                val sampleResults = """
                    📊 同伴评价结果:
                    • 张三: 参与度⭐⭐⭐⭐⭐ 合作⭐⭐⭐⭐
                    • 李四: 参与度⭐⭐⭐⭐ 合作⭐⭐⭐⭐⭐
                    • 王五: 参与度⭐⭐⭐⭐⭐ 合作⭐⭐⭐⭐
                    • 赵六: 参与度⭐⭐⭐ 合作⭐⭐⭐⭐⭐
                """.trimIndent()
                
                _binding?.tvReviewResults?.text = sampleResults
                
            } catch (e: Exception) {
                _binding?.tvReviewStatus?.text = "同伴评价启动失败"
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        isGroupingActive = false
        _binding = null
    }
}






