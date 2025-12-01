package com.example.educationapp.data.dao

import androidx.room.*
import com.example.educationapp.data.LearningProgress
import com.example.educationapp.data.LearningStatistics
import com.example.educationapp.data.LearningBehavior
import kotlinx.coroutines.flow.Flow

/**
 * 📊 学习进度数据访问对象
 */
@Dao
interface LearningProgressDao {
    
    // === 学习进度查询 ===
    @Query("SELECT * FROM learning_progress WHERE userId = :userId")
    suspend fun getUserProgress(userId: Long): List<LearningProgress>
    
    @Query("SELECT * FROM learning_progress WHERE userId = :userId AND subject = :subject")
    suspend fun getSubjectProgress(userId: Long, subject: String): List<LearningProgress>
    
    @Query("SELECT * FROM learning_progress WHERE userId = :userId AND knowledgePoint = :knowledgePoint")
    suspend fun getKnowledgePointProgress(userId: Long, knowledgePoint: String): LearningProgress?
    
    @Query("""
        SELECT AVG(masteryLevel) as avgMastery 
        FROM learning_progress 
        WHERE userId = :userId AND subject = :subject
    """)
    suspend fun getSubjectAverageMastery(userId: Long, subject: String): Float?
    
    @Query("""
        SELECT * FROM learning_progress 
        WHERE userId = :userId 
        ORDER BY masteryLevel ASC 
        LIMIT :limit
    """)
    suspend fun getWeakestKnowledgePoints(userId: Long, limit: Int = 5): List<LearningProgress>
    
    @Query("""
        SELECT * FROM learning_progress 
        WHERE userId = :userId AND masteryLevel >= 0.8 
        ORDER BY masteryLevel DESC
    """)
    suspend fun getMasteredKnowledgePoints(userId: Long): List<LearningProgress>
    
    // === 学习进度更新 ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: LearningProgress): Long
    
    @Update
    suspend fun updateProgress(progress: LearningProgress)
    
    @Transaction
    suspend fun updateKnowledgePointMastery(
        userId: Long, 
        knowledgePoint: String, 
        subject: String,
        isCorrect: Boolean, 
        studyTime: Long,
        source: String
    ) {
        val existing = getKnowledgePointProgress(userId, knowledgePoint)
        if (existing != null) {
            val newCorrect = existing.correctAnswers + if (isCorrect) 1 else 0
            val newTotal = existing.totalAnswers + 1
            val newMastery = calculateMastery(newCorrect, newTotal, existing.studyTime + studyTime)
            
            val updated = existing.copy(
                masteryLevel = newMastery,
                studyTime = existing.studyTime + studyTime,
                correctAnswers = newCorrect,
                totalAnswers = newTotal,
                lastStudyTime = System.currentTimeMillis(),
                studySource = source,
                updatedAt = System.currentTimeMillis()
            )
            updateProgress(updated)
        } else {
            val newProgress = LearningProgress(
                userId = userId,
                subject = subject,
                knowledgePoint = knowledgePoint,
                masteryLevel = if (isCorrect) 0.6f else 0.2f,
                studyTime = studyTime,
                correctAnswers = if (isCorrect) 1 else 0,
                totalAnswers = 1,
                lastStudyTime = System.currentTimeMillis(),
                difficultyLevel = "基础",
                studySource = source
            )
            insertProgress(newProgress)
        }
    }
    
    // === 学习统计 ===
    @Query("SELECT * FROM learning_statistics WHERE userId = :userId ORDER BY date DESC LIMIT 30")
    suspend fun getRecentStatistics(userId: Long): List<LearningStatistics>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatistics(statistics: LearningStatistics)
    
    // === 学习行为 ===
    @Query("SELECT * FROM learning_behaviors WHERE userId = :userId ORDER BY timestamp DESC LIMIT 100")
    suspend fun getRecentBehaviors(userId: Long): List<LearningBehavior>
    
    @Insert
    suspend fun insertBehavior(behavior: LearningBehavior)
    
    @Query("""
        SELECT COUNT(*) FROM learning_behaviors 
        WHERE userId = :userId AND behaviorType = :behaviorType 
        AND timestamp > :since
    """)
    suspend fun getBehaviorCount(userId: Long, behaviorType: String, since: Long): Int
    
    // === 智能分析 ===
    @Query("""
        SELECT subject, AVG(masteryLevel) as avgMastery, COUNT(*) as pointCount
        FROM learning_progress 
        WHERE userId = :userId 
        GROUP BY subject
        ORDER BY avgMastery DESC
    """)
    suspend fun getSubjectMasteryOverview(userId: Long): List<SubjectMasterySummary>
    
    @Query("""
        SELECT knowledgePoint, masteryLevel, studyTime, totalAnswers
        FROM learning_progress 
        WHERE userId = :userId AND subject = :subject
        AND lastStudyTime > :recentThreshold
        ORDER BY lastStudyTime DESC
    """)
    suspend fun getRecentStudyActivity(userId: Long, subject: String, recentThreshold: Long): List<RecentActivity>
    
    // === 新增缺失的方法 ===
    @Query("SELECT * FROM learning_progress WHERE userId = :userId")
    suspend fun getProgressByUserId(userId: Long): List<LearningProgress>
    
    @Query("SELECT COUNT(*) FROM learning_progress")
    suspend fun getTotalProgressCount(): Int
    
    @Query("DELETE FROM learning_progress")
    suspend fun deleteAllProgress()
    
    @Query("DELETE FROM learning_statistics")
    suspend fun deleteAllStatistics()
    
    @Query("DELETE FROM learning_behaviors")
    suspend fun deleteAllBehaviors()
}

/**
 * 📈 学科掌握度概览
 */
data class SubjectMasterySummary(
    val subject: String,
    val avgMastery: Float,
    val pointCount: Int
)

/**
 * 🕒 最近学习活动
 */
data class RecentActivity(
    val knowledgePoint: String,
    val masteryLevel: Float,
    val studyTime: Long,
    val totalAnswers: Int
)

/**
 * 🧮 掌握度计算函数
 */
private fun calculateMastery(correct: Int, total: Int, studyTime: Long): Float {
    val accuracyWeight = 0.6f
    val consistencyWeight = 0.3f
    val timeWeight = 0.1f
    
    val accuracy = if (total > 0) correct.toFloat() / total else 0f
    val consistency = minOf(total.toFloat() / 10f, 1f) // 答题次数越多越稳定
    val timeBonus = minOf(studyTime.toFloat() / (30 * 60), 0.2f) // 学习时间加分
    
    return minOf(
        accuracy * accuracyWeight + 
        consistency * consistencyWeight + 
        timeBonus * timeWeight, 
        1.0f
    )
}
