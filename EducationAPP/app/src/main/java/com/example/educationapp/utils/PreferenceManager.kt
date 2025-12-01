package com.example.educationapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.educationapp.data.User
import com.google.gson.Gson

class PreferenceManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val PREF_NAME = "education_app_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER = "user"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_ROLE = "user_role"
    }
    
    fun setLoggedIn(isLoggedIn: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }
    
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit()
            .putString(KEY_USER, userJson)
            .putLong(KEY_USER_ID, user.id)
            .putString(KEY_USER_ROLE, user.userType.name)
            .apply()
    }
    
    fun getUser(): User? {
        val userJson = sharedPreferences.getString(KEY_USER, null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else {
            null
        }
    }
    
    fun getUserId(): Long {
        return sharedPreferences.getLong(KEY_USER_ID, -1)
    }
    
    fun getUserName(): String {
        return getUser()?.name ?: "用户"
    }
    
    fun getUserRole(): String {
        return sharedPreferences.getString(KEY_USER_ROLE, "STUDENT") ?: "STUDENT"
    }
    
    fun getUserGrade(): String? {
        val user = getUser()
        return when {
            user?.name == "张小明" -> "七年级"
            user?.name == "李老师" -> "教师"
            getUserRole() == "STUDENT" -> "七年级"
            getUserRole() == "TEACHER" -> "教师"
            else -> "七年级"
        }
    }
    
    fun clearUser() {
        sharedPreferences.edit()
            .remove(KEY_USER)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_ROLE)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }
}
