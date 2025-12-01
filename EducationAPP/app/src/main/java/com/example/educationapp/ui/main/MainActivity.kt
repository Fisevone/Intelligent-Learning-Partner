package com.example.educationapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.educationapp.R
import com.example.educationapp.data.EducationDatabase
import com.example.educationapp.service.DataInitializationService
import com.example.educationapp.ui.auth.LoginActivity
import com.example.educationapp.utils.PreferenceManager
import com.example.educationapp.data.DemoDataInitializer
import com.example.educationapp.service.QuestionPreloadService
import com.example.educationapp.service.AIQuestionPreloadService
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

// Fragment imports
import com.example.educationapp.ui.main.HomeFragment
import com.example.educationapp.ui.main.AITeacherFragmentSimple
import com.example.educationapp.ui.main.StudentCollaborationFragment
import com.example.educationapp.ui.main.AnalysisFragment
import com.example.educationapp.ui.main.ProfileFragment
import com.example.educationapp.debug.ApiTestActivity

class MainActivity : AppCompatActivity() {
    
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var bottomNavigation: BottomNavigationView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        preferenceManager = PreferenceManager(this)
        
        // 检查登录状态
        if (!preferenceManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        // 检查用户类型，如果是教师则跳转到教师工作台
        val userRole = preferenceManager.getUserRole()
        if (userRole == "TEACHER") {
            startActivity(Intent(this, com.example.educationapp.ui.teacher.TeacherMainActivity::class.java))
            finish()
            return
        }
        
        setContentView(R.layout.activity_main_app)
        
        setupBottomNavigation()
        initializeData()
        
        // 默认显示首页
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
            
            // 设置底部导航选中首页
            bottomNavigation.selectedItemId = R.id.nav_home
        }
    }
    
    private fun initializeData() {
        // 🚫 暂时禁用所有自动数据初始化，防止生成大学数据
        // 只有在教师端手动刷新时才生成纯净的七年级数据
        lifecycleScope.launch {
            try {
                // 只启动必要的预加载服务，不生成任何用户数据
                QuestionPreloadService.startPreloading(this@MainActivity)
                AIQuestionPreloadService.startAIPreloading(this@MainActivity)
               } catch (e: Exception) {
                // 忽略初始化错误，不影响主要功能
            }
        }
    }
    
    private fun setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }
                R.id.nav_ai_teacher -> {
                    replaceFragment(AITeacherFragmentSimple())
                    true
                }
                R.id.nav_collaboration -> {
                    replaceFragment(StudentCollaborationFragment())
                    true
                }
                R.id.nav_analysis -> {
                    replaceFragment(AnalysisFragment())
                    true
                }
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }
    
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            R.id.action_debug -> {
                startActivity(Intent(this, ApiTestActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun logout() {
        preferenceManager.clearUser()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
