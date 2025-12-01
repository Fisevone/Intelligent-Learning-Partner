package com.example.educationapp.service

import com.example.educationapp.data.LearningRecord
import com.example.educationapp.data.Recommendation
import com.example.educationapp.data.Resource
import com.example.educationapp.data.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RecommendationService {
    
    /**
     * 基于用户学习记录生成个性化推荐
     */
    fun generateRecommendations(
        user: User,
        learningRecords: List<LearningRecord>,
        availableResources: List<Resource>
    ): Flow<List<Recommendation>> = flow {
        val recommendations = mutableListOf<Recommendation>()
        
        // 1. 基于学习风格推荐
        val styleRecommendations = generateStyleBasedRecommendations(user, availableResources)
        recommendations.addAll(styleRecommendations)
        
        // 2. 基于学习进度推荐
        val progressRecommendations = generateProgressBasedRecommendations(learningRecords, availableResources)
        recommendations.addAll(progressRecommendations)
        
        // 3. 基于兴趣推荐
        val interestRecommendations = generateInterestBasedRecommendations(user, availableResources)
        recommendations.addAll(interestRecommendations)
        
        // 4. 基于难度适应性推荐
        val difficultyRecommendations = generateDifficultyBasedRecommendations(learningRecords, availableResources)
        recommendations.addAll(difficultyRecommendations)
        
        emit(recommendations.distinctBy { it.resourceId })
    }
    
    /**
     * 基于学习风格推荐
     */
    private fun generateStyleBasedRecommendations(
        user: User,
        resources: List<Resource>
    ): List<Recommendation> {
        return resources.filter { resource ->
            when (user.learningStyle) {
                "visual" -> resource.type in listOf("video", "article")
                "auditory" -> resource.type in listOf("video", "audio")
                "kinesthetic" -> resource.type in listOf("exercise", "quiz")
                else -> true
            }
        }.map { resource ->
            Recommendation(
                userId = user.id,
                resourceId = resource.id,
                reason = "基于你的${getLearningStyleText(user.learningStyle)}特点推荐",
                score = 0.8f
            )
        }
    }
    
    /**
     * 基于学习进度推荐
     */
    private fun generateProgressBasedRecommendations(
        learningRecords: List<LearningRecord>,
        resources: List<Resource>
    ): List<Recommendation> {
        val subjectProgress = learningRecords.groupBy { it.subject }
            .mapValues { (_, records) ->
                records.map { it.score }.average().toFloat()
            }
        
        return resources.filter { resource ->
            val subjectScore = subjectProgress[resource.subject] ?: 0f
            // 推荐得分较低科目的资源
            subjectScore < 80f
        }.map { resource ->
            Recommendation(
                userId = 0, // 将在调用时设置
                resourceId = resource.id,
                reason = "你在${resource.subject}科目需要加强，推荐相关学习资源",
                score = 0.9f
            )
        }
    }
    
    /**
     * 基于兴趣推荐
     */
    private fun generateInterestBasedRecommendations(
        user: User,
        resources: List<Resource>
    ): List<Recommendation> {
        val userInterests = user.interests.split(",").map { it.trim() }
        
        return resources.filter { resource ->
            userInterests.any { interest ->
                resource.title.contains(interest, ignoreCase = true) ||
                resource.description.contains(interest, ignoreCase = true) ||
                resource.tags.contains(interest, ignoreCase = true)
            }
        }.map { resource ->
            Recommendation(
                userId = user.id,
                resourceId = resource.id,
                reason = "基于你的兴趣领域推荐",
                score = 0.7f
            )
        }
    }
    
    /**
     * 基于难度适应性推荐
     */
    private fun generateDifficultyBasedRecommendations(
        learningRecords: List<LearningRecord>,
        resources: List<Resource>
    ): List<Recommendation> {
        val difficultyPerformance = learningRecords.groupBy { it.difficulty }
            .mapValues { (_, records) ->
                records.map { it.score }.average().toFloat()
            }
        
        return resources.map { resource ->
            val performance = difficultyPerformance[resource.difficulty] ?: 0f
            val score = when {
                performance > 90f -> 0.6f // 表现很好，推荐更高难度
                performance > 70f -> 0.8f // 表现良好，推荐相似难度
                else -> 0.9f // 表现一般，推荐较低难度
            }
            
            Recommendation(
                userId = 0, // 将在调用时设置
                resourceId = resource.id,
                reason = "基于你的${resource.difficulty}难度学习表现推荐",
                score = score
            )
        }
    }
    
    /**
     * 分析学习模式
     */
    fun analyzeLearningPattern(learningRecords: List<LearningRecord>): LearningAnalysis {
        val totalTime = learningRecords.sumOf { it.duration }
        val averageScore = learningRecords.map { it.score }.average().toFloat()
        val subjectPerformance = learningRecords.groupBy { it.subject }
            .mapValues { (_, records) ->
                records.map { it.score }.average().toFloat()
            }
        val difficultyPerformance = learningRecords.groupBy { it.difficulty }
            .mapValues { (_, records) ->
                records.map { it.score }.average().toFloat()
            }
        
        return LearningAnalysis(
            totalLearningTime = totalTime,
            averageScore = averageScore,
            subjectPerformance = subjectPerformance,
            difficultyPerformance = difficultyPerformance,
            learningStrengths = findLearningStrengths(subjectPerformance),
            learningWeaknesses = findLearningWeaknesses(subjectPerformance)
        )
    }
    
    private fun findLearningStrengths(subjectPerformance: Map<String, Float>): List<String> {
        return subjectPerformance.filter { it.value > 85f }.keys.toList()
    }
    
    private fun findLearningWeaknesses(subjectPerformance: Map<String, Float>): List<String> {
        return subjectPerformance.filter { it.value < 70f }.keys.toList()
    }
    
    private fun getLearningStyleText(style: String): String {
        return when (style) {
            "visual" -> "视觉型学习"
            "auditory" -> "听觉型学习"
            "kinesthetic" -> "动觉型学习"
            else -> "混合型学习"
        }
    }
}

data class LearningAnalysis(
    val totalLearningTime: Long,
    val averageScore: Float,
    val subjectPerformance: Map<String, Float>,
    val difficultyPerformance: Map<String, Float>,
    val learningStrengths: List<String>,
    val learningWeaknesses: List<String>
)
