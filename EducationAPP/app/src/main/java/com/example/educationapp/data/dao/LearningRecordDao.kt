package com.example.educationapp.data.dao

import androidx.room.*
import com.example.educationapp.data.LearningRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningRecordDao {
    @Query("SELECT * FROM learning_records WHERE userId = :userId ORDER BY timestamp DESC")
    fun getLearningRecordsByUser(userId: Long): Flow<List<LearningRecord>>

    @Query("SELECT * FROM learning_records WHERE userId = :userId AND subject = :subject")
    fun getLearningRecordsBySubject(userId: Long, subject: String): Flow<List<LearningRecord>>

    @Query("SELECT AVG(score) FROM learning_records WHERE userId = :userId AND subject = :subject")
    suspend fun getAverageScoreBySubject(userId: Long, subject: String): Float?

    @Query("SELECT SUM(duration) FROM learning_records WHERE userId = :userId")
    suspend fun getTotalLearningTime(userId: Long): Long?

    @Insert
    suspend fun insertLearningRecord(record: LearningRecord): Long

    @Update
    suspend fun updateLearningRecord(record: LearningRecord)

    @Delete
    suspend fun deleteLearningRecord(record: LearningRecord)

    @Query("SELECT * FROM learning_records")
    fun getAllLearningRecords(): Flow<List<LearningRecord>>
    
    @Query("SELECT * FROM learning_records WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getRecordsByUserId(userId: Long): List<LearningRecord>
    
    @Query("SELECT COUNT(*) FROM learning_records")
    suspend fun getTotalRecordCount(): Int
    
    @Query("DELETE FROM learning_records")
    suspend fun deleteAllRecords()
}
