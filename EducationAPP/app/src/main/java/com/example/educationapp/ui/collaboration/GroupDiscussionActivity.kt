package com.example.educationapp.ui.collaboration

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.educationapp.databinding.ActivityGroupDiscussionBinding
import com.example.educationapp.ui.collaboration.adapter.MessageAdapter
import com.example.educationapp.ui.collaboration.data.DiscussionMessage
import com.example.educationapp.ui.collaboration.data.MessageType
import com.example.educationapp.utils.PreferenceManager
import com.example.educationapp.ai.ZhipuAIService
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * 🗣️ 小组讨论界面
 * 实现实时聊天、AI话题引导、参与度统计等功能
 */
class GroupDiscussionActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityGroupDiscussionBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var aiService: ZhipuAIService
    
    // 讨论数据
    private val messages = mutableListOf<DiscussionMessage>()
    private var currentTopic = "数学函数的应用"
    private var groupName = "第1组"
    private var myUserId = ""
    private var myUserName = ""
    private var participationCount = 0
    private var discussionStartTime = System.currentTimeMillis()
    
    // 模拟其他组员（包括AI成员）
    private val groupMembers = listOf("李四", "王五", "赵六", "张三")
    private val aiMemberName = "张三"  // AI成员使用真实姓名
    private val aiAssistantName = "AI助手"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupDiscussionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeComponents()
        setupUI()
        setupRecyclerView()
        setupClickListeners()
        startDiscussion()
    }
    
    private fun initializeComponents() {
        preferenceManager = PreferenceManager(this)
        aiService = ZhipuAIService()
        myUserId = preferenceManager.getUserId().toString()
        myUserName = preferenceManager.getUser()?.name ?: "我"
    }
    
    private fun setupUI() {
        binding.apply {
            // 设置标题
            setSupportActionBar(toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                title = "$groupName - 讨论：$currentTopic"
            }
            
            // 设置讨论信息
            tvDiscussionTopic.text = "📚 讨论主题：$currentTopic"
            tvParticipants.text = "👥 参与成员：${groupMembers.joinToString("、")}、$myUserName"
            tvParticipationCount.text = "🗣️ 我的发言：$participationCount 次"
            
            // 初始化输入框
            etMessage.hint = "输入你的观点..."
        }
    }
    
    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter(messages, myUserId)
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@GroupDiscussionActivity)
            adapter = messageAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.apply {
            // 发送消息
            btnSend.setOnClickListener {
                sendMessage()
            }
            
            // AI话题建议
            btnTopicSuggestion.setOnClickListener {
                generateTopicSuggestion()
            }
            
            // 智能总结
            btnSummary.setOnClickListener {
                generateDiscussionSummary()
            }
            
            // 返回按钮
            toolbar.setNavigationOnClickListener {
                finish()
            }
        }
    }
    
    private fun startDiscussion() {
        // 添加欢迎消息
        addSystemMessage("欢迎来到小组讨论！让我们一起探讨「$currentTopic」这个话题吧！")
        addSystemMessage("💡 张三已加入讨论，AI助手将为大家提供学习指导")
        
        // 模拟其他成员的消息
        simulateGroupActivity()
        
        // 启动AI主动讨论机制
        initiateAITopicDiscussion()
        
        // 定期更新统计信息
        updateStatistics()
    }
    
    private fun sendMessage() {
        val messageText = binding.etMessage.text.toString().trim()
        if (messageText.isEmpty()) {
            Toast.makeText(this, "请输入消息内容", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 创建消息
        val message = DiscussionMessage(
            id = UUID.randomUUID().toString(),
            senderId = myUserId,
            senderName = myUserName,
            content = messageText,
            timestamp = System.currentTimeMillis(),
            type = MessageType.USER_MESSAGE
        )
        
        // 添加到列表
        addMessage(message)
        
        // 清空输入框
        binding.etMessage.setText("")
        
        // 更新参与统计
        participationCount++
        binding.tvParticipationCount.text = "🗣️ 我的发言：$participationCount 次"
        
        // 触发智能AI回应和讨论引导
        lifecycleScope.launch {
            delay(2000 + (0..3000).random().toLong()) // 随机延迟
            generateIntelligentAIResponse(messageText)
        }
        
        // 触发AI成员参与讨论
        lifecycleScope.launch {
            delay(5000 + (0..8000).random().toLong()) // 稍长延迟，模拟思考时间
            generateAIMemberResponse(messageText)
        }
    }
    
    private fun addMessage(message: DiscussionMessage) {
        messages.add(message)
        messageAdapter.notifyItemInserted(messages.size - 1)
        binding.rvMessages.scrollToPosition(messages.size - 1)
    }
    
    private fun addSystemMessage(content: String) {
        val message = DiscussionMessage(
            id = UUID.randomUUID().toString(),
            senderId = "system",
            senderName = "系统",
            content = content,
            timestamp = System.currentTimeMillis(),
            type = MessageType.SYSTEM_MESSAGE
        )
        addMessage(message)
    }
    
    private fun simulateGroupActivity() {
        lifecycleScope.launch {
            // 模拟其他成员的发言
            val sampleMessages = listOf(
                "我觉得函数在实际生活中应用很广泛",
                "比如说，二次函数可以用来计算抛物线运动",
                "还有指数函数在复利计算中的应用",
                "对数函数在地震强度测量中也很重要",
                "我们可以举一些具体的例子来说明"
            )
            
            repeat(sampleMessages.size) { index ->
                delay((10000..30000).random().toLong()) // 随机间隔
                
                val member = groupMembers.random()
                val message = DiscussionMessage(
                    id = UUID.randomUUID().toString(),
                    senderId = member,
                    senderName = member,
                    content = sampleMessages[index],
                    timestamp = System.currentTimeMillis(),
                    type = MessageType.USER_MESSAGE
                )
                addMessage(message)
            }
        }
    }
    
    private fun generateTopicSuggestion() {
        binding.btnTopicSuggestion.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val prompt = """
                    作为教学助手，请为小组讨论「$currentTopic」提供3个深入的话题建议。
                    
                    当前讨论内容：
                    ${messages.takeLast(5).joinToString("\n") { "${it.senderName}: ${it.content}" }}
                    
                    请提供：
                    1. 具体的讨论问题
                    2. 实际应用场景
                    3. 思考角度建议
                    
                    格式要简洁，每个建议不超过30字。
                """.trimIndent()
                
                val user = preferenceManager.getUser() ?: return@launch
                val result = aiService.sendChatMessage(prompt, user)
                
                result.fold(
                    onSuccess = { response ->
                        addSystemMessage("💡 AI话题建议：\n$response")
                    },
                    onFailure = {
                        addSystemMessage("💡 建议话题：\n1. 函数图像的实际意义\n2. 生活中的函数关系\n3. 函数与科技的结合")
                    }
                )
            } catch (e: Exception) {
                addSystemMessage("💡 建议话题：\n1. 函数图像的实际意义\n2. 生活中的函数关系\n3. 函数与科技的结合")
            } finally {
                binding.btnTopicSuggestion.isEnabled = true
            }
        }
    }
    
    
    private fun generateDiscussionSummary() {
        binding.btnSummary.isEnabled = false
        
        lifecycleScope.launch {
            try {
                val discussionContent = messages
                    .filter { it.type == MessageType.USER_MESSAGE }
                    .takeLast(10)
                    .joinToString("\n") { "${it.senderName}: ${it.content}" }
                
                val prompt = """
                    请为这次小组讨论做一个简洁的总结：
                    
                    讨论主题：$currentTopic
                    讨论内容：
                    $discussionContent
                    
                    请提供：
                    1. 主要观点总结
                    2. 讨论亮点
                    3. 待深入的问题
                    
                    总结要简洁明了，不超过200字。
                """.trimIndent()
                
                val user = preferenceManager.getUser() ?: return@launch
                val result = aiService.sendChatMessage(prompt, user)
                
                result.fold(
                    onSuccess = { response ->
                        addSystemMessage("📋 讨论总结：\n$response")
                    },
                    onFailure = {
                        addSystemMessage("📋 讨论总结：\n大家积极参与了关于「$currentTopic」的讨论，提出了很多有价值的观点和实际应用场景。")
                    }
                )
            } catch (e: Exception) {
                addSystemMessage("📋 讨论总结：\n大家积极参与了关于「$currentTopic」的讨论，提出了很多有价值的观点和实际应用场景。")
            } finally {
                binding.btnSummary.isEnabled = true
            }
        }
    }
    
    private fun updateStatistics() {
        lifecycleScope.launch {
            while (true) {
                delay(30000) // 每30秒更新一次
                
                val duration = (System.currentTimeMillis() - discussionStartTime) / 60000 // 分钟
                binding.tvDiscussionTime.text = "⏱️ 讨论时长：${duration}分钟"
                
                // 更新活跃度
                val totalMessages = messages.count { it.type == MessageType.USER_MESSAGE }
                val myMessages = messages.count { it.senderId == myUserId }
                val activityRate = if (totalMessages > 0) (myMessages * 100 / totalMessages) else 0
                
                binding.tvActivityRate.text = "📊 参与度：$activityRate%"
            }
        }
    }
    
    /**
     * 🤖 智能AI回应 - 基于用户输入生成针对性回应
     */
    private suspend fun generateIntelligentAIResponse(userMessage: String) {
        try {
            val recentContext = messages.takeLast(5).joinToString("\n") { 
                "${it.senderName}: ${it.content}" 
            }
            
            val prompt = """
                你是一个专业的教学AI助手，正在参与关于「$currentTopic」的小组讨论。
                
                用户刚刚说："$userMessage"
                
                最近的讨论内容：
                $recentContext
                
                请作为AI助手，针对用户的发言提供一个智能的回应。要求：
                1. 对用户观点给予积极反馈
                2. 提出启发性问题引导深入思考
                3. 补充相关知识点或实际应用
                4. 语气友好，像学习伙伴一样
                5. 回应控制在50字以内
                
                不要只是简单的赞同，要有建设性的内容。
            """.trimIndent()
            
            val user = preferenceManager.getUser() ?: return
            val result = aiService.sendChatMessage(prompt, user)
            
            result.fold(
                onSuccess = { response ->
                    val aiMessage = DiscussionMessage(
                        id = UUID.randomUUID().toString(),
                        senderId = "ai_assistant",
                        senderName = aiAssistantName,
                        content = response.trim(),
                        timestamp = System.currentTimeMillis(),
                        type = MessageType.AI_MESSAGE
                    )
                    addMessage(aiMessage)
                },
                onFailure = {
                    generateFallbackAIResponse(userMessage)
                }
            )
        } catch (e: Exception) {
            generateFallbackAIResponse(userMessage)
        }
    }
    
    /**
     * 🤖 AI成员回应 - 模拟AI学生参与讨论
     */
    private suspend fun generateAIMemberResponse(userMessage: String) {
        // 30% 概率AI成员参与
        if ((1..10).random() > 3) return
        
        try {
            val recentContext = messages.takeLast(3).joinToString("\n") { 
                "${it.senderName}: ${it.content}" 
            }
            
            val prompt = """
                你是一个名叫「张三」的学生，正在参与关于「$currentTopic」的小组讨论。
                
                最近的讨论：
                $recentContext
                
                请作为小组成员，提供你的观点或想法。要求：
                1. 像真实学生一样思考和表达
                2. 可以提出新的观点或问题
                3. 可以分享相关的例子或经验
                4. 语气自然，不要太正式
                5. 回应控制在40字以内
                6. 偶尔可以表达困惑或请教
                
                不要总是完美的回答，要有学生的特点。
            """.trimIndent()
            
            val user = preferenceManager.getUser() ?: return
            val result = aiService.sendChatMessage(prompt, user)
            
            result.fold(
                onSuccess = { response ->
                    val aiMessage = DiscussionMessage(
                        id = UUID.randomUUID().toString(),
                        senderId = "ai_member",
                        senderName = aiMemberName,
                        content = response.trim(),
                        timestamp = System.currentTimeMillis(),
                        type = MessageType.USER_MESSAGE // 作为普通学生消息
                    )
                    addMessage(aiMessage)
                },
                onFailure = {
                    generateFallbackAIMemberResponse()
                }
            )
        } catch (e: Exception) {
            generateFallbackAIMemberResponse()
        }
    }
    
    /**
     * 🔄 备用AI回应
     */
    private fun generateFallbackAIResponse(userMessage: String) {
        val responses = listOf(
            "这个观点很有意思！你能举个具体的例子来说明吗？🤔",
            "我觉得你说得很有道理，这让我想到了相关的应用场景...",
            "从这个角度分析确实如此，大家还有其他不同的看法吗？",
            "这个想法可以进一步延伸，比如在其他领域的应用",
            "很棒的思路！这和我们之前学习的哪个知识点有联系呢？📚",
            "你提到的这点很关键，我们可以深入探讨一下原理",
            "这个角度我之前没想到，能否详细解释一下？",
            "很好的分享！其他同学有类似的经历或想法吗？"
        )
        
        val aiMessage = DiscussionMessage(
            id = UUID.randomUUID().toString(),
            senderId = "ai_assistant",
            senderName = aiAssistantName,
            content = responses.random(),
            timestamp = System.currentTimeMillis(),
            type = MessageType.AI_MESSAGE
        )
        addMessage(aiMessage)
    }
    
    /**
     * 🔄 备用AI成员回应
     */
    private fun generateFallbackAIMemberResponse() {
        val responses = listOf(
            "我也觉得这个问题很有趣，不过我有点不太明白...",
            "从我的理解来看，这个应该是...",
            "我想到了一个类似的例子，就是...",
            "这个知识点我之前也遇到过，感觉挺实用的",
            "我有个疑问，这种情况下会怎么样呢？",
            "对！我也想到了这个，还有其他的应用吗？",
            "这个解释很清楚，我明白了！",
            "我觉得我们可以从另一个角度来看这个问题"
        )
        
        val aiMessage = DiscussionMessage(
            id = UUID.randomUUID().toString(),
            senderId = "ai_member",
            senderName = aiMemberName,
            content = responses.random(),
            timestamp = System.currentTimeMillis(),
            type = MessageType.USER_MESSAGE
        )
        addMessage(aiMessage)
    }
    
    /**
     * 🎯 AI主动发起话题讨论
     */
    private fun initiateAITopicDiscussion() {
        lifecycleScope.launch {
            delay(120000) // 2分钟后如果讨论冷场，AI主动发起话题
            
            if (messages.isEmpty() || 
                System.currentTimeMillis() - messages.last().timestamp > 60000) {
                
                val topics = listOf(
                    "大家觉得数学函数在日常生活中最常见的应用是什么？",
                    "有没有人遇到过用函数解决实际问题的经历？",
                    "我们来讨论一下：为什么函数这么重要？",
                    "谁能分享一个有趣的函数应用案例？",
                    "大家觉得学习函数最难的地方是什么？"
                )
                
                val aiMessage = DiscussionMessage(
                    id = UUID.randomUUID().toString(),
                    senderId = "ai_member",
                    senderName = aiMemberName,
                    content = topics.random(),
                    timestamp = System.currentTimeMillis(),
                    type = MessageType.USER_MESSAGE
                )
                addMessage(aiMessage)
            }
        }
    }
}
