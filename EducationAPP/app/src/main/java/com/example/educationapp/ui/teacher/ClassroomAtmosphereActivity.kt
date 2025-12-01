package com.example.educationapp.ui.teacher

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.educationapp.R
import com.example.educationapp.databinding.ActivityClassroomAtmosphereBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * 👨‍🏫 AI课堂氛围分析 - 教师专用功能
 */
class ClassroomAtmosphereActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityClassroomAtmosphereBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassroomAtmosphereBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupClickListeners()
        loadClassroomData()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "AI课堂氛围分析"
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupClickListeners() {
        // 开始实时分析
        binding.btnStartAnalysis.setOnClickListener {
            startRealTimeAnalysis()
        }
        
        // 查看详细报告
        binding.btnDetailedReport.setOnClickListener {
            showDetailedReport()
        }
        
        // 刷新数据
        binding.btnRefreshData.setOnClickListener {
            loadClassroomData()
        }
    }
    
    private fun loadClassroomData() {
        lifecycleScope.launch {
            try {
                binding.progressLoading.visibility = android.view.View.VISIBLE
                
                // 模拟加载班级数据
                delay(1500)
                
                // 更新UI显示
                updateClassroomStats()
                updateStudentEngagement()
                updateAtmosphereAnalysis()
                
                binding.progressLoading.visibility = android.view.View.GONE
                
            } catch (e: Exception) {
                binding.progressLoading.visibility = android.view.View.GONE
                android.widget.Toast.makeText(this@ClassroomAtmosphereActivity, 
                    "数据加载失败: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateClassroomStats() {
        // 班级基础统计
        binding.tvTotalStudents.text = "42"
        binding.tvActiveStudents.text = "38"
        binding.tvEngagementRate.text = "90%"
        binding.tvAttentionLevel.text = "85%"
        
        // 更新进度条
        binding.progressEngagement.progress = 90
        binding.progressAttention.progress = 85
        binding.progressParticipation.progress = 78
    }
    
    private fun updateStudentEngagement() {
        // 学生参与度分析
        binding.tvHighEngagement.text = "26人"
        binding.tvMediumEngagement.text = "12人"
        binding.tvLowEngagement.text = "4人"
        
        // 需要关注的学生
        binding.tvAtRiskStudents.text = """
            🔴 需要重点关注:
            • 张三 - 连续3天参与度低于50%
            • 李四 - 答题正确率下降明显
            • 王五 - 课堂互动较少
        """.trimIndent()
    }
    
    private fun updateAtmosphereAnalysis() {
        // AI氛围分析结果
        binding.tvAtmosphereScore.text = "82分"
        binding.tvAtmosphereLevel.text = "良好"
        
        binding.tvAtmosphereInsights.text = """
            📊 课堂氛围分析:
            
            ✅ 优势:
            • 学生整体参与度较高
            • 互动频率适中
            • 学习专注度良好
            
            ⚠️ 需要改进:
            • 部分学生回答问题较为被动
            • 小组讨论环节可以更活跃
            • 建议增加趣味性教学元素
            
            💡 建议措施:
            • 采用更多互动式教学方法
            • 关注参与度较低的学生
            • 适当调整教学节奏
        """.trimIndent()
    }
    
    private fun startRealTimeAnalysis() {
        binding.btnStartAnalysis.text = "分析中..."
        binding.btnStartAnalysis.isEnabled = false
        
        lifecycleScope.launch {
            try {
                // 模拟实时分析过程
                for (i in 1..10) {
                    delay(500)
                    binding.tvRealTimeStatus.text = "正在分析学生行为数据... ${i * 10}%"
                    binding.progressRealTime.progress = i * 10
                }
                
                binding.tvRealTimeStatus.text = "✅ 实时分析完成！"
                binding.tvRealTimeResults.text = """
                    📈 实时分析结果:
                    
                    🎯 当前课堂状态: 活跃
                    👥 参与学生数量: 35/42
                    ⏱️ 平均注意力持续时间: 12分钟
                    💬 互动频次: 每5分钟3次
                    📱 设备使用情况: 学习相关80%
                    
                    🚨 实时提醒:
                    • 后排3名学生注意力分散
                    • 建议在15分钟后进行互动环节
                """.trimIndent()
                
            } catch (e: Exception) {
                binding.tvRealTimeStatus.text = "❌ 分析失败"
            } finally {
                binding.btnStartAnalysis.text = "开始实时分析"
                binding.btnStartAnalysis.isEnabled = true
            }
        }
    }
    
    private fun showDetailedReport() {
        // 暂时使用Toast，后续可以创建详细报告页面
        Toast.makeText(this, "📊 详细报告功能开发中", Toast.LENGTH_SHORT).show()
    }
}
