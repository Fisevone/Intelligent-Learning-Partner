package com.example.educationapp.ui.ai

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.educationapp.R
import com.example.educationapp.ai.AILearningAssistant
import com.example.educationapp.data.EducationDatabase
import com.example.educationapp.data.LearningRecord
import com.example.educationapp.databinding.ActivityAiChatBinding
// import com.example.educationapp.ui.adapter.AIChatAdapter // 已移除
import com.example.educationapp.utils.PreferenceManager
import kotlinx.coroutines.launch

class AIChatActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAiChatBinding
    private lateinit var aiAssistant: AILearningAssistant
    // private lateinit var chatAdapter: AIChatAdapter // 已移除
    private lateinit var preferenceManager: PreferenceManager
    
    private val chatMessages = mutableListOf<ChatMessage>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupAI()
        setupRecyclerView()
        setupClickListeners()
        
        // 发送欢迎消息
        sendWelcomeMessage()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "AI学习助手"
    }
    
    private fun setupAI() {
        aiAssistant = AILearningAssistant()
        preferenceManager = PreferenceManager(this)
    }
    
    private fun setupRecyclerView() {
        // chatAdapter = AIChatAdapter() // 已移除
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(this@AIChatActivity)
            // adapter = chatAdapter // 已移除
        }
    }
    
    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            sendMessage()
        }
        
        binding.etMessage.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
        
        // 快捷问题按钮
        binding.btnQuickQuestion1.setOnClickListener {
            sendQuickQuestion("帮我分析一下最近的学习情况")
        }
        
        binding.btnQuickQuestion2.setOnClickListener {
            sendQuickQuestion("推荐一些适合我的学习资源")
        }
        
        binding.btnQuickQuestion3.setOnClickListener {
            sendQuickQuestion("制定一个学习计划")
        }
    }
    
    private fun sendWelcomeMessage() {
        val welcomeMessage = ChatMessage(
            text = "你好！我是你的AI学习助手。我可以帮你分析学习情况、制定学习计划、解答问题等。有什么需要帮助的吗？",
            isFromUser = false,
            timestamp = System.currentTimeMillis(),
            messageType = "welcome"
        )
        addMessage(welcomeMessage)
    }
    
    private fun sendMessage() {
        val messageText = binding.etMessage.text.toString().trim()
        if (messageText.isEmpty()) return
        
        // 添加用户消息
        val userMessage = ChatMessage(
            text = messageText,
            isFromUser = true,
            timestamp = System.currentTimeMillis(),
            messageType = "text"
        )
        addMessage(userMessage)
        
        // 清空输入框
        binding.etMessage.text?.clear()
        
        // 显示AI正在思考
        showTypingIndicator()
        
        // 处理AI回复
        processAIResponse(messageText)
    }
    
    private fun sendQuickQuestion(question: String) {
        binding.etMessage.setText(question)
        sendMessage()
    }
    
    private fun processAIResponse(userMessage: String) {
        lifecycleScope.launch {
            try {
                val user = preferenceManager.getUser()
                if (user != null) {
                    val database = EducationDatabase.getDatabase(this@AIChatActivity)
                    val learningRecordsFlow = database.learningRecordDao().getLearningRecordsByUser(user.id)
                    
                    // 获取学习记录数据
                    learningRecordsFlow.collect { records ->
                        val result = when {
                            userMessage.contains("学习情况") || userMessage.contains("分析") -> {
                                aiAssistant.generateLearningAnalysisResponse(user, records)
                            }
                            userMessage.contains("推荐") || userMessage.contains("资源") -> {
                                aiAssistant.generateRecommendationResponse(user, records)
                            }
                            userMessage.contains("计划") || userMessage.contains("安排") -> {
                                aiAssistant.generatePlanResponse(user, records)
                            }
                            userMessage.contains("错题") || userMessage.contains("错误") -> {
                                aiAssistant.generateMistakeAnalysisResponse(records)
                            }
                            userMessage.contains("情绪") || userMessage.contains("心情") -> {
                                aiAssistant.generateMoodAnalysisResponse(user, records)
                            }
                            else -> {
                                aiAssistant.processChatMessage(userMessage, user, records)
                            }
                        }
                        
                        // 移除打字指示器
                        hideTypingIndicator()
                        
                        // 处理结果
                        if (result.isSuccess) {
                            val aiMessage = ChatMessage(
                                text = result.getOrNull() ?: "抱歉，我无法生成回复。",
                                isFromUser = false,
                                timestamp = System.currentTimeMillis(),
                                messageType = "ai_response"
                            )
                            addMessage(aiMessage)
                        } else {
                            val errorMessage = ChatMessage(
                                text = "抱歉，我遇到了一些问题：${result.exceptionOrNull()?.message ?: "网络连接失败"}",
                                isFromUser = false,
                                timestamp = System.currentTimeMillis(),
                                messageType = "error"
                            )
                            addMessage(errorMessage)
                        }
                    }
                } else {
                    // 用户未登录，使用通用回复
                    val result = aiAssistant.processChatMessage(userMessage)
                    hideTypingIndicator()
                    
                    if (result.isSuccess) {
                        val aiMessage = ChatMessage(
                            text = result.getOrNull() ?: "抱歉，我无法生成回复。",
                            isFromUser = false,
                            timestamp = System.currentTimeMillis(),
                            messageType = "ai_response"
                        )
                        addMessage(aiMessage)
                    } else {
                        val errorMessage = ChatMessage(
                            text = "抱歉，我遇到了一些问题：${result.exceptionOrNull()?.message ?: "网络连接失败"}",
                            isFromUser = false,
                            timestamp = System.currentTimeMillis(),
                            messageType = "error"
                        )
                        addMessage(errorMessage)
                    }
                }
            } catch (e: Exception) {
                hideTypingIndicator()
                val errorMessage = ChatMessage(
                    text = "抱歉，我遇到了一些问题。请稍后再试。",
                    isFromUser = false,
                    timestamp = System.currentTimeMillis(),
                    messageType = "error"
                )
                addMessage(errorMessage)
            }
        }
    }
    
    
    private fun addMessage(message: ChatMessage) {
        chatMessages.add(message)
        // chatAdapter.submitList(chatMessages.toList()) // 已移除
        binding.rvChat.scrollToPosition(chatMessages.size - 1)
        
        // 添加消息动画效果
        animateMessageAppearance()
    }
    
    private fun animateMessageAppearance() {
        val lastPosition = chatMessages.size - 1
        if (lastPosition >= 0) {
            val viewHolder = binding.rvChat.findViewHolderForAdapterPosition(lastPosition)
            viewHolder?.itemView?.let { view ->
                view.alpha = 0f
                view.scaleX = 0.8f
                view.scaleY = 0.8f
                
                val animatorSet = AnimatorSet()
                val alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
                val scaleXAnimator = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1f)
                val scaleYAnimator = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1f)
                
                animatorSet.playTogether(alphaAnimator, scaleXAnimator, scaleYAnimator)
                animatorSet.duration = 300
                animatorSet.interpolator = AccelerateDecelerateInterpolator()
                animatorSet.start()
            }
        }
    }
    
    private fun showTypingIndicator() {
        binding.typingIndicator.visibility = View.VISIBLE
        binding.rvChat.scrollToPosition(chatMessages.size)
        
        // 添加打字指示器动画
        animateTypingIndicator()
    }
    
    private fun hideTypingIndicator() {
        binding.typingIndicator.visibility = View.GONE
    }
    
    private fun animateTypingIndicator() {
        val animatorSet = AnimatorSet()
        val alphaAnimator = ObjectAnimator.ofFloat(binding.typingIndicator, "alpha", 0f, 1f)
        val scaleXAnimator = ObjectAnimator.ofFloat(binding.typingIndicator, "scaleX", 0.8f, 1f)
        val scaleYAnimator = ObjectAnimator.ofFloat(binding.typingIndicator, "scaleY", 0.8f, 1f)
        
        animatorSet.playTogether(alphaAnimator, scaleXAnimator, scaleYAnimator)
        animatorSet.duration = 200
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.start()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val messageType: String
)
