package com.example.educationapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "learning_records")
data class LearningRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val subject: String,
    val topic: String,
    val duration: Long, // 学习时长（分钟）
    val score: Float, // 学习得分
    val difficulty: String, // easy, medium, hard
    val learningStyle: String, // 使用的学习方式
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
