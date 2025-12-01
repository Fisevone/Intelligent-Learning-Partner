package com.example.educationapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.educationapp.R
import com.example.educationapp.auth.AuthenticationManager
import com.example.educationapp.data.UserType
import com.example.educationapp.data.EducationDatabase
import com.example.educationapp.ui.main.MainActivity
import com.example.educationapp.ui.teacher.TeacherMainActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

/**
 * 登录界面
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var authManager: AuthenticationManager
    
    // UI组件
    private lateinit var radioStudent: MaterialRadioButton
    private lateinit var radioTeacher: MaterialRadioButton
    private lateinit var tilUsername: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etUsername: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var progressIndicator: CircularProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            android.util.Log.d("LoginActivity", "🚀 启动LoginActivity")
            setContentView(R.layout.activity_login)
            android.util.Log.d("LoginActivity", "✅ 布局加载成功")
            
            initializeAuth()
            initViews()
            setupClickListeners()
            
            android.util.Log.d("LoginActivity", "✅ LoginActivity初始化完成")
            
        } catch (e: Exception) {
            android.util.Log.e("LoginActivity", "❌ LoginActivity启动失败: ${e.message}", e)
            // 显示一个简单的错误界面
            try {
                setContentView(android.R.layout.activity_list_item)
            } catch (ex: Exception) {
                android.util.Log.e("LoginActivity", "❌ 连基础布局都无法加载", ex)
            }
        }
    }

    private fun initializeAuth() {
        val database = EducationDatabase.getDatabase(this)
        authManager = AuthenticationManager(this, database.userDao())
    }

    private fun initViews() {
        radioStudent = findViewById(R.id.radioStudent)
        radioTeacher = findViewById(R.id.radioTeacher)
        tilUsername = findViewById(R.id.tilUsername)
        tilPassword = findViewById(R.id.tilPassword)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressIndicator = findViewById(R.id.progressIndicator)
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            performLogin()
        }

        findViewById<View>(R.id.tvRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<View>(R.id.tvForgotPassword).setOnClickListener {
            // TODO: 实现忘记密码功能
            Toast.makeText(this, "忘记密码功能即将上线", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkExistingLogin() {
        authManager.isLoggedIn.observe(this) { isLoggedIn ->
            if (isLoggedIn) {
                navigateToMainScreen()
            }
        }
    }

    private fun performLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val userType = if (radioStudent.isChecked) UserType.STUDENT else UserType.TEACHER

        // 🚀 快速测试：如果是默认账户，直接创建并登录
        if (username == "student" && password == "123456") {
            createTestStudentAndLogin()
            return
        }
        
        // 🎓 教师测试账户
        if (username == "teacher" && password == "123456") {
            createTestTeacherAndLogin()
            return
        }

        // 验证输入
        if (!validateInput(username, password)) {
            return
        }

        // 显示加载状态
        showLoading(true)

        lifecycleScope.launch {
            try {
                val result = authManager.login(username, password, userType)
                
                result.onSuccess { user ->
                    showLoading(false)
                    Toast.makeText(
                        this@LoginActivity,
                        "欢迎回来，${user.name}！",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    navigateToMainScreen()
                    
                }.onFailure { error ->
                    showLoading(false)
                    showError(error.message ?: "登录失败")
                }
                
            } catch (e: Exception) {
                showLoading(false)
                showError("网络连接异常，请重试")
            }
        }
    }

    private fun createTestStudentAndLogin() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                // 先尝试注册测试学生
                val registerResult = authManager.registerStudent(
                    username = "student",
                    email = "student@test.com",
                    password = "123456",
                    name = "测试学生",
                    grade = "大学",
                    school = "测试学校"
                )
                
                // 无论注册成功还是失败（可能已存在），都尝试登录
                val loginResult = authManager.login("student", "123456", UserType.STUDENT)
                
                loginResult.onSuccess { user ->
                    showLoading(false)
                    Toast.makeText(
                        this@LoginActivity,
                        "登录成功！欢迎 ${user.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToMainScreen()
                }.onFailure { error ->
                    showLoading(false)
                    showError("登录失败: ${error.message}")
                }
                
            } catch (e: Exception) {
                showLoading(false)
                showError("创建测试账户失败: ${e.message}")
            }
        }
    }

    private fun createTestTeacherAndLogin() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                // 先尝试注册测试教师
                val registerResult = authManager.registerTeacher(
                    username = "teacher",
                    email = "teacher@test.com",
                    password = "123456",
                    name = "张老师",
                    school = "测试学校",
                    subjects = "数学,物理,英语",
                    gradeRange = "高中",
                    teacherCode = "TEACHER2024"
                )
                
                // 无论注册成功还是失败（可能已存在），都尝试登录
                val loginResult = authManager.login("teacher", "123456", UserType.TEACHER)
                
                loginResult.onSuccess { user ->
                    showLoading(false)
                    Toast.makeText(
                        this@LoginActivity,
                        "教师登录成功！欢迎 ${user.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    navigateToMainScreen()
                    
                }.onFailure { error ->
                    showLoading(false)
                    showError("教师登录失败: ${error.message}")
                }
                
            } catch (e: Exception) {
                showLoading(false)
                showError("教师测试账户创建失败: ${e.message}")
            }
        }
    }

    private fun validateInput(username: String, password: String): Boolean {
        var isValid = true

        // 验证用户名
        if (username.isEmpty()) {
            tilUsername.error = "请输入用户名"
            isValid = false
        } else {
            tilUsername.error = null
        }

        // 验证密码
        if (password.isEmpty()) {
            tilPassword.error = "请输入密码"
            isValid = false
        } else if (password.length < 6) {
            tilPassword.error = "密码长度至少6位"
            isValid = false
        } else {
            tilPassword.error = null
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            progressIndicator.visibility = View.VISIBLE
            btnLogin.isEnabled = false
            btnLogin.text = "登录中..."
        } else {
            progressIndicator.visibility = View.GONE
            btnLogin.isEnabled = true
            btnLogin.text = "登录"
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToMainScreen() {
        // 确保登录状态已保存到PreferenceManager
        val user = authManager.currentUser.value
        if (user != null) {
            val preferenceManager = com.example.educationapp.utils.PreferenceManager(this)
            preferenceManager.saveUser(user)
            preferenceManager.setLoggedIn(true)
        }
        
        val userType = authManager.getCurrentUserType()
        val intent = when (userType) {
            UserType.STUDENT -> Intent(this, MainActivity::class.java)
            UserType.TEACHER -> Intent(this, TeacherMainActivity::class.java)
            null -> {
                // 如果无法确定用户类型，重新登录
                authManager.logout()
                return
            }
        }
        
        startActivity(intent)
        finish()
    }
}