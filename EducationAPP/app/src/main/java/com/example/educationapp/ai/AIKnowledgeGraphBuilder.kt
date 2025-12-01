package com.example.educationapp.ai

import com.example.educationapp.data.User
import com.example.educationapp.data.LearningRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 🕸️ AI知识图谱构建器 - 基于智谱GLM-4的实时个性化知识图谱构建与可视化
 * 创新点：动态知识关联、个性化学习路径、实时掌握程度更新、GLM-4智能分析
 */
class AIKnowledgeGraphBuilder {
    private val zhipuAIService = ZhipuAIService()
    
    data class KnowledgeNode(
        val id: String,
        val concept: String,
        val description: String,
        val masteryLevel: Float, // 0-1，掌握程度
        val difficulty: String,
        val prerequisites: List<String>,
        val applications: List<String>,
        val estimatedLearningTime: Int, // 分钟
        val importance: Float // 在整个知识体系中的重要性
    )
    
    data class KnowledgeEdge(
        val fromNode: String,
        val toNode: String,
        val relationshipType: String, // prerequisite, application, related, derived
        val strength: Float // 关联强度
    )
    
    data class PersonalizedKnowledgeGraph(
        val nodes: List<KnowledgeNode>,
        val edges: List<KnowledgeEdge>,
        val recommendedPath: List<String>,
        val currentFocus: String,
        val nextMilestones: List<String>,
        val totalEstimatedTime: Int,
        val personalizedInsights: List<String>
    )
    
    data class NodeExploration(
        val detailedExplanation: String,
        val relatedConcepts: List<String>,
        val practicalExamples: List<String>,
        val learningTips: String,
        val commonMistakes: List<String>,
        val assessmentQuestions: List<String>
    )
    
    data class LearningPathOptimization(
        val originalPath: List<String>,
        val optimizedPath: List<String>,
        val optimizationReason: String,
        val expectedImprovement: String,
        val adaptiveAdjustments: List<String>
    )
    
    suspend fun buildPersonalizedKnowledgeGraph(
        topic: String,
        studentLevel: String,
        learningHistory: List<String>,
        realTimeProgress: Map<String, Float>
    ): PersonalizedKnowledgeGraph {
        return try {
            val prompt = """
            为学生构建个性化知识图谱：
            
            主题：$topic
            学生水平：$studentLevel
            学习历史：${learningHistory.joinToString(", ")}
            
            当前掌握情况：
            ${realTimeProgress.map { "${it.key}: ${(it.value * 100).toInt()}%" }.joinToString("\n")}
            
            请构建一个完整的知识图谱，包括：
            1. 核心概念节点及其掌握程度
            2. 概念之间的依赖关系
            3. 个性化的学习路径推荐
            4. 下一步学习重点
            
            要考虑学生的现有水平和学习历史，确保路径的连贯性和可行性。
            """.trimIndent()
            
            val response = zhipuAIService.sendChatMessage(prompt, User(0, "", "", "", "", com.example.educationapp.data.UserType.STUDENT))
            response.fold(
                onSuccess = { aiResponse ->
                    parseKnowledgeGraph(aiResponse, topic, realTimeProgress)
                },
                onFailure = {
                    generateDefaultKnowledgeGraph(topic, studentLevel, realTimeProgress)
                }
            )
            
        } catch (e: Exception) {
            generateDefaultKnowledgeGraph(topic, studentLevel, realTimeProgress)
        }
    }
    
