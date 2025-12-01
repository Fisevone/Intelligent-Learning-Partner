package com.example.educationapp.repository

import com.example.educationapp.data.LearningRecord
import com.example.educationapp.data.dao.LearningRecordDao
import kotlinx.coroutines.flow.Flow

class LearningRepository(private val learningRecordDao: LearningRecordDao) {
    fun getLearningRecordsByUser(userId: Long): Flow<List<LearningRecord>> {
        return learningRecordDao.getLearningRecordsByUser(userId)
    }

    fun getLearningRecordsBySubject(userId: Long, subject: String): Flow<List<LearningRecord>> {
        return learningRecordDao.getLearningRecordsBySubject(userId, subject)
    }

    suspend fun getAverageScoreBySubject(userId: Long, subject: String): Float? {
        return learningRecordDao.getAverageScoreBySubject(userId, subject)
    }

    suspend fun getTotalLearningTime(userId: Long): Long? {
        return learningRecordDao.getTotalLearningTime(userId)
    }

    suspend fun insertLearningRecord(record: LearningRecord): Long {
        return learningRecordDao.insertLearningRecord(record)
    }

    suspend fun updateLearningRecord(record: LearningRecord) {
        learningRecordDao.updateLearningRecord(record)
    }

    suspend fun deleteLearningRecord(record: LearningRecord) {
        learningRecordDao.deleteLearningRecord(record)
    }
}
