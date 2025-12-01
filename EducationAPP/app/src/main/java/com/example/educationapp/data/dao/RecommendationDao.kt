package com.example.educationapp.data.dao

import androidx.room.*
import com.example.educationapp.data.Recommendation
import kotlinx.coroutines.flow.Flow

@Dao
interface RecommendationDao {
    @Query("SELECT * FROM recommendations WHERE userId = :userId ORDER BY score DESC")
    fun getRecommendationsByUser(userId: Long): Flow<List<Recommendation>>

    @Query("SELECT * FROM recommendations WHERE userId = :userId AND isViewed = 0")
    fun getUnviewedRecommendations(userId: Long): Flow<List<Recommendation>>

    @Insert
    suspend fun insertRecommendation(recommendation: Recommendation): Long

    @Update
    suspend fun updateRecommendation(recommendation: Recommendation)

    @Delete
    suspend fun deleteRecommendation(recommendation: Recommendation)

    @Query("UPDATE recommendations SET isViewed = 1 WHERE id = :recommendationId")
    suspend fun markAsViewed(recommendationId: Long)

    @Query("UPDATE recommendations SET isCompleted = 1 WHERE id = :recommendationId")
    suspend fun markAsCompleted(recommendationId: Long)
}
