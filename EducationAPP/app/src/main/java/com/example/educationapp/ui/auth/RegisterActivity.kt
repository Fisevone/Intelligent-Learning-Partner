package com.example.educationapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.educationapp.R
import com.example.educationapp.auth.AuthenticationManager
import com.example.educationapp.data.UserType
import com.example.educationapp.data.EducationDatabase
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

/**
 * 注册界面
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var authManager: AuthenticationManager
    
    // UI组件
    private lateinit var radioGroupUserType: RadioGroup
    private lateinit var radioStudent: MaterialRadioButton
    private lateinit var radioTeacher: MaterialRadioButton
    
    private lateinit var tilName: TextInputLayout
    private lateinit var tilUsername: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var tilSchool: TextInputLayout
    
    private lateinit var etName: TextInputEditText
    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var etSchool: TextInputEditText
    
    // 学生专用
    private lateinit var cardStudentInfo: MaterialCardView
    private lateinit var tilGrade: TextInputLayout
    private lateinit var tilClass: TextInputLayout
    private lateinit var etGrade: TextInputEditText
    private lateinit var etClass: TextInputEditText
    
    // 教师专用
    private lateinit var cardTeacherInfo: MaterialCardView
    private lateinit var tilSubjects: TextInputLayout
    private lateinit var tilGradeRange: TextInputLayout
    private lateinit var tilTeacherCode: TextInputLayout
    private lateinit var etSubjects: TextInputEditText
    private lateinit var etGradeRange: TextInputEditText
    private lateinit var etTeacherCode: TextInputEditText
    
    private lateinit var btnRegister: MaterialButton
    private lateinit var progressIndicator: CircularProgressIndicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initializeAuth()
        initViews()
        setupClickListeners()
    }

    private fun initializeAuth() {
        val database = EducationDatabase.getDatabase(this)
        authManager = AuthenticationManager(this, database.userDao())
    }

    private fun initViews() {
        radioGroupUserType = findViewById(R.id.radioGroupUserType)
        radioStudent = findViewById(R.id.radioStudent)
        radioTeacher = findViewById(R.id.radioTeacher)
        
        tilName = findViewById(R.id.tilName)
        tilUsername = findViewById(R.id.tilUsername)
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)
        tilSchool = findViewById(R.id.tilSchool)
        
        etName = findViewById(R.id.etName)
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        etSchool = findViewById(R.id.etSchool)
        
        // 学生专用
        cardStudentInfo = findViewById(R.id.cardStudentInfo)
        tilGrade = findViewById(R.id.tilGrade)
        tilClass = findViewById(R.id.tilClass)
        etGrade = findViewById(R.id.etGrade)
        etClass = findViewById(R.id.etClass)
        
        // 教师专用
        cardTeacherInfo = findViewById(R.id.cardTeacherInfo)
        tilSubjects = findViewById(R.id.tilSubjects)
        tilGradeRange = findViewById(R.id.tilGradeRange)
        tilTeacherCode = findViewById(R.id.tilTeacherCode)
        etSubjects = findViewById(R.id.etSubjects)
        etGradeRange = findViewById(R.id.etGradeRange)
        etTeacherCode = findViewById(R.id.etTeacherCode)
        
        btnRegister = findViewById(R.id.btnRegister)
        progressIndicator = findViewById(R.id.progressIndicator)
    }

    private fun setupClickListeners() {
        // 身份类型切换
        radioGroupUserType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioStudent -> {
                    cardStudentInfo.visibility = View.VISIBLE
                    cardTeacherInfo.visibility = View.GONE
                    btnRegister.text = "创建学生账户"
                }
                R.id.radioTeacher -> {
                    cardStudentInfo.visibility = View.GONE
                    cardTeacherInfo.visibility = View.VISIBLE
                    btnRegister.text = "创建教师账户"
                }
            }
        }

        btnRegister.setOnClickListener {
            performRegister()
        }

        findViewById<View>(R.id.tvBackToLogin).setOnClickListener {
            finish()
        }
    }

    private fun performRegister() {
        val userType = if (radioStudent.isChecked) UserType.STUDENT else UserType.TEACHER
        
        // 获取基本信息
        val name = etName.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        val school = etSchool.text.toString().trim()

        // 验证基本输入
        if (!validateBasicInput(name, username, email, password, confirmPassword, school)) {
            return
        }

        // 显示加载状态
        showLoading(true)

        lifecycleScope.launch {
            try {
                val result = if (userType == UserType.STUDENT) {
                    registerStudent(name, username, email, password, school)
                } else {
                    registerTeacher(name, username, email, password, school)
                }
                
                result.onSuccess { user ->
                    showLoading(false)
                    Toast.makeText(
                        this@RegisterActivity,
                        "注册成功！欢迎加入智能教育平台，${user.name}！",
                        Toast.LENGTH_LONG
                    ).show()
                    
                    // 注册成功后返回登录页面
                    finish()
                    
                }.onFailure { error ->
                    showLoading(false)
                    showError(error.message ?: "注册失败")
                }
                
            } catch (e: Exception) {
                showLoading(false)
                showError("网络连接异常，请重试")
            }
        }
    }

    private suspend fun registerStudent(
        name: String,
        username: String,
        email: String,
        password: String,
        school: String
    ): Result<com.example.educationapp.data.User> {
        val grade = etGrade.text.toString().trim()
        val classId = etClass.text.toString().trim()

        // 验证学生专用信息
        if (grade.isEmpty()) {
            tilGrade.error = "请输入年级"
            return Result.failure(Exception("请完善学生信息"))
        }

        return authManager.registerStudent(
            username = username,
            email = email,
            password = password,
            name = name,
            grade = grade,
            school = school,
            classId = classId,
            learningStyle = "visual",
            interests = ""
        )
    }

    private suspend fun registerTeacher(
        name: String,
        username: String,
        email: String,
        password: String,
        school: String
    ): Result<com.example.educationapp.data.User> {
        val subjects = etSubjects.text.toString().trim()
        val gradeRange = etGradeRange.text.toString().trim()
        val teacherCode = etTeacherCode.text.toString().trim()

        // 验证教师专用信息
        var isValid = true
        if (subjects.isEmpty()) {
            tilSubjects.error = "请输入教学科目"
            isValid = false
        } else {
            tilSubjects.error = null
        }

        if (gradeRange.isEmpty()) {
            tilGradeRange.error = "请输入教学年级"
            isValid = false
        } else {
            tilGradeRange.error = null
        }

        if (teacherCode.isEmpty()) {
            tilTeacherCode.error = "请输入教师验证码"
            isValid = false
        } else {
            tilTeacherCode.error = null
        }

        if (!isValid) {
            return Result.failure(Exception("请完善教师信息"))
        }

        return authManager.registerTeacher(
            username = username,
            email = email,
            password = password,
            name = name,
            school = school,
            subjects = subjects,
            gradeRange = gradeRange,
            teacherCode = teacherCode
        )
    }

    private fun validateBasicInput(
        name: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        school: String
    ): Boolean {
        var isValid = true

        // 验证姓名
        if (name.isEmpty()) {
            tilName.error = "请输入真实姓名"
            isValid = false
        } else {
            tilName.error = null
        }

        // 验证用户名
        if (username.isEmpty()) {
            tilUsername.error = "请输入用户名"
            isValid = false
        } else if (username.length < 3) {
            tilUsername.error = "用户名长度至少3位"
            isValid = false
        } else {
            tilUsername.error = null
        }

        // 验证邮箱
        if (email.isEmpty()) {
            tilEmail.error = "请输入邮箱地址"
            isValid = false
        } else if (!authManager.isValidEmail(email)) {
            tilEmail.error = "邮箱格式不正确"
            isValid = false
        } else {
            tilEmail.error = null
        }

        // 验证密码
        val passwordValidation = authManager.isValidPassword(password)
        if (!passwordValidation.first) {
            tilPassword.error = passwordValidation.second
            isValid = false
        } else {
            tilPassword.error = null
        }

        // 验证确认密码
        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.error = "请确认密码"
            isValid = false
        } else if (password != confirmPassword) {
            tilConfirmPassword.error = "两次输入的密码不一致"
            isValid = false
        } else {
            tilConfirmPassword.error = null
        }

        // 验证学校
        if (school.isEmpty()) {
            tilSchool.error = "请输入学校名称"
            isValid = false
        } else {
            tilSchool.error = null
        }

        return isValid
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            progressIndicator.visibility = View.VISIBLE
            btnRegister.isEnabled = false
            btnRegister.text = "注册中..."
        } else {
            progressIndicator.visibility = View.GONE
            btnRegister.isEnabled = true
            btnRegister.text = if (radioStudent.isChecked) "创建学生账户" else "创建教师账户"
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}