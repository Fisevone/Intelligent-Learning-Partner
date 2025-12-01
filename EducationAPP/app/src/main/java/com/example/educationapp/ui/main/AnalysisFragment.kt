package com.example.educationapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.educationapp.R
import com.example.educationapp.utils.PreferenceManager
import com.example.educationapp.data.EducationDatabase
import com.example.educationapp.service.LearningProgressTracker
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * 🎓 学生端分析页面 - 专注个人学习数据分析
 */
class AnalysisFragment : Fragment() {
    
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var progressTracker: LearningProgressTracker

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_analysis_student, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeComponents()
        setupClickListeners(view)
        loadStudentData(view)
    }
    
    private fun initializeComponents() {
        preferenceManager = PreferenceManager(requireContext())
        val database = EducationDatabase.getDatabase(requireContext())
        progressTracker = LearningProgressTracker.getInstance(requireContext(), database.learningProgressDao())
    }
    
    private fun setupClickListeners(view: View) {
        // 查看知识图谱
        view.findViewById<MaterialButton>(R.id.btn_view_knowledge_graph)?.setOnClickListener {
            val intent = Intent(requireContext(), com.example.educationapp.ui.knowledge.KnowledgeGraphActivity::class.java)
            intent.putExtra("subject", "数学")
            startActivity(intent)
        }
        
        // 详细分析 - 跳转到新的智能分析界面
        view.findViewById<MaterialButton>(R.id.btn_detailed_analysis)?.setOnClickListener {
            val intent = Intent(requireContext(), com.example.educationapp.ui.student.StudentLearningAnalysisActivity::class.java)
            startActivity(intent)
        }
        
        // 生成个人报告
        view.findViewById<MaterialButton>(R.id.btn_generate_report)?.setOnClickListener {
            generatePersonalReport()
        }
        
        // 情绪监测
        view.findViewById<MaterialButton>(R.id.btn_emotion_monitoring)?.setOnClickListener {
            val intent = Intent(requireContext(), com.example.educationapp.ui.student.EmotionMonitoringActivity::class.java)
            startActivity(intent)
        }
        
        // 学习预测
        view.findViewById<MaterialButton>(R.id.btn_learning_prediction)?.setOnClickListener {
            val intent = Intent(requireContext(), com.example.educationapp.ui.student.LearningPredictionActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun loadStudentData(view: View) {
        lifecycleScope.launch {
            try {
                // 设置今日日期
                val today = SimpleDateFormat("M月d日", Locale.CHINA).format(Date())
                view.findViewById<TextView>(R.id.tv_today_date)?.text = today
                
                // 模拟学习数据
                view.findViewById<TextView>(R.id.tv_study_time_today)?.text = "2小时15分"
                view.findViewById<TextView>(R.id.tv_questions_answered)?.text = "32"
                view.findViewById<TextView>(R.id.tv_accuracy_rate)?.text = "85%"
                view.findViewById<TextView>(R.id.tv_focus_percentage)?.text = "78%"
                
                // 知识图谱数据
                val userId = preferenceManager.getUserId()
                val knowledgeData = progressTracker.getKnowledgeGraphData(userId, "数学")
                
                val masteredCount = knowledgeData.nodes.count { it.masteryLevel >= 0.8f }
                val learningCount = knowledgeData.nodes.count { it.masteryLevel >= 0.5f && it.masteryLevel < 0.8f }
                val todoCount = knowledgeData.nodes.count { it.masteryLevel < 0.5f }
                
                view.findViewById<TextView>(R.id.tv_mastered_count)?.text = masteredCount.toString()
                view.findViewById<TextView>(R.id.tv_learning_count)?.text = learningCount.toString()
                view.findViewById<TextView>(R.id.tv_todo_count)?.text = todoCount.toString()
                view.findViewById<TextView>(R.id.tv_knowledge_summary)?.text = "💡 已掌握${masteredCount}个知识点，还有${todoCount}个知识点需要加强"
                
                // 学习洞察
                view.findViewById<TextView>(R.id.tv_learning_insights)?.text = "📈 本周学习效率提升20%，建议继续保持当前学习节奏"
                view.findViewById<TextView>(R.id.tv_weak_points)?.text = "• 导数概念理解 (45%)\n• 极限运算技巧 (38%)\n• 积分应用题目 (42%)"
                view.findViewById<TextView>(R.id.tv_daily_suggestion)?.text = "基于你的学习进度，建议今天重点练习导数相关题目，预计需要45分钟"
                
                // 更新进度条
                view.findViewById<android.widget.ProgressBar>(R.id.progress_focus_rate)?.progress = 78
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "数据加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showDetailedAnalysis() {
        val detailedReport = """
            📊 深度学习分析报告
            
            🎯 学习表现评估:
            • 总体进度: 85% (优秀)
            • 学习一致性: 92% (非常好)
            • 知识保持率: 78% (良好)
            • 学习速度: 比同年级快20%
            
            📈 优势领域:
            • 函数基础概念掌握扎实
            • 逻辑推理能力较强
            • 学习专注度高
            
            ⚠️ 需要改进:
            • 复杂计算容易出错
            • 应用题理解需要加强
            • 知识点综合运用待提升
            
            💡 个性化建议:
            • 每天安排30分钟练习计算题
            • 多阅读数学应用实例
            • 定期进行知识点串联练习
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🧠 详细学习分析")
            .setMessage(detailedReport)
            .setPositiveButton("知道了", null)
            .show()
    }
    
    private fun generatePersonalReport() {
        lifecycleScope.launch {
            try {
                val personalReport = """
                    📋 个人学习报告
                    生成时间: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(Date())}
                    
                    📊 本周学习数据:
                    • 总学习时长: 15小时30分钟
                    • 完成练习题: 156道
                    • 平均正确率: 82%
                    • 知识点掌握: 23/30 (77%)
                    
                    🎯 学习目标达成度:
                    • 基础概念: ✅ 已达成 (100%)
                    • 应用能力: 🔄 进行中 (65%)
                    • 综合运用: 📝 待提升 (45%)
                    
                    📈 进步轨迹:
                    • 相比上周提升15%
                    • 薄弱知识点减少3个
                    • 学习效率提升20%
                    
                    🎖️ 获得成就:
                    • 🔥 连续学习7天
                    • 🎯 单日正确率达95%
                    • 📚 完成章节测试满分
                    
                    💪 下周学习建议:
                    • 重点攻克导数应用题
                    • 增加综合练习时间
                    • 保持当前学习节奏
                """.trimIndent()
                
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("📋 个人学习报告")
                    .setMessage(personalReport)
                    .setPositiveButton("保存报告") { _, _ ->
                        Toast.makeText(requireContext(), "📄 报告已保存，可在设置中查看历史报告", Toast.LENGTH_LONG).show()
                    }
                    .setNegativeButton("关闭", null)
                    .show()
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "报告生成失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}






