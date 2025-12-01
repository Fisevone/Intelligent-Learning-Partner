package com.example.educationapp.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.educationapp.R
import com.example.educationapp.databinding.FragmentAiTeacherBinding
import com.example.educationapp.utils.PreferenceManager
import com.example.educationapp.data.EducationDatabase
import com.example.educationapp.data.User
import com.example.educationapp.data.UserType
import com.example.educationapp.ai.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class AITeacherFragment : Fragment() {
    private var _binding: FragmentAiTeacherBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var virtualTeacher: AIVirtualTeacher
    private lateinit var knowledgeGraphBuilder: AIKnowledgeGraphBuilder
    
    // AI老师状态
    private var currentPersonality = "鼓励型"
    private var currentSession: AIVirtualTeacher.InteractiveSession? = null
    private var isSessionActive = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiTeacherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeComponents()
        setupPersonalitySelection()
        setupAITeacherInteraction()
        setupKnowledgeExploration()
    }
    
    private fun initializeComponents() {
        preferenceManager = PreferenceManager(requireContext())
        virtualTeacher = AIVirtualTeacher()
        knowledgeGraphBuilder = AIKnowledgeGraphBuilder()
    }
    
    private fun setupPersonalitySelection() {
        binding.btnChangePersonality.setOnClickListener {
            showPersonalitySelector()
        }
        
        // 默认显示当前个性
        updatePersonalityDisplay()
    }
    
    private fun showPersonalitySelector() {
        val personalities = listOf("鼓励型", "挑战型", "幽默型", "严格型", "创意型")
        val descriptions = mapOf(
            "鼓励型" to "耐心温和，多鼓励少批评",
            "挑战型" to "设置挑战，推动突破极限", 
            "幽默型" to "寓教于乐，用幽默化解压力",
            "严格型" to "严谨纪律，注重基础细节",
            "创意型" to "启发思维，鼓励创新方案"
        )
        
        // 这里应该显示一个选择对话框，简化处理随机选择
        currentPersonality = personalities.random()
        updatePersonalityDisplay()
        
        lifecycleScope.launch {
            try {
                val response = virtualTeacher.switchPersonality(currentPersonality, getCurrentUser())
                binding.tvAiTeacherMessage.text = response.welcomeMessage
                binding.tvPersonalityDescription.text = descriptions[currentPersonality] ?: ""
                Toast.makeText(context, "AI老师已切换为${currentPersonality}模式", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                binding.tvAiTeacherMessage.text = "AI老师正在准备中..."
            }
        }
    }
    
    private fun updatePersonalityDisplay() {
        binding.tvCurrentPersonality.text = "当前AI老师: $currentPersonality"
        
        // 根据个性类型设置图标
        val personalityIcon = when (currentPersonality) {
            "鼓励型" -> "🌟"
            "挑战型" -> "⚡"
            "幽默型" -> "😄"
            "严格型" -> "📚"
            "创意型" -> "💡"
            else -> "🤖"
        }
        binding.tvPersonalityIcon.text = personalityIcon
    }
    
    private fun setupAITeacherInteraction() {
        binding.btnStartSession.setOnClickListener {
            if (isSessionActive) {
                endCurrentSession()
            } else {
                startNewSession()
            }
        }
        
        binding.btnSendMessage.setOnClickListener {
            sendMessageToAI()
        }
        
        binding.btnGetHint.setOnClickListener {
            requestHintFromAI()
        }
        
        binding.btnAskQuestion.setOnClickListener {
            askQuestionToAI()
        }
    }
    
    private fun startNewSession() {
        lifecycleScope.launch {
            try {
                binding.tvSessionStatus.text = "🔄 启动AI老师会话中..."
                
                val sessionData = virtualTeacher.startInteractiveSession(
                    subject = "高等数学",
                    difficulty = "中等",
                    studentLevel = "大二",
                    personality = currentPersonality
                )
                
                currentSession = sessionData
                isSessionActive = true
                
                binding.tvAiTeacherMessage.text = sessionData.openingMessage
                binding.progressEngagement.progress = (sessionData.engagementLevel * 100).toInt()
                binding.tvSessionStatus.text = "✅ 会话进行中"
                binding.btnStartSession.text = "结束会话"
                
                // 显示教学策略
                binding.tvTeachingStrategy.text = "📋 教学策略: ${sessionData.teachingStrategy}"
                
                // 显示上下文提示
                binding.tvContextualHints.text = "💡 学习提示:\n${sessionData.contextualHints.joinToString("\n") { "• $it" }}"
                
                // 启用交互按钮
                enableInteractionButtons(true)
                
                // 开始实时互动
                startRealTimeInteraction(sessionData)
                
            } catch (e: Exception) {
                binding.tvSessionStatus.text = "❌ 启动失败: ${e.message}"
                Toast.makeText(context, "启动AI老师会话失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun endCurrentSession() {
        isSessionActive = false
        currentSession = null
        
        binding.tvSessionStatus.text = "⏸️ 会话已结束"
        binding.btnStartSession.text = "开始会话"
        binding.tvAiTeacherMessage.text = "感谢与我的互动！期待下次见面 😊"
        
        enableInteractionButtons(false)
    }
    
    private fun enableInteractionButtons(enabled: Boolean) {
        binding.btnSendMessage.isEnabled = enabled
        binding.btnGetHint.isEnabled = enabled
        binding.btnAskQuestion.isEnabled = enabled
        binding.etUserInput.isEnabled = enabled
    }
    
    private fun startRealTimeInteraction(sessionData: AIVirtualTeacher.InteractiveSession) {
        lifecycleScope.launch {
            while (isSessionActive) {
                delay(45000) // 每45秒进行一次主动互动
                
                if (!isSessionActive) break
                
                try {
                    val interaction = virtualTeacher.generateContextualInteraction(
                        sessionData,
                        "专注", // 当前课堂氛围
                        0.8f    // 实时参与度
                    )
                    
                    // 更新AI消息
                    binding.tvAiTeacherMessage.text = interaction.message
                    
                    // 显示互动类型
                    val typeIcon = when (interaction.interactionType) {
                        "question" -> "❓"
                        "encouragement" -> "🌟"
                        "challenge" -> "⚡"
                        "hint" -> "💡"
                        else -> "💬"
                    }
                    binding.tvInteractionType.text = "$typeIcon ${interaction.interactionType}"
                    
                    // 如果需要回应，显示建议回复
                    if (interaction.needsResponse) {
                        binding.tvSuggestedResponses.text = "建议回复:\n${interaction.suggestedResponses.joinToString("\n") { "• $it" }}"
                        binding.tvSuggestedResponses.visibility = View.VISIBLE
                    } else {
                        binding.tvSuggestedResponses.visibility = View.GONE
                    }
                    
                } catch (e: Exception) {
                    // 静默处理错误，继续下一次循环
                }
            }
        }
    }
    
    private fun sendMessageToAI() {
        val userInput = binding.etUserInput.text.toString().trim()
        if (userInput.isEmpty()) {
            Toast.makeText(context, "请输入消息", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                binding.tvUserLastMessage.text = "你: $userInput"
                binding.etUserInput.text.clear()
                
                currentSession?.let { session ->
                    val interaction = virtualTeacher.generateContextualInteraction(
                        session,
                        "专注",
                        0.8f
                    )
                    
                    binding.tvAiTeacherMessage.text = interaction.message
                }
                
            } catch (e: Exception) {
                Toast.makeText(context, "发送失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun requestHintFromAI() {
        lifecycleScope.launch {
            try {
                binding.tvAiTeacherMessage.text = "💡 让我给你一个提示..."
                
                delay(1000) // 模拟思考时间
                
                val hints = listOf(
                    "记住，极限的本质是无限接近但不一定等于",
                    "试着从图形的角度来理解这个概念",
                    "可以尝试用具体的数值来验证你的推理",
                    "想想这个问题与之前学过的函数性质有什么联系"
                )
                
                binding.tvAiTeacherMessage.text = hints.random()
                
            } catch (e: Exception) {
                binding.tvAiTeacherMessage.text = "抱歉，暂时无法提供提示"
            }
        }
    }
    
    private fun askQuestionToAI() {
        lifecycleScope.launch {
            try {
                binding.tvAiTeacherMessage.text = "🤔 我来为你出一道题..."
                
                delay(1500) // 模拟生成时间
                
                val questions = listOf(
                    "计算极限: lim(x→0) sin(x)/x 的值",
                    "判断函数 f(x) = x² 在 x=2 处是否连续",
                    "求函数 y = 3x² + 2x - 1 的导数",
                    "证明: lim(x→∞) (1 + 1/x)^x = e"
                )
                
                binding.tvAiTeacherMessage.text = "📝 练习题:\n${questions.random()}\n\n思考一下，需要提示吗？"
                
            } catch (e: Exception) {
                binding.tvAiTeacherMessage.text = "题目生成失败，请稍后重试"
            }
        }
    }
    
    private fun setupKnowledgeExploration() {
        binding.btnExploreKnowledge.setOnClickListener {
            exploreCurrentTopic()
        }
    }
    
    private fun exploreCurrentTopic() {
        lifecycleScope.launch {
            try {
                binding.tvKnowledgeStatus.text = "🔍 AI正在深度分析知识点..."
                
                // 模拟知识节点
                val mockNode = AIKnowledgeGraphBuilder.KnowledgeNode(
                    id = "limit_001",
                    concept = "极限概念",
                    description = "函数在某点的极限值",
                    masteryLevel = 0.7f,
                    difficulty = "中等",
                    prerequisites = listOf("函数基础"),
                    applications = listOf("连续性", "导数"),
                    estimatedLearningTime = 60,
                    importance = 0.9f
                )
                
                val exploration = knowledgeGraphBuilder.exploreNodeInDepth(mockNode, getCurrentUser())
                
                binding.tvKnowledgeExploration.text = """
                    🎯 深度解析: ${mockNode.concept}
                    
                    📚 详细说明:
                    ${exploration.detailedExplanation}
                    
                    🔗 相关概念:
                    ${exploration.relatedConcepts.joinToString(", ")}
                    
                    💡 学习技巧:
                    ${exploration.learningTips}
                    
                    ⚠️ 常见误区:
                    ${exploration.commonMistakes.joinToString("\n") { "• $it" }}
                """.trimIndent()
                
                binding.tvKnowledgeStatus.text = "✅ 知识探索完成"
                
            } catch (e: Exception) {
                binding.tvKnowledgeStatus.text = "❌ 探索失败: ${e.message}"
            }
        }
    }
    
    private fun getCurrentUser(): User {
        val savedUser = preferenceManager.getUser()
        return savedUser ?: User(
            id = 1,
            username = preferenceManager.getUserName(),
            email = "student@example.com",
            password = "",
            name = preferenceManager.getUserName(),
            userType = UserType.STUDENT,
            grade = "大二",
            interests = "数学,物理"
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        isSessionActive = false
        _binding = null
    }
}






