package com.example.educationapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.cardview.widget.CardView
import com.example.educationapp.R
import com.example.educationapp.utils.PreferenceManager
import com.example.educationapp.data.EducationDatabase
import com.example.educationapp.ui.auth.LoginActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 🎭 增强版个人信息页面
 */
class ProfileFragmentEnhanced : Fragment() {
    
    private lateinit var preferenceManager: PreferenceManager
    
    // UI组件
    private lateinit var tvUserName: TextView
    private lateinit var tvUserGrade: TextView
    private lateinit var tvStudyDays: TextView
    private lateinit var tvTotalQuestions: TextView
    private lateinit var tvAccuracyRate: TextView
    private lateinit var tvStudyHours: TextView
    private lateinit var tvLearningStyle: TextView
    private lateinit var tvInterests: TextView
    private lateinit var tvAchievements: TextView
    private lateinit var tvWrongCount: TextView
    
    private lateinit var ivEditProfile: ImageView
    private lateinit var ivAvatar: ImageView
    
    private lateinit var cardLearningStyle: CardView
    private lateinit var cardInterests: CardView
    private lateinit var cardAchievements: CardView
    private lateinit var cardSettings: CardView
    
    private lateinit var btnLogout: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile_enhanced, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeComponents()
        initializeViews(view)
        setupClickListeners()
        loadUserData()
    }
    
    private fun initializeComponents() {
        preferenceManager = PreferenceManager(requireContext())
    }
    
    private fun initializeViews(view: View) {
        // 基本信息
        tvUserName = view.findViewById(R.id.tv_user_name)
        tvUserGrade = view.findViewById(R.id.tv_user_grade)
        tvStudyDays = view.findViewById(R.id.tv_study_days)
        tvTotalQuestions = view.findViewById(R.id.tv_total_questions)
        tvAccuracyRate = view.findViewById(R.id.tv_accuracy_rate)
        tvStudyHours = view.findViewById(R.id.tv_study_hours)
        
        // 设置信息
        tvLearningStyle = view.findViewById(R.id.tv_learning_style)
        tvInterests = view.findViewById(R.id.tv_interests)
        tvAchievements = view.findViewById(R.id.tv_achievements)
        tvWrongCount = view.findViewById(R.id.tv_wrong_count)
        
        // 图像组件
        ivEditProfile = view.findViewById(R.id.iv_edit_profile)
        ivAvatar = view.findViewById(R.id.iv_avatar)
        
        // 卡片组件
        cardLearningStyle = view.findViewById(R.id.card_learning_style)
        cardInterests = view.findViewById(R.id.card_interests)
        cardAchievements = view.findViewById(R.id.card_achievements)
        cardSettings = view.findViewById(R.id.card_settings)
        
        // 按钮组件
        btnLogout = view.findViewById(R.id.btn_logout)
    }
    
    private fun setupClickListeners() {
        // 编辑个人信息
        ivEditProfile.setOnClickListener {
            editProfile()
        }
        
        // 头像点击
        ivAvatar.setOnClickListener {
            changeAvatar()
        }
        
        // 学习风格设置
        cardLearningStyle.setOnClickListener {
            openLearningStyleSettings()
        }
        
        // 学习兴趣设置
        cardInterests.setOnClickListener {
            openInterestsSettings()
        }
        
        // 学习成就
        cardAchievements.setOnClickListener {
            openAchievements()
        }
        
        // 应用设置
        cardSettings.setOnClickListener {
            openAppSettings()
        }
        
        // 学习历史
        view?.findViewById<View>(R.id.layout_study_history)?.setOnClickListener {
            openStudyHistory()
        }
        
        // 错题本
        view?.findViewById<View>(R.id.layout_wrong_questions)?.setOnClickListener {
            openWrongQuestions()
        }
        
        // 数据导出
        view?.findViewById<View>(R.id.layout_data_export)?.setOnClickListener {
            exportData()
        }
        
        // 退出登录
        btnLogout.setOnClickListener {
            logout()
        }
    }
    
    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                // 获取用户信息
                val user = preferenceManager.getUser()
                val userName = user?.name ?: preferenceManager.getUserName()
                
                // 更新基本信息
                tvUserName.text = userName
                tvUserGrade.text = user?.grade ?: "高二年级 · 理科班"
                
                // 模拟学习数据（实际应用中应从数据库获取）
                loadLearningStatistics()
                
                // 更新个人设置
                tvLearningStyle.text = user?.learningStyle ?: "视觉型"
                tvInterests.text = user?.interests?.ifEmpty { "数学・物理" } ?: "数学・物理"
                
                // 加载其他数据
                loadAchievements()
                loadWrongQuestions()
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "数据加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private suspend fun loadLearningStatistics() {
        // 模拟学习统计数据
        val studyDays = (100..200).random()
        val totalQuestions = (1000..2000).random()
        val accuracyRate = (75..95).random()
        val studyHours = (100..300).random()
        
        tvStudyDays.text = "${studyDays}天"
        tvTotalQuestions.text = String.format("%,d", totalQuestions)
        tvAccuracyRate.text = "${accuracyRate}%"
        tvStudyHours.text = "${studyHours}h"
    }
    
    private fun loadAchievements() {
        // 模拟成就数据
        val achievementCount = (15..30).random()
        tvAchievements.text = "${achievementCount}个徽章"
    }
    
    private fun loadWrongQuestions() {
        // 模拟错题数量
        val wrongCount = (10..50).random()
        tvWrongCount.text = wrongCount.toString()
    }
    
    private fun editProfile() {
        Toast.makeText(requireContext(), "📝 个人信息编辑功能开发中", Toast.LENGTH_SHORT).show()
        // TODO: 实现个人信息编辑功能
    }
    
    private fun changeAvatar() {
        Toast.makeText(requireContext(), "📷 头像更换功能开发中", Toast.LENGTH_SHORT).show()
        // TODO: 实现头像更换功能
    }
    
    private fun openLearningStyleSettings() {
        val styles = arrayOf("视觉型学习者", "听觉型学习者", "动觉型学习者", "阅读型学习者")
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🧠 选择学习风格")
            .setItems(styles) { _, which ->
                tvLearningStyle.text = styles[which].replace("学习者", "")
                Toast.makeText(requireContext(), "学习风格已更新为：${styles[which]}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun openInterestsSettings() {
        val interests = arrayOf("数学", "物理", "化学", "生物", "英语", "语文", "历史", "地理", "政治", "编程")
        val checkedItems = booleanArrayOf(true, true, false, false, false, false, false, false, false, true)
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("⭐ 选择学习兴趣")
            .setMultiChoiceItems(interests, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("确定") { _, _ ->
                val selectedInterests = interests.filterIndexed { index, _ -> checkedItems[index] }
                tvInterests.text = selectedInterests.joinToString("・")
                Toast.makeText(requireContext(), "兴趣设置已更新", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun openAchievements() {
        val achievements = """
            🏆 你的学习成就:
            
            🥇 连续学习者 - 连续学习30天
            🎯 精准射手 - 单日正确率达95%
            📚 知识探索者 - 完成100道题目
            🔥 学习狂人 - 单日学习超过3小时
            ⭐ 全能学霸 - 掌握5个知识领域
            🚀 进步之星 - 月度进步最快
            💪 坚持达人 - 学习天数超过100天
            🎓 优秀学员 - 综合评分A+
            
            还有更多成就等你解锁！
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🏆 学习成就")
            .setMessage(achievements)
            .setPositiveButton("继续努力", null)
            .show()
    }
    
    private fun openAppSettings() {
        val settingsOptions = arrayOf(
            "🔔 通知设置",
            "🎨 主题设置", 
            "🌐 语言设置",
            "💾 缓存清理",
            "📊 数据同步",
            "🔒 隐私设置"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("⚙️ 应用设置")
            .setItems(settingsOptions) { _, which ->
                Toast.makeText(requireContext(), "${settingsOptions[which]} 功能开发中", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("关闭", null)
            .show()
    }
    
    private fun openStudyHistory() {
        val historyInfo = """
            📊 近期学习记录:
            
            📅 今日: 完成32道题，学习2.5小时
            📅 昨日: 完成28道题，学习2小时
            📅 前日: 完成35道题，学习3小时
            
            📈 本周统计:
            • 总学习时间: 15小时30分
            • 完成题目: 156道
            • 平均正确率: 87%
            • 学习科目: 数学、物理、英语
            
            📚 知识点掌握:
            • 函数基础: ⭐⭐⭐⭐⭐
            • 极限概念: ⭐⭐⭐⭐
            • 导数应用: ⭐⭐⭐
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📚 学习历史")
            .setMessage(historyInfo)
            .setPositiveButton("查看详细", null)
            .setNegativeButton("关闭", null)
            .show()
    }
    
    private fun openWrongQuestions() {
        val wrongQuestionsInfo = """
            ❌ 错题分析:
            
            📊 错题统计:
            • 数学: 15道 (主要: 导数计算)
            • 物理: 8道 (主要: 力学分析)
            
            🎯 薄弱知识点:
            • 复合函数求导
            • 牛顿第二定律应用
            • 极限的四则运算
            
            💡 改进建议:
            • 加强基础概念理解
            • 多做相关练习题
            • 定期复习错题
            
            📝 建议今日重点练习导数相关题目
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📝 错题本")
            .setMessage(wrongQuestionsInfo)
            .setPositiveButton("开始练习", null)
            .setNegativeButton("关闭", null)
            .show()
    }
    
    private fun exportData() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📤 数据导出")
            .setMessage("选择要导出的数据类型:")
            .setItems(arrayOf(
                "📊 学习报告 (PDF)",
                "📈 成绩统计 (Excel)", 
                "📝 错题集 (Word)",
                "🎯 完整数据包 (ZIP)"
            )) { _, which ->
                val types = arrayOf("学习报告", "成绩统计", "错题集", "完整数据包")
                Toast.makeText(requireContext(), "正在导出${types[which]}...", Toast.LENGTH_SHORT).show()
                
                // 模拟导出过程
                lifecycleScope.launch {
                    kotlinx.coroutines.delay(2000)
                    Toast.makeText(requireContext(), "📄 ${types[which]}导出成功！", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun logout() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("⚠️ 确认退出")
            .setMessage("确定要退出登录吗？\n\n退出后需要重新登录才能使用个性化功能。")
            .setPositiveButton("确定退出") { _, _ ->
                performLogout()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun performLogout() {
        lifecycleScope.launch {
            try {
                // 清除用户数据
                preferenceManager.clearUser()
                
                Toast.makeText(requireContext(), "👋 已安全退出登录", Toast.LENGTH_SHORT).show()
                
                // 跳转到登录页面
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "退出失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

