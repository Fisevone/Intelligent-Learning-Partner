package com.example.educationapp.service

import com.example.educationapp.data.EducationDatabase
import com.example.educationapp.data.LearningRecord
import com.example.educationapp.data.Recommendation
import com.example.educationapp.data.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataInitializationService(private val database: EducationDatabase) {
    
    suspend fun initializeSampleData() = withContext(Dispatchers.IO) {
        // 初始化学习资源
        initializeResources()
        
        // 初始化学习记录
        initializeLearningRecords()
        
        // 初始化推荐
        initializeRecommendations()
    }
    
    private suspend fun initializeResources() {
        val resources = listOf(
            Resource(
                title = "高中数学函数专题",
                description = "深入理解函数的概念、性质和图像变换",
                type = "video",
                subject = "数学",
                difficulty = "medium",
                url = "https://example.com/math-function",
                duration = 45,
                rating = 4.5f,
                tags = "函数,图像,变换",
                isRecommended = true
            ),
            Resource(
                title = "英语语法基础",
                description = "掌握英语基本语法规则和句型结构",
                type = "article",
                subject = "英语",
                difficulty = "easy",
                url = "https://example.com/english-grammar",
                duration = 30,
                rating = 4.2f,
                tags = "语法,基础,句型",
                isRecommended = true
            ),
            Resource(
                title = "物理力学练习",
                description = "通过练习巩固力学基本概念",
                type = "exercise",
                subject = "物理",
                difficulty = "hard",
                url = "https://example.com/physics-mechanics",
                duration = 60,
                rating = 4.7f,
                tags = "力学,练习,概念",
                isRecommended = false
            ),
            Resource(
                title = "化学元素周期表",
                description = "学习元素周期表的规律和特点",
                type = "video",
                subject = "化学",
                difficulty = "medium",
                url = "https://example.com/chemistry-periodic",
                duration = 35,
                rating = 4.3f,
                tags = "元素,周期表,规律",
                isRecommended = true
            ),
            Resource(
                title = "历史朝代更替",
                description = "了解中国历史各朝代的兴衰",
                type = "article",
                subject = "历史",
                difficulty = "easy",
                url = "https://example.com/history-dynasties",
                duration = 25,
                rating = 4.0f,
                tags = "历史,朝代,兴衰",
                isRecommended = false
            ),
            Resource(
                title = "地理气候类型",
                description = "掌握世界主要气候类型及其分布",
                type = "video",
                subject = "地理",
                difficulty = "medium",
                url = "https://example.com/geography-climate",
                duration = 40,
                rating = 4.4f,
                tags = "气候,分布,类型",
                isRecommended = true
            ),
            Resource(
                title = "生物细胞结构",
                description = "学习细胞的基本结构和功能",
                type = "quiz",
                subject = "生物",
                difficulty = "easy",
                url = "https://example.com/biology-cell",
                duration = 20,
                rating = 4.1f,
                tags = "细胞,结构,功能",
                isRecommended = false
            ),
            Resource(
                title = "政治制度比较",
                description = "比较不同政治制度的特点",
                type = "article",
                subject = "政治",
                difficulty = "hard",
                url = "https://example.com/politics-systems",
                duration = 50,
                rating = 4.6f,
                tags = "政治,制度,比较",
                isRecommended = true
            )
        )
        
        resources.forEach { resource ->
            database.resourceDao().insertResource(resource)
        }
    }
    
    private suspend fun initializeLearningRecords() {
        val currentTime = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L
        
        val learningRecords = listOf(
            LearningRecord(
                userId = 1,
                subject = "数学",
                topic = "函数与导数",
                duration = 45,
                score = 85f,
                difficulty = "medium",
                learningStyle = "visual",
                timestamp = currentTime - oneDay * 2
            ),
            LearningRecord(
                userId = 1,
                subject = "英语",
                topic = "语法基础",
                duration = 30,
                score = 78f,
                difficulty = "easy",
                learningStyle = "auditory",
                timestamp = currentTime - oneDay * 3
            ),
            LearningRecord(
                userId = 1,
                subject = "物理",
                topic = "力学基础",
                duration = 60,
                score = 72f,
                difficulty = "hard",
                learningStyle = "kinesthetic",
                timestamp = currentTime - oneDay * 1
            ),
            LearningRecord(
                userId = 1,
                subject = "数学",
                topic = "三角函数",
                duration = 40,
                score = 88f,
                difficulty = "medium",
                learningStyle = "visual",
                timestamp = currentTime - oneDay * 4
            ),
            LearningRecord(
                userId = 1,
                subject = "化学",
                topic = "元素周期表",
                duration = 35,
                score = 82f,
                difficulty = "medium",
                learningStyle = "visual",
                timestamp = currentTime - oneDay * 5
            )
        )
        
        learningRecords.forEach { record ->
            database.learningRecordDao().insertLearningRecord(record)
        }
    }
    
    private suspend fun initializeRecommendations() {
        val recommendations = listOf(
            Recommendation(
                userId = 1,
                resourceId = 1,
                reason = "基于你的数学学习进度，推荐函数专题视频",
                score = 0.9f
            ),
            Recommendation(
                userId = 1,
                resourceId = 2,
                reason = "你在英语语法方面需要加强，推荐基础语法文章",
                score = 0.8f
            ),
            Recommendation(
                userId = 1,
                resourceId = 4,
                reason = "基于你的化学学习表现，推荐元素周期表视频",
                score = 0.7f
            ),
            Recommendation(
                userId = 1,
                resourceId = 6,
                reason = "基于你的学习风格，推荐地理气候类型视频",
                score = 0.6f
            )
        )
        
        recommendations.forEach { recommendation ->
            database.recommendationDao().insertRecommendation(recommendation)
        }
    }
}
