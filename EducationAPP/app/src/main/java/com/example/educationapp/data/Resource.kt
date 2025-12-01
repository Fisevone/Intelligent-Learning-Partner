package com.example.educationapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "resources")
data class Resource(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val type: String, // video, article, quiz, exercise
    val subject: String,
    val difficulty: String,
    val url: String,
    val thumbnail: String = "",
    val duration: Int = 0, // 资源时长（分钟）
    val rating: Float = 0f,
    val tags: String = "",
    val isRecommended: Boolean = false
)
