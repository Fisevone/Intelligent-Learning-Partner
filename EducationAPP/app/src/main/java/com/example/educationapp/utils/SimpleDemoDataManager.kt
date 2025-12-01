package com.example.educationapp.utils

import android.content.Context

/**
 * 简化的演示数据管理器
 */
class SimpleDemoDataManager(private val context: Context) {
    
    private val preferenceManager = PreferenceManager(context)
    
    /**
     * 获取学习统计数据
     */
    fun getLearningStats(): Map<String, Any> {
        return mapOf(
            "current_streak" to 7,
            "average_score" to 83.2f,
            "completed_contents" to 67,
            "total_contents" to 120,
            "weekly_goal" to 10,
            "this_week_time" to (7 * 60 * 60 * 1000L)
        )
    }
    
    /**
     * 获取各科目进度
     */
    fun getSubjectProgress(): Map<String, Float> {
        return mapOf(
            "数学" to 0.75f,
            "英语" to 0.68f,
            "物理" to 0.82f,
            "语文" to 0.61f,
            "化学" to 0.45f,
            "生物" to 0.71f
        )
    }
    
    /**
     * 获取情绪监测数据
     */
    fun getEmotionData(): Map<String, Any> {
        return mapOf(
            "current_emotion" to "专注",
            "current_focus" to 8.5f,
            "current_stress" to 2.1f,
            "current_confidence" to 7.8f
        )
    }
    
    /**
     * 获取用户资料
     */
    fun getUserProfile(): Map<String, Any> {
        val userType = preferenceManager.getUserName().lowercase()
        android.util.Log.d("DemoData", "当前用户名: $userType")
        
        return if (userType.contains("student") || userType == "张小明") {
            mapOf(
                "name" to "张小明",
                "grade" to "七年级",
                "class" to "七年级3班",
                "school" to "实验中学"
            )
        } else if (userType.contains("teacher") || userType == "李老师") {
            mapOf(
                "name" to "李老师",
                "subject" to "数学",
                "title" to "高级教师",
                "school" to "实验中学",
                "total_students" to 45
            )
        } else {
            // 默认为学生账号
            mapOf(
                "name" to "张小明",
                "grade" to "七年级",
                "class" to "七年级3班",
                "school" to "实验中学"
            )
        }
    }
}
