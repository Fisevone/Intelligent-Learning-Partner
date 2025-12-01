package com.example.educationapp.ui.collaboration

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.educationapp.databinding.ActivityGroupQuizBinding
import com.example.educationapp.ui.collaboration.data.QuizQuestion
import com.example.educationapp.ui.collaboration.data.QuizAnswer
import com.example.educationapp.ui.collaboration.data.GroupScore
import com.example.educationapp.utils.PreferenceManager
import com.example.educationapp.ai.ZhipuAIService
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * 🏆 小组竞赛界面
 * 实现实时答题对战、积分排行榜、智能出题等功能
 */
class GroupQuizActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityGroupQuizBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var aiService: ZhipuAIService
    
    // 竞赛数据
    private val questions = mutableListOf<QuizQuestion>()
    private val groupScores = mutableListOf<GroupScore>()
    private var currentQuestionIndex = 0
    private var myGroupScore = 0
    private var myPersonalScore = 0
    private var countDownTimer: CountDownTimer? = null
    private var isQuizActive = false
    
    // 模拟数据
    private val myGroupName = "第1组"
    private val competingGroups = listOf("第2组", "第3组", "第4组")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeComponents()
        setupUI()
        setupClickListeners()
        initializeQuiz()
    }
    
    private fun initializeComponents() {
        preferenceManager = PreferenceManager(this)
        aiService = ZhipuAIService()
    }
    
    private fun setupUI() {
        binding.apply {
            // 设置标题
            setSupportActionBar(toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                title = "小组竞赛 - $myGroupName"
            }
            
            // 初始化分数显示
            updateScoreDisplay()
            
            // 隐藏答题区域
            layoutQuestion.visibility = android.view.View.GONE
        }
    }
    
    private fun setupClickListeners() {
        binding.apply {
            // 开始竞赛
            btnStartQuiz.setOnClickListener {
                startQuiz()
            }
            
            // 答案选项
            btnOptionA.setOnClickListener { submitAnswer("A") }
            btnOptionB.setOnClickListener { submitAnswer("B") }
            btnOptionC.setOnClickListener { submitAnswer("C") }
            btnOptionD.setOnClickListener { submitAnswer("D") }
            
            // 返回按钮
            toolbar.setNavigationOnClickListener {
                finish()
            }
        }
    }
    
    private fun initializeQuiz() {
        // 初始化小组分数
        groupScores.clear()
        groupScores.add(GroupScore(myGroupName, 0, true))
        competingGroups.forEach { groupName ->
            groupScores.add(GroupScore(groupName, 0, false))
        }
        
        // 生成题目
        generateQuestions()
        
        // 更新排行榜
        updateLeaderboard()
    }
    
    private fun generateQuestions() {
        // 预设题目，实际应该从AI生成或题库获取
        questions.clear()
        questions.addAll(listOf(
            QuizQuestion(
                id = "1",
                question = "下列哪个函数是二次函数？",
                options = listOf("y = 2x + 1", "y = x² + 2x + 1", "y = 1/x", "y = 2^x"),
                correctAnswer = "B",
                difficulty = "medium",
                subject = "数学"
            ),
            QuizQuestion(
                id = "2",
                question = "二次函数 y = x² - 4x + 3 的对称轴是？",
                options = listOf("x = 1", "x = 2", "x = 3", "x = 4"),
                correctAnswer = "B",
                difficulty = "medium",
                subject = "数学"
            ),
            QuizQuestion(
                id = "3",
                question = "函数 y = 2x + 1 在区间 [0, 3] 上的最大值是？",
                options = listOf("1", "3", "5", "7"),
                correctAnswer = "D",
                difficulty = "easy",
                subject = "数学"
            )
        ))
    }
    
    private fun startQuiz() {
        if (questions.isEmpty()) {
            Toast.makeText(this, "题目加载中，请稍候", Toast.LENGTH_SHORT).show()
            return
        }
        
        isQuizActive = true
        currentQuestionIndex = 0
        
        binding.apply {
            btnStartQuiz.visibility = android.view.View.GONE
            layoutQuestion.visibility = android.view.View.VISIBLE
        }
        
        // 显示第一题
        showCurrentQuestion()
        
        // 模拟其他小组的答题
        simulateOtherGroupsActivity()
        
        Toast.makeText(this, "🏆 竞赛开始！", Toast.LENGTH_SHORT).show()
    }
    
    private fun showCurrentQuestion() {
        if (currentQuestionIndex >= questions.size) {
            endQuiz()
            return
        }
        
        val question = questions[currentQuestionIndex]
        
        binding.apply {
            tvQuestionNumber.text = "第 ${currentQuestionIndex + 1} 题 / ${questions.size}"
            tvQuestion.text = question.question
            btnOptionA.text = "A. ${question.options[0]}"
            btnOptionB.text = "B. ${question.options[1]}"
            btnOptionC.text = "C. ${question.options[2]}"
            btnOptionD.text = "D. ${question.options[3]}"
            
            // 重置按钮状态
            resetOptionButtons()
            
            // 启动倒计时
            startQuestionTimer()
        }
    }
    
    private fun resetOptionButtons() {
        binding.apply {
            listOf(btnOptionA, btnOptionB, btnOptionC, btnOptionD).forEach { button ->
                button.isEnabled = true
                button.setBackgroundColor(resources.getColor(android.R.color.transparent))
            }
        }
    }
    
    private fun startQuestionTimer() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(15000, 1000) { // 15秒答题时间
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.tvTimer.text = "⏰ ${seconds}s"
            }
            
            override fun onFinish() {
                binding.tvTimer.text = "⏰ 时间到！"
                // 自动提交（算错误）
                submitAnswer("")
            }
        }
        countDownTimer?.start()
    }
    
    private fun submitAnswer(selectedOption: String) {
        if (!isQuizActive) return
        
        countDownTimer?.cancel()
        
        val question = questions[currentQuestionIndex]
        val isCorrect = selectedOption == question.correctAnswer
        
        // 更新分数
        if (isCorrect) {
            myPersonalScore += 10
            myGroupScore += 10
            
            // 更新我的小组分数
            groupScores.find { it.groupName == myGroupName }?.let {
                it.score = myGroupScore
            }
        }
        
        // 显示答案反馈
        showAnswerFeedback(selectedOption, question.correctAnswer, isCorrect)
        
        // 更新UI
        updateScoreDisplay()
        updateLeaderboard()
        
        // 2秒后显示下一题
        lifecycleScope.launch {
            delay(2000)
            currentQuestionIndex++
            showCurrentQuestion()
        }
    }
    
    private fun showAnswerFeedback(selected: String, correct: String, isCorrect: Boolean) {
        binding.apply {
            // 禁用所有按钮
            listOf(btnOptionA, btnOptionB, btnOptionC, btnOptionD).forEach { button ->
                button.isEnabled = false
            }
            
            // 显示正确答案
            val correctButton = when (correct) {
                "A" -> btnOptionA
                "B" -> btnOptionB
                "C" -> btnOptionC
                "D" -> btnOptionD
                else -> null
            }
            correctButton?.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
            
            // 如果选错了，显示错误答案
            if (!isCorrect && selected.isNotEmpty()) {
                val selectedButton = when (selected) {
                    "A" -> btnOptionA
                    "B" -> btnOptionB
                    "C" -> btnOptionC
                    "D" -> btnOptionD
                    else -> null
                }
                selectedButton?.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))
            }
            
            // 显示结果消息
            val message = if (isCorrect) "✅ 回答正确！+10分" else "❌ 回答错误"
            Toast.makeText(this@GroupQuizActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun simulateOtherGroupsActivity() {
        lifecycleScope.launch {
            while (isQuizActive && currentQuestionIndex < questions.size) {
                delay((3000..8000).random().toLong()) // 随机延迟
                
                // 随机更新其他小组分数
                groupScores.filter { !it.isMyGroup }.forEach { group ->
                    if ((1..10).random() <= 7) { // 70%概率答对
                        group.score += 10
                    }
                }
                
                updateLeaderboard()
            }
        }
    }
    
    private fun updateScoreDisplay() {
        binding.apply {
            tvMyScore.text = "我的得分：$myPersonalScore"
            tvGroupScore.text = "小组得分：$myGroupScore"
        }
    }
    
    private fun updateLeaderboard() {
        // 按分数排序
        val sortedGroups = groupScores.sortedByDescending { it.score }
        
        binding.apply {
            // 显示前3名
            if (sortedGroups.isNotEmpty()) {
                tvRank1.text = "🥇 ${sortedGroups[0].groupName}: ${sortedGroups[0].score}分"
            }
            if (sortedGroups.size > 1) {
                tvRank2.text = "🥈 ${sortedGroups[1].groupName}: ${sortedGroups[1].score}分"
            }
            if (sortedGroups.size > 2) {
                tvRank3.text = "🥉 ${sortedGroups[2].groupName}: ${sortedGroups[2].score}分"
            }
            
            // 显示我的小组排名
            val myRank = sortedGroups.indexOfFirst { it.isMyGroup } + 1
            tvMyRank.text = "我的小组排名：第 $myRank 名"
        }
    }
    
    private fun endQuiz() {
        isQuizActive = false
        countDownTimer?.cancel()
        
        binding.apply {
            layoutQuestion.visibility = android.view.View.GONE
            btnStartQuiz.visibility = android.view.View.VISIBLE
            btnStartQuiz.text = "再来一轮"
        }
        
        // 显示最终结果
        val myRank = groupScores.sortedByDescending { it.score }.indexOfFirst { it.isMyGroup } + 1
        val message = when (myRank) {
            1 -> "🎉 恭喜！你的小组获得第一名！"
            2 -> "👏 不错！你的小组获得第二名！"
            3 -> "💪 加油！你的小组获得第三名！"
            else -> "📚 继续努力，下次一定能更好！"
        }
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🏆 竞赛结束")
            .setMessage("$message\n\n个人得分：$myPersonalScore\n小组得分：$myGroupScore")
            .setPositiveButton("确定") { _, _ -> }
            .show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}