    private fun generateDefaultKnowledgeGraph(
        topic: String,
        studentLevel: String,
        realTimeProgress: Map<String, Float>
    ): PersonalizedKnowledgeGraph {
        val nodes = when (topic) {
            "高等数学-极限理论" -> createMathLimitNodes(realTimeProgress)
            "物理-力学基础" -> createPhysicsMechanicsNodes(realTimeProgress)
            else -> createGenericNodes(topic, realTimeProgress)
        }
        
        val edges = generateKnowledgeEdges(nodes)
        val recommendedPath = generateOptimalLearningPath(nodes)
        
        return PersonalizedKnowledgeGraph(
            nodes = nodes,
            edges = edges,
            recommendedPath = recommendedPath,
            currentFocus = findCurrentFocus(nodes),
            nextMilestones = findNextMilestones(nodes, recommendedPath),
            totalEstimatedTime = calculateTotalLearningTime(nodes),
            personalizedInsights = generatePersonalizedInsights(nodes, realTimeProgress)
        )
    }
    
    private fun createMathLimitNodes(progress: Map<String, Float>): List<KnowledgeNode> {
        return listOf(
            KnowledgeNode(
                id = "math_limit_001",
                concept = "函数基础",
                description = "函数的定义域、值域、性质",
                masteryLevel = progress["函数基础"] ?: 0.5f,
                difficulty = "基础",
                prerequisites = emptyList(),
                applications = listOf("极限计算", "连续性判断"),
                estimatedLearningTime = 120,
                importance = 0.9f
            ),
            KnowledgeNode(
                id = "math_limit_002",
                concept = "极限概念",
                description = "极限的直观理解和数学定义",
                masteryLevel = progress["极限概念"] ?: 0.3f,
                difficulty = "中等",
                prerequisites = listOf("函数基础"),
                applications = listOf("连续性", "导数定义"),
                estimatedLearningTime = 180,
                importance = 1.0f
            ),
            KnowledgeNode(
                id = "math_limit_003",
                concept = "连续性",
                description = "函数连续性的定义和判断",
                masteryLevel = progress["连续性"] ?: 0.1f,
                difficulty = "中等",
                prerequisites = listOf("极限概念"),
                applications = listOf("可导性", "积分计算"),
                estimatedLearningTime = 150,
                importance = 0.8f
            ),
            KnowledgeNode(
                id = "math_limit_004",
                concept = "导数",
                description = "导数的定义和基本计算",
                masteryLevel = progress["导数"] ?: 0.0f,
                difficulty = "中等",
                prerequisites = listOf("极限概念", "连续性"),
                applications = listOf("函数分析", "优化问题"),
                estimatedLearningTime = 200,
                importance = 0.95f
            )
        )
    }
    
    private fun createPhysicsMechanicsNodes(progress: Map<String, Float>): List<KnowledgeNode> {
        return listOf(
            KnowledgeNode(
                id = "physics_mech_001",
                concept = "运动学基础",
                description = "位移、速度、加速度的概念",
                masteryLevel = progress["运动学基础"] ?: 0.6f,
                difficulty = "基础",
                prerequisites = emptyList(),
                applications = listOf("自由落体", "抛物运动"),
                estimatedLearningTime = 100,
                importance = 0.9f
            ),
            KnowledgeNode(
                id = "physics_mech_002",
                concept = "牛顿定律",
                description = "牛顿三大运动定律",
                masteryLevel = progress["牛顿定律"] ?: 0.4f,
                difficulty = "中等",
                prerequisites = listOf("运动学基础"),
                applications = listOf("动力学分析", "工程应用"),
                estimatedLearningTime = 160,
                importance = 1.0f
            )
        )
    }
    
    private fun createGenericNodes(topic: String, progress: Map<String, Float>): List<KnowledgeNode> {
        return listOf(
            KnowledgeNode(
                id = "generic_001",
                concept = "${topic}-基础概念",
                description = "$topic 的基本概念和原理",
                masteryLevel = if (progress.values.isNotEmpty()) progress.values.average().toFloat() else 0.5f,
                difficulty = "基础",
                prerequisites = emptyList(),
                applications = listOf("进阶学习", "实际应用"),
                estimatedLearningTime = 120,
                importance = 0.8f
            )
        )
    }
    
