package com.example.educationapp.repository

import com.example.educationapp.data.Recommendation
import com.example.educationapp.data.dao.RecommendationDao
import kotlinx.coroutines.flow.Flow

class RecommendationRepository(private val recommendationDao: RecommendationDao) {
    fun getRecommendationsByUser(userId: Long): Flow<List<Recommendation>> {
        return recommendationDao.getRecommendationsByUser(userId)
    }

    fun getUnviewedRecommendations(userId: Long): Flow<List<Recommendation>> {
        return recommendationDao.getUnviewedRecommendations(userId)
    }

    suspend fun insertRecommendation(recommendation: Recommendation): Long {
        return recommendationDao.insertRecommendation(recommendation)
    }

    suspend fun updateRecommendation(recommendation: Recommendation) {
        recommendationDao.updateRecommendation(recommendation)
    }

    suspend fun deleteRecommendation(recommendation: Recommendation) {
        recommendationDao.deleteRecommendation(recommendation)
    }

    suspend fun markAsViewed(recommendationId: Long) {
        recommendationDao.markAsViewed(recommendationId)
    }

    suspend fun markAsCompleted(recommendationId: Long) {
        recommendationDao.markAsCompleted(recommendationId)
    }
}
