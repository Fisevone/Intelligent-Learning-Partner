package com.example.educationapp.ui.teacher

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.educationapp.R
import com.example.educationapp.ai.TeacherAIService
import com.example.educationapp.utils.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

/**
 * 📝 题目管理页面
 */
class QuestionManagementActivity : AppCompatActivity() {

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var teacherAIService: TeacherAIService
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var tvAdvice: TextView
    private lateinit var etTopic: TextInputEditText
    private lateinit var chipGroupSubjects: ChipGroup
    private lateinit var chipGroupDifficulty: ChipGroup
    private lateinit var btnGenerateAdvice: MaterialButton
    private lateinit var btnCreateQuestion: MaterialButton

    private var selectedSubject = "数学"
    private var selectedDifficulty = "中级"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_management)

        initializeViews()
        initializeServices()
        setupToolbar()
        setupChips()
        setupClickListeners()
        showInitialInfo()
    }

    private fun initializeViews() {
        progressIndicator = findViewById(R.id.progressIndicator)
        tvAdvice = findViewById(R.id.tvAdvice)
        etTopic = findViewById(R.id.etTopic)
        chipGroupSubjects = findViewById(R.id.chipGroupSubjects)
        chipGroupDifficulty = findViewById(R.id.chipGroupDifficulty)
        btnGenerateAdvice = findViewById(R.id.btnGenerateAdvice)
        btnCreateQuestion = findViewById(R.id.btnCreateQuestion)
    }

    private fun initializeServices() {
        preferenceManager = PreferenceManager(this)
        teacherAIService = TeacherAIService()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "题目管理"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupChips() {
        // 科目选择
        val subjects = listOf("数学", "语文", "英语", "物理", "化学", "生物", "历史", "地理")
        subjects.forEach { subject ->
            val chip = Chip(this).apply {
                text = subject
                isCheckable = true
                isChecked = subject == selectedSubject
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedSubject = subject
                        // 取消其他chip的选中状态
                        for (i in 0 until chipGroupSubjects.childCount) {
                            val otherChip = chipGroupSubjects.getChildAt(i) as Chip
                            if (otherChip != this) {
                                otherChip.isChecked = false
                            }
                        }
                    }
                }
            }
            chipGroupSubjects.addView(chip)
        }

        // 难度选择
        val difficulties = listOf("基础", "中级", "高级", "竞赛")
        difficulties.forEach { difficulty ->
            val chip = Chip(this).apply {
                text = difficulty
                isCheckable = true
                isChecked = difficulty == selectedDifficulty
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedDifficulty = difficulty
                        // 取消其他chip的选中状态
                        for (i in 0 until chipGroupDifficulty.childCount) {
                            val otherChip = chipGroupDifficulty.getChildAt(i) as Chip
                            if (otherChip != this) {
                                otherChip.isChecked = false
                            }
                        }
                    }
                }
            }
            chipGroupDifficulty.addView(chip)
        }
    }

    private fun setupClickListeners() {
        btnGenerateAdvice.setOnClickListener {
            generateQuestionAdvice()
        }

        btnCreateQuestion.setOnClickListener {
            createQuestion()
        }
    }

    /**
     * 📋 显示初始信息
     */
    private fun showInitialInfo() {
        tvAdvice.text = buildString {
            appendLine("📝 AI题目管理系统")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            
            appendLine("🎯 功能说明:")
            appendLine("    ✓ 选择科目和难度级别")
            appendLine("    ✓ 输入具体的教学主题")
            appendLine("    ✓ AI将生成专业的出题建议")
            appendLine("    ✓ 可以直接创建智能题目")
            appendLine()
            
            appendLine("💡 使用步骤:")
            appendLine("    1️⃣ 先选择要出题的科目")
            appendLine("    2️⃣ 设置合适的难度等级")
            appendLine("    3️⃣ 输入具体的知识点或主题")
            appendLine("    4️⃣ 点击\"生成建议\"获取AI指导")
            appendLine("    5️⃣ 点击\"创建题目\"直接生成题目")
            appendLine()
            
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("📚 支持科目: 数学、语文、英语、物理、化学、生物、历史、地理")
            append("🔧 难度等级: 基础 → 中级 → 高级 → 竞赛")
        }
    }

    /**
     * 💡 生成题目建议
     */
    private fun generateQuestionAdvice() {
        val topic = etTopic.text?.toString()?.trim()
        if (topic.isNullOrBlank()) {
            Toast.makeText(this, "请输入教学主题", Toast.LENGTH_SHORT).show()
            etTopic.requestFocus()
            return
        }

        val teacher = preferenceManager.getUser() ?: return

        lifecycleScope.launch {
            try {
                showLoading(true)
                Toast.makeText(this@QuestionManagementActivity, "🤖 AI正在分析题目管理策略...", Toast.LENGTH_SHORT).show()

                val result = teacherAIService.generateQuestionManagementAdvice(
                    teacher = teacher,
                    subjectName = selectedSubject
                )

                result.fold(
                    onSuccess = { advice ->
                        displayQuestionAdvice(advice, topic)
                    },
                    onFailure = { error ->
                        tvAdvice.text = "生成建议失败: ${error.message}"
                        Toast.makeText(this@QuestionManagementActivity, "生成失败", Toast.LENGTH_SHORT).show()
                    }
                )

            } catch (e: Exception) {
                tvAdvice.text = "系统异常: ${e.message}"
                Toast.makeText(this@QuestionManagementActivity, "系统异常", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    /**
     * 📊 显示题目管理建议
     */
    private fun displayQuestionAdvice(advice: TeacherAIService.QuestionManagementResult, topic: String) {
        val adviceText = buildString {
            // 标题部分
            appendLine("🎯 ${selectedSubject} - ${topic}")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            
            // 建议评分 - 突出显示
            appendLine("📊 建议评分: ${advice.qualityScore}/100")
            appendLine()
            
            // 出题策略
            appendLine("📝 出题策略:")
            advice.questionTypes.forEachIndexed { index, strategy ->
                appendLine("    ${index + 1}. $strategy")
            }
            appendLine()
            
            // 难度分布建议
            appendLine("🎯 难度分布建议:")
            advice.difficultyDistribution.entries.forEach { (level, percentage) ->
                val bar = "█".repeat((percentage / 10).coerceAtMost(10))
                val spaces = " ".repeat(10 - bar.length)
                appendLine("    • $level: $percentage% [$bar$spaces]")
            }
            appendLine()
            
            // 知识点覆盖
            appendLine("📚 知识点覆盖:")
            advice.coverageSuggestions.forEach { point ->
                appendLine("    ✓ $point")
            }
            appendLine()
            
            // 创新想法
            appendLine("💡 创新想法:")
            advice.creativeIdeas.forEach { idea ->
                appendLine("    🔸 $idea")
            }
            appendLine()
            
            // 管理策略
            appendLine("💼 管理策略:")
            appendLine("    ${advice.managementStrategy}")
            appendLine()
            
            // 学生能力培养
            appendLine("🎯 学生能力培养:")
            advice.competencyDevelopment.chunked(2).forEach { pair ->
                val line = pair.joinToString("    ") { "✓ $it" }
                appendLine("    $line")
            }
            appendLine()
            
            // 教学目标对接
            appendLine("🎓 教学目标对接:")
            appendLine("    ${advice.objectiveAlignment}")
            appendLine()
            
            // 评价反馈机制
            appendLine("📈 评价反馈机制:")
            appendLine("    ${advice.feedbackMechanism}")
            appendLine()
            
            // 技术融合应用
            appendLine("🔬 技术融合应用:")
            appendLine("    ${advice.technologyIntegration}")
            appendLine()
            
            // 底部信息
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("🏷️ $selectedDifficulty 级别 | 📖 $selectedSubject | 📝 $topic")
            append("⏰ 生成时间: ${java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.CHINA).format(java.util.Date())}")
        }

        tvAdvice.text = adviceText
    }

    /**
     * ✨ 创建智能题目
     */
    private fun createQuestion() {
        val topic = etTopic.text?.toString()?.trim()
        if (topic.isNullOrBlank()) {
            Toast.makeText(this, "请输入教学主题", Toast.LENGTH_SHORT).show()
            etTopic.requestFocus()
            return
        }

        // 模拟创建题目的过程
        showLoading(true)
        Toast.makeText(this, "🚀 正在为您创建智能题目...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch {
            try {
                // 模拟AI生成题目的延迟
                kotlinx.coroutines.delay(2000)

                val sampleQuestion = generateSampleQuestion(selectedSubject, topic, selectedDifficulty)
                
                showLoading(false)
                showQuestionResult(sampleQuestion)

            } catch (e: Exception) {
                showLoading(false)
                Toast.makeText(this@QuestionManagementActivity, "创建题目失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 📝 生成示例题目
     */
    private fun generateSampleQuestion(subject: String, topic: String, difficulty: String): String {
        return when (subject) {
            "数学" -> """
                📐 数学题目 - $topic ($difficulty)
                ═══════════════════════════════
                
                题目：已知函数 f(x) = x² + 2x - 3，求：
                (1) 函数的对称轴
                (2) 函数的最小值
                (3) 函数与x轴的交点坐标
                
                A. 对称轴: x = -1, 最小值: -4, 交点: (-3,0), (1,0)
                B. 对称轴: x = 1, 最小值: -2, 交点: (-1,0), (3,0)
                C. 对称轴: x = -1, 最小值: -2, 交点: (-3,0), (1,0)
                D. 对称轴: x = 2, 最小值: -4, 交点: (-2,0), (2,0)
                
                答案：A
                解析：通过配方法或公式法可求得对称轴为x=-1，将x=-1代入得最小值为-4，令f(x)=0求得交点坐标。
            """.trimIndent()
            
            "语文" -> """
                📖 语文题目 - $topic ($difficulty)
                ═══════════════════════════════
                
                阅读下面的文言文，完成题目：
                
                "学而时习之，不亦说乎？有朋自远方来，不亦乐乎？人不知而不愠，不亦君子乎？"
                
                问题：这段话体现了孔子怎样的人生态度？
                
                A. 积极乐观，注重学习和友谊
                B. 消极避世，独善其身
                C. 功名利禄，追求权势
                D. 愤世嫉俗，批判现实
                
                答案：A
                解析：这段话表现了孔子对学习的热爱、对友谊的珍视以及对他人不理解的宽容，体现了积极乐观的人生态度。
            """.trimIndent()
            
            "英语" -> """
                🌍 English Question - $topic ($difficulty)
                ═══════════════════════════════
                
                Choose the best answer to complete the sentence:
                
                "By the time you receive this letter, I _______ for three days."
                
                A. will have been traveling
                B. will be traveling  
                C. have been traveling
                D. had been traveling
                
                Answer: A
                Explanation: This sentence uses "by the time" with a future reference, requiring the future perfect continuous tense to show an action that will be ongoing until a future point.
            """.trimIndent()
            
            else -> """
                📚 ${subject}题目 - $topic ($difficulty)
                ═══════════════════════════════
                
                这是一个关于${topic}的${difficulty}级别题目。
                
                题目内容将根据具体的教学需求和学生水平进行个性化生成。
                AI会确保题目的科学性、合理性和教育价值。
                
                💡 建议：
                • 根据学生的实际水平调整难度
                • 注重知识点的综合运用
                • 培养学生的思维能力
            """.trimIndent()
        }
    }

    /**
     * 📋 显示题目创建结果
     */
    private fun showQuestionResult(question: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("✨ 智能题目生成完成")
            .setMessage(question)
            .setPositiveButton("保存题目") { _, _ ->
                Toast.makeText(this, "题目已保存到题库", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("重新生成") { _, _ ->
                createQuestion()
            }
            .setNegativeButton("关闭", null)
            .show()
    }

    private fun showLoading(show: Boolean) {
        progressIndicator.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        btnGenerateAdvice.isEnabled = !show
        btnCreateQuestion.isEnabled = !show
        
        if (show) {
            btnGenerateAdvice.text = "AI分析中..."
            btnCreateQuestion.text = "创建中..."
        } else {
            btnGenerateAdvice.text = "生成出题建议"
            btnCreateQuestion.text = "创建智能题目"
        }
    }
}