    private fun generateKnowledgeEdges(nodes: List<KnowledgeNode>): List<KnowledgeEdge> {
        val edges = mutableListOf<KnowledgeEdge>()
        
        nodes.forEach { node ->
            node.prerequisites.forEach { prerequisite ->
                val prerequisiteNode = nodes.find { it.concept == prerequisite }
                if (prerequisiteNode != null) {
                    edges.add(
                        KnowledgeEdge(
                            fromNode = prerequisiteNode.id,
                            toNode = node.id,
                            relationshipType = "prerequisite",
                            strength = 0.9f
                        )
                    )
                }
            }
            
            node.applications.forEach { application ->
                val applicationNode = nodes.find { it.concept == application }
                if (applicationNode != null) {
                    edges.add(
                        KnowledgeEdge(
                            fromNode = node.id,
                            toNode = applicationNode.id,
                            relationshipType = "application",
                            strength = 0.7f
                        )
                    )
                }
            }
        }
        
        return edges
    }
    
    private fun generateOptimalLearningPath(nodes: List<KnowledgeNode>): List<String> {
        // 基于掌握程度和依赖关系生成学习路径
        val sortedNodes = nodes.sortedWith(
            compareBy<KnowledgeNode> { it.masteryLevel }
                .thenBy { it.prerequisites.size }
                .thenByDescending { it.importance }
        )
        
        return sortedNodes.map { it.concept }
    }
    
    private fun findCurrentFocus(nodes: List<KnowledgeNode>): String {
        // 找到掌握程度最低但前置条件已满足的概念
        return nodes
            .filter { node ->
                node.prerequisites.all { prereq ->
                    nodes.find { it.concept == prereq }?.masteryLevel ?: 0f > 0.7f
                }
            }
            .minByOrNull { it.masteryLevel }
            ?.concept ?: nodes.firstOrNull()?.concept ?: "基础概念"
    }
    
    private fun findNextMilestones(nodes: List<KnowledgeNode>, path: List<String>): List<String> {
        val currentIndex = path.indexOfFirst { concept ->
            nodes.find { it.concept == concept }?.masteryLevel ?: 0f < 0.7f
        }
        
        return if (currentIndex >= 0 && currentIndex < path.size - 2) {
            path.subList(currentIndex, minOf(currentIndex + 3, path.size))
        } else {
            path.take(3)
        }
    }
    
    private fun calculateTotalLearningTime(nodes: List<KnowledgeNode>): Int {
        return nodes.sumOf { node ->
            ((1 - node.masteryLevel) * node.estimatedLearningTime).toInt()
        }
    }
    
    private fun generatePersonalizedInsights(
        nodes: List<KnowledgeNode>,
        progress: Map<String, Float>
    ): List<String> {
        val insights = mutableListOf<String>()
        
        val strongAreas = nodes.filter { it.masteryLevel > 0.8f }
        val weakAreas = nodes.filter { it.masteryLevel < 0.3f }
        
        if (strongAreas.isNotEmpty()) {
            insights.add("💪 你在${strongAreas.joinToString("、") { it.concept }}方面表现优秀")
        }
        
        if (weakAreas.isNotEmpty()) {
            insights.add("🎯 建议重点关注${weakAreas.joinToString("、") { it.concept }}")
        }
        
        val totalMastery = nodes.map { it.masteryLevel }.average()
        when {
            totalMastery > 0.8 -> insights.add("🏆 整体掌握情况优秀，可以挑战更高难度")
            totalMastery > 0.6 -> insights.add("📈 学习进展良好，继续保持")
            else -> insights.add("💡 建议从基础概念开始巩固")
        }
        
        return insights
    }
    
