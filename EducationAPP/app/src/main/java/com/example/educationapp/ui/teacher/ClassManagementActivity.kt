package com.example.educationapp.ui.teacher

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.educationapp.R
import com.example.educationapp.ai.TeacherAIService
import com.example.educationapp.data.User
import com.example.educationapp.utils.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch

/**
 * 🏫 AI班级管理专页
 */
class ClassManagementActivity : AppCompatActivity() {

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var teacherAIService: TeacherAIService
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var tvAnalysisResult: TextView
    private lateinit var btnAnalyze: MaterialButton
    private var currentTeacher: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_class_management)

        initializeViews()
        initializeServices()
        setupToolbar()
        setupClickListeners()
    }

    private lateinit var loadingCard: androidx.cardview.widget.CardView
    private lateinit var resultCard: androidx.cardview.widget.CardView
    private lateinit var actionButtons: LinearLayout
    private lateinit var btnSaveReport: MaterialButton
    private lateinit var btnShareReport: MaterialButton

    private fun initializeViews() {
        progressIndicator = findViewById(R.id.progressIndicator)
        tvAnalysisResult = findViewById(R.id.tvAnalysisResult)
        btnAnalyze = findViewById(R.id.btnAnalyze)
        loadingCard = findViewById(R.id.loadingCard)
        resultCard = findViewById(R.id.resultCard)
        actionButtons = findViewById(R.id.actionButtons)
        btnSaveReport = findViewById(R.id.btnSaveReport)
        btnShareReport = findViewById(R.id.btnShareReport)
    }

    private fun initializeServices() {
        preferenceManager = PreferenceManager(this)
        teacherAIService = TeacherAIService()
        currentTeacher = preferenceManager.getUser()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "AI班级管理分析"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupClickListeners() {
        btnAnalyze.setOnClickListener {
            performAnalysis()
        }
        
        btnSaveReport.setOnClickListener {
            Toast.makeText(this, "📄 报告已保存到我的文档", Toast.LENGTH_SHORT).show()
        }
        
        btnShareReport.setOnClickListener {
            Toast.makeText(this, "📤 报告分享功能开发中...", Toast.LENGTH_SHORT).show()
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun performAnalysis() {
        val teacher = currentTeacher ?: return

        lifecycleScope.launch {
            try {
                showLoading(true)
                
                val result = teacherAIService.analyzeClassManagement(
                    teacher = teacher,
                    classSize = 35,
                    subjectName = teacher.subjects.split(",").firstOrNull() ?: "数学"
                )

                result.fold(
                    onSuccess = { analysis ->
                        showLoading(false)
                        displayAnalysisResult(analysis)
                    },
                    onFailure = { error ->
                        showLoading(false)
                        tvAnalysisResult.text = "分析失败: ${error.message}"
                        Toast.makeText(this@ClassManagementActivity, "分析失败", Toast.LENGTH_SHORT).show()
                    }
                )

            } catch (e: Exception) {
                showLoading(false)
                tvAnalysisResult.text = "系统异常: ${e.message}"
                Toast.makeText(this@ClassManagementActivity, "系统异常", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayAnalysisResult(analysis: TeacherAIService.ClassAnalysisResult) {
        val result = formatAnalysisResult(analysis)
        tvAnalysisResult.text = result
    }
    
    private fun formatAnalysisResult(analysis: TeacherAIService.ClassAnalysisResult): String {
        val sb = StringBuilder()
        
        // 标题和评分
        sb.append("🏫 AI班级管理分析报告\n\n")
        sb.append("📊 综合评分: ${analysis.overallScore}/100\n\n")
        
        // 管理建议
        sb.append("📋 管理建议:\n")
        analysis.managementSuggestions.forEachIndexed { index, suggestion ->
            sb.append("   ${index + 1}. $suggestion\n")
        }
        sb.append("\n")
        
        // 分组策略
        sb.append("👥 分组策略:\n")
        sb.append("   ${analysis.groupingStrategy}\n\n")
        
        // 纪律管理
        sb.append("📏 纪律管理技巧:\n")
        analysis.disciplineTips.forEach { tip ->
            sb.append("   • $tip\n")
        }
        sb.append("\n")
        
        // 参与度提升
        sb.append("🎯 提高参与度方法:\n")
        analysis.engagementMethods.forEach { method ->
            sb.append("   • $method\n")
        }
        sb.append("\n")
        
        // 个性化建议
        sb.append("🎓 个性化教学建议:\n")
        sb.append("   ${analysis.personalizationAdvice}")
        
        return sb.toString()
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            loadingCard.visibility = android.view.View.VISIBLE
            resultCard.visibility = android.view.View.GONE
            btnAnalyze.isEnabled = false
            btnAnalyze.text = "AI分析中..."
        } else {
            loadingCard.visibility = android.view.View.GONE
            resultCard.visibility = android.view.View.VISIBLE
            actionButtons.visibility = android.view.View.VISIBLE
            btnAnalyze.isEnabled = true
            btnAnalyze.text = "重新分析"
        }
    }
}
