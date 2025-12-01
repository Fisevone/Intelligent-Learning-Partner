package com.example.educationapp.service

import com.example.educationapp.data.LearningRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*

class LearningAnalysisService {
    
    /**
     * 生成学习分析报告
     */
    fun generateLearningReport(learningRecords: List<LearningRecord>): Flow<LearningReport> = flow {
        val report = LearningReport(
            totalLearningTime = calculateTotalLearningTime(learningRecords),
            averageScore = calculateAverageScore(learningRecords),
            subjectAnalysis = analyzeBySubject(learningRecords),
            difficultyAnalysis = analyzeByDifficulty(learningRecords),
            timeAnalysis = analyzeByTime(learningRecords),
            learningTrends = analyzeLearningTrends(learningRecords),
            recommendations = generatePersonalizedRecommendations(learningRecords)
        )
        emit(report)
    }
    
    /**
     * 计算总学习时长
     */
    private fun calculateTotalLearningTime(records: List<LearningRecord>): Long {
        return records.sumOf { it.duration }
    }
    
    /**
     * 计算平均得分
     */
    private fun calculateAverageScore(records: List<LearningRecord>): Float {
        return if (records.isNotEmpty()) {
            records.map { it.score }.average().toFloat()
        } else 0f
    }
    
    /**
     * 按科目分析
     */
    private fun analyzeBySubject(records: List<LearningRecord>): Map<String, SubjectAnalysis> {
        return records.groupBy { it.subject }.mapValues { (subject, subjectRecords) ->
            SubjectAnalysis(
                subject = subject,
                totalTime = subjectRecords.sumOf { it.duration },
                averageScore = subjectRecords.map { it.score }.average().toFloat(),
                recordCount = subjectRecords.size,
                bestScore = subjectRecords.maxOfOrNull { it.score } ?: 0f,
                worstScore = subjectRecords.minOfOrNull { it.score } ?: 0f,
                improvement = calculateImprovement(subjectRecords)
            )
        }
    }
    
    /**
     * 按难度分析
     */
    private fun analyzeByDifficulty(records: List<LearningRecord>): Map<String, DifficultyAnalysis> {
        return records.groupBy { it.difficulty }.mapValues { (difficulty, difficultyRecords) ->
            DifficultyAnalysis(
                difficulty = difficulty,
                totalTime = difficultyRecords.sumOf { it.duration },
                averageScore = difficultyRecords.map { it.score }.average().toFloat(),
                recordCount = difficultyRecords.size,
                completionRate = calculateCompletionRate(difficultyRecords)
            )
        }
    }
    
    /**
     * 按时间分析
     */
    private fun analyzeByTime(records: List<LearningRecord>): TimeAnalysis {
        val calendar = Calendar.getInstance()
        val dailyData = mutableMapOf<String, Long>()
        val weeklyData = mutableMapOf<String, Long>()
        val monthlyData = mutableMapOf<String, Long>()
        
        records.forEach { record ->
            calendar.timeInMillis = record.timestamp
            
            // 每日数据
            val dayKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_MONTH)}"
            dailyData[dayKey] = (dailyData[dayKey] ?: 0) + record.duration
            
            // 每周数据
            val weekKey = "${calendar.get(Calendar.YEAR)}-W${calendar.get(Calendar.WEEK_OF_YEAR)}"
            weeklyData[weekKey] = (weeklyData[weekKey] ?: 0) + record.duration
            
