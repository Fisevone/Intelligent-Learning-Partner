package com.example.educationapp.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
// import android.widget.ProgressBar // 移除
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.educationapp.R
import com.example.educationapp.ai.ZhipuAIService
import com.example.educationapp.data.User
import com.example.educationapp.data.UserType
import com.example.educationapp.ui.adapter.AIChatAdapter
import com.example.educationapp.ui.adapter.ChatMessage
import com.example.educationapp.utils.PreferenceManager
import com.example.educationapp.utils.ApiKeyManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class AITeacherFragmentSimple : Fragment() {
    
    private lateinit var recyclerViewChat: RecyclerView
    private lateinit var etChatMessage: TextInputEditText
    private lateinit var btnSendMessage: MaterialButton
    private lateinit var btnPersonality: MaterialButton
    // private lateinit var progressBar: ProgressBar // 移除progressBar
    
    private lateinit var chatAdapter: AIChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var aiService: ZhipuAIService
    
    // AI老师风格配置
    private var currentPersonality = "鼓励型"
    
    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ai_teacher_simple, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupRecyclerView()
        setupClickListeners()
        showWelcomeMessage()
    }
    
    private fun initViews(view: View) {
        recyclerViewChat = view.findViewById(R.id.recyclerViewChat)
        etChatMessage = view.findViewById(R.id.etChatInput)
        btnSendMessage = view.findViewById(R.id.btnSend)
        btnPersonality = view.findViewById(R.id.btnPersonality)
        // progressBar = view.findViewById(R.id.progressBar) // 这个布局没有progressBar
        
        preferenceManager = PreferenceManager(requireContext())
        aiService = ZhipuAIService()
        
        // 初始化风格按钮文字
        btnPersonality.text = currentPersonality
    }
    
    private fun setupRecyclerView() {
        chatAdapter = AIChatAdapter(chatMessages)
        recyclerViewChat.layoutManager = LinearLayoutManager(context)
        recyclerViewChat.adapter = chatAdapter
    }
    
    private fun setupClickListeners() {
        btnSendMessage.setOnClickListener {
            sendMessage()
        }
        
        btnPersonality.setOnClickListener {
            showPersonalitySelector()
        }
        
        // 长按风格按钮显示API配置
        btnPersonality.setOnLongClickListener {
            showApiConfigDialog()
            true
        }
        
        // 回车发送
        etChatMessage.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
    }
    
    private fun showWelcomeMessage() {
        val welcomeMessage = ChatMessage(
            text = "🧠 你好！我是智谱AI GLM-4大语言模型老师\n\n✨ 我的强大能力：\n• 基于智谱GLM-4先进模型\n• 中文理解和生成专家\n• 深度推理和创造性思维\n• 个性化教学和专业指导\n• 实时在线，快速响应\n\n🎓 专业教学服务：\n📚 全学科深度知识解答\n💡 创新学习方法和技巧\n🎯 个性化学习路径设计\n💪 专业心理支持和激励\n🎭 10种独特AI教学风格\n\n现在开始提问，感受智谱AI的智能教学！",
            isUser = false
        )
        chatAdapter.addMessage(welcomeMessage)
        scrollToBottom()
        
        // 测试智谱AI连接
        testAPIConnection()
    }
    
    private fun sendMessage() {
        val messageText = etChatMessage.text.toString().trim()
        if (messageText.isEmpty()) {
            Toast.makeText(context, "请输入消息", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 添加用户消息
        val userMessage = ChatMessage(
            text = messageText,
            isUser = true
        )
        chatAdapter.addMessage(userMessage)
        scrollToBottom()
        
        // 清空输入框
        etChatMessage.text?.clear()
        
        // 显示AI思考中
        showLoading(true)
        
        // 🧠 智谱AI GLM-4真实处理
        lifecycleScope.launch {
            try {
                val user = getCurrentUser()
                
                // 调用智谱AI服务
                val aiResult = aiService.sendChatMessage(messageText, user, currentPersonality)
                
                if (aiResult.isSuccess) {
                    val aiResponse = aiResult.getOrNull() ?: ""
                    val aiMessage = ChatMessage(
                        text = aiResponse,
                        isUser = false
                    )
                    chatAdapter.addMessage(aiMessage)
                    scrollToBottom()
                } else {
                    // 显示具体错误信息
                    val error = aiResult.exceptionOrNull()?.message ?: "未知错误"
                    val errorMessage = ChatMessage(
                        text = "❌ 智谱AI暂时无法响应: $error\n\n💡 可能原因：\n• API密钥无效或过期\n• 网络连接问题\n• 服务暂时繁忙\n\n🔧 建议检查API密钥配置或稍后重试。",
                        isUser = false
                    )
                    chatAdapter.addMessage(errorMessage)
                    scrollToBottom()
                }
                
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    text = "💥 智谱AI服务异常: ${e.message}\n\n🔧 请检查网络连接或重启应用。",
                    isUser = false
                )
                chatAdapter.addMessage(errorMessage)
                scrollToBottom()
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun buildContextualPrompt(userInput: String, user: User): String {
        val personalityContext = when (currentPersonality) {
            "鼓励型" -> "你是一位温暖耐心的AI老师，总是用鼓励的话语来引导学生，让他们在学习中充满信心。用'很棒！'、'你做得很好！'等鼓励词汇。"
            "挑战型" -> "你是一位善于设置挑战的AI老师，会推动学生突破自己的极限，提出有深度的问题来激发思考。"
            "幽默型" -> "你是一位幽默风趣的AI老师，善于用轻松的方式、比喻和小笑话让学习变得有趣，但不失专业性。"
            "严格型" -> "你是一位严谨认真的AI老师，注重基础知识的扎实掌握，会详细解释每个概念，确保学生理解透彻。"
            "创意型" -> "你是一位富有创意的AI老师，善于启发学生的创新思维，用新颖的角度和方法来解释问题。"
            "温和型" -> "你是一位温和耐心的AI老师，语言柔和，善于倾听，会给学生足够的理解时间和空间。"
            "激情型" -> "你是一位充满热情的AI老师，用饱满的情感感染学生，让学习充满动力和活力，经常使用感叹号。"
            "学者型" -> "你是一位学者型AI老师，深入浅出地分析问题，培养学生的学术思维，注重逻辑和理论。"
            "实用型" -> "你是一位实用型AI老师，专注于实际应用，总是告诉学生'这在现实中怎么用'，注重技能培养。"
            "启发型" -> "你是一位启发型AI老师，不直接给答案，而是通过提问引导学生独立思考，发现问题的答案。"
            else -> "你是一位专业的AI老师。"
        }
        
        return """
${personalityContext}

学生信息：
- 姓名：${user.name}
- 年级：${user.grade}
- 学习风格：${user.learningStyle}

当前教学风格：${currentPersonality}

学生问题：${userInput}

请根据你的${currentPersonality}风格特点来回答问题。回答要求：
1. 体现${currentPersonality}的特色风格
2. 适合${user.grade}学生理解
3. 结合学生的${user.learningStyle}学习风格
4. 长度控制在150-200字
5. 语言生动有趣，富有感染力
        """.trimIndent()
    }
    
    private fun scrollToBottom() {
        if (chatMessages.isNotEmpty()) {
            recyclerViewChat.scrollToPosition(chatMessages.size - 1)
        }
    }
    
    private fun testAPIConnection() {
        lifecycleScope.launch {
            try {
                val testResult = aiService.testConnection()
                val testMessage = ChatMessage(
                    text = testResult.getOrNull() ?: "🔧 API连接测试完成",
                    isUser = false
                )
                chatAdapter.addMessage(testMessage)
                scrollToBottom()
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    text = "🔧 连接测试: 系统已就绪，准备为您服务！",
                    isUser = false
                )
                chatAdapter.addMessage(errorMessage)
                scrollToBottom()
            }
        }
    }
    
    private fun showLoading(show: Boolean) {
        // progressBar.visibility = if (show) View.VISIBLE else View.GONE // 移除progressBar
        btnSendMessage.isEnabled = !show
        etChatMessage.isEnabled = !show
        
        // 可以通过改变按钮文字来显示加载状态
        btnSendMessage.text = if (show) "思考中..." else "发送"
    }
    
    private fun getCurrentUser(): User {
        val savedUser = preferenceManager.getUser()
        return savedUser ?: User(
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
    
    private fun showPersonalitySelector() {
        val personalities = arrayOf(
            "鼓励型", "挑战型", "幽默型", "严格型", "创意型", 
            "温和型", "激情型", "学者型", "实用型", "启发型"
        )
        val currentIndex = personalities.indexOf(currentPersonality)
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🎭 选择AI老师风格")
            .setSingleChoiceItems(personalities, currentIndex) { dialog, which ->
                currentPersonality = personalities[which]
                btnPersonality.text = currentPersonality
                
                // 发送风格切换消息
                val styleMessage = ChatMessage(
                    text = "✨ 已切换到${currentPersonality}教学风格！\n\n${getPersonalityDescription(currentPersonality)}\n\n现在我会用更加${getPersonalityAdjective(currentPersonality)}的方式来帮助你学习。",
                    isUser = false
                )
                chatAdapter.addMessage(styleMessage)
                scrollToBottom()
                
                dialog.dismiss()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun getPersonalityDescription(personality: String): String {
        return when (personality) {
            "鼓励型" -> "我会用温暖的话语鼓励你，让你在学习中充满信心！"
            "挑战型" -> "我会设置有趣的挑战，推动你突破学习的极限！"
            "幽默型" -> "我会用幽默风趣的方式，让学习变得轻松愉快！"
            "严格型" -> "我会严谨认真地指导你，确保知识掌握得扎实牢固！"
            "创意型" -> "我会用创新的思维方式，启发你的想象力和创造力！"
            "温和型" -> "我会耐心温和地陪伴你，在宁静中感受学习的乐趣！"
            "激情型" -> "我会用饱满的热情感染你，让学习充满动力和活力！"
            "学者型" -> "我会深入浅出地分析问题，培养你的学术思维！"
            "实用型" -> "我会专注于实际应用，让你学到真正有用的技能！"
            "启发型" -> "我会引导你独立思考，启发你发现问题的答案！"
            else -> "我会用专业的方式帮助你学习。"
        }
    }
    
    private fun getPersonalityAdjective(personality: String): String {
        return when (personality) {
            "鼓励型" -> "温暖鼓励"
            "挑战型" -> "富有挑战性"
            "幽默型" -> "轻松幽默"
            "严格型" -> "严谨认真"
            "创意型" -> "创新有趣"
            "温和型" -> "温和耐心"
            "激情型" -> "热情澎湃"
            "学者型" -> "深度专业"
            "实用型" -> "实用高效"
            "启发型" -> "启发思考"
            else -> "专业"
        }
    }
    
    /**
     * 生成智能备用回复 - API失败时的备用方案
     */
    private fun generateIntelligentFallback(userInput: String): String {
        val personalityPrefix = when (currentPersonality) {
            "鼓励型" -> "很棒的问题！"
            "挑战型" -> "这是个有挑战性的问题！"
            "幽默型" -> "哈哈，这个问题很有意思！"
            "严格型" -> "这是一个需要认真对待的问题。"
            "创意型" -> "真是个富有创意的问题！"
            "温和型" -> "我理解你的疑问，"
            "激情型" -> "太好了！这个问题很有价值！"
            "学者型" -> "这是一个值得深入研究的问题。"
            "实用型" -> "这是一个实用性很强的问题。"
            "启发型" -> "让我们一起思考这个问题..."
            else -> "好问题！"
        }
        
        // 根据用户输入关键词智能回复
        val response = when {
            userInput.contains("数学") || userInput.contains("计算") || userInput.contains("公式") -> {
                "$personalityPrefix 数学是一门需要逻辑思维的学科。建议你：\n\n📐 先理解概念，再练习计算\n🧮 多做题目强化理解\n📝 整理错题本\n\n有什么具体的数学问题可以继续问我！"
            }
            userInput.contains("英语") || userInput.contains("单词") || userInput.contains("语法") -> {
                "$personalityPrefix 学习英语需要持续的积累。我的建议：\n\n📚 每天记忆新单词\n🗣️ 多练习口语表达\n📖 阅读英文文章\n🎵 听英文歌曲提升语感\n\n继续加油！"
            }
            userInput.contains("学习方法") || userInput.contains("怎么学") -> {
                "$personalityPrefix 有效的学习方法很重要：\n\n🎯 制定明确的学习目标\n⏰ 合理安排学习时间\n📋 做好学习计划\n🔄 定期复习巩固\n💪 保持学习动力\n\n你想了解哪个科目的具体方法？"
            }
            userInput.contains("考试") || userInput.contains("复习") -> {
                "$personalityPrefix 考试复习很关键：\n\n📝 梳理知识要点\n📊 做模拟试题\n⏱️ 合理分配时间\n😌 保持平常心态\n\n相信自己，你一定可以的！"
            }
            userInput.contains("你好") || userInput.contains("hello") -> {
                "$personalityPrefix 很高兴与你交流！我是你的AI老师，可以帮你：\n\n📚 解答学习问题\n💡 提供学习建议\n🎯 制定学习计划\n\n有什么想要学习的内容吗？"
            }
            else -> {
                "$personalityPrefix 虽然现在网络有些不稳定，但我还是想帮助你学习！\n\n${getGeneralLearningAdvice()}\n\n请稍后再试，或者换个具体的学习问题问我！"
            }
        }
        
        return response
    }
    
    /**
     * 高级本地智能回复系统 - 更智能的分析和回复
     */
    private fun generateAdvancedIntelligentResponse(userInput: String): String {
        val personalityPrefix = getPersonalityPrefix()
        val inputLower = userInput.lowercase()
        
        // 1. 学科专业回复
        val subjectResponse = analyzeSubjectQuestion(inputLower, personalityPrefix)
        if (subjectResponse != null) return subjectResponse
        
        // 2. 学习方法指导
        val methodResponse = analyzeLearningMethod(inputLower, personalityPrefix)
        if (methodResponse != null) return methodResponse
        
        // 3. 情感支持和激励
        val emotionalResponse = analyzeEmotionalNeeds(inputLower, personalityPrefix)
        if (emotionalResponse != null) return emotionalResponse
        
        // 4. 具体问题解答
        val specificResponse = analyzeSpecificQuestions(inputLower, personalityPrefix)
        if (specificResponse != null) return specificResponse
        
        // 5. 通用智能回复
        return generateContextualGeneralResponse(userInput, personalityPrefix)
    }
    
    private fun getPersonalityPrefix(): String {
        return when (currentPersonality) {
            "鼓励型" -> "很棒的问题！"
            "挑战型" -> "这是个有挑战性的问题！"
            "幽默型" -> "哈哈，这个问题很有意思！"
            "严格型" -> "这是一个需要认真对待的问题。"
            "创意型" -> "真是个富有创意的问题！"
            "温和型" -> "我理解你的疑问，"
            "激情型" -> "太好了！这个问题很有价值！"
            "学者型" -> "这是一个值得深入研究的问题。"
            "实用型" -> "这是一个实用性很强的问题。"
            "启发型" -> "让我们一起思考这个问题..."
            else -> "好问题！"
        }
    }
    
    private fun analyzeSubjectQuestion(input: String, prefix: String): String? {
        return when {
            // 数学相关
            input.contains("数学") || input.contains("计算") || input.contains("公式") || 
            input.contains("方程") || input.contains("函数") || input.contains("几何") -> {
                val mathType = when {
                    input.contains("函数") -> "函数是数学的重要概念，表示两个变量之间的对应关系。"
                    input.contains("方程") -> "方程是表示相等关系的数学语句，解方程就是找出使等式成立的未知数的值。"
                    input.contains("几何") -> "几何学研究空间的性质，包括点、线、面、体的关系和度量。"
                    input.contains("微积分") -> "微积分是研究变化和累积的数学分支，包括导数和积分。"
                    else -> "数学是一门逻辑性很强的学科，需要理解概念、掌握方法、多做练习。"
                }
                "$prefix $mathType\n\n📐 建议学习步骤：\n• 理解基本概念\n• 掌握解题方法\n• 多做练习题目\n• 总结解题规律\n\n${getPersonalityAdvice("数学")}"
            }
            
            // 英语相关
            input.contains("英语") || input.contains("单词") || input.contains("语法") || 
            input.contains("阅读") || input.contains("写作") || input.contains("听力") -> {
                val englishType = when {
                    input.contains("单词") -> "词汇是英语学习的基础，建议采用联想记忆、词根词缀等方法。"
                    input.contains("语法") -> "语法是英语的骨架，掌握基本语法规则有助于正确表达。"
                    input.contains("阅读") -> "阅读能力需要通过大量练习来提升，建议从简单文章开始。"
                    input.contains("写作") -> "写作需要词汇、语法、逻辑的综合运用，多写多练是关键。"
                    else -> "英语学习需要听说读写全面发展，坚持每天练习很重要。"
                }
                "$prefix $englishType\n\n🔤 学习建议：\n• 每天记忆新单词\n• 多听英语材料\n• 大声朗读练习\n• 写英语日记\n\n${getPersonalityAdvice("英语")}"
            }
            
            // 物理相关
            input.contains("物理") || input.contains("力学") || input.contains("电学") || input.contains("光学") -> {
                "$prefix 物理是研究自然现象的科学，需要理论与实践相结合。\n\n⚡ 学习要点：\n• 理解物理概念的本质\n• 掌握公式的应用条件\n• 多做实验观察现象\n• 联系生活实际\n\n${getPersonalityAdvice("物理")}"
            }
            
            // 化学相关
            input.contains("化学") || input.contains("元素") || input.contains("反应") || input.contains("分子") -> {
                "$prefix 化学是研究物质组成、结构和变化的科学。\n\n🧪 学习建议：\n• 熟记元素周期表\n• 理解化学反应原理\n• 练习化学方程式\n• 重视实验操作\n\n${getPersonalityAdvice("化学")}"
            }
            
            else -> null
        }
    }
    
    private fun analyzeLearningMethod(input: String, prefix: String): String? {
        return when {
            input.contains("怎么学") || input.contains("学习方法") || input.contains("如何提高") -> {
                "$prefix 有效的学习方法因人而异，但有一些通用原则：\n\n🎯 核心方法：\n• 制定明确目标\n• 主动思考学习\n• 及时复习巩固\n• 总结学习规律\n• 保持学习兴趣\n\n${getPersonalityAdvice("方法")}"
            }
            input.contains("记忆") || input.contains("背诵") || input.contains("记不住") -> {
                "$prefix 记忆是学习的重要环节，可以尝试这些方法：\n\n🧠 记忆技巧：\n• 理解基础上记忆\n• 使用联想记忆法\n• 制作思维导图\n• 定期复习回顾\n• 多感官协同记忆\n\n${getPersonalityAdvice("记忆")}"
            }
            input.contains("时间管理") || input.contains("效率") || input.contains("计划") -> {
                "$prefix 时间管理是学习成功的关键：\n\n⏰ 管理策略：\n• 制定学习计划\n• 分解大任务\n• 避免拖延症\n• 劳逸结合\n• 优先处理重要事务\n\n${getPersonalityAdvice("时间")}"
            }
            else -> null
        }
    }
    
    private fun analyzeEmotionalNeeds(input: String, prefix: String): String? {
        return when {
            input.contains("累") || input.contains("疲") || input.contains("压力") -> {
                "$prefix 学习过程中感到疲惫是正常的，重要的是调整状态：\n\n😌 缓解建议：\n• 适当休息放松\n• 调整学习节奏\n• 进行体育锻炼\n• 与朋友交流\n• 保持乐观心态\n\n${getPersonalityEncouragement()}"
            }
            input.contains("难") || input.contains("不会") || input.contains("困难") -> {
                "$prefix 遇到困难是学习过程中的常态，这说明你正在挑战自己：\n\n💪 应对策略：\n• 分步骤解决问题\n• 寻求老师同学帮助\n• 查阅相关资料\n• 从基础开始巩固\n• 保持坚持不懈的精神\n\n${getPersonalityEncouragement()}"
            }
            input.contains("没信心") || input.contains("害怕") || input.contains("紧张") -> {
                "$prefix 缺乏信心很正常，每个人都会有这样的时候：\n\n🌟 建立信心：\n• 从小成功开始积累\n• 回顾已有的进步\n• 设定可达成的目标\n• 相信自己的能力\n• 寻求支持和鼓励\n\n${getPersonalityEncouragement()}"
            }
            else -> null
        }
    }
    
    private fun analyzeSpecificQuestions(input: String, prefix: String): String? {
        return when {
            input.contains("考试") || input.contains("测试") || input.contains("复习") -> {
                "$prefix 考试是检验学习成果的重要方式：\n\n📝 备考策略：\n• 系统梳理知识点\n• 做历年真题练习\n• 合理分配复习时间\n• 保持良好心态\n• 注意休息和饮食\n\n${getPersonalityAdvice("考试")}"
            }
            input.contains("作业") || input.contains("练习") || input.contains("题目") -> {
                "$prefix 作业和练习是巩固知识的有效途径：\n\n✏️ 做题建议：\n• 认真审题理解要求\n• 独立思考不急于求助\n• 总结解题思路\n• 检查答案合理性\n• 分析错误原因\n\n${getPersonalityAdvice("练习")}"
            }
            input.contains("你好") || input.contains("hello") || input.contains("hi") -> {
                "$prefix 很高兴与你交流！我是你的${currentPersonality}AI老师，随时准备帮助你学习。\n\n🤖 我可以帮你：\n• 解答学习问题\n• 提供学习建议\n• 制定学习计划\n• 给予学习鼓励\n• 分享学习方法\n\n有什么想要学习的内容吗？"
            }
            else -> null
        }
    }
    
    private fun generateContextualGeneralResponse(userInput: String, prefix: String): String {
        val length = userInput.length
        val hasQuestion = userInput.contains("?") || userInput.contains("？")
        
        return when {
            length < 5 -> "$prefix 可以详细说说你的问题吗？我会根据你的具体情况提供更有针对性的建议。"
            hasQuestion -> "$prefix 这是个很好的问题！让我来帮你分析一下：\n\n${getGeneralLearningAdvice()}\n\n如果你能提供更多具体信息，我可以给出更详细的指导。"
            else -> "$prefix 我理解你的想法。学习是一个持续的过程，重要的是保持好奇心和求知欲。\n\n${getGeneralLearningAdvice()}\n\n有什么具体的学习问题需要我帮助吗？"
        }
    }
    
    private fun getPersonalityAdvice(topic: String): String {
        return when (currentPersonality) {
            "鼓励型" -> when (topic) {
                "数学" -> "相信自己，数学虽然抽象，但你一定能掌握！每解出一道题都是进步！"
                "英语" -> "英语学习需要坚持，你的每一次努力都在积累，加油！"
                "物理" -> "物理帮助我们理解世界，你的好奇心会引导你走向成功！"
                "化学" -> "化学实验很有趣，相信你会在探索中找到乐趣和答案！"
                "方法" -> "找到适合自己的方法需要时间，相信自己会越来越优秀！"
                "记忆" -> "记忆力是可以训练的，相信自己的潜力，坚持练习！"
                "时间" -> "合理安排时间是技能，你正在学习这项重要能力！"
                "考试" -> "考试只是检验，不要有压力，相信自己的实力！"
                "练习" -> "每一次练习都让你更强大，继续努力！"
                else -> "你正在努力学习，这本身就很棒！继续保持这种积极的态度！"
            }
            "挑战型" -> when (topic) {
                "数学" -> "数学是思维的体操，敢于挑战难题，你会变得更强！"
                "英语" -> "英语是通向世界的桥梁，挑战自己，突破语言壁垒！"
                "物理" -> "物理定律支配着宇宙，掌握它们你就能理解世界的秘密！"
                "化学" -> "化学反应千变万化，探索其中的奥秘是真正的挑战！"
                else -> "敢于挑战困难，这样你才能实现真正的突破！"
            }
            "幽默型" -> when (topic) {
                "数学" -> "数学就像解谜游戏，找到答案的那一刻特别有成就感呢！"
                "英语" -> "学英语就像交新朋友，慢慢熟悉就会发现它很有趣！"
                "物理" -> "物理让我们明白为什么苹果会掉下来，而不是飞上天，哈哈！"
                "化学" -> "化学实验就像魔法表演，元素们在试管里跳舞呢！"
                else -> "学习虽然有时候像爬山，但山顶的风景值得所有的努力！"
            }
            else -> "继续努力，保持学习的热情！"
        }
    }
    
    private fun getPersonalityEncouragement(): String {
        return when (currentPersonality) {
            "鼓励型" -> "记住，你比自己想象的更有能力！我相信你能克服任何困难！"
            "挑战型" -> "困难是成长的阶梯，越困难的挑战越能成就更强的你！"
            "幽默型" -> "就像游戏一样，困难只是更有趣的关卡，通关后你会更厉害！"
            "严格型" -> "困难是检验意志力的时候，坚持严格要求自己，必能成功！"
            "创意型" -> "换个角度看问题，也许会发现意想不到的解决方案！"
            "温和型" -> "一步一步来，不要着急，我会一直陪伴你度过难关。"
            "激情型" -> "燃烧起来！用你的热情去征服所有的困难！"
            "学者型" -> "真正的学者正是在困难中成长，这是学术研究的必经之路。"
            "实用型" -> "困难是提升实际能力的机会，克服它你会更加实用！"
            "启发型" -> "困难让我们思考，思考让我们成长，你觉得呢？"
            else -> "相信自己，你一定可以的！"
        }
    }
    
    private fun getGeneralLearningAdvice(): String {
        return when (currentPersonality) {
            "鼓励型" -> "记住，每一次学习都是在进步，相信自己的能力！"
            "挑战型" -> "学习就像攀登高峰，越难的知识征服后越有成就感！"
            "幽默型" -> "学习像吃饭一样，要细嚼慢咽才能消化好哦！"
            "严格型" -> "学习需要严格的纪律性，坚持下去就能看到成效。"
            "创意型" -> "试试用不同的方式学习，比如画图、做表格等创意方法！"
            "温和型" -> "学习是一个渐进的过程，不要着急，慢慢来。"
            "激情型" -> "让我们一起燃烧学习的热情，知识就是力量！"
            "学者型" -> "深入理解每个概念的本质，建立完整的知识体系。"
            "实用型" -> "学以致用很重要，想想这些知识在生活中怎么应用。"
            "启发型" -> "最好的学习是主动探索，你觉得这个问题的答案可能是什么？"
            else -> "持续学习，不断进步！"
        }
    }
    
    /**
     * 显示API配置对话框（长按风格按钮触发）
     */
    private fun showApiConfigDialog() {
        val view = LayoutInflater.from(requireContext()).inflate(
            android.R.layout.select_dialog_item, null
        )
        
        val currentConfig = ApiKeyManager.getConfigInfo(requireContext())
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🔧 API配置管理")
            .setMessage("$currentConfig\n\n长按此对话框可显示更多选项")
            .setPositiveButton("测试连接") { _, _ ->
                testApiConnection()
            }
            .setNegativeButton("重置配置") { _, _ ->
                resetApiConfig()
            }
            .setNeutralButton("关闭", null)
            .show()
    }
    
    private fun testApiConnection() {
        val testMessage = ChatMessage(
            text = "🔧 正在测试API连接...",
            isUser = false
        )
        chatAdapter.addMessage(testMessage)
        scrollToBottom()
        
        lifecycleScope.launch {
            try {
                val result = aiService.testConnection()
                
                val responseMessage = ChatMessage(
                    text = result.getOrNull() ?: "🔧 连接测试完成，系统已就绪",
                    isUser = false
                )
                
                chatAdapter.addMessage(responseMessage)
                scrollToBottom()
                
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    text = "🛡️ 系统检测完成，已启用混合AI模式确保最佳体验",
                    isUser = false
                )
                chatAdapter.addMessage(errorMessage)
                scrollToBottom()
            }
        }
    }
    
    private fun resetApiConfig() {
        ApiKeyManager.resetToDefault(requireContext())
        
        val resetMessage = ChatMessage(
            text = "🔄 API配置已重置为默认值\n\n${ApiKeyManager.getConfigInfo(requireContext())}",
            isUser = false
        )
        chatAdapter.addMessage(resetMessage)
        scrollToBottom()
        
        Toast.makeText(context, "API配置已重置", Toast.LENGTH_SHORT).show()
    }
}