package com.example.educationapp.ui.teacher

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.educationapp.R
import com.example.educationapp.auth.AuthenticationManager
import com.example.educationapp.data.EducationDatabase
import com.example.educationapp.data.User
import com.example.educationapp.ai.TeacherAIService
import com.example.educationapp.ui.auth.LoginActivity
import com.example.educationapp.utils.PreferenceManager
import androidx.cardview.widget.CardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.launch

/**
 * 🎓 教师主界面 - AI驱动的智能教学工作台
 */
class TeacherMainActivity : AppCompatActivity() {

    private lateinit var authManager: AuthenticationManager
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var teacherAIService: TeacherAIService
    private var currentTeacher: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teacher_main)

        initializeServices()
        setupToolbar()
        setupClickListeners()
        loadTeacherInfo()
    }

    private fun initializeServices() {
        val database = EducationDatabase.getDatabase(this)
        authManager = AuthenticationManager(this, database.userDao())
        preferenceManager = PreferenceManager(this)
        teacherAIService = TeacherAIService()
        
        // 获取当前教师信息
        currentTeacher = preferenceManager.getUser()
    }
    
    private fun loadTeacherInfo() {
        currentTeacher?.let { teacher ->
            // 这里可以更新UI显示教师信息
            android.util.Log.d("TeacherMain", "当前教师: ${teacher.name}, 科目: ${teacher.subjects}")
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "教师工作台"
    }

    private fun setupClickListeners() {
        // 🏫 AI班级管理
        findViewById<CardView>(R.id.cardClassManagement).setOnClickListener {
            handleClassManagement()
        }

        // 📊 AI学生进度分析
        findViewById<CardView>(R.id.cardStudentProgress).setOnClickListener {
            handleStudentProgress()
        }

        // 📝 AI题目管理
        findViewById<CardView>(R.id.cardQuestionManagement).setOnClickListener {
            handleQuestionManagement()
        }

        // 📈 AI教学分析
        findViewById<CardView>(R.id.cardTeachingAnalysis).setOnClickListener {
            handleTeachingAnalysis()
        }

        // 👥 协作管理
        findViewById<CardView>(R.id.cardCollaborationManagement).setOnClickListener {
            handleCollaborationManagement()
        }

        // ⭐ AI创建新任务
        findViewById<ExtendedFloatingActionButton>(R.id.fabCreateTask).setOnClickListener {
            handleCreateTask()
        }
        
        // 🎭 AI课堂氛围分析 (如果布局中存在，暂时注释掉)
        // findViewById<MaterialCardView>(R.id.cardClassroomAtmosphere)?.setOnClickListener {
        //     handleClassroomAtmosphere()
        // }
        
        // 📊 数据管理 (如果布局中存在，暂时注释掉)
        // findViewById<MaterialCardView>(R.id.cardDataManagement)?.setOnClickListener {
        //     handleDataManagement()
        // }
    }
    
    /**
     * 🏫 AI班级管理分析 - 跳转到专门页面
     */
    private fun handleClassManagement() {
        val intent = Intent(this, ClassManagementActivity::class.java)
        startActivity(intent)
    }
    
    /**
     * 📊 AI学生进度分析 - 跳转到专门页面
     */
    private fun handleStudentProgress() {
        val intent = Intent(this, StudentProgressActivity::class.java)
        startActivity(intent)
    }
    
    /**
     * 📝 AI题目管理建议 - 跳转到专门页面
     */
    private fun handleQuestionManagement() {
        val intent = Intent(this, QuestionManagementActivity::class.java)
        startActivity(intent)
    }
    
    /**
     * 📈 AI教学分析报告 - 跳转到专门页面  
     */
    private fun handleTeachingAnalysis() {
        val intent = Intent(this, TeachingAnalysisActivity::class.java)
        startActivity(intent)
    }
    
    /**
     * 👥 协作管理 - 跳转到协作管理页面
     */
    private fun handleCollaborationManagement() {
        val intent = Intent(this, CollaborationManagementActivity::class.java)
        startActivity(intent)
    }
    
    /**
     * ⭐ AI创建新任务
     */
    private fun handleCreateTask() {
        val options = arrayOf("创建AI任务", "数据管理中心", "系统设置")
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🛠️ 管理功能")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        Toast.makeText(this, "🚀 AI任务创建功能开发中...", Toast.LENGTH_SHORT).show()
                    }
                    1 -> {
                        handleDataManagement()
                    }
                    2 -> {
                        Toast.makeText(this, "⚙️ 系统设置功能开发中...", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    /**
     * 🎭 AI课堂氛围分析
     */
    private fun handleClassroomAtmosphere() {
        val intent = Intent(this, com.example.educationapp.ui.teacher.ClassroomAtmosphereActivity::class.java)
        startActivity(intent)
    }
    
    /**
     * 📊 数据管理中心
     */
    private fun handleDataManagement() {
        val intent = Intent(this, com.example.educationapp.ui.data.DataManagementActivity::class.java)
        startActivity(intent)
    }
    
    /**
     * 显示班级管理分析结果
     */
    private fun showClassManagementResult(analysis: TeacherAIService.ClassAnalysisResult) {
        val message = """
            🏫 AI班级管理分析报告
            评分：${analysis.overallScore}/100
            
            📋 管理建议：
            ${analysis.managementSuggestions.mapIndexed { index, suggestion -> 
                "${index + 1}. $suggestion" 
            }.joinToString("\n")}
            
            👥 分组策略：
            ${analysis.groupingStrategy}
            
            📏 纪律管理：
            ${analysis.disciplineTips.joinToString("\n• ", "• ")}
            
            🎯 参与度提升：
            ${analysis.engagementMethods.joinToString("\n• ", "• ")}
            
            🎓 个性化建议：
            ${analysis.personalizationAdvice}
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🤖 AI班级管理分析")
            .setMessage(message)
            .setPositiveButton("收藏建议") { _, _ ->
                Toast.makeText(this, "📌 建议已收藏到我的资料库", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("关闭", null)
            .show()
    }
    
    /**
     * 显示学生进度分析结果
     */
    private fun showStudentProgressResult(progress: TeacherAIService.StudentProgressResult) {
        val message = """
            📊 AI学生进度分析报告
            整体进度：${progress.progressPercentage}%
            
            📈 整体情况：
            ${progress.overallProgress}
            
            ⭐ 优秀学生特征：
            ${progress.excellentStudents.joinToString("\n• ", "• ")}
            
            🆘 需要帮助的学生：
            ${progress.strugglingStudents.joinToString("\n• ", "• ")}
            
            💪 知识掌握强项：
            ${progress.knowledgeMastery.strongAreas.joinToString("、")}
            
            📝 需要加强领域：
            ${progress.knowledgeMastery.weakAreas.joinToString("、")}
            
            🎯 改进建议：
            ${progress.improvementSuggestions.mapIndexed { index, suggestion -> 
                "${index + 1}. $suggestion" 
            }.joinToString("\n")}
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("📊 AI学生进度分析")
            .setMessage(message)
            .setPositiveButton("制定计划") { _, _ ->
                Toast.makeText(this, "📋 AI正在为您制定个性化教学计划...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("关闭", null)
            .show()
    }
    
    /**
     * 显示题目管理建议结果
     */
    private fun showQuestionManagementResult(management: TeacherAIService.QuestionManagementResult) {
        val difficultyInfo = management.difficultyDistribution.map { (level, percentage) ->
            "$level: $percentage%"
        }.joinToString(", ")
        
        val message = """
            📝 AI题目管理建议
            质量评分：${management.qualityScore}/100
            
            📊 难度分布建议：
            $difficultyInfo
            
            📋 推荐题型：
            ${management.questionTypes.joinToString("、")}
            
            🎯 知识点覆盖：
            ${management.coverageSuggestions.mapIndexed { index, suggestion -> 
                "${index + 1}. $suggestion" 
            }.joinToString("\n")}
            
            💡 创新设计思路：
            ${management.creativeIdeas.mapIndexed { index, idea -> 
                "${index + 1}. $idea" 
            }.joinToString("\n")}
            
            📚 管理策略：
            ${management.managementStrategy}
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("📝 AI题目管理建议")
            .setMessage(message)
            .setPositiveButton("生成题目") { _, _ ->
                Toast.makeText(this, "🤖 AI正在根据建议生成题目...", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("关闭", null)
            .show()
    }
    
    /**
     * 显示教学分析报告结果
     */
    private fun showTeachingAnalysisResult(analysis: TeacherAIService.TeachingAnalysisResult) {
        val message = """
            📈 AI教学分析报告
            综合评级：${analysis.overallRating}
            
            🎯 教学效果：
            评分：${analysis.teachingEffectiveness.score}/100
            ${analysis.teachingEffectiveness.description}
            
            👥 学生反馈：
            ✅ 正面评价：
            ${analysis.studentFeedback.positive.joinToString("\n• ", "• ")}
            
            📝 改进建议：
            ${analysis.studentFeedback.areasForImprovement.joinToString("\n• ", "• ")}
            
            🎨 推荐教学方法：
            ${analysis.teachingMethods.joinToString("、")}
            
            🚀 课程改进建议：
            ${analysis.courseImprovements.mapIndexed { index, improvement -> 
                "${index + 1}. $improvement" 
            }.joinToString("\n")}
            
            🔮 未来发展规划：
            ${analysis.futurePlanning}
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("📈 AI教学分析报告")
            .setMessage(message)
            .setPositiveButton("保存报告") { _, _ ->
                Toast.makeText(this, "📄 报告已保存到我的文档", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("关闭", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.teacher_main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                // TODO: 打开个人资料页面
                true
            }
            R.id.action_settings -> {
                // TODO: 打开设置页面
                true
            }
            R.id.action_logout -> {
                performLogout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun performLogout() {
        lifecycleScope.launch {
            authManager.logout()
            startActivity(Intent(this@TeacherMainActivity, LoginActivity::class.java))
            finish()
        }
    }
}