            // 每月数据
            val monthKey = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}"
            monthlyData[monthKey] = (monthlyData[monthKey] ?: 0) + record.duration
        }
        
        return TimeAnalysis(
            dailyData = dailyData,
            weeklyData = weeklyData,
            monthlyData = monthlyData,
            mostProductiveDay = findMostProductiveDay(dailyData),
            mostProductiveWeek = findMostProductiveWeek(weeklyData),
            mostProductiveMonth = findMostProductiveMonth(monthlyData)
        )
    }
    
    /**
     * 分析学习趋势
     */
    private fun analyzeLearningTrends(records: List<LearningRecord>): LearningTrends {
        val sortedRecords = records.sortedBy { it.timestamp }
        val recentRecords = sortedRecords.takeLast(10)
        val olderRecords = sortedRecords.dropLast(10)
        
        val recentAverage = if (recentRecords.isNotEmpty()) {
            recentRecords.map { it.score }.average().toFloat()
        } else 0f
        
        val olderAverage = if (olderRecords.isNotEmpty()) {
            olderRecords.map { it.score }.average().toFloat()
        } else 0f
        
        val trend = when {
            recentAverage > olderAverage + 5 -> "上升"
            recentAverage < olderAverage - 5 -> "下降"
            else -> "稳定"
        }
        
        return LearningTrends(
            trend = trend,
            recentAverage = recentAverage,
            olderAverage = olderAverage,
            improvement = recentAverage - olderAverage
        )
    }
    
    /**
     * 生成个性化建议
     */
    private fun generatePersonalizedRecommendations(records: List<LearningRecord>): List<String> {
        val recommendations = mutableListOf<String>()
        val subjectAnalysis = analyzeBySubject(records)
        val difficultyAnalysis = analyzeByDifficulty(records)
        
        // 基于科目表现的建议
        subjectAnalysis.forEach { (subject, analysis) ->
            when {
                analysis.averageScore < 60f -> {
                    recommendations.add("建议在${subject}科目上投入更多时间，考虑寻求额外帮助")
                }
                analysis.averageScore > 90f -> {
                    recommendations.add("你在${subject}科目表现优秀，可以挑战更高难度的内容")
                }
                analysis.improvement > 10f -> {
                    recommendations.add("${subject}科目进步明显，继续保持当前学习方法")
                }
            }
        }
        
        // 基于难度的建议
        difficultyAnalysis.forEach { (difficulty, analysis) ->
            when {
                analysis.completionRate < 0.7f -> {
                    recommendations.add("建议从${difficulty}难度开始，逐步提升")
                }
                analysis.averageScore > 85f -> {
                    recommendations.add("你在${difficulty}难度表现良好，可以尝试更高难度")
                }
            }
        }
        
        // 基于学习时长的建议
        val totalTime = calculateTotalLearningTime(records)
        when {
            totalTime < 300 -> {
                recommendations.add("建议增加学习时间，每天至少学习30分钟")
            }
            totalTime > 1800 -> {
                recommendations.add("学习时间充足，注意劳逸结合，保持学习效率")
            }
        }
        
        return recommendations
    }
    
    private fun calculateImprovement(records: List<LearningRecord>): Float {
        if (records.size < 2) return 0f
        val sortedRecords = records.sortedBy { it.timestamp }
        val firstHalf = sortedRecords.take(records.size / 2)
        val secondHalf = sortedRecords.drop(records.size / 2)
        
        val firstAverage = firstHalf.map { it.score }.average().toFloat()
        val secondAverage = secondHalf.map { it.score }.average().toFloat()
        
        return secondAverage - firstAverage
    }
    
    private fun calculateCompletionRate(records: List<LearningRecord>): Float {
        val completedRecords = records.count { it.score >= 60f }
        return if (records.isNotEmpty()) {
            completedRecords.toFloat() / records.size
        } else 0f
    }
    
    private fun findMostProductiveDay(dailyData: Map<String, Long>): String {
        return dailyData.maxByOrNull { it.value }?.key ?: ""
    }
    
    private fun findMostProductiveWeek(weeklyData: Map<String, Long>): String {
        return weeklyData.maxByOrNull { it.value }?.key ?: ""
    }
    
    private fun findMostProductiveMonth(monthlyData: Map<String, Long>): String {
        return monthlyData.maxByOrNull { it.value }?.key ?: ""
    }
}

data class LearningReport(
    val totalLearningTime: Long,
    val averageScore: Float,
    val subjectAnalysis: Map<String, SubjectAnalysis>,
    val difficultyAnalysis: Map<String, DifficultyAnalysis>,
    val timeAnalysis: TimeAnalysis,
    val learningTrends: LearningTrends,
    val recommendations: List<String>
)

data class SubjectAnalysis(
    val subject: String,
    val totalTime: Long,
    val averageScore: Float,
    val recordCount: Int,
    val bestScore: Float,
    val worstScore: Float,
    val improvement: Float
)

data class DifficultyAnalysis(
    val difficulty: String,
    val totalTime: Long,
    val averageScore: Float,
    val recordCount: Int,
    val completionRate: Float
)

data class TimeAnalysis(
    val dailyData: Map<String, Long>,
    val weeklyData: Map<String, Long>,
    val monthlyData: Map<String, Long>,
    val mostProductiveDay: String,
    val mostProductiveWeek: String,
    val mostProductiveMonth: String
)

data class LearningTrends(
    val trend: String,
    val recentAverage: Float,
    val olderAverage: Float,
    val improvement: Float
)
