package com.example.educationapp.debug

import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.educationapp.R
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

/**
 * 🔍 API测试和诊断界面
 */
class ApiTestActivity : AppCompatActivity() {
    
    private lateinit var tvResults: TextView
    private lateinit var btnRunDiagnostic: MaterialButton
    private lateinit var scrollView: ScrollView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_api_test)
        
        initViews()
        setupClickListeners()
    }
    
    private fun initViews() {
        tvResults = findViewById(R.id.tvResults)
        btnRunDiagnostic = findViewById(R.id.btnRunDiagnostic)
        scrollView = findViewById(R.id.scrollView)
        
        // 设置工具栏
        supportActionBar?.apply {
            title = "API诊断工具"
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun setupClickListeners() {
        btnRunDiagnostic.setOnClickListener {
            runDiagnostic()
        }
    }
    
    private fun runDiagnostic() {
        btnRunDiagnostic.isEnabled = false
        btnRunDiagnostic.text = "诊断中..."
        tvResults.text = "🔍 开始API诊断...\n\n"
        
        lifecycleScope.launch {
            try {
                // 1. 运行基础诊断
                val diagnostic = ApiDiagnosticTool(this@ApiTestActivity)
                val results = diagnostic.runFullDiagnostic()
                val report = diagnostic.generateReport(results)
                
                tvResults.text = report + "\n\n" + "🔑 正在验证API密钥..."
                
                // 2. 详细的API密钥验证
                val validator = ApiKeyValidator()
                val validationResult = validator.validateApiKey()
                val validationReport = validator.generateValidationReport(validationResult)
                
                tvResults.text = report + "\n\n" + validationReport
                
                scrollView.post {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                }
                
            } catch (e: Exception) {
                tvResults.text = "❌ 诊断过程中出现错误:\n${e.message}"
            } finally {
                btnRunDiagnostic.isEnabled = true
                btnRunDiagnostic.text = "重新诊断"
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
