package com.example.educationapp.ui.interaction

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.educationapp.R
import com.example.educationapp.databinding.ActivityInteractionBinding

class InteractionActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityInteractionBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInteractionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupBottomNavigation()
        
        // 默认显示问答界面
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, QAFragment())
                .commit()
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "课堂互动"
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_qa -> {
                    replaceFragment(QAFragment())
                    true
                }
                R.id.nav_discussion -> {
                    replaceFragment(DiscussionFragment())
                    true
                }
                R.id.nav_vote -> {
                    replaceFragment(VoteFragment())
                    true
                }
                R.id.nav_quiz -> {
                    replaceFragment(QuizFragment())
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
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
