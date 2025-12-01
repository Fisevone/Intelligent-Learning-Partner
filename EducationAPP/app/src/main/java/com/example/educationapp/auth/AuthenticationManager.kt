package com.example.educationapp.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.educationapp.data.User
import com.example.educationapp.data.UserType
import com.example.educationapp.data.dao.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

/**
 * 身份认证管理器 - 处理登录、注册、权限验证
 */
class AuthenticationManager(
    private val context: Context,
    private val userDao: UserDao
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser
    
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn
    
    companion object {
        private const val TAG = "AuthManager"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_TYPE = "user_type"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
    }
    
    init {
        // 检查是否已登录
        checkLoginStatus()
    }
    
    /**
     * 学生注册
     */
    suspend fun registerStudent(
        username: String,
        email: String,
        password: String,
        name: String,
        grade: String,
        school: String,
        classId: String = "",
        learningStyle: String = "visual",
        interests: String = ""
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            // 检查用户名是否已存在
            val existingUser = userDao.getUserByUsername(username)
            if (existingUser != null) {
                return@withContext Result.failure(Exception("用户名已存在"))
            }
            
            // 检查邮箱是否已存在
            val existingEmail = userDao.getUserByEmail(email)
            if (existingEmail != null) {
                return@withContext Result.failure(Exception("邮箱已被注册"))
            }
            
            // 创建学生用户
            val hashedPassword = hashPassword(password)
            val student = User(
                username = username,
                email = email,
                password = hashedPassword,
                name = name,
                userType = UserType.STUDENT,
                grade = grade,
                school = school,
                classId = classId,
                learningStyle = learningStyle,
                interests = interests,
                createdAt = System.currentTimeMillis()
            )
            
            val userId = userDao.insertUser(student)
            val newStudent = student.copy(id = userId)
            
            Log.d(TAG, "学生注册成功: $username")
            Result.success(newStudent)
            
        } catch (e: Exception) {
            Log.e(TAG, "学生注册失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 教师注册
     */
    suspend fun registerTeacher(
        username: String,
        email: String,
        password: String,
        name: String,
        school: String,
        subjects: String,
        gradeRange: String,
        teacherCode: String // 教师验证码
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            // 验证教师验证码
            if (!validateTeacherCode(teacherCode)) {
                return@withContext Result.failure(Exception("教师验证码无效"))
            }
            
            // 检查用户名是否已存在
            val existingUser = userDao.getUserByUsername(username)
            if (existingUser != null) {
                return@withContext Result.failure(Exception("用户名已存在"))
            }
            
            // 检查邮箱是否已存在
            val existingEmail = userDao.getUserByEmail(email)
            if (existingEmail != null) {
                return@withContext Result.failure(Exception("邮箱已被注册"))
            }
            
            // 创建教师用户
            val hashedPassword = hashPassword(password)
            val teacher = User(
                username = username,
                email = email,
                password = hashedPassword,
                name = name,
                userType = UserType.TEACHER,
                grade = gradeRange,
                school = school,
                subjects = subjects,
                createdAt = System.currentTimeMillis()
            )
            
            val userId = userDao.insertUser(teacher)
            val newTeacher = teacher.copy(id = userId)
            
            Log.d(TAG, "教师注册成功: $username")
            Result.success(newTeacher)
            
        } catch (e: Exception) {
            Log.e(TAG, "教师注册失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 用户登录
     */
    suspend fun login(
        username: String,
        password: String,
        userType: UserType
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            val user = userDao.getUserByUsername(username)
            if (user == null) {
                return@withContext Result.failure(Exception("用户不存在"))
            }
            
            // 验证用户类型
            if (user.userType != userType) {
                val expectedType = if (userType == UserType.STUDENT) "学生" else "教师"
                return@withContext Result.failure(Exception("请使用${expectedType}身份登录"))
            }
            
            // 验证密码
            val hashedPassword = hashPassword(password)
            if (user.password != hashedPassword) {
                return@withContext Result.failure(Exception("密码错误"))
            }
            
            // 更新最后登录时间
            val updatedUser = user.copy(lastLoginTime = System.currentTimeMillis())
            userDao.updateUser(updatedUser)
            
            // 保存登录状态
            saveLoginState(updatedUser)
            
            Log.d(TAG, "用户登录成功: $username (${userType.name})")
            Result.success(updatedUser)
            
        } catch (e: Exception) {
            Log.e(TAG, "登录失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 退出登录
     */
    fun logout() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_USER_TYPE)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
        
        _currentUser.postValue(null)
        _isLoggedIn.postValue(false)
        
        Log.d(TAG, "用户已退出登录")
    }
    
    /**
     * 检查登录状态
     */
    private fun checkLoginStatus() {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (isLoggedIn) {
            val userId = prefs.getLong(KEY_USER_ID, -1)
            if (userId != -1L) {
                // 从数据库加载用户信息
                loadCurrentUser(userId)
            }
        }
        _isLoggedIn.postValue(isLoggedIn)
    }
    
    /**
     * 加载当前用户
     */
    private fun loadCurrentUser(userId: Long) {
        // 这里应该使用协程，但为了简化示例，暂时省略
        // 在实际应用中，应该在适当的协程作用域中调用
    }
    
    /**
     * 保存登录状态
     */
    private fun saveLoginState(user: User) {
        prefs.edit()
            .putLong(KEY_USER_ID, user.id)
            .putString(KEY_USER_TYPE, user.userType.name)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
        
        _currentUser.postValue(user)
        _isLoggedIn.postValue(true)
    }
    
    /**
     * 密码哈希
     */
    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashBytes = md.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    /**
     * 验证教师验证码
     */
    private fun validateTeacherCode(code: String): Boolean {
        // 这里可以实现更复杂的验证逻辑
        // 比如从服务器验证，或者使用预设的验证码列表
        val validCodes = listOf(
            "TEACHER2024",
            "EDU_ADMIN",
            "SCHOOL_STAFF",
            "INSTRUCTOR"
        )
        return validCodes.contains(code.uppercase())
    }
    
    /**
     * 获取当前用户类型
     */
    fun getCurrentUserType(): UserType? {
        val typeString = prefs.getString(KEY_USER_TYPE, null)
        return typeString?.let { UserType.valueOf(it) }
    }
    
    /**
     * 检查是否为教师
     */
    fun isTeacher(): Boolean {
        return getCurrentUserType() == UserType.TEACHER
    }
    
    /**
     * 检查是否为学生
     */
    fun isStudent(): Boolean {
        return getCurrentUserType() == UserType.STUDENT
    }
    
    /**
     * 验证邮箱格式
     */
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * 验证密码强度
     */
    fun isValidPassword(password: String): Pair<Boolean, String> {
        if (password.length < 6) {
            return false to "密码长度至少6位"
        }
        if (!password.any { it.isDigit() }) {
            return false to "密码必须包含数字"
        }
        if (!password.any { it.isLetter() }) {
            return false to "密码必须包含字母"
        }
        return true to "密码格式正确"
    }
}

