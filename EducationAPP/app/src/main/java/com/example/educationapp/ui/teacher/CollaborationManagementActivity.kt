package com.example.educationapp.ui.teacher

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.educationapp.R
import com.example.educationapp.ui.main.CollaborationFragment
import com.example.educationapp.utils.PreferenceManager

/**
 * 教师端协作管理界面
 * 包含AI智能分组、实时协作监控等功能
 */
class CollaborationManagementActivity : AppCompatActivity() {
    
    private lateinit var preferenceManager: PreferenceManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collaboration_management)
        
        preferenceManager = PreferenceManager(this)
        
        setupToolbar()
        loadCollaborationFragment()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "协作管理"
        }
    }
    
    private fun loadCollaborationFragment() {
        // 验证教师权限
        if (preferenceManager.getUserRole() != "TEACHER") {
            Toast.makeText(this, "权限不足", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // 加载协作管理Fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CollaborationFragment())
            .commit()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

