package com.example.educationapp.data.dao

import androidx.room.*
import com.example.educationapp.data.Resource
import kotlinx.coroutines.flow.Flow

@Dao
interface ResourceDao {
    @Query("SELECT * FROM resources WHERE subject = :subject AND difficulty = :difficulty")
    fun getResourcesBySubjectAndDifficulty(subject: String, difficulty: String): Flow<List<Resource>>

    @Query("SELECT * FROM resources WHERE isRecommended = 1")
    fun getRecommendedResources(): Flow<List<Resource>>

    @Query("SELECT * FROM resources WHERE type = :type")
    fun getResourcesByType(type: String): Flow<List<Resource>>

    @Query("SELECT * FROM resources WHERE id = :resourceId")
    suspend fun getResourceById(resourceId: Long): Resource?

    @Insert
    suspend fun insertResource(resource: Resource): Long

    @Update
    suspend fun updateResource(resource: Resource)

    @Delete
    suspend fun deleteResource(resource: Resource)

    @Query("SELECT * FROM resources")
    fun getAllResources(): Flow<List<Resource>>
}