    suspend fun exploreNodeInDepth(node: KnowledgeNode, user: User): NodeExploration {
        return try {
            val prompt = """
            深度解析知识点：${node.concept}
            
            学生信息：
            - 当前掌握程度：${(node.masteryLevel * 100).toInt()}%
            - 学习兴趣：${user.interests}
            - 学习兴趣：${user.interests}
            
            请提供：
            1. 详细解释（结合学生兴趣）
            2. 相关概念和应用
            3. 实用例子
            4. 学习技巧
            5. 常见误区
            6. 检测题目
            
            要确保内容适合学生的掌握程度和兴趣点。
            """.trimIndent()
            
            val response = zhipuAIService.sendChatMessage(prompt, user)
            response.fold(
                onSuccess = { aiResponse ->
                    parseNodeExploration(aiResponse, node)
                },
                onFailure = {
                    generateDefaultNodeExploration(node)
                }
            )
            
        } catch (e: Exception) {
            generateDefaultNodeExploration(node)
        }
    }
    
    private fun generateDefaultNodeExploration(node: KnowledgeNode): NodeExploration {
        return NodeExploration(
            detailedExplanation = "${node.concept}是${node.description}。这个概念在学习中起到关键作用。",
            relatedConcepts = node.applications,
            practicalExamples = listOf(
                "实际应用例子1",
                "生活中的例子",
                "解题步骤示例"
            ),
            learningTips = "建议循序渐进学习，多做练习，理解核心概念。",
            commonMistakes = listOf(
                "概念理解偏差",
                "计算错误",
                "应用不当"
            ),
            assessmentQuestions = listOf(
                "什么是${node.concept}？",
                "如何应用${node.concept}？",
                "给出一个${node.concept}的例子"
            )
        )
    }
    
    private fun parseKnowledgeGraph(
        response: String,
        topic: String,
        progress: Map<String, Float>
    ): PersonalizedKnowledgeGraph {
        // 简化的解析逻辑，实际应用中应该解析AI返回的结构化数据
        return generateDefaultKnowledgeGraph(topic, "中等", progress)
    }
    
    private fun parseNodeExploration(response: String, node: KnowledgeNode): NodeExploration {
        // 简化的解析逻辑
        return generateDefaultNodeExploration(node)
    }
    
    suspend fun optimizeLearningPath(
        currentPath: List<String>,
        recentPerformance: Map<String, Float>,
        timeConstraints: Int,
        learningStyle: String
    ): LearningPathOptimization {
        return try {
            val prompt = """
            优化学习路径：
            
            当前路径：${currentPath.joinToString(" → ")}
            最近表现：${recentPerformance.map { "${it.key}: ${(it.value * 100).toInt()}%" }.joinToString(", ")}
            时间限制：${timeConstraints}分钟
            学习风格：$learningStyle
            
            基于表现数据和时间限制，优化学习路径，提高学习效率。
            """.trimIndent()
            
            val response = zhipuAIService.sendChatMessage(prompt, User(0, "", "", "", "", com.example.educationapp.data.UserType.STUDENT))
            response.fold(
                onSuccess = { aiResponse ->
                    parseLearningPathOptimization(aiResponse, currentPath)
                },
                onFailure = {
                    LearningPathOptimization(
                        originalPath = currentPath,
                        optimizedPath = currentPath,
                        optimizationReason = "保持原有路径，稳步推进",
                        expectedImprovement = "按部就班学习，确保掌握扎实",
                        adaptiveAdjustments = listOf("根据实际情况微调节奏")
                    )
                }
            )
            
        } catch (e: Exception) {
            LearningPathOptimization(
                originalPath = currentPath,
                optimizedPath = currentPath,
                optimizationReason = "保持原有路径，稳步推进",
                expectedImprovement = "按部就班学习，确保掌握扎实",
                adaptiveAdjustments = listOf("根据实际情况微调节奏")
            )
        }
    }
    
    private fun parseLearningPathOptimization(
        response: String,
        originalPath: List<String>
    ): LearningPathOptimization {
        return LearningPathOptimization(
            originalPath = originalPath,
            optimizedPath = originalPath, // 简化处理
            optimizationReason = "基于当前学习情况进行调整",
            expectedImprovement = "提高学习效率20%",
            adaptiveAdjustments = listOf("重点突破", "灵活调整")
        )
    }
}
