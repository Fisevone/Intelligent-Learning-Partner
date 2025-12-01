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
import com.example.educationapp.ui.auth.LoginActivity
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

/**
 * 🎭 个人信息页面 - 使用增强版布局
 */
class ProfileFragment : Fragment() {
    
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 使用增强版布局
        return inflater.inflate(R.layout.fragment_profile_enhanced, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeComponents()
        setupBasicUI(view)
        setupClickListeners(view)
        loadUserData(view)
    }
    
    private fun initializeComponents() {
        preferenceManager = PreferenceManager(requireContext())
    }
    
    private fun setupBasicUI(view: View) {
        // 设置基本UI信息，使用真实的演示数据
        val userType = preferenceManager.getUserName().lowercase()
        val userName = if (userType.contains("student") || userType == "张小明") {
            "张小明"
        } else if (userType.contains("teacher") || userType == "李老师") {
            "李老师"
        } else {
            "张小明" // 默认为学生
        }
        
        view.findViewById<TextView>(R.id.tv_user_name)?.text = userName
        view.findViewById<TextView>(R.id.tv_user_grade)?.text = if (userName == "张小明") "七年级 · 理科班" else "数学教师 · 高级职称"
    }
    
    private fun setupClickListeners(view: View) {
        // 退出登录按钮
        view.findViewById<MaterialButton>(R.id.btn_logout)?.setOnClickListener {
            logout()
        }
        
        // 其他卡片点击事件
        view.findViewById<CardView>(R.id.card_learning_style)?.setOnClickListener {
            Toast.makeText(requireContext(), "🧠 学习风格设置功能开发中", Toast.LENGTH_SHORT).show()
        }
        
        view.findViewById<CardView>(R.id.card_interests)?.setOnClickListener {
            Toast.makeText(requireContext(), "⭐ 学习兴趣设置功能开发中", Toast.LENGTH_SHORT).show()
        }
        
        view.findViewById<CardView>(R.id.card_achievements)?.setOnClickListener {
            showAchievements()
        }
        
        view.findViewById<CardView>(R.id.card_settings)?.setOnClickListener {
            Toast.makeText(requireContext(), "⚙️ 应用设置功能开发中", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadUserData(view: View) {
        lifecycleScope.launch {
            try {
                // 模拟数据加载
                view.findViewById<TextView>(R.id.tv_study_days)?.text = "${(100..200).random()}天"
                view.findViewById<TextView>(R.id.tv_total_questions)?.text = String.format("%,d", (1000..2000).random())
                view.findViewById<TextView>(R.id.tv_accuracy_rate)?.text = "${(75..95).random()}%"
                view.findViewById<TextView>(R.id.tv_study_hours)?.text = "${(100..300).random()}h"
                view.findViewById<TextView>(R.id.tv_learning_style)?.text = "视觉型"
                view.findViewById<TextView>(R.id.tv_interests)?.text = "数学・物理"
                view.findViewById<TextView>(R.id.tv_achievements)?.text = "${(15..30).random()}个徽章"
                view.findViewById<TextView>(R.id.tv_wrong_count)?.text = "${(10..50).random()}"
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "数据加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showAchievements() {
        val achievements = """
            🏆 你的学习成就:
            
            🥇 连续学习者 - 连续学习30天
            🎯 精准射手 - 单日正确率达95%
            📚 知识探索者 - 完成100道题目
            🔥 学习狂人 - 单日学习超过3小时
            ⭐ 全能学霸 - 掌握5个知识领域
            
            还有更多成就等你解锁！
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🏆 学习成就")
            .setMessage(achievements)
            .setPositiveButton("继续努力", null)
            .show()
    }
    
    private fun logout() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("⚠️ 确认退出")
            .setMessage("确定要退出登录吗？")
            .setPositiveButton("确定退出") { _, _ ->
                performLogout()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun performLogout() {
        lifecycleScope.launch {
            try {
                preferenceManager.clearUser()
                Toast.makeText(requireContext(), "👋 已安全退出登录", Toast.LENGTH_SHORT).show()
                
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "退出失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}