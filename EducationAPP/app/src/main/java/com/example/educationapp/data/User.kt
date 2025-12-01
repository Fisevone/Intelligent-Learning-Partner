package com.example.educationapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val email: String,
    val password: String,
    val name: String,
    val userType: UserType, // 用户类型：学生或教师
    val grade: String = "", // 学生：年级信息；教师：教学年级范围
    val learningStyle: String = "visual", // 仅学生使用
    val interests: String = "",
    val avatar: String = "",
    val school: String = "", // 学校信息
    val classId: String = "", // 学生：所属班级；教师：管理的班级
    val subjects: String = "", // 教师：教学科目；学生：学习科目
    val teacherId: String = "", // 仅学生使用，关联的教师ID
    val isActive: Boolean = true,
    val lastLoginTime: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class UserType {
    STUDENT,    // 学生
    TEACHER     // 教师
}
