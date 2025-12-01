package com.example.educationapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recommendations")
data class Recommendation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val resourceId: Long,
    val reason: String, // 推荐理由
    val score: Float, // 推荐分数
    val isViewed: Boolean = false,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
