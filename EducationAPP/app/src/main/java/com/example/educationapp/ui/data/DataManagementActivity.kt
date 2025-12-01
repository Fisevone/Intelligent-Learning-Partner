package com.example.educationapp.ui.data

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.educationapp.R
import com.example.educationapp.data.EducationDatabase
import com.example.educationapp.service.DataManagementService
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch

/**
 * 📊 数据管理界面 - 生成和管理真实的教育数据
 */
class DataManagementActivity : AppCompatActivity() {

    private lateinit var dataService: DataManagementService
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var tvStatistics: TextView
    private lateinit var btnGenerateStudents: MaterialButton
    private lateinit var btnGenerateTeachers: MaterialButton
    private lateinit var btnViewData: MaterialButton
    private lateinit var btnClearData: MaterialButton
    private lateinit var btnRefreshStats: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_management)

        initializeViews()
        initializeServices()
        setupToolbar()
        setupClickListeners()
        refreshStatistics()
    }

    private fun initializeViews() {
        progressIndicator = findViewById(R.id.progressIndicator)
        tvStatistics = findViewById(R.id.tvStatistics)
        btnGenerateStudents = findViewById(R.id.btnGenerateStudents)
        btnGenerateTeachers = findViewById(R.id.btnGenerateTeachers)
        btnViewData = findViewById(R.id.btnViewData)
        btnClearData = findViewById(R.id.btnClearData)
        btnRefreshStats = findViewById(R.id.btnRefreshStats)
    }

    private fun initializeServices() {
        val database = EducationDatabase.getDatabase(this)
        dataService = DataManagementService.getInstance(this, database)
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "数据管理中心"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupClickListeners() {
        btnGenerateStudents.setOnClickListener {
            android.util.Log.d("DataManagement", "🎓 点击生成学生按钮")
            showGenerateStudentDialog()
        }

        btnGenerateTeachers.setOnClickListener {
            android.util.Log.d("DataManagement", "👨‍🏫 点击生成教师按钮")
            showGenerateTeacherDialog()
        }

        btnViewData.setOnClickListener {
            viewAllData()
        }

        btnClearData.setOnClickListener {
            showClearDataDialog()
        }

        btnRefreshStats.setOnClickListener {
            refreshStatistics()
        }
    }

    /**
     * 📊 刷新统计信息
     */
    private fun refreshStatistics() {
        lifecycleScope.launch {
            try {
                showLoading(true)
                val result = dataService.getDataStatistics()
                
                result.fold(
                    onSuccess = { stats ->
                        displayStatistics(stats)
                    },
                    onFailure = { error ->
                        tvStatistics.text = "获取统计信息失败: ${error.message}"
                    }
                )
            } catch (e: Exception) {
                tvStatistics.text = "系统异常: ${e.message}"
            } finally {
                showLoading(false)
            }
        }
    }

    /**
     * 📈 显示统计信息
     */
    private fun displayStatistics(stats: DataManagementService.DataStatistics) {
        val statisticsText = """
            📊 数据库统计信息
            ═══════════════════════════════
            
            👥 用户数据:
            • 学生用户: ${stats.studentCount} 人
            • 教师用户: ${stats.teacherCount} 人
            
            📚 学习数据:
            • 学习记录: ${stats.learningRecordCount} 条
            • 进度记录: ${stats.progressRecordCount} 条
            
            🕒 最后更新: ${formatTime(stats.lastUpdated)}
            
            ═══════════════════════════════
            💡 提示: 点击"生成数据"创建测试数据
        """.trimIndent()

        tvStatistics.text = statisticsText
    }

    /**
     * 🎓 显示生成学生数据对话框
     */
    private fun showGenerateStudentDialog() {
        android.util.Log.d("DataManagement", "📋 显示学生数量选择对话框")
        val options = arrayOf("20个学生", "50个学生(推荐)", "100个学生", "自定义数量")
        val counts = arrayOf(20, 50, 100, -1) // -1表示自定义

        AlertDialog.Builder(this)
            .setTitle("🎓 生成学生数据")
            .setSingleChoiceItems(options, -1) { dialog, which ->
                android.util.Log.d("DataManagement", "✅ 用户选择: ${options[which]} (${counts[which]})")
                dialog.dismiss()
                if (counts[which] == -1) {
                    // 自定义数量
                    showCustomStudentCountDialog()
                } else {
                    generateStudents(counts[which])
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 📝 显示自定义学生数量对话框
     */
    private fun showCustomStudentCountDialog() {
        val editText = android.widget.EditText(this).apply {
            hint = "请输入学生数量 (1-200)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText("50")
        }
        
        AlertDialog.Builder(this)
            .setTitle("自定义学生数量")
            .setMessage("请输入要生成的学生数量:")
            .setView(editText)
            .setPositiveButton("确定") { _, _ ->
                val countText = editText.text.toString()
                try {
                    val count = countText.toInt()
                    if (count in 1..200) {
                        generateStudents(count)
                    } else {
                        Toast.makeText(this, "请输入1-200之间的数字", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "请输入有效的数字", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 👨‍🏫 显示生成教师数据对话框
     */
    private fun showGenerateTeacherDialog() {
        android.util.Log.d("DataManagement", "📋 显示教师数量选择对话框")
        val options = arrayOf("5个教师", "10个教师(推荐)", "20个教师", "自定义数量")
        val counts = arrayOf(5, 10, 20, -1)

        AlertDialog.Builder(this)
            .setTitle("👨‍🏫 生成教师数据")
            .setSingleChoiceItems(options, -1) { dialog, which ->
                android.util.Log.d("DataManagement", "✅ 用户选择: ${options[which]} (${counts[which]})")
                dialog.dismiss()
                if (counts[which] == -1) {
                    // 自定义数量
                    showCustomTeacherCountDialog()
                } else {
                    generateTeachers(counts[which])
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 📝 显示自定义教师数量对话框
     */
    private fun showCustomTeacherCountDialog() {
        val editText = android.widget.EditText(this).apply {
            hint = "请输入教师数量 (1-50)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText("10")
        }
        
        AlertDialog.Builder(this)
            .setTitle("自定义教师数量")
            .setMessage("请输入要生成的教师数量:")
            .setView(editText)
            .setPositiveButton("确定") { _, _ ->
                val countText = editText.text.toString()
                try {
                    val count = countText.toInt()
                    if (count in 1..50) {
                        generateTeachers(count)
                    } else {
                        Toast.makeText(this, "请输入1-50之间的数字", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "请输入有效的数字", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 🎓 生成学生数据
     */
    private fun generateStudents(count: Int) {
        lifecycleScope.launch {
            try {
                showLoading(true)
                Toast.makeText(this@DataManagementActivity, "🤖 开始生成 $count 个学生数据...", Toast.LENGTH_SHORT).show()

                val result = dataService.generateStudentData(count)
                
                result.fold(
                    onSuccess = { students ->
                        android.util.Log.d("DataManagement", "✅ 成功生成 ${students.size} 个学生数据")
                        Toast.makeText(this@DataManagementActivity, "✅ 成功生成 ${students.size} 个学生数据", Toast.LENGTH_LONG).show()
                        refreshStatistics()
                    },
                    onFailure = { error ->
                        android.util.Log.e("DataManagement", "❌ 生成学生数据失败: ${error.message}", error)
                        Toast.makeText(this@DataManagementActivity, "❌ 生成失败: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(this@DataManagementActivity, "系统异常: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    /**
     * 👨‍🏫 生成教师数据
     */
    private fun generateTeachers(count: Int) {
        lifecycleScope.launch {
            try {
                showLoading(true)
                Toast.makeText(this@DataManagementActivity, "🤖 开始生成 $count 个教师数据...", Toast.LENGTH_SHORT).show()

                val result = dataService.generateTeacherData(count)
                
                result.fold(
                    onSuccess = { teachers ->
                        android.util.Log.d("DataManagement", "✅ 成功生成 ${teachers.size} 个教师数据")
                        Toast.makeText(this@DataManagementActivity, "✅ 成功生成 ${teachers.size} 个教师数据", Toast.LENGTH_LONG).show()
                        refreshStatistics()
                    },
                    onFailure = { error ->
                        android.util.Log.e("DataManagement", "❌ 生成教师数据失败: ${error.message}", error)
                        Toast.makeText(this@DataManagementActivity, "❌ 生成失败: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(this@DataManagementActivity, "系统异常: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    /**
     * 👀 查看所有数据
     */
    private fun viewAllData() {
        lifecycleScope.launch {
            try {
                showLoading(true)
                
                val studentsResult = dataService.getAllStudents()
                val teachersResult = dataService.getAllTeachers()
                
                val students = studentsResult.getOrNull() ?: emptyList()
                val teachers = teachersResult.getOrNull() ?: emptyList()
                
                showDataViewDialog(students, teachers)
                
            } catch (e: Exception) {
                Toast.makeText(this@DataManagementActivity, "获取数据失败: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    /**
     * 📋 显示数据查看对话框
     */
    private fun showDataViewDialog(students: List<com.example.educationapp.data.User>, teachers: List<com.example.educationapp.data.User>) {
        val dataText = buildString {
            appendLine("👥 学生数据 (${students.size}人):")
            appendLine("═".repeat(30))
            students.take(10).forEach { student ->
                appendLine("• ${student.name} (${student.grade}) - ${student.subjects}")
            }
            if (students.size > 10) {
                appendLine("... 还有 ${students.size - 10} 个学生")
            }
            
            appendLine("\n👨‍🏫 教师数据 (${teachers.size}人):")
            appendLine("═".repeat(30))
            teachers.forEach { teacher ->
                appendLine("• ${teacher.name} - ${teacher.subjects} (${teacher.school})")
            }
            
            if (students.isEmpty() && teachers.isEmpty()) {
                appendLine("暂无数据，请先生成测试数据")
            }
        }

        AlertDialog.Builder(this)
            .setTitle("📊 数据概览")
            .setMessage(dataText)
            .setPositiveButton("查看详细", null) // 可以扩展为详细查看页面
            .setNegativeButton("关闭", null)
            .show()
    }

    /**
     * 🗑️ 显示清除数据对话框
     */
    private fun showClearDataDialog() {
        AlertDialog.Builder(this)
            .setTitle("⚠️ 清除数据")
            .setMessage("确定要清除所有生成的测试数据吗？\n\n这将删除:\n• 所有生成的学生和教师\n• 所有学习记录和进度\n• 此操作不可恢复")
            .setPositiveButton("确定清除") { _, _ ->
                clearAllData()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 🗑️ 清除所有数据
     */
    private fun clearAllData() {
        lifecycleScope.launch {
            try {
                showLoading(true)
                Toast.makeText(this@DataManagementActivity, "🧹 开始清除数据...", Toast.LENGTH_SHORT).show()

                val result = dataService.clearAllTestData()
                
                result.fold(
                    onSuccess = {
                        Toast.makeText(this@DataManagementActivity, "✅ 数据清除完成", Toast.LENGTH_LONG).show()
                        refreshStatistics()
                    },
                    onFailure = { error ->
                        Toast.makeText(this@DataManagementActivity, "❌ 清除失败: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(this@DataManagementActivity, "系统异常: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    /**
     * 显示/隐藏加载状态
     */
    private fun showLoading(show: Boolean) {
        progressIndicator.visibility = if (show) android.view.View.VISIBLE else android.view.View.GONE
        btnGenerateStudents.isEnabled = !show
        btnGenerateTeachers.isEnabled = !show
        btnViewData.isEnabled = !show
        btnClearData.isEnabled = !show
        btnRefreshStats.isEnabled = !show
    }

    /**
     * 格式化时间
     */

    private fun formatTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "刚刚"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前"
            else -> "${diff / (24 * 60 * 60 * 1000)}天前"
        }
    }
}

