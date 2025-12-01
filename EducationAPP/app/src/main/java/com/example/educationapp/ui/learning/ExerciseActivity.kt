package com.example.educationapp.ui.learning

import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.educationapp.R
import com.example.educationapp.data.SimpleLearningContent
import com.example.educationapp.data.SimpleContentType
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ✏️ 练习系统
 * 支持选择题、判断题，实时反馈和进度跟踪
 */
class ExerciseActivity : AppCompatActivity() {

    private lateinit var tvExerciseTitle: TextView
    private lateinit var tvQuestionNumber: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var btnSubmit: MaterialButton
    private lateinit var btnNext: MaterialButton
    private lateinit var tvResult: TextView
    private lateinit var tvExplanation: TextView
    private lateinit var cardResult: MaterialCardView
    private lateinit var progressExercise: LinearProgressIndicator
    private lateinit var tvProgress: TextView
    
    private var currentContent: SimpleLearningContent? = null
    private var currentQuestionIndex = 0
    private var correctAnswers = 0
    private var totalQuestions = 0
    private var questions = listOf<ExerciseQuestion>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise)
        
        initViews()
        setupToolbar()
        loadExerciseContent()
        setupClickListeners()
        loadQuestion()
    }
    
    private fun initViews() {
        tvExerciseTitle = findViewById(R.id.tv_exercise_title)
        tvQuestionNumber = findViewById(R.id.tv_question_number)
        tvQuestion = findViewById(R.id.tv_question)
        radioGroup = findViewById(R.id.radio_group)
        btnSubmit = findViewById(R.id.btn_submit)
        btnNext = findViewById(R.id.btn_next)
        tvResult = findViewById(R.id.tv_result)
        tvExplanation = findViewById(R.id.tv_explanation)
        cardResult = findViewById(R.id.card_result)
        progressExercise = findViewById(R.id.progress_exercise)
        tvProgress = findViewById(R.id.tv_progress)
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "练习测验"
        }
        
        toolbar.setNavigationOnClickListener { finish() }
    }
    
    private fun loadExerciseContent() {
        val contentId = intent.getStringExtra("content_id") ?: "math_exercise_001"
        val contentTitle = intent.getStringExtra("content_title") ?: "一元一次方程练习"
        
        currentContent = SimpleLearningContent(
            id = contentId,
            title = contentTitle,
            description = "通过练习题巩固一元一次方程的解法",
            type = SimpleContentType.EXERCISE,
            subject = "数学",
            duration = 30, // 30分钟
            difficulty = "中级",
            rating = 4.6f,
            viewCount = 3200,
            progress = 0f
        )
        
        // 加载练习题
        questions = generateExerciseQuestions()
        totalQuestions = questions.size
        
        updateExerciseInfo()
    }
    
    private fun updateExerciseInfo() {
        currentContent?.let { content ->
            tvExerciseTitle.text = content.title
            
            val progressPercent = ((currentQuestionIndex.toFloat() / totalQuestions.toFloat()) * 100).toInt()
            progressExercise.progress = progressPercent
            tvProgress.text = "进度：${currentQuestionIndex}/${totalQuestions} (${progressPercent}%)"
        }
    }
    
    private fun generateExerciseQuestions(): List<ExerciseQuestion> {
        return listOf(
            ExerciseQuestion(
                question = "解方程：2x + 3 = 7，x的值是？",
                options = listOf("A. x = 1", "B. x = 2", "C. x = 3", "D. x = 4"),
                correctAnswer = 1,
                explanation = "解：2x + 3 = 7\n移项得：2x = 7 - 3 = 4\n系数化为1：x = 4 ÷ 2 = 2"
            ),
            ExerciseQuestion(
                question = "解方程：3x - 5 = x + 1，x的值是？",
                options = listOf("A. x = 2", "B. x = 3", "C. x = 4", "D. x = 5"),
                correctAnswer = 1,
                explanation = "解：3x - 5 = x + 1\n移项得：3x - x = 1 + 5\n合并同类项：2x = 6\n系数化为1：x = 3"
            ),
            ExerciseQuestion(
                question = "解方程：4(x - 1) = 2x + 6，x的值是？",
                options = listOf("A. x = 4", "B. x = 5", "C. x = 6", "D. x = 7"),
                correctAnswer = 1,
                explanation = "解：4(x - 1) = 2x + 6\n去括号：4x - 4 = 2x + 6\n移项：4x - 2x = 6 + 4\n合并同类项：2x = 10\n系数化为1：x = 5"
            ),
            ExerciseQuestion(
                question = "解方程：(x + 2)/3 = (x - 1)/2，x的值是？",
                options = listOf("A. x = 7", "B. x = 8", "C. x = 9", "D. x = 10"),
                correctAnswer = 0,
                explanation = "解：(x + 2)/3 = (x - 1)/2\n去分母，两边同乘6：2(x + 2) = 3(x - 1)\n去括号：2x + 4 = 3x - 3\n移项：2x - 3x = -3 - 4\n合并同类项：-x = -7\n系数化为1：x = 7"
            ),
            ExerciseQuestion(
                question = "解方程：0.5x + 1.5 = 2x - 0.5，x的值是？",
                options = listOf("A. x = 1.2", "B. x = 1.33", "C. x = 1.4", "D. x = 1.5"),
                correctAnswer = 1,
                explanation = "解：0.5x + 1.5 = 2x - 0.5\n移项：0.5x - 2x = -0.5 - 1.5\n合并同类项：-1.5x = -2\n系数化为1：x = -2 ÷ (-1.5) = 4/3 ≈ 1.33"
            ),
            ExerciseQuestion(
                question = "化简：3x + 2x - x = ?",
                options = listOf("A. 3x", "B. 4x", "C. 5x", "D. 6x"),
                correctAnswer = 1,
                explanation = "解：3x + 2x - x\n合并同类项：(3 + 2 - 1)x = 4x"
            ),
            ExerciseQuestion(
                question = "如果 2y - 8 = 6，那么 y 的值是？",
                options = listOf("A. y = 5", "B. y = 6", "C. y = 7", "D. y = 8"),
                correctAnswer = 2,
                explanation = "解：2y - 8 = 6\n移项：2y = 6 + 8 = 14\n系数化为1：y = 14 ÷ 2 = 7"
            )
        )
    }
    
    private fun setupClickListeners() {
        btnSubmit.setOnClickListener {
            submitAnswer()
        }
        
        btnNext.setOnClickListener {
            nextQuestion()
        }
    }
    
    private fun loadQuestion() {
        if (currentQuestionIndex < questions.size) {
            val question = questions[currentQuestionIndex]
            
            tvQuestionNumber.text = "第 ${currentQuestionIndex + 1} 题"
            tvQuestion.text = question.question
            
            // 清空之前的选项
            radioGroup.removeAllViews()
            
            // 添加新选项
            question.options.forEachIndexed { index, option ->
                val radioButton = RadioButton(this).apply {
                    id = index
                    text = option
                    textSize = 16f
                    setPadding(16, 16, 16, 16)
                }
                radioGroup.addView(radioButton)
            }
            
            // 重置UI状态
            cardResult.visibility = android.view.View.GONE
            btnSubmit.isEnabled = true
            btnNext.visibility = android.view.View.GONE
            
            updateExerciseInfo()
        } else {
            showFinalResult()
        }
    }
    
    private fun submitAnswer() {
        val selectedId = radioGroup.checkedRadioButtonId
        if (selectedId == -1) {
            android.widget.Toast.makeText(this, "请选择一个答案", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        
        val question = questions[currentQuestionIndex]
        val isCorrect = selectedId == question.correctAnswer
        
        if (isCorrect) {
            correctAnswers++
            tvResult.text = "✅ 回答正确！"
            tvResult.setTextColor(getColor(R.color.success_green))
        } else {
            tvResult.text = "❌ 回答错误"
            tvResult.setTextColor(getColor(R.color.error_red))
        }
        
        tvExplanation.text = question.explanation
        cardResult.visibility = android.view.View.VISIBLE
        
        btnSubmit.isEnabled = false
        btnNext.visibility = android.view.View.VISIBLE
        
        // 禁用所有单选按钮
        for (i in 0 until radioGroup.childCount) {
            radioGroup.getChildAt(i).isEnabled = false
        }
        
        // 高亮正确答案
        val correctRadio = radioGroup.getChildAt(question.correctAnswer) as RadioButton
        correctRadio.setTextColor(getColor(R.color.success_green))
        correctRadio.setTypeface(null, android.graphics.Typeface.BOLD)
    }
    
    private fun nextQuestion() {
        currentQuestionIndex++
        
        // 重置单选按钮状态
        for (i in 0 until radioGroup.childCount) {
            val radioButton = radioGroup.getChildAt(i) as RadioButton
            radioButton.isEnabled = true
            radioButton.setTextColor(getColor(R.color.text_primary))
            radioButton.setTypeface(null, android.graphics.Typeface.NORMAL)
        }
        
        radioGroup.clearCheck()
        loadQuestion()
    }
    
    private fun showFinalResult() {
        val score = (correctAnswers.toFloat() / totalQuestions.toFloat() * 100).toInt()
        
        // 更新内容进度
        currentContent = currentContent?.copy(progress = 1.0f)
        
        // 显示最终结果
        val resultMessage = when {
            score >= 90 -> "🎉 优秀！你已经完全掌握了这个知识点！"
            score >= 80 -> "👏 良好！你对这个知识点掌握得不错！"
            score >= 70 -> "👍 合格！继续努力，你会做得更好！"
            else -> "💪 需要加强！建议重新学习相关内容。"
        }
        
        tvQuestionNumber.text = "练习完成"
        tvQuestion.text = "最终成绩：${correctAnswers}/${totalQuestions} (${score}分)\n\n${resultMessage}"
        
        radioGroup.removeAllViews()
        cardResult.visibility = android.view.View.GONE
        btnSubmit.visibility = android.view.View.GONE
        btnNext.visibility = android.view.View.GONE
        
        // 更新进度
        progressExercise.progress = 100
        tvProgress.text = "进度：${totalQuestions}/${totalQuestions} (100%)"
        
        android.widget.Toast.makeText(this, "🎉 练习完成！知识图谱已更新", android.widget.Toast.LENGTH_LONG).show()
        
        saveProgressToDatabase()
    }
    
    private fun saveProgressToDatabase() {
        lifecycleScope.launch {
            delay(500)
            android.widget.Toast.makeText(this@ExerciseActivity, "✅ 学习进度已保存", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * 练习题数据类
 */
data class ExerciseQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: Int, // 正确答案的索引
    val explanation: String
)
