package com.example.educationapp.ui.teacher

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.educationapp.R
import com.example.educationapp.ai.TeacherAIService
import com.example.educationapp.data.EducationDatabase
import com.example.educationapp.service.DataManagementService
import com.example.educationapp.utils.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch

/**
 * 📈 教学分析报告页面
 */
class TeachingAnalysisActivity : AppCompatActivity() {

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var teacherAIService: TeacherAIService
    private lateinit var dataService: DataManagementService
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var tvAnalysisReport: TextView
    private lateinit var btnGenerateReport: MaterialButton
    private lateinit var btnRefreshData: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teaching_analysis)

        initializeViews()
        initializeServices()
        setupToolbar()
        setupClickListeners()
        showInitialInfo()
    }

    private fun initializeViews() {
        progressIndicator = findViewById(R.id.progressIndicator)
        tvAnalysisReport = findViewById(R.id.tvAnalysisReport)
        btnGenerateReport = findViewById(R.id.btnGenerateReport)
        btnRefreshData = findViewById(R.id.btnRefreshData)
    }

    private fun initializeServices() {
        preferenceManager = PreferenceManager(this)
        teacherAIService = TeacherAIService()
        val database = EducationDatabase.getDatabase(this)
        dataService = DataManagementService.getInstance(this, database)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "教学分析报告"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupClickListeners() {
        btnGenerateReport.setOnClickListener {
            generateTeachingAnalysis()
        }

        btnRefreshData.setOnClickListener {
            refreshDataStatistics()
        }
    }

    /**
     * 📋 显示初始信息
     */
    private fun showInitialInfo() {
        refreshDataStatistics()
    }

    /**
     * 📊 刷新数据统计
     */
    private fun refreshDataStatistics() {
        lifecycleScope.launch {
            try {
                showLoading(true, "正在加载数据统计...")

                val statsResult = dataService.getDataStatistics()
                val studentsResult = dataService.getAllStudents()
                val teachersResult = dataService.getAllTeachers()

                val stats = statsResult.getOrNull()
                val students = studentsResult.getOrNull() ?: emptyList()
                val teachers = teachersResult.getOrNull() ?: emptyList()

                if (stats != null) {
                    displayDataOverview(stats, students, teachers)
                } else {
                    tvAnalysisReport.text = "数据加载失败，请重试"
                }

            } catch (e: Exception) {
                tvAnalysisReport.text = "系统异常: ${e.message}"
            } finally {
                showLoading(false)
            }
        }
    }

    /**
     * 📊 显示数据概览
     */
    private fun displayDataOverview(
        stats: DataManagementService.DataStatistics,
        students: List<com.example.educationapp.data.User>,
        teachers: List<com.example.educationapp.data.User>
    ) {
        val currentTeacher = preferenceManager.getUser()?.name ?: "教师"
        
        // 计算一些教学统计数据
        val activeStudents = students.filter { 
            System.currentTimeMillis() - it.lastLoginTime < 7 * 24 * 60 * 60 * 1000 // 7天内活跃
        }
        val gradeDistribution = students.groupBy { it.grade }
        val subjectDistribution = students.flatMap { it.subjects.split(",") }
            .filter { it.isNotBlank() }
            .groupBy { it.trim() }

        val overview = buildString {
            // 标题部分
            appendLine("📊 AI教学分析报告")
            appendLine("基于真实数据的智能教学效果分析与优化建议")
            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            
            // 教师信息
            appendLine("👨‍🏫 教师: $currentTeacher")
            appendLine("📅 报告生成时间: ${formatTime(System.currentTimeMillis())}")
            appendLine()
            
            // 基础数据统计
            appendLine("📊 基础数据统计:")
            appendLine("    • 学生总数: ${stats.studentCount} 人")
            appendLine("    • 活跃学生: ${activeStudents.size} 人 (7天内)")
            appendLine("    • 学习记录: ${stats.learningRecordCount} 条")
            appendLine("    • 进度记录: ${stats.progressRecordCount} 条")
            appendLine()
            
            // 学生分布分析 - 可视化显示
            appendLine("👥 学生分布分析:")
            gradeDistribution.entries.sortedByDescending { it.value.size }.forEach { (grade, studentList) ->
                val count = studentList.size
                val percentage = if (stats.studentCount > 0) (count * 100 / stats.studentCount) else 0
                val bar = "█".repeat((percentage / 5).coerceAtMost(20))
                val spaces = " ".repeat(20 - bar.length)
                appendLine("    • $grade: ${count}人 (${percentage}%) [$bar$spaces]")
            }
            appendLine()
            
            // 科目覆盖情况
            appendLine("📚 科目覆盖情况:")
            subjectDistribution.entries.take(6).sortedByDescending { it.value.size }.forEach { (subject, studentList) ->
                val count = studentList.size
                val percentage = if (stats.studentCount > 0) (count * 100 / stats.studentCount) else 0
                appendLine("    • $subject: ${count}人学习 (${percentage}%)")
            }
            appendLine()
            
            // 活跃度分析 - 可视化显示
            val activityRate = if (stats.studentCount > 0) String.format("%.1f", activeStudents.size * 100.0 / stats.studentCount).toDouble() else 0.0
            val avgRecords = if (stats.studentCount > 0) stats.learningRecordCount / stats.studentCount else 0
            
            appendLine("📈 活跃度分析:")
            appendLine("    • 活跃率: ${activityRate}%")
            val activityBar = "█".repeat((activityRate / 5).toInt().coerceAtMost(20))
            val activitySpaces = " ".repeat(20 - activityBar.length)
            appendLine("    • 活跃度可视化: [$activityBar$activitySpaces]")
            appendLine("    • 平均学习记录: $avgRecords 条/人")
            appendLine("    • 学习参与度: ${when {
                activityRate >= 80 -> "优秀 🏆"
                activityRate >= 60 -> "良好 👍"
                activityRate >= 40 -> "一般 📈"
                else -> "需改进 ⚠️"
            }}")
            appendLine()
            
            // 教学建议
            appendLine("💡 智能教学建议:")
            val suggestions = generateQuickSuggestions(stats, activeStudents.size, students.size)
            suggestions.split("• ").filter { it.isNotBlank() }.forEach { suggestion ->
                appendLine("    ✓ $suggestion")
            }
            appendLine()
            
            // 数据洞察
            appendLine("🔍 数据洞察:")
            appendLine("    📊 最活跃年级: ${gradeDistribution.maxByOrNull { it.value.size }?.key ?: "暂无数据"}")
            appendLine("    📚 热门科目: ${subjectDistribution.maxByOrNull { it.value.size }?.key ?: "暂无数据"}")
            appendLine("    📈 整体趋势: ${if (activityRate > 50) "积极向上" else "有待提升"}")
            appendLine()
            
            // 底部信息
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            append("🤖 点击\"生成AI分析报告\"获取更详细的智能教学效果分析")
        }

        tvAnalysisReport.text = overview
    }

    /**
     * 💡 生成快速建议
     */
    private fun generateQuickSuggestions(
        stats: DataManagementService.DataStatistics,
        activeStudents: Int,
        totalStudents: Int
    ): String {
        val suggestions = mutableListOf<String>()

        val activityRate = if (totalStudents > 0) activeStudents * 100.0 / totalStudents else 0.0
        
        when {
            totalStudents == 0 -> suggestions.add("建议先生成学生数据以便进行教学分析")
            activityRate < 30 -> suggestions.add("学生活跃度较低，建议增加互动性教学活动")
            activityRate < 60 -> suggestions.add("部分学生参与度不高，可考虑个性化教学策略")
            else -> suggestions.add("学生整体活跃度良好，可适当提升教学难度")
        }

        if (stats.learningRecordCount < totalStudents * 5) {
            suggestions.add("学习记录偏少，建议增加课后练习和作业")
        }

        if (stats.progressRecordCount > 0) {
            suggestions.add("进度追踪系统运行正常，建议定期查看学习效果")
        }

        return suggestions.joinToString("\n• ", "• ")
    }

    /**
     * 🤖 生成AI教学分析
     */
    private fun generateTeachingAnalysis() {
        val teacher = preferenceManager.getUser() ?: return

        lifecycleScope.launch {
            try {
                showLoading(true, "AI正在分析教学数据...")
                Toast.makeText(this@TeachingAnalysisActivity, "🤖 AI正在生成详细教学分析报告...", Toast.LENGTH_SHORT).show()

                val result = teacherAIService.generateTeachingAnalysis(
                    teacher = teacher,
                    subjectName = teacher.subjects.split(",").firstOrNull() ?: "综合"
                )

                result.fold(
                    onSuccess = { analysis ->
                        displayTeachingAnalysis(analysis)
                    },
                    onFailure = { error ->
                        tvAnalysisReport.text = "AI分析失败: ${error.message}\n\n${tvAnalysisReport.text}"
                        Toast.makeText(this@TeachingAnalysisActivity, "分析失败", Toast.LENGTH_SHORT).show()
                    }
                )

            } catch (e: Exception) {
                tvAnalysisReport.text = "系统异常: ${e.message}\n\n${tvAnalysisReport.text}"
                Toast.makeText(this@TeachingAnalysisActivity, "系统异常", Toast.LENGTH_SHORT).show()
            } finally {
                showLoading(false)
            }
        }
    }

    /**
     * 📊 显示AI教学分析结果
     */
    private fun displayTeachingAnalysis(analysis: TeacherAIService.TeachingAnalysisResult) {
        val analysisReport = buildString {
            // 标题部分
            appendLine("🤖 AI深度教学分析报告")
            appendLine("基于智谱AI GLM-4的专业教学效果评估与优化建议")
            appendLine()
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine()
            
            // 教学效果评分 - 可视化显示
            val score = analysis.teachingEffectiveness.score
            appendLine("📊 教学效果综合评分: $score/100")
            val scoreBar = "█".repeat((score / 5).coerceAtMost(20))
            val scoreSpaces = " ".repeat(20 - scoreBar.length)
            appendLine("    评分可视化: [$scoreBar$scoreSpaces] ${when {
                score >= 90 -> "卓越 🏆"
                score >= 80 -> "优秀 🌟"
                score >= 70 -> "良好 👍"
                score >= 60 -> "合格 📈"
                else -> "需改进 ⚠️"
            }}")
            appendLine()
            
            // 效果分析
            appendLine("📈 深度效果分析:")
            appendLine("    ${analysis.teachingEffectiveness.description}")
            appendLine()
            
            // 学生正面反馈
            appendLine("👍 学生正面反馈:")
            analysis.studentFeedback.positive.forEachIndexed { index, feedback ->
                appendLine("    ${index + 1}. ✓ $feedback")
            }
            appendLine()
            
            // 改进建议
            appendLine("📝 专业改进建议:")
            analysis.studentFeedback.areasForImprovement.forEach { suggestion ->
                appendLine("    🔸 $suggestion")
            }
            appendLine()
            
            // 推荐教学方法
            appendLine("🎯 AI推荐教学方法:")
            analysis.teachingMethods.forEach { method ->
                appendLine("    ⭐ $method")
            }
            appendLine()
            
            // 课程改进建议
            appendLine("📚 课程优化建议:")
            analysis.courseImprovements.forEachIndexed { index, improvement ->
                appendLine("    ${index + 1}. 📋 $improvement")
            }
            appendLine()
            
            // 未来发展规划
            appendLine("🚀 未来发展规划:")
            appendLine("    ${analysis.futurePlanning}")
            appendLine()
            
            // 综合评价
            appendLine("🏆 AI综合评价:")
            appendLine("    ${analysis.overallRating}")
            appendLine()
            
            // 数据分析摘要
            val positiveCount = analysis.studentFeedback.positive.size
            val improvementCount = analysis.studentFeedback.areasForImprovement.size
            val methodCount = analysis.teachingMethods.size
            
            appendLine("📊 分析数据摘要:")
            appendLine("    • 正面反馈项: ${positiveCount}个")
            appendLine("    • 改进建议项: ${improvementCount}个")
            appendLine("    • 推荐方法数: ${methodCount}个")
            appendLine("    • 综合建议度: ${if (improvementCount < positiveCount) "积极" else "需关注"}")
            appendLine()
            
            // 底部信息
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("📅 报告生成时间: ${formatTime(System.currentTimeMillis())}")
            appendLine("🤖 分析模型: 智谱AI GLM-4")
            append("⚡ 分析精度: 专业级教学效果评估")
        }

        tvAnalysisReport.text = analysisReport
    }

    private fun showLoading(show: Boolean, message: String = "") {
        progressIndicator.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        btnGenerateReport.isEnabled = !show
        btnRefreshData.isEnabled = !show

        if (show) {
            btnGenerateReport.text = "AI分析中..."
            if (message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        } else {
            btnGenerateReport.text = "生成AI分析报告"
        }
    }

    private fun formatTime(timestamp: Long): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
    }
}
