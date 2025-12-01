package com.example.educationapp.ai

import android.util.Log
import com.example.educationapp.data.LearningRecord
import com.example.educationapp.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * AI智能出题引擎
 * 创新功能：根据学生学习状态和知识掌握情况，实时生成个性化题目
 */
class AIQuestionGenerator {
    
    private val zhipuAIService = ZhipuAIService()
    private val emotionRecognizer = AIEmotionRecognizer()
    
    companion object {
        private const val TAG = "AIQuestionGenerator"
    }
    
    /**
     * 智能题目数据类
     */
    data class AIGeneratedQuestion(
        val id: String,
        val subject: String,
        val topic: String,
        val question: String,
        val options: List<String> = emptyList(), // 选择题选项
        val correctAnswer: String,
        val explanation: String,
        val difficulty: String, // "入门", "基础", "中级", "高级", "挑战"
        val questionType: String, // "选择题", "填空题", "解答题", "创意题"
        val knowledgePoints: List<String>,
        val estimatedTime: Int, // 预计完成时间（秒）
        val adaptiveReason: String, // AI生成这道题的原因
        val creativityLevel: String, // "标准", "创新", "突破"
        val scenarioContext: String = "" // 题目场景背景
    )
    
    /**
     * 题目生成配置
     */
    data class QuestionGenerationConfig(
        val targetDifficulty: String? = null,
        val questionCount: Int = 1,
        val focusWeakPoints: Boolean = true,
        val includeCreativeQuestions: Boolean = true,
        val timeLimit: Int? = null,
        val preferredQuestionTypes: List<String> = emptyList(),
        val learningObjective: String = ""
    )
    
    /**
     * 核心创新功能：AI动态出题
     * 根据学生当前状态和学习历史，实时生成最适合的题目
     */
    suspend fun generateAdaptiveQuestions(
        user: User,
        subject: String,
        topic: String,
        learningHistory: List<LearningRecord>,
        currentEmotionalState: AIEmotionRecognizer.EmotionalState? = null,
        config: QuestionGenerationConfig = QuestionGenerationConfig()
    ): Result<List<AIGeneratedQuestion>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "开始AI智能出题...")
            
            // 1. 分析学生知识掌握情况
            val knowledgeAnalysis = analyzeKnowledgeMastery(user, subject, topic, learningHistory)
            
            // 2. 根据情绪状态调整出题策略
            val adaptiveStrategy = determineAdaptiveStrategy(currentEmotionalState, knowledgeAnalysis, config)
            
            // 3. 构建AI出题提示
            val questionPrompt = buildQuestionGenerationPrompt(
                user, subject, topic, knowledgeAnalysis, adaptiveStrategy, config
            )
            
            // 4. 调用AI生成题目
            val aiResult = zhipuAIService.sendChatMessage(questionPrompt, user)
            
            aiResult.fold(
                onSuccess = { aiResponse ->
                    Log.d(TAG, "AI回复内容: $aiResponse")
                    val questions = parseGeneratedQuestions(aiResponse, subject, topic, adaptiveStrategy)
                    if (questions.isNotEmpty()) {
                        Log.d(TAG, "AI出题成功: ${questions.size}道题")
                    Result.success(questions)
                    } else {
                        Log.w(TAG, "AI回复解析失败，使用智能默认题目")
                        // 使用智能默认题目而不是模板
                        val smartQuestions = generateSmartDefaultQuestions(subject, topic, user.grade, config.questionCount)
                        Result.success(smartQuestions)
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "AI出题失败: ${error.message}", error)
                    // 使用智能默认题目
                    val smartQuestions = generateSmartDefaultQuestions(subject, topic, user.grade, config.questionCount)
                    Result.success(smartQuestions)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "出题异常", e)
            Result.failure(e)
        }
    }
    
    /**
     * 创新功能：创意题目生成
     * 将抽象概念转化为有趣的现实场景
     */
    suspend fun generateCreativeScenarioQuestions(
        user: User,
        subject: String,
        concept: String,
        studentInterests: List<String> = emptyList()
    ): Result<List<AIGeneratedQuestion>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "生成创意场景题目...")
            
            val creativePrompt = buildCreativePrompt(user, subject, concept, studentInterests)
            val result = zhipuAIService.sendChatMessage(creativePrompt, user)
            
            result.fold(
                onSuccess = { response ->
                    val creativeQuestions = parseCreativeQuestions(response, subject, concept)
                    Log.d(TAG, "创意题目生成完成: ${creativeQuestions.size}道题")
                    Result.success(creativeQuestions)
                },
                onFailure = { error ->
                    Log.w(TAG, "创意题目生成失败", error)
                    Result.success(emptyList())
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "创意题目异常", e)
            Result.failure(e)
        }
    }
    
    /**
     * 创新功能：难度动态调节
     * 根据学生答题表现实时调整下一题难度
     */
    suspend fun adjustDifficultyBasedOnPerformance(
        user: User,
        lastQuestion: AIGeneratedQuestion,
        answerCorrect: Boolean,
        responseTime: Long,
        confidenceLevel: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val adjustmentPrompt = buildDifficultyAdjustmentPrompt(
                user, lastQuestion, answerCorrect, responseTime, confidenceLevel
            )
            
            val result = zhipuAIService.sendChatMessage(adjustmentPrompt, user)
            
            result.fold(
                onSuccess = { adjustment ->
                    val newDifficulty = parseDifficultyAdjustment(adjustment)
                    Log.d(TAG, "难度调整: ${lastQuestion.difficulty} -> $newDifficulty")
                    Result.success(newDifficulty)
                },
                onFailure = { error ->
                    Log.w(TAG, "难度调整失败", error)
                    // 简单的规则调整
                    val newDifficulty = if (answerCorrect && responseTime < 30000) {
                        increaseDifficulty(lastQuestion.difficulty)
                    } else if (!answerCorrect) {
                        decreaseDifficulty(lastQuestion.difficulty)
                    } else {
                        lastQuestion.difficulty
                    }
                    Result.success(newDifficulty)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "难度调整异常", e)
            Result.failure(e)
        }
    }
    
    // 私有实现方法
    private fun analyzeKnowledgeMastery(
        user: User,
        subject: String,
        topic: String,
        learningHistory: List<LearningRecord>
    ): KnowledgeAnalysis {
        val subjectRecords = learningHistory.filter { it.subject == subject }
        val topicRecords = subjectRecords.filter { it.topic == topic }
        
        val averageScore = topicRecords.map { it.score }.average().toFloat()
        val recentPerformance = topicRecords.takeLast(5).map { it.score }.average().toFloat()
        val improvementTrend = recentPerformance - averageScore
        
        val commonMistakes = identifyCommonMistakes(topicRecords)
        val strongAreas = identifyStrongAreas(topicRecords)
        
        return KnowledgeAnalysis(
            masteryLevel = when {
                averageScore >= 90 -> "精通"
                averageScore >= 80 -> "熟练"
                averageScore >= 70 -> "掌握"
                averageScore >= 60 -> "基础"
                else -> "薄弱"
            },
            averageScore = averageScore,
            improvementTrend = improvementTrend,
            commonMistakes = commonMistakes,
            strongAreas = strongAreas,
            practiceCount = topicRecords.size
        )
    }
    
    private fun determineAdaptiveStrategy(
        emotionalState: AIEmotionRecognizer.EmotionalState?,
        knowledgeAnalysis: KnowledgeAnalysis,
        config: QuestionGenerationConfig
    ): AdaptiveStrategy {
        val baseStrategy = when (knowledgeAnalysis.masteryLevel) {
            "精通" -> "挑战提升"
            "熟练" -> "巩固拓展"
            "掌握" -> "强化练习"
            "基础" -> "基础巩固"
            else -> "基础入门"
        }
        
        // 根据情绪状态调整策略
        val adjustedStrategy = emotionalState?.let { state ->
            when {
                state.stressLevel > 7 -> "减压练习"
                state.fatigueLevel > 7 -> "轻松巩固"
                state.focusLevel < 4 -> "趣味激发"
                state.confidenceLevel > 8 -> "挑战进阶"
                else -> baseStrategy
            }
        } ?: baseStrategy
        
        return AdaptiveStrategy(
            strategy = adjustedStrategy,
            targetDifficulty = config.targetDifficulty ?: mapStrategyToDifficulty(adjustedStrategy),
            emphasizeWeakPoints = config.focusWeakPoints && knowledgeAnalysis.commonMistakes.isNotEmpty(),
            includeCreative = config.includeCreativeQuestions && emotionalState?.focusLevel ?: 5 >= 6,
            timeConstraint = config.timeLimit
        )
    }
    
    private fun buildQuestionGenerationPrompt(
        user: User,
        subject: String,
        topic: String,
        knowledgeAnalysis: KnowledgeAnalysis,
        strategy: AdaptiveStrategy,
        config: QuestionGenerationConfig
    ): String {
        // 🎯 优化：使用高成功率的专门化提示词，严格科目匹配
        return when (subject.lowercase()) {
            "数学", "math" -> buildOptimizedMathPrompt(user, topic, strategy, knowledgeAnalysis)
            "物理", "physics" -> buildOptimizedPhysicsPrompt(user, topic, strategy, knowledgeAnalysis)
            "语文", "chinese" -> buildOptimizedChinesePrompt(user, topic, strategy, knowledgeAnalysis)
            "英语", "english" -> buildOptimizedEnglishPrompt(user, topic, strategy, knowledgeAnalysis)
            "化学", "chemistry" -> buildOptimizedChemistryPrompt(user, topic, strategy, knowledgeAnalysis)
            "生物", "biology" -> buildOptimizedBiologyPrompt(user, topic, strategy, knowledgeAnalysis)
            "历史", "history" -> buildOptimizedHistoryPrompt(user, topic, strategy, knowledgeAnalysis)
            "地理", "geography" -> buildOptimizedGeographyPrompt(user, topic, strategy, knowledgeAnalysis)
            "计算机", "computer", "编程", "programming" -> buildOptimizedComputerPrompt(user, topic, strategy, knowledgeAnalysis)
            else -> buildOptimizedGeneralPrompt(user, subject, topic, knowledgeAnalysis, strategy, config)
        }
    }
    
    /**
     * 🎯 优化版数学专门化提示词 - 高成功率AI出题
     */
    private fun buildOptimizedMathPrompt(
        user: User,
        topic: String,
        strategy: AdaptiveStrategy,
        knowledgeAnalysis: KnowledgeAnalysis
    ): String {
        val gradeLevel = when {
            user.grade.contains("小学") -> "小学数学（基础算术、简单几何）"
            user.grade.contains("初中") -> "初中数学（代数基础、平面几何）"
            user.grade.contains("高中") -> "高中数学（函数、导数、立体几何）"
            user.grade.contains("大学") -> "大学数学（高等数学、线性代数）"
            else -> "基础数学"
        }
        
        val difficultyGuide = when(strategy.targetDifficulty) {
            "入门" -> "概念理解为主，计算简单"
            "基础" -> "基本公式应用，一步计算"
            "中级" -> "需要2-3步推理，适中计算"
            "高级" -> "综合应用，多步骤解题"
            "挑战" -> "创新思维，复杂推理"
            else -> "适中难度"
        }

        return """
作为${gradeLevel}专业教师，请为学生生成一道高质量数学题目。

【学生分析】
年级：${user.grade}
主题：${topic}
难度：${strategy.targetDifficulty}（${difficultyGuide}）
掌握水平：${knowledgeAnalysis.masteryLevel}
薄弱环节：${knowledgeAnalysis.commonMistakes.joinToString("、")}

【出题要求】
1. 题目必须是${topic}相关的具体数学问题
2. 包含明确的数值、条件和要求
3. 4个选择项，只有1个正确答案
4. 干扰项要合理（常见错误、计算失误等）
5. 难度符合${strategy.targetDifficulty}水平

【输出格式】严格JSON格式：
```json
{
  "question": "具体数学题目（包含数值和条件）",
  "options": ["A. 选项内容1", "B. 选项内容2", "C. 选项内容3", "D. 选项内容4"],
  "correct_answer": "正确答案",
  "explanation": "详细解题步骤和知识点分析",
  "knowledge_points": ["知识点1", "知识点2"],
  "estimated_time": 90
}
```

【示例参考】
如果是函数题目，应该像：
"已知函数f(x) = 2x² - 4x + 1，求f(x)在区间[0,3]上的最小值"
选项设计：A. -1  B. 0  C. 1  D. 2

请确保：
- 题目数据具体明确
- 计算过程可验证
- 选项设计合理
- 解析清晰完整
        """.trimIndent()
    }
    
    /**
     * 🎯 优化版物理专门化提示词
     */
    private fun buildOptimizedPhysicsPrompt(
        user: User,
        topic: String,
        strategy: AdaptiveStrategy,
        knowledgeAnalysis: KnowledgeAnalysis
    ): String {
        val gradeContext = when {
            user.grade.contains("初中") -> "初中物理（基础概念、简单计算）"
            user.grade.contains("高中") -> "高中物理（牛顿定律、电磁学、热学）"
            user.grade.contains("大学") -> "大学物理（理论力学、电磁学、量子物理）"
            else -> "基础物理"
        }

        return """
作为${gradeContext}专业教师，请生成一道实用的物理题目。

【学生情况】
年级：${user.grade}
主题：${topic}
难度：${strategy.targetDifficulty}
掌握程度：${knowledgeAnalysis.masteryLevel}

【出题要求】
1. 题目要有具体的物理情景和数值
2. 涉及${topic}的核心概念和公式
3. 4个选择选项，答案唯一且可计算验证
4. 干扰项基于常见物理概念混淆

【JSON格式输出】：
```json
{
  "question": "具体物理情景题目（包含数值和单位）",
  "options": ["A选项（含单位）", "B选项", "C选项", "D选项"],
  "correct_answer": "正确答案",
  "explanation": "物理原理分析和计算过程",
  "knowledge_points": ["物理概念1", "物理概念2"],
  "estimated_time": 120
}
```

示例：一个质量为2kg的物体，受到10N的水平拉力，摩擦系数为0.2，求加速度。
        """.trimIndent()
    }
    
    /**
     * 🎯 优化版语文专门化提示词 - 包含完整阅读材料
     */
    private fun buildOptimizedChinesePrompt(
        user: User,
        topic: String,
        strategy: AdaptiveStrategy,
        knowledgeAnalysis: KnowledgeAnalysis
    ): String {
        return """
作为语文教育专家，请为${user.grade}学生生成一道包含完整阅读材料的${topic}题目。

【严格要求 - 必须包含具体的阅读内容】
🚨 绝对禁止：只写"阅读以下短文"、"阅读下面的材料"等提示语而不提供具体内容！
✅ 必须包含：具体的诗歌全文、文章段落、对话内容等完整文字材料！

【材料内容要求】
- 现代文阅读：必须提供200-500字的具体文章段落，包含完整故事或说明
- 古文阅读：必须提供完整的古文片段原文（50-200字），并标注重点字词解释  
- 诗词鉴赏：必须提供完整的诗词原文，包含作者、朝代、每一句诗
- 文学常识：必须提供具体的作品片段或背景描述
- 语言文字运用：必须提供具体的句子或段落实例

【错误示例 - 绝对不允许】
❌ "阅读以下短文，回答问题。"（没有提供短文内容）
❌ "根据下面的材料回答问题。"（没有提供材料内容）
❌ "阅读这首诗，分析其情感。"（没有提供诗歌内容）

【正确示例 - 必须这样做】
✅ "阅读以下短文，回答问题：
小明是一个爱读书的孩子。每天放学后，他都会到图书馆里安静地阅读各种书籍。今天，他发现了一本关于宇宙探索的科普书，书中详细介绍了太阳系的八大行星..."

【题目结构要求】
1. 阅读材料：完整展示诗歌/文章/片段原文
2. 题目问句：基于材料的理解分析问题
3. 选项设计：4个选择项，考查理解、分析、鉴赏能力
4. 难度控制：符合${strategy.targetDifficulty}水平

【示例格式】
题目应包含：
阅读下面的诗歌，完成题目：

春晓
孟浩然（唐）
春眠不觉晓，处处闻啼鸟。
夜来风雨声，花落知多少。

这首诗表达了诗人怎样的思想感情？

【JSON格式输出 - 严格要求】：
```json
{
  "question": "阅读以下材料，回答问题：

[这里必须是具体的文字内容，比如：]
春天到了，公园里的花儿都开了。小红和妈妈一起去公园散步。她看到了红色的玫瑰花、黄色的迎春花，还有粉色的桃花。小红高兴地说："妈妈，这些花真美丽！"妈妈笑着说："是啊，春天是最美的季节。"

根据短文内容，小红在公园里看到了哪些颜色的花？",
  "options": ["A. 红色、黄色、粉色", "B. 红色、蓝色、白色", "C. 黄色、紫色、粉色", "D. 红色、黄色、白色"],
  "correct_answer": "A. 红色、黄色、粉色",
  "explanation": "根据短文内容，小红看到了红色的玫瑰花、黄色的迎春花和粉色的桃花，所以答案是A。",
  "knowledge_points": ["现代文阅读理解", "细节信息提取"],
  "estimated_time": 120
}
```

🚨 重要提醒：
1. question字段必须包含完整的具体文字内容，不能只有提示语！
2. 材料内容必须完整，学生能完全看到要阅读的文字！
3. 不要使用占位符如[内容]，要写出具体的文字！

务必确保question字段包含完整的阅读材料！
        """.trimIndent()
    }

    /**
     * 🎯 优化版英语专门化提示词 - 包含完整阅读材料
     */
    private fun buildOptimizedEnglishPrompt(
        user: User,
        topic: String,
        strategy: AdaptiveStrategy,
        knowledgeAnalysis: KnowledgeAnalysis
    ): String {
        val gradeLevel = when {
            user.grade.contains("小学") -> "Elementary English (basic vocabulary, simple grammar)"
            user.grade.contains("初中") -> "Middle School English (grammar, reading comprehension)"
            user.grade.contains("高中") -> "High School English (advanced grammar, literature)"
            user.grade.contains("大学") -> "College English (academic writing, complex grammar)"
            else -> "General English"
        }
        
        val difficultyGuide = when(strategy.targetDifficulty) {
            "入门" -> "basic vocabulary and simple sentence structures"
            "基础" -> "common grammar rules and everyday vocabulary"
            "中级" -> "intermediate grammar and reading comprehension"
            "高级" -> "advanced grammar, complex sentences, and idioms"
            "挑战" -> "sophisticated language use and critical thinking"
            else -> "appropriate difficulty level"
        }

        return """
As an experienced English teacher for ${gradeLevel}, please generate a comprehensive English question with complete reading material for a ${user.grade} student.

【CRITICAL REQUIREMENT - Must Include Specific Reading Content】
🚨 ABSOLUTELY FORBIDDEN: Writing only "Read the passage below" or "Read the following text" without providing the actual text content!
✅ MUST INCLUDE: Specific complete text content - actual passages, dialogues, stories, poems with every word written out!

【Content Requirements】
- Reading Comprehension: Must provide 100-300 word specific passage with complete story/explanation
- Grammar in Context: Must provide specific complete sentences or paragraphs with actual grammar examples
- Vocabulary: Must provide specific context sentences with actual word usage examples
- Literature: Must provide complete poem text or story excerpts with every line written
- Dialogue: Must provide complete conversation with actual spoken words

【WRONG Examples - Absolutely NOT Allowed】
❌ "Read the passage below and answer the question." (No passage provided)
❌ "Based on the following dialogue, choose the correct answer." (No dialogue provided)
❌ "Read this story and analyze it." (No story content provided)

【CORRECT Examples - Must Do This】
✅ "Read the following passage and answer the question:
Tom loves playing basketball. Every afternoon after school, he goes to the basketball court with his friends. Today, they played for two hours. Tom scored 15 points and helped his team win the game..."

【Question Structure Requirements】
1. Reading Material: Complete text/passage/dialogue that students can fully read
2. Question: Based on the provided material, testing comprehension/grammar/vocabulary
3. Options: 4 choices testing understanding, analysis, or language skills
4. Difficulty: Match ${strategy.targetDifficulty} level (${difficultyGuide})

【Example Format】
Question should include:
Read the following passage:

[Complete passage/dialogue/story content here - 100-300 words]

Based on the passage above, [specific question about the content]

【Student Analysis】
Grade: ${user.grade}
Topic: ${topic}
Difficulty: ${strategy.targetDifficulty} (${difficultyGuide})
Mastery Level: ${knowledgeAnalysis.masteryLevel}
Common Mistakes: ${knowledgeAnalysis.commonMistakes.joinToString(", ")}

【REQUIREMENTS】
1. The question MUST be in English language subject area
2. MUST include complete reading material that students can see in full
3. Focus on ${topic} (grammar/vocabulary/reading comprehension etc.)
4. Include 4 multiple choice options with only 1 correct answer
5. Distractors should be based on common English learning mistakes
6. Difficulty should match ${strategy.targetDifficulty} level
7. Question should be practical and educational

【CRITICAL】
- This is an ENGLISH subject question, NOT Chinese literature
- All content must relate to English language learning
- Include English grammar, vocabulary, or language skills

【JSON Format Output - Strict Requirements】:
```json
{
  "question": "Read the following passage and answer the question:

[Must be specific text content, for example:]
Sarah woke up early on Saturday morning. She was excited because today was her birthday party. Her mom had prepared a chocolate cake and invited all her friends from school. At 2 o'clock, her friends arrived with colorful balloons and presents. They played games, sang songs, and had lots of fun together.

Based on the passage, what kind of cake did Sarah's mom prepare?",
  "options": ["A. Chocolate cake", "B. Vanilla cake", "C. Strawberry cake", "D. Lemon cake"],
  "correct_answer": "A. Chocolate cake",
  "explanation": "According to the passage, Sarah's mom had prepared a chocolate cake for the birthday party.",
  "knowledge_points": ["Reading comprehension", "Detail extraction"],
  "estimated_time": 150
}
```

🚨 CRITICAL REMINDERS:
1. The question field MUST contain complete specific text content, not just prompts!
2. Students must be able to see the complete text they need to read!
3. Do not use placeholders like [content] - write actual specific text!

ENSURE the question field includes complete English reading material that students can see and understand!
        """.trimIndent()
    }

    /**
     * 🎯 优化版化学专门化提示词 - 确保生成化学题目
     */
    private fun buildOptimizedChemistryPrompt(
        user: User,
        topic: String,
        strategy: AdaptiveStrategy,
        knowledgeAnalysis: KnowledgeAnalysis
    ): String {
        return """
作为化学教育专家，请为${user.grade}学生生成一道化学${topic}题目。

【学生分析】
年级：${user.grade}
主题：${topic}
难度：${strategy.targetDifficulty}
掌握水平：${knowledgeAnalysis.masteryLevel}

【严格要求】
1. 题目必须是化学学科内容，不能是其他科目
2. 涉及化学反应、化学方程式、元素性质、化学实验等
3. 包含4个选择项，只有1个正确答案
4. 干扰项基于常见化学概念混淆
5. 难度符合${strategy.targetDifficulty}水平

【重要提醒】
- 这是化学科目题目，不是数学、物理或语文
- 必须包含化学元素、化合物、反应等化学概念
- 题目应该考查化学知识和原理

【JSON格式输出】：
```json
{
  "question": "化学题目内容（包含化学方程式或化学概念）",
  "options": ["A选项", "B选项", "C选项", "D选项"],
  "correct_answer": "正确答案",
  "explanation": "化学原理解释和答案分析",
  "knowledge_points": ["化学概念1", "化学概念2"],
  "estimated_time": 150
}
```

示例：下列化学反应中，属于置换反应的是？
        """.trimIndent()
    }

    /**
     * 🎯 优化版生物专门化提示词 - 确保生成生物题目
     */
    private fun buildOptimizedBiologyPrompt(
        user: User,
        topic: String,
        strategy: AdaptiveStrategy,
        knowledgeAnalysis: KnowledgeAnalysis
    ): String {
        return """
作为生物教育专家，请为${user.grade}学生生成一道生物${topic}题目。

【学生分析】
年级：${user.grade}
主题：${topic}
难度：${strategy.targetDifficulty}
掌握水平：${knowledgeAnalysis.masteryLevel}

【严格要求】
1. 题目必须是生物学科内容，不能是其他科目
2. 涉及细胞、遗传、生态、进化、生理等生物概念
3. 包含4个选择项，只有1个正确答案
4. 干扰项基于常见生物概念混淆
5. 难度符合${strategy.targetDifficulty}水平

【重要提醒】
- 这是生物科目题目，不是化学、物理或语文
- 必须包含生物体结构、功能、生命过程等
- 题目应该考查生物学知识和原理

【JSON格式输出】：
```json
{
  "question": "生物题目内容（包含生物学概念）",
  "options": ["A选项", "B选项", "C选项", "D选项"],
  "correct_answer": "正确答案",
  "explanation": "生物学原理解释和答案分析",
  "knowledge_points": ["生物概念1", "生物概念2"],
  "estimated_time": 140
}
```

示例：细胞膜的主要功能是什么？
        """.trimIndent()
    }

    /**
     * 🎯 优化版历史专门化提示词 - 确保生成历史题目
     */
    private fun buildOptimizedHistoryPrompt(
        user: User,
        topic: String,
        strategy: AdaptiveStrategy,
        knowledgeAnalysis: KnowledgeAnalysis
    ): String {
        return """
作为历史教育专家，请为${user.grade}学生生成一道历史${topic}题目。

【学生分析】
年级：${user.grade}
主题：${topic}
难度：${strategy.targetDifficulty}
掌握水平：${knowledgeAnalysis.masteryLevel}

【严格要求】
1. 题目必须是历史学科内容，不能是其他科目
2. 涉及历史事件、历史人物、历史背景、历史意义等
3. 包含4个选择项，只有1个正确答案
4. 干扰项基于历史时期或事件的混淆
5. 难度符合${strategy.targetDifficulty}水平

【重要提醒】
- 这是历史科目题目，不是地理、语文或政治
- 必须包含具体的历史时间、人物、事件
- 题目应该考查历史知识和历史思维

【JSON格式输出】：
```json
{
  "question": "历史题目内容（包含历史事件或人物）",
  "options": ["A选项", "B选项", "C选项", "D选项"],
  "correct_answer": "正确答案",
  "explanation": "历史背景和事件分析",
  "knowledge_points": ["历史概念1", "历史概念2"],
  "estimated_time": 160
}
```

示例：中国古代哪个朝代统一了货币和文字？
        """.trimIndent()
    }

    /**
     * 🎯 优化版地理专门化提示词 - 确保生成地理题目
     */
    private fun buildOptimizedGeographyPrompt(
        user: User,
        topic: String,
        strategy: AdaptiveStrategy,
        knowledgeAnalysis: KnowledgeAnalysis
    ): String {
        return """
作为地理教育专家，请为${user.grade}学生生成一道地理${topic}题目。

【学生分析】
年级：${user.grade}
主题：${topic}
难度：${strategy.targetDifficulty}
掌握水平：${knowledgeAnalysis.masteryLevel}

【严格要求】
1. 题目必须是地理学科内容，不能是其他科目
2. 涉及地形、气候、人文地理、自然地理等
3. 包含4个选择项，只有1个正确答案
4. 干扰项基于地理概念或地区的混淆
5. 难度符合${strategy.targetDifficulty}水平

【重要提醒】
- 这是地理科目题目，不是历史、语文或政治
- 必须包含地理位置、地理特征、地理现象
- 题目应该考查地理知识和空间思维

【JSON格式输出】：
```json
{
  "question": "地理题目内容（包含地理概念或地区）",
  "options": ["A选项", "B选项", "C选项", "D选项"],
  "correct_answer": "正确答案",
  "explanation": "地理原理和现象分析",
  "knowledge_points": ["地理概念1", "地理概念2"],
  "estimated_time": 140
}
```

示例：世界上最大的沙漠是哪个？
        """.trimIndent()
    }

    /**
     * 🎯 优化版计算机专门化提示词 - 确保生成计算机题目
     */
    private fun buildOptimizedComputerPrompt(
        user: User,
        topic: String,
        strategy: AdaptiveStrategy,
        knowledgeAnalysis: KnowledgeAnalysis
    ): String {
        return """
作为计算机科学教育专家，请为${user.grade}学生生成一道计算机${topic}题目。

【学生分析】
年级：${user.grade}
主题：${topic}
难度：${strategy.targetDifficulty}
掌握水平：${knowledgeAnalysis.masteryLevel}

【严格要求】
1. 题目必须是计算机学科内容，不能是其他科目
2. 涉及编程、算法、数据结构、操作系统、网络等
3. 包含4个选择项，只有1个正确答案
4. 干扰项基于常见编程概念或技术混淆
5. 难度符合${strategy.targetDifficulty}水平

【重要提醒】
- 这是计算机科目题目，不是数学、物理或英语
- 必须包含编程语言、算法逻辑、计算机原理
- 题目应该考查计算机科学知识和编程思维

【JSON格式输出】：
```json
{
  "question": "计算机题目内容（包含编程或计算机概念）",
  "options": ["A选项", "B选项", "C选项", "D选项"],
  "correct_answer": "正确答案",
  "explanation": "计算机原理和编程逻辑解释",
  "knowledge_points": ["计算机概念1", "计算机概念2"],
  "estimated_time": 180
}
```

示例：在Python中，下列哪个关键字用于定义函数？
        """.trimIndent()
    }
    
    /**
     * 🎯 优化版通用提示词 - 严格科目限制
     */
    private fun buildOptimizedGeneralPrompt(
        user: User,
        subject: String,
        topic: String,
        knowledgeAnalysis: KnowledgeAnalysis,
        strategy: AdaptiveStrategy,
        config: QuestionGenerationConfig
    ): String {
        return """
作为${subject}专业教师，为${user.grade}学生生成${topic}题目。

【严格科目要求】
⚠️ 重要：题目必须严格属于${subject}学科，不能是其他任何科目！
- 如果是数学，必须包含数学概念、公式、计算
- 如果是语文，必须包含语言文字、文学、阅读理解
- 如果是英语，必须包含英语语法、词汇、语言技能
- 如果是物理，必须包含物理概念、定律、实验
- 如果是化学，必须包含化学反应、元素、化合物
- 如果是生物，必须包含生物体、生命过程、生态
- 如果是历史，必须包含历史事件、人物、时代
- 如果是地理，必须包含地理位置、地形、气候

学生信息：
年级：${user.grade}
主题：${topic}
难度：${strategy.targetDifficulty}
掌握水平：${knowledgeAnalysis.masteryLevel}

【题目要求】
1. 科目：严格限定为${subject}，绝对不能跨科目
2. 选项：4个选择项，只有1个正确答案
3. 难度：符合${strategy.targetDifficulty}水平
4. 时间：预计完成时间合理

JSON格式输出：
```json
{
  "question": "${subject}题目内容（必须包含${subject}学科特有概念）",
  "options": ["A. 选项内容1", "B. 选项内容2", "C. 选项内容3", "D. 选项内容4"],
  "correct_answer": "正确答案",
  "explanation": "${subject}学科原理解释和答案分析",
  "knowledge_points": ["${subject}知识点1", "${subject}知识点2"],
  "estimated_time": 120
}
```

示例说明：确保题目内容、选项、解释都严格属于${subject}学科范围。
        """.trimIndent()
    }
    
    /**
     * 🎯 数学专门化提示词 - 确保生成真实可用的数学题目
     */
    private fun buildMathQuestionPrompt(
        user: User,
        topic: String,
        grade: String,
        difficulty: String,
        knowledgeAnalysis: KnowledgeAnalysis
    ): String {
        val gradeContext = when (grade) {
            "高中", "高一", "高二", "高三" -> "高中数学水平，注重逻辑推理，可以有适中的计算量"
            "初中", "初一", "初二", "初三" -> "初中数学水平，注重基础概念，计算相对简单"
            "大学", "大一", "大二", "大三", "大四" -> "大学数学水平，可以有理论深度和复杂计算"
            else -> "基础数学水平"
        }
        
        val topicSpecific = when (topic) {
            "函数", "函数与导数" -> """
                函数专题具体要求：
                - 必须包含具体的函数表达式，如 f(x) = 2x² - 4x + 1
                - 可以考查定义域、值域、单调性、极值
                - 如果涉及导数，要有具体的求导过程
                - 题目要有明确的数值和条件
            """
            "极限", "极限理论" -> """
                极限专题具体要求：
                - 必须给出具体的极限表达式，如 lim(x→0) sinx/x
                - 要明确极限的计算方法（洛必达法则、等价无穷小等）
                - 包含具体的数值计算步骤
            """
            "立体几何" -> """
                立体几何专题要求：
                - 必须描述具体的几何体（正方体、圆锥等）
                - 给出明确的尺寸数据
                - 考查体积、表面积、距离、角度等
            """
            else -> "基础数学概念的具体应用"
        }
        
        return """
            你是一位经验丰富的${grade}数学老师，请为学生生成一道关于"${topic}"的${difficulty}级数学题。
            
            学生情况：
            - 年级：${grade}（${gradeContext}）
            - 掌握水平：${knowledgeAnalysis.masteryLevel}
            - 平均分：${knowledgeAnalysis.averageScore}分
            - 需要强化：${knowledgeAnalysis.commonMistakes.joinToString("、")}
            
            ${topicSpecific}
            
            ⚠️ 重要要求（必须严格遵守）：
            1. 题目必须包含具体的数值、函数表达式或几何尺寸
            2. 选择题必须提供4个具体的选项（A、B、C、D）
            3. 答案要是具体的数值或表达式，不能是"选项A"
            4. 解题步骤要详细，每一步都要写清楚
            5. 难度符合${difficulty}等级
            
            请严格按照以下格式返回：
            
            题目内容：
            [写出完整具体的题目，包含所有数据]
            
            选项：
            A. [具体答案内容]
            B. [具体答案内容]
            C. [具体答案内容]
            D. [具体答案内容]
            
            正确答案：
            [写出具体答案，如 "2" 或 "x=3"，不要写 "A" 或 "选项A"]
            
            解题步骤：
            1. [具体操作，如：将x=1代入f(x)=x²+2x+1]
            2. [具体计算，如：f(1)=1+2+1=4]
            3. [继续步骤...]
            
            知识点：
            [涉及的具体知识点]
            
            解题关键：
            [容易出错的地方和解题技巧]
            
            示例题目：
            题目内容：
            已知函数f(x) = x² - 4x + 3，求f(x)在区间[1,4]上的最小值。
            
            选项：
            A. -1
            B. 0  
            C. 3
            D. 7
            
            正确答案：
            -1
            
            解题步骤：
            1. 求导数：f'(x) = 2x - 4
            2. 令f'(x) = 0，得：2x - 4 = 0，解得x = 2
            3. 计算关键点函数值：f(1) = 1-4+3 = 0，f(2) = 4-8+3 = -1，f(4) = 16-16+3 = 3
            4. 比较得最小值为-1
            
            知识点：
            二次函数性质、导数的应用、函数在闭区间上的最值
            
            解题关键：
            找到所有可能的极值点和端点，逐一计算比较
        """.trimIndent()
    }
    
    /**
     * 🎯 物理专门化提示词
     */
    private fun buildPhysicsQuestionPrompt(
        user: User,
        topic: String,
        grade: String,
        difficulty: String,
        knowledgeAnalysis: KnowledgeAnalysis
    ): String {
        return """
            作为${grade}物理老师，请生成一道"${topic}"的${difficulty}级物理题。
            
            要求：
            1. 必须有具体的物理情境和数值
            2. 包含明确的物理量和单位
            3. 解答要体现物理思维过程
            4. 符合${grade}学生的认知水平
            
            格式同数学题目，但注重物理过程分析。
        """.trimIndent()
    }
    
    /**
     * 🎯 语文专门化提示词
     */
    private fun buildChineseQuestionPrompt(
        user: User,
        topic: String,
        grade: String,
        difficulty: String,
        knowledgeAnalysis: KnowledgeAnalysis
    ): String {
        return """
            作为${grade}语文老师，请生成一道"${topic}"的${difficulty}级语文题。
            
            要求：
            1. 选择适合${grade}的文本材料
            2. 考查理解、分析、表达能力
            3. 题目要有一定的思维深度
            4. 选项要有明确的区分度
            
            格式同上，注重语言文字运用和理解能力。
        """.trimIndent()
    }
    
    /**
     * 🎯 通用科目提示词（保留原有逻辑）
     */
    private fun buildGeneralQuestionPrompt(
        user: User,
        subject: String,
        topic: String,
        knowledgeAnalysis: KnowledgeAnalysis,
        strategy: AdaptiveStrategy,
        config: QuestionGenerationConfig
    ): String {
        return """
            作为AI出题专家，请为以下学生生成个性化题目：
            
            学生信息：
            - 姓名: ${user.name}
            - 年级: ${user.grade}
            - 学习风格: ${user.learningStyle}
            
            科目信息：
            - 科目: $subject
            - 主题: $topic
            - 学习目标: ${config.learningObjective}
            
            知识掌握分析：
            - 掌握水平: ${knowledgeAnalysis.masteryLevel}
            - 平均得分: ${knowledgeAnalysis.averageScore}分
            - 常见错误: ${knowledgeAnalysis.commonMistakes.joinToString(", ")}
            
            出题策略：
            - 策略类型: ${strategy.strategy}
            - 目标难度: ${strategy.targetDifficulty}
            
            请生成具体、实用的题目，按照标准格式返回。
        """.trimIndent()
    }
    
    private fun buildCreativePrompt(
        user: User,
        subject: String,
        concept: String,
        interests: List<String>
    ): String {
        val interestContext = if (interests.isNotEmpty()) {
            "学生感兴趣的领域：${interests.joinToString(", ")}"
        } else {
            "请创造有趣的现实场景"
        }
        
        return """
            作为AI创意出题专家，请将抽象的学习概念转化为有趣的现实场景题目：
            
            学生：${user.name} (${user.grade})
            科目：$subject
            核心概念：$concept
            $interestContext
            
            创意要求：
            1. 将抽象概念具象化为生动的故事场景
            2. 题目要有趣、贴近生活
            3. 保持教育价值，确保概念理解
            4. 适合学生年龄和认知水平
            5. 如果可能，结合学生兴趣领域
            
            请生成2-3道创意题目，每道题包含：
            - 有趣的场景背景
            - 具体的问题
            - 答案和解释
            - 创意亮点说明
            
            示例格式：
            创意题目1：
            场景：[生动的故事背景]
            问题：[基于场景的具体问题]
            答案：[答案]
            解释：[结合场景的解释]
            创意点：[这道题的创新之处]
        """.trimIndent()
    }
    
    private fun buildDifficultyAdjustmentPrompt(
        user: User,
        lastQuestion: AIGeneratedQuestion,
        answerCorrect: Boolean,
        responseTime: Long,
        confidenceLevel: Int
    ): String {
        return """
            作为AI难度调节专家，请分析学生答题表现并调整难度：
            
            学生：${user.name}
            
            上一题信息：
            - 题目：${lastQuestion.question}
            - 难度：${lastQuestion.difficulty}
            - 类型：${lastQuestion.questionType}
            
            答题表现：
            - 答案正确：${if (answerCorrect) "是" else "否"}
            - 用时：${responseTime/1000}秒
            - 学生自信度：${confidenceLevel}/10
            
            请分析并建议下一题的难度等级。
            
            难度等级：入门 < 基础 < 中级 < 高级 < 挑战
            
            调整原则：
            - 答对且用时短且自信高：提升难度
            - 答错或用时长或自信低：降低难度  
            - 表现平稳：保持难度
            
            请简洁回答建议的难度等级和调整理由。
        """.trimIndent()
    }
    
    private fun parseGeneratedQuestions(
        response: String,
        subject: String,
        topic: String,
        strategy: AdaptiveStrategy
    ): List<AIGeneratedQuestion> {
        Log.d(TAG, "🎯 开始解析AI响应: ${response.take(200)}...")
        
        val questions = mutableListOf<AIGeneratedQuestion>()
        
        try {
            // 🎯 优化1: 多种JSON解析策略
            val parsedQuestion = tryParseJsonResponse(response, subject, topic, strategy)
            if (parsedQuestion != null) {
                questions.add(parsedQuestion)
                Log.d(TAG, "✅ JSON解析成功")
                return questions
            }
            
            // 🎯 优化2: 智能文本解析
            val textParsedQuestion = tryParseTextResponse(response, subject, topic, strategy)
            if (textParsedQuestion != null) {
                questions.add(textParsedQuestion)
                Log.d(TAG, "✅ 文本解析成功")
                return questions
            }
            
            // 🎯 优化3: 关键词提取解析
            val keywordParsedQuestion = tryParseByKeywords(response, subject, topic, strategy)
            if (keywordParsedQuestion != null) {
                questions.add(keywordParsedQuestion)
                Log.d(TAG, "✅ 关键词解析成功")
                return questions
            }
            
            Log.w(TAG, "⚠️ 所有解析方法都失败，AI响应: $response")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ 解析异常: ${e.message}", e)
        }
        
        return questions.ifEmpty { 
            generateFallbackQuestions(subject, topic, 1)
        }
    }
    
    /**
     * 🎯 优化解析方法1: JSON格式解析
     */
    private fun tryParseJsonResponse(
        response: String,
        subject: String,
        topic: String,
        strategy: AdaptiveStrategy
    ): AIGeneratedQuestion? {
        return try {
            // 提取JSON部分
            val jsonStart = response.indexOf("{")
            val jsonEnd = response.lastIndexOf("}") + 1
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonString = response.substring(jsonStart, jsonEnd)
                Log.d(TAG, "提取的JSON: $jsonString")
                
                // 使用简单的JSON解析
                val questionText = extractJsonValue(jsonString, "question")
                val optionsText = extractJsonArray(jsonString, "options")
                val correctAnswer = extractJsonValue(jsonString, "correct_answer")
                val explanation = extractJsonValue(jsonString, "explanation")
                val knowledgePoints = extractJsonArray(jsonString, "knowledge_points")
                val estimatedTime = extractJsonValue(jsonString, "estimated_time")?.toIntOrNull() ?: 120
                
                if (questionText.isNotEmpty() && optionsText.isNotEmpty() && correctAnswer.isNotEmpty()) {
                    return createAIQuestion(
                        question = questionText,
                        options = optionsText,
                        correctAnswer = correctAnswer,
                        explanation = explanation,
                        knowledgePoints = knowledgePoints,
                        estimatedTime = estimatedTime,
                        subject = subject,
                        topic = topic,
                        difficulty = strategy.targetDifficulty
                    )
                }
            }
            null
        } catch (e: Exception) {
            Log.w(TAG, "JSON解析失败: ${e.message}")
            null
        }
    }
    
    /**
     * 🎯 优化解析方法2: 智能文本解析
     */
    private fun tryParseTextResponse(
        response: String,
        subject: String,
        topic: String,
        strategy: AdaptiveStrategy
    ): AIGeneratedQuestion? {
        return try {
            val lines = response.lines().map { it.trim() }.filter { it.isNotEmpty() }
            
            // 寻找题目内容
            val questionLine = lines.find { line ->
                line.contains("题目") || line.contains("问题") || 
                line.contains("已知") || line.contains("计算") ||
                line.contains("求") || line.contains("判断")
            }
            
            // 寻找选项
            val optionLines = lines.filter { line ->
                line.matches(Regex("^[ABCD][.、)].*")) ||
                line.matches(Regex("^[①②③④].*")) ||
                line.contains("A.") || line.contains("B.") || line.contains("C.") || line.contains("D.")
            }.take(4)
            
            // 寻找答案
            val answerLine = lines.find { line ->
                line.contains("答案") || line.contains("正确答案") ||
                line.matches(Regex(".*答案[是为：:].+"))
            }
            
            // 寻找解析
            val explanationLines = lines.filter { line ->
                line.contains("解析") || line.contains("解释") || line.contains("解答") ||
                line.contains("因为") || line.contains("所以")
            }
            
            if (questionLine != null && optionLines.size >= 4 && answerLine != null) {
                val question = cleanQuestionText(questionLine)
                val options = optionLines.map { cleanOptionText(it) }
                val correctAnswer = extractAnswerFromLine(answerLine)
                val explanation = explanationLines.joinToString("\n") { cleanExplanationText(it) }
                
                return createAIQuestion(
                    question = question,
                    options = options,
                    correctAnswer = correctAnswer,
                    explanation = explanation.ifEmpty { "这是一道${subject}题目，考查${topic}相关知识点。" },
                    knowledgePoints = listOf(topic),
                    estimatedTime = 120,
                    subject = subject,
                    topic = topic,
                    difficulty = strategy.targetDifficulty
                )
            }
            
            null
        } catch (e: Exception) {
            Log.w(TAG, "文本解析失败: ${e.message}")
            null
        }
    }
    
    /**
     * 🎯 优化解析方法3: 关键词提取解析
     */
    private fun tryParseByKeywords(
        response: String,
        subject: String,
        topic: String,
        strategy: AdaptiveStrategy
    ): AIGeneratedQuestion? {
        return try {
            // 如果AI响应包含数学相关内容，尝试构造题目
            if (subject == "数学" && (response.contains("函数") || response.contains("方程") || response.contains("计算"))) {
                return createMathQuestionFromResponse(response, topic, strategy.targetDifficulty)
            }
            
            // 如果AI响应包含物理相关内容
            if (subject == "物理" && (response.contains("力") || response.contains("速度") || response.contains("加速度"))) {
                return createPhysicsQuestionFromResponse(response, topic, strategy.targetDifficulty)
            }
            
            null
        } catch (e: Exception) {
            Log.w(TAG, "关键词解析失败: ${e.message}")
            null
        }
    }
    
    /**
     * 辅助方法：创建AI题目对象
     */
    private fun createAIQuestion(
        question: String,
        options: List<String>,
        correctAnswer: String,
        explanation: String,
        knowledgePoints: List<String>,
        estimatedTime: Int,
        subject: String,
        topic: String,
        difficulty: String
    ): AIGeneratedQuestion {
        return AIGeneratedQuestion(
            id = "ai_${System.currentTimeMillis()}",
            subject = subject,
            topic = topic,
            question = question,
            options = options,
            correctAnswer = correctAnswer,
            explanation = explanation,
            difficulty = difficulty,
            questionType = "选择题",
            knowledgePoints = knowledgePoints,
            estimatedTime = estimatedTime,
            adaptiveReason = "AI根据学生情况生成",
            creativityLevel = "创新",
            scenarioContext = "个性化学习场景"
        )
    }
    
    // 辅助解析方法 - 修复多行JSON值解析
    private fun extractJsonValue(json: String, key: String): String {
        return try {
            // 查找键的开始位置
            val keyPattern = "\"$key\"\\s*:"
            val keyRegex = Regex(keyPattern)
            val keyMatch = keyRegex.find(json) ?: return ""
            
            // 从键后面开始查找值
            var startIndex = keyMatch.range.last + 1
            
            // 跳过空白字符
            while (startIndex < json.length && json[startIndex].isWhitespace()) {
                startIndex++
            }
            
            // 检查是否是字符串值（以引号开始）
            if (startIndex >= json.length || json[startIndex] != '"') {
                return ""
            }
            
            startIndex++ // 跳过开始的引号
            
            // 查找结束的引号，考虑转义字符
            val result = StringBuilder()
            var i = startIndex
            while (i < json.length) {
                when (json[i]) {
                    '"' -> {
                        // 检查是否是转义的引号
                        if (i > 0 && json[i - 1] == '\\') {
                            result.append('"')
                        } else {
                            // 找到结束引号
                            return result.toString()
                        }
                    }
                    '\\' -> {
                        // 处理转义字符
                        if (i + 1 < json.length) {
                            when (json[i + 1]) {
                                'n' -> result.append('\n')
                                't' -> result.append('\t')
                                'r' -> result.append('\r')
                                '\\' -> result.append('\\')
                                '"' -> result.append('"')
                                else -> {
                                    result.append(json[i])
                                    result.append(json[i + 1])
                                }
                            }
                            i++ // 跳过下一个字符
                        } else {
                            result.append(json[i])
                        }
                    }
                    else -> result.append(json[i])
                }
                i++
            }
            
            result.toString()
        } catch (e: Exception) {
            Log.w(TAG, "解析JSON值失败: key=$key, error=${e.message}")
            ""
        }
    }
    
    private fun extractJsonArray(json: String, key: String): List<String> {
        return try {
            val pattern = "\"$key\"\\s*:\\s*\\[([^\\]]*)]"
            val regex = Regex(pattern, RegexOption.DOT_MATCHES_ALL)
            val match = regex.find(json)?.groupValues?.get(1) ?: return emptyList()
            
            // 解析数组元素
            val elements = mutableListOf<String>()
            var currentElement = StringBuilder()
            var inQuotes = false
            var escapeNext = false
            
            for (char in match) {
                when {
                    escapeNext -> {
                        currentElement.append(char)
                        escapeNext = false
                    }
                    char == '\\' -> {
                        escapeNext = true
                        currentElement.append(char)
                    }
                    char == '"' -> {
                        inQuotes = !inQuotes
                        if (!inQuotes && currentElement.isNotEmpty()) {
                            // 结束一个元素
                            elements.add(currentElement.toString().trim().removeSurrounding("\""))
                            currentElement.clear()
                        }
                    }
                    char == ',' && !inQuotes -> {
                        if (currentElement.isNotEmpty()) {
                            elements.add(currentElement.toString().trim().removeSurrounding("\""))
                            currentElement.clear()
                        }
                    }
                    else -> {
                        currentElement.append(char)
                    }
                }
            }
            
            // 添加最后一个元素
            if (currentElement.isNotEmpty()) {
                elements.add(currentElement.toString().trim().removeSurrounding("\""))
            }
            
            elements.filter { it.isNotEmpty() }
        } catch (e: Exception) {
            Log.w(TAG, "解析JSON数组失败: key=$key, error=${e.message}")
            emptyList()
        }
    }
    
    private fun cleanQuestionText(text: String): String {
        return text.replace(Regex("^[题目问题：:]*"), "").trim()
    }
    
    private fun cleanOptionText(text: String): String {
        return text.replace(Regex("^[ABCD①②③④.、)：:]*"), "").trim()
    }
    
    private fun cleanExplanationText(text: String): String {
        return text.replace(Regex("^[解析解释解答：:]*"), "").trim()
    }
    
    private fun extractAnswerFromLine(line: String): String {
        val patterns = listOf(
            Regex("答案[是为：:]*([ABCD])"),
            Regex("正确答案[是为：:]*([ABCD])"),
            Regex("([ABCD])[是为正确]")
        )
        
        for (pattern in patterns) {
            val match = pattern.find(line)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        
        return "A" // 默认答案
    }
    
    private fun createMathQuestionFromResponse(response: String, topic: String, difficulty: String): AIGeneratedQuestion? {
        // 基于AI响应中的数学内容构造题目
        return if (response.contains("函数")) {
            createAIQuestion(
                question = "根据AI分析，以下关于${topic}的描述正确的是：",
                options = listOf("选项A", "选项B", "选项C", "选项D"),
                correctAnswer = "选项A",
                explanation = "基于AI响应生成的解析：$response",
                knowledgePoints = listOf(topic),
                estimatedTime = 120,
                subject = "数学",
                topic = topic,
                difficulty = difficulty
            )
        } else null
    }
    
    private fun createPhysicsQuestionFromResponse(response: String, topic: String, difficulty: String): AIGeneratedQuestion? {
        // 基于AI响应中的物理内容构造题目
        return if (response.contains("力")) {
            createAIQuestion(
                question = "根据AI分析，以下关于${topic}的描述正确的是：",
                options = listOf("选项A", "选项B", "选项C", "选项D"),
                correctAnswer = "选项A",
                explanation = "基于AI响应生成的解析：$response",
                knowledgePoints = listOf(topic),
                estimatedTime = 120,
                subject = "物理",
                topic = topic,
                difficulty = difficulty
            )
        } else null
    }
    
    private fun parseQuestionBlock(
        block: String,
        subject: String,
        topic: String,
        strategy: AdaptiveStrategy,
        index: Int
    ): AIGeneratedQuestion? {
        return try {
            val lines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }
            
            // 更智能的字段提取
            val content = extractFieldByKeywords(lines, listOf("题目内容", "内容", "题目"))
            val optionsText = extractFieldByKeywords(lines, listOf("选项", "选择项"))
            val answer = extractFieldByKeywords(lines, listOf("正确答案", "答案"))
            val steps = extractFieldByKeywords(lines, listOf("解题步骤", "解答过程", "解法"))
            val knowledge = extractFieldByKeywords(lines, listOf("知识点", "涉及知识点"))
            val keyPoint = extractFieldByKeywords(lines, listOf("解题关键", "关键思路", "注意点"))
            val difficulty = extractFieldByKeywords(lines, listOf("难度", "难度等级"), strategy.targetDifficulty)
            
            // 更智能的选项解析
            val options = parseOptionsIntelligent(optionsText)
            
            // 构建完整的解释
            val explanation = buildCompleteExplanation(steps, knowledge, keyPoint)
            
            // 清理答案格式
            val cleanAnswer = cleanAnswerFormat(answer)
            
            // 估算时间
            val estimatedTime = estimateTimeByDifficultyAndSubject(difficulty, subject)
            
            if (content.isNotEmpty() && cleanAnswer.isNotEmpty()) {
                AIGeneratedQuestion(
                    id = "ai_q_${System.currentTimeMillis()}_$index",
                    subject = subject,
                    topic = topic,
                    question = content,
                    options = options,
                    correctAnswer = cleanAnswer,
                    explanation = explanation.ifEmpty { "AI生成的题目解析" },
                    difficulty = difficulty,
                    questionType = if (options.isNotEmpty()) "选择题" else "解答题",
                    knowledgePoints = knowledge.split("、", ",", "，").map { it.trim() }.filter { it.isNotEmpty() },
                    estimatedTime = estimatedTime,
                    adaptiveReason = "基于${strategy.strategy}策略生成",
                    creativityLevel = if (strategy.includeCreative) "创新" else "标准"
                )
            } else {
                Log.w(TAG, "题目内容或答案为空: content='$content', answer='$cleanAnswer'")
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "解析题目失败", e)
            null
        }
    }
    
    /**
     * 通过关键词智能提取字段值
     */
    private fun extractFieldByKeywords(lines: List<String>, keywords: List<String>, default: String = ""): String {
        for (keyword in keywords) {
            // 尝试匹配 "关键词：内容" 或 "关键词: 内容" 格式
            val line = lines.find { 
                it.startsWith("$keyword：") || it.startsWith("$keyword:") || 
                it.startsWith("$keyword ：") || it.startsWith("$keyword :")
            }
            if (line != null) {
                return line.substringAfter("：").substringAfter(":").trim()
            }
            
            // 尝试匹配包含关键词的行
            val containingLine = lines.find { it.contains(keyword) && (it.contains("：") || it.contains(":")) }
            if (containingLine != null) {
                return containingLine.substringAfter("：").substringAfter(":").trim()
            }
        }
        return default
    }
    
    /**
     * 智能解析选项，支持多种格式
     */
    private fun parseOptionsIntelligent(optionsText: String): List<String> {
        if (optionsText.isEmpty()) return emptyList()
        
        // 尝试匹配 A. B. C. D. 格式
        val standardPattern = Regex("[A-D][.．]\\s*([^A-D]*?)(?=[A-D][.．]|$)")
        val standardMatches = standardPattern.findAll(optionsText).map { 
            it.groupValues[1].trim() 
        }.filter { it.isNotEmpty() }.toList()
        
        if (standardMatches.size >= 2) {
            return standardMatches
        }
        
        // 尝试按行分割
        val lines = optionsText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.size >= 2) {
            return lines.take(4)  // 最多4个选项
        }
        
        // 尝试按特殊分隔符分割
        val separators = listOf("；", ";", "｜", "|", "、")
        for (separator in separators) {
            if (optionsText.contains(separator)) {
                val parts = optionsText.split(separator).map { it.trim() }.filter { it.isNotEmpty() }
                if (parts.size >= 2) {
                    return parts.take(4)
                }
            }
        }
        
        return emptyList()
    }
    
    /**
     * 构建完整的解释内容
     */
    private fun buildCompleteExplanation(steps: String, knowledge: String, keyPoint: String): String {
        return buildString {
            if (steps.isNotEmpty()) {
                append("📝 解题步骤：\n$steps\n\n")
            }
            if (knowledge.isNotEmpty()) {
                append("📚 涉及知识点：\n$knowledge\n\n")
            }
            if (keyPoint.isNotEmpty()) {
                append("🔑 解题关键：\n$keyPoint")
            }
        }.trim()
    }
    
    /**
     * 清理答案格式
     */
    private fun cleanAnswerFormat(answer: String): String {
        return answer
            .removePrefix("A.").removePrefix("B.").removePrefix("C.").removePrefix("D.")
            .removePrefix("A").removePrefix("B").removePrefix("C").removePrefix("D")
            .removePrefix("选项A").removePrefix("选项B").removePrefix("选项C").removePrefix("选项D")
            .trim()
    }
    
    /**
     * 根据难度和科目估算时间
     */
    private fun estimateTimeByDifficultyAndSubject(difficulty: String, subject: String): Int {
        val baseTime = when (subject.lowercase()) {
            "数学", "math" -> 90
            "物理", "physics" -> 120
            "语文", "chinese" -> 180
            "英语", "english" -> 150
            "化学", "chemistry" -> 100
            else -> 120
        }
        
        return when (difficulty) {
            "入门" -> (baseTime * 0.6).toInt()
            "基础" -> (baseTime * 0.8).toInt()
            "中级" -> baseTime
            "高级" -> (baseTime * 1.3).toInt()
            "挑战" -> (baseTime * 1.6).toInt()
            else -> baseTime
        }
    }
    
    private fun parseCreativeQuestions(response: String, subject: String, concept: String): List<AIGeneratedQuestion> {
        // 解析创意题目的逻辑
        return emptyList() // 简化实现
    }
    
    /**
     * 🎯 智能默认题目生成（当AI调用失败时使用）
     * 根据科目和年级生成具体的真实题目
     */
    private fun generateSmartDefaultQuestions(subject: String, topic: String, grade: String, count: Int): List<AIGeneratedQuestion> {
        return (1..count).map { index ->
            when (subject.lowercase()) {
                "数学", "math" -> generateMathDefaultQuestion(topic, grade, index)
                "物理", "physics" -> generatePhysicsDefaultQuestion(topic, grade, index)
                "语文", "chinese" -> generateChineseDefaultQuestion(topic, grade, index)
                else -> generateGeneralDefaultQuestion(subject, topic, grade, index)
            }
        }
    }
    
    /**
     * 生成数学默认题目 - 🎯 大幅扩充题目库
     */
    private fun generateMathDefaultQuestion(topic: String, grade: String, index: Int): AIGeneratedQuestion {
        return when (topic) {
            "函数", "函数与导数" -> {
                val functions = listOf(
                    // 基础函数题目
                    Triple("已知函数f(x) = x² - 2x + 1，求f(x)的最小值。", listOf("0", "1", "-1", "2"), "0"),
                    Triple("已知函数f(x) = 2x + 3，求f(2)的值。", listOf("7", "5", "6", "4"), "7"),
                    Triple("已知函数f(x) = x² + 4x + 3，求f(x) = 0的解。", listOf("x = -1或x = -3", "x = 1或x = 3", "x = -1或x = 3", "x = 1或x = -3"), "x = -1或x = -3"),
                    
                    // 导数题目
                    Triple("函数f(x) = x³ - 3x的导数f'(x)是：", listOf("3x² - 3", "3x² + 3", "x² - 3", "3x - 3"), "3x² - 3"),
                    Triple("已知f(x) = sin x，则f'(π/2)的值是：", listOf("0", "1", "-1", "π/2"), "0"),
                    Triple("函数f(x) = x²在x = 2处的切线斜率是：", listOf("4", "2", "8", "1"), "4"),
                    
                    // 复合函数
                    Triple("已知f(x) = x + 1，g(x) = 2x，则f(g(x))等于：", listOf("2x + 1", "2x - 1", "x + 2", "2x + 2"), "2x + 1"),
                    Triple("函数f(x) = (x - 1)²的对称轴是：", listOf("x = 1", "x = -1", "x = 0", "x = 2"), "x = 1"),
                    Triple("二次函数f(x) = ax² + bx + c的判别式Δ = b² - 4ac，当Δ > 0时，方程有几个实根？", listOf("2个", "1个", "0个", "无数个"), "2个"),
                    
                    // 函数性质
                    Triple("函数f(x) = |x|是：", listOf("偶函数", "奇函数", "既奇又偶", "非奇非偶"), "偶函数"),
                    Triple("函数f(x) = x³的单调性是：", listOf("在R上单调递增", "在R上单调递减", "先减后增", "先增后减"), "在R上单调递增"),
                    Triple("指数函数f(x) = aˣ (a > 1)的值域是：", listOf("(0, +∞)", "(-∞, +∞)", "[0, +∞)", "(1, +∞)"), "(0, +∞)"),
                    
                    // 三角函数
                    Triple("sin²x + cos²x的值等于：", listOf("1", "0", "2", "sin x"), "1"),
                    Triple("函数y = sin x的周期是：", listOf("2π", "π", "π/2", "4π"), "2π"),
                    Triple("tan(π/4)的值是：", listOf("1", "0", "√3", "1/√3"), "1"),
                    
                    // 对数函数
                    Triple("log₂ 8的值是：", listOf("3", "4", "2", "8"), "3"),
                    Triple("ln e的值是：", listOf("1", "0", "e", "2"), "1"),
                    Triple("如果log_a x = 2，则x等于：", listOf("a²", "2a", "a + 2", "2ᵃ"), "a²"),
                    
                    // 实际应用
                    Triple("某商品原价100元，先涨价20%，再降价20%，现价是多少元？", listOf("96", "100", "80", "120"), "96"),
                    Triple("一个球从高度h米处自由落下，t秒后的高度为h - 5t²米，从64米高处落下，需要多少秒落地？", listOf("√12.8秒", "8秒", "4秒", "√6.4秒"), "√12.8秒"),
                    Triple("某工厂生产的产品数量y与时间x(天)的关系为y = 100 + 50x，第10天的产品数量是：", listOf("600", "500", "550", "650"), "600")
                )
                val (question, options, answer) = functions[index % functions.size]
                
                AIGeneratedQuestion(
                    id = "math_default_${System.currentTimeMillis()}_$index",
                    subject = "数学",
                    topic = topic,
                    question = question,
                    options = options,
                    correctAnswer = answer,
                    explanation = "这是一道关于$topic 的典型题目，考查基本的函数概念和计算能力。",
                    difficulty = if (grade.contains("高中") || grade.contains("大学")) "中级" else "基础",
                    questionType = "选择题",
                    knowledgePoints = listOf("函数基本概念", "函数计算", "方程求解"),
                    estimatedTime = 90,
                    adaptiveReason = "根据$topic 主题的经典题型生成",
                    creativityLevel = "标准"
                )
            }
            "极限", "极限理论" -> {
                val limits = listOf(
                    Triple("计算极限 lim(x→0) (sin x / x) 的值。", listOf("1", "0", "∞", "不存在"), "1"),
                    Triple("计算极限 lim(x→1) (x² - 1)/(x - 1) 的值。", listOf("2", "1", "0", "不存在"), "2"),
                    Triple("计算极限 lim(x→∞) (1/x) 的值。", listOf("0", "1", "∞", "不存在"), "0"),
                    Triple("计算极限 lim(x→0) (1 - cos x)/x² 的值。", listOf("1/2", "1", "0", "不存在"), "1/2"),
                    Triple("计算极限 lim(x→∞) (1 + 1/x)^x 的值。", listOf("e", "1", "∞", "0"), "e"),
                    Triple("计算极限 lim(x→0⁺) x ln x 的值。", listOf("0", "1", "-∞", "不存在"), "0"),
                    Triple("已知数列 aₙ = n/(n+1)，则 lim(n→∞) aₙ 的值是：", listOf("1", "0", "∞", "不存在"), "1"),
                    Triple("计算极限 lim(x→2) (x³ - 8)/(x - 2) 的值。", listOf("12", "8", "4", "0"), "12"),
                    Triple("函数f(x) = 1/x在x = 0处：", listOf("极限不存在", "极限为0", "极限为1", "极限为∞"), "极限不存在"),
                    Triple("洛必达法则适用于求解哪种类型的极限？", listOf("0/0型或∞/∞型", "0·∞型", "∞-∞型", "所有极限"), "0/0型或∞/∞型")
                )
                val (question, options, answer) = limits[index % limits.size]
                
                AIGeneratedQuestion(
                    id = "math_limit_${System.currentTimeMillis()}_$index",
                    subject = "数学",
                    topic = topic,
                    question = question,
                    options = options,
                    correctAnswer = answer,
                    explanation = "这是极限的基本计算题，需要掌握极限的基本性质和计算法则。",
                    difficulty = "中级",
                    questionType = "选择题",
                    knowledgePoints = listOf("极限概念", "极限计算", "特殊极限"),
                    estimatedTime = 120,
                    adaptiveReason = "极限理论的典型计算题",
                    creativityLevel = "标准"
                )
            }
            "立体几何" -> {
                AIGeneratedQuestion(
                    id = "math_geometry_${System.currentTimeMillis()}_$index",
                    subject = "数学",
                    topic = topic,
                    question = "一个正方体的棱长为3cm，求它的体积。",
                    options = listOf("27 cm³", "18 cm³", "9 cm³", "36 cm³"),
                    correctAnswer = "27 cm³",
                    explanation = "正方体体积 = 棱长³ = 3³ = 27 cm³",
                    difficulty = "基础",
                    questionType = "选择题",
                    knowledgePoints = listOf("正方体", "体积计算"),
                    estimatedTime = 60,
                    adaptiveReason = "立体几何基础计算题",
                    creativityLevel = "标准"
                )
            }
            else -> generateGeneralMathQuestion(topic, grade, index)
        }
    }
    
    /**
     * 生成物理默认题目
     */
    private fun generatePhysicsDefaultQuestion(topic: String, grade: String, index: Int): AIGeneratedQuestion {
        return when (topic) {
            "力学", "牛顿定律" -> {
                AIGeneratedQuestion(
                    id = "physics_mechanics_${System.currentTimeMillis()}_$index",
                    subject = "物理",
                    topic = topic,
                    question = "一个质量为2kg的物体，受到10N的水平拉力，如果摩擦力为4N，求物体的加速度。",
                    options = listOf("3 m/s²", "5 m/s²", "2 m/s²", "7 m/s²"),
                    correctAnswer = "3 m/s²",
                    explanation = "根据牛顿第二定律：F合 = ma，合外力 = 10N - 4N = 6N，加速度 a = F合/m = 6N/2kg = 3 m/s²",
                    difficulty = if (grade.contains("高中") || grade.contains("大学")) "中级" else "基础",
                    questionType = "选择题",
                    knowledgePoints = listOf("牛顿第二定律", "力的合成", "加速度计算"),
                    estimatedTime = 120,
                    adaptiveReason = "力学基础应用题",
                    creativityLevel = "标准"
                )
            }
            "电磁学" -> {
                AIGeneratedQuestion(
                    id = "physics_electric_${System.currentTimeMillis()}_$index",
                    subject = "物理",
                    topic = topic,
                    question = "在电路中，电阻R=5Ω，电流I=2A，求电阻两端的电压。",
                    options = listOf("10V", "2.5V", "7V", "3V"),
                    correctAnswer = "10V",
                    explanation = "根据欧姆定律：U = IR = 2A × 5Ω = 10V",
                    difficulty = "基础",
                    questionType = "选择题",
                    knowledgePoints = listOf("欧姆定律", "电压计算", "电路基础"),
                    estimatedTime = 90,
                    adaptiveReason = "电磁学基础计算题",
                    creativityLevel = "标准"
                )
            }
            else -> generateGeneralPhysicsQuestion(topic, grade, index)
        }
    }
    
    /**
     * 生成语文默认题目
     */
    private fun generateChineseDefaultQuestion(topic: String, grade: String, index: Int): AIGeneratedQuestion {
        return when (topic) {
            "古诗词鉴赏" -> {
                AIGeneratedQuestion(
                    id = "chinese_poetry_${System.currentTimeMillis()}_$index",
                    subject = "语文",
                    topic = topic,
                    question = "\"春眠不觉晓，处处闻啼鸟。夜来风雨声，花落知多少。\"这首诗表达了诗人怎样的感情？",
                    options = listOf("对春天的喜爱和对时光的感慨", "对战争的厌恶", "对家乡的思念", "对友人的怀念"),
                    correctAnswer = "对春天的喜爱和对时光的感慨",
                    explanation = "这首《春晓》通过描写春天早晨的美景，表达了诗人对春天的喜爱，同时又有淡淡的惜春之情。",
                    difficulty = if (grade.contains("高中") || grade.contains("大学")) "中级" else "基础",
                    questionType = "选择题",
                    knowledgePoints = listOf("古诗鉴赏", "情感分析", "意境理解"),
                    estimatedTime = 150,
                    adaptiveReason = "古诗词情感理解典型题",
                    creativityLevel = "标准"
                )
            }
            "现代文阅读" -> {
            AIGeneratedQuestion(
                    id = "chinese_reading_${System.currentTimeMillis()}_$index",
                    subject = "语文",
                    topic = topic,
                    question = "阅读下面句子：\"知识就是力量\"，这句话强调了什么？",
                    options = listOf("知识的重要性", "力量的来源", "学习的方法", "教育的意义"),
                    correctAnswer = "知识的重要性",
                    explanation = "这句话强调了知识对于个人和社会发展的重要意义，知识能够赋予人力量。",
                    difficulty = "基础",
                    questionType = "选择题",
                    knowledgePoints = listOf("现代文理解", "语言表达", "思想内容"),
                    estimatedTime = 120,
                    adaptiveReason = "现代文理解基础题",
                    creativityLevel = "标准"
                )
            }
            else -> generateGeneralChineseQuestion(topic, grade, index)
        }
    }
    
    /**
     * 生成通用数学题目
     */
    private fun generateGeneralMathQuestion(topic: String, grade: String, index: Int): AIGeneratedQuestion {
        return AIGeneratedQuestion(
            id = "math_general_${System.currentTimeMillis()}_$index",
            subject = "数学",
            topic = topic,
            question = "计算：2 + 3 × 4 - 1 = ?",
            options = listOf("13", "19", "20", "11"),
            correctAnswer = "13",
            explanation = "按照运算法则，先算乘法：2 + 12 - 1 = 13",
            difficulty = "基础",
            questionType = "选择题",
            knowledgePoints = listOf("四则运算", "运算法则"),
            estimatedTime = 60,
            adaptiveReason = "数学基础计算题",
            creativityLevel = "标准"
        )
    }
    
    /**
     * 生成通用物理题目
     */
    private fun generateGeneralPhysicsQuestion(topic: String, grade: String, index: Int): AIGeneratedQuestion {
        return AIGeneratedQuestion(
            id = "physics_general_${System.currentTimeMillis()}_$index",
            subject = "物理",
            topic = topic,
            question = "声音在空气中的传播速度大约是多少？",
            options = listOf("340 m/s", "300 m/s", "400 m/s", "500 m/s"),
            correctAnswer = "340 m/s",
            explanation = "声音在15°C空气中的传播速度约为340 m/s",
            difficulty = "基础",
            questionType = "选择题",
            knowledgePoints = listOf("声音传播", "物理常识"),
            estimatedTime = 60,
            adaptiveReason = "物理基础知识题",
            creativityLevel = "标准"
        )
    }
    
    /**
     * 生成通用语文题目
     */
    private fun generateGeneralChineseQuestion(topic: String, grade: String, index: Int): AIGeneratedQuestion {
        return AIGeneratedQuestion(
            id = "chinese_general_${System.currentTimeMillis()}_$index",
            subject = "语文",
            topic = topic,
            question = "下列词语中，书写完全正确的是：",
            options = listOf("知识渊博", "知识渊薄", "知识深博", "知识深薄"),
            correctAnswer = "知识渊博",
            explanation = "\"渊博\"是正确写法，表示知识深广。",
            difficulty = "基础",
            questionType = "选择题",
            knowledgePoints = listOf("汉字书写", "词语辨析"),
            estimatedTime = 90,
            adaptiveReason = "语文基础知识题",
            creativityLevel = "标准"
        )
    }
    
    /**
     * 生成通用题目 - 改为真实具体的题目
     */
    private fun generateGeneralDefaultQuestion(subject: String, topic: String, grade: String, index: Int): AIGeneratedQuestion {
        return when (subject.lowercase()) {
            "计算机", "computer" -> generateComputerQuestion(topic, grade, index)
            "生物", "biology" -> generateBiologyQuestion(topic, grade, index)
            "地理", "geography" -> generateGeographyQuestion(topic, grade, index)
            "历史", "history" -> generateHistoryQuestion(topic, grade, index)
            else -> generateFallbackRealQuestion(subject, topic, grade, index)
        }
    }
    
    /**
     * 🎯 计算机科目具体题目
     */
    private fun generateComputerQuestion(topic: String, grade: String, index: Int): AIGeneratedQuestion {
        return when (topic) {
            "数据结构" -> {
                val questions = listOf(
                    Triple("在一个空栈中依次压入元素1、2、3、4，然后依次弹出，弹出顺序是：", 
                          listOf("1、2、3、4", "4、3、2、1", "2、1、4、3", "1、3、2、4"), "4、3、2、1"),
                    Triple("二叉树的前序遍历顺序是：", 
                          listOf("根→左→右", "左→根→右", "左→右→根", "右→根→左"), "根→左→右"),
                    Triple("在单链表中删除一个结点的时间复杂度是：", 
                          listOf("O(1)", "O(n)", "O(log n)", "O(n²)"), "O(1)")
                )
                val (question, options, answer) = questions[index % questions.size]
                
                AIGeneratedQuestion(
                    id = "computer_ds_${System.currentTimeMillis()}_$index",
                    subject = "计算机",
                    topic = topic,
                    question = question,
                    options = options,
                    correctAnswer = answer,
                    explanation = "这是数据结构的基础概念，需要理解栈的后进先出特性、二叉树遍历方法和链表操作。",
                    difficulty = if (grade.contains("大学")) "高级" else "中级",
                    questionType = "选择题",
                    knowledgePoints = listOf("数据结构", "算法基础"),
                    estimatedTime = 120,
                    adaptiveReason = "数据结构核心概念题",
                    creativityLevel = "标准"
                )
            }
            "算法" -> {
                val questions = listOf(
                    Triple("快速排序算法的平均时间复杂度是：", 
                          listOf("O(n)", "O(n log n)", "O(n²)", "O(log n)"), "O(n log n)"),
                    Triple("二分查找要求数组必须是：", 
                          listOf("有序的", "无序的", "倒序的", "任意顺序"), "有序的"),
                    Triple("深度优先搜索（DFS）通常使用哪种数据结构实现：", 
                          listOf("栈", "队列", "堆", "数组"), "栈")
                )
                val (question, options, answer) = questions[index % questions.size]
                
                AIGeneratedQuestion(
                    id = "computer_algo_${System.currentTimeMillis()}_$index",
                    subject = "计算机",
                    topic = topic,
                    question = question,
                    options = options,
                    correctAnswer = answer,
                    explanation = "算法分析是计算机科学的核心，需要掌握时间复杂度分析和经典算法的实现原理。",
                    difficulty = "高级",
                    questionType = "选择题",
                    knowledgePoints = listOf("算法分析", "时间复杂度", "搜索算法"),
                    estimatedTime = 180,
                    adaptiveReason = "算法核心理论题",
                    creativityLevel = "标准"
                )
            }
            "数据库" -> {
                val questions = listOf(
                    Triple("在关系数据库中，确保数据完整性的约束包括：", 
                          listOf("主键约束和外键约束", "只有主键约束", "只有外键约束", "不需要约束"), "主键约束和外键约束"),
                    Triple("SQL中用于查询数据的关键字是：", 
                          listOf("SELECT", "INSERT", "UPDATE", "DELETE"), "SELECT"),
                    Triple("数据库的ACID特性中，A代表：", 
                          listOf("原子性(Atomicity)", "一致性(Consistency)", "隔离性(Isolation)", "持久性(Durability)"), "原子性(Atomicity)")
                )
                val (question, options, answer) = questions[index % questions.size]
                
                AIGeneratedQuestion(
                    id = "computer_db_${System.currentTimeMillis()}_$index",
                    subject = "计算机",
                    topic = topic,
                    question = question,
                    options = options,
                    correctAnswer = answer,
                    explanation = "数据库是现代信息系统的核心，需要理解关系模型、SQL语言和事务处理的基本原理。",
                    difficulty = "中级",
                    questionType = "选择题",
                    knowledgePoints = listOf("关系数据库", "SQL", "数据完整性"),
                    estimatedTime = 120,
                    adaptiveReason = "数据库基础理论题",
                    creativityLevel = "标准"
                )
            }
            "操作系统" -> {
                val questions = listOf(
                    Triple("操作系统中，进程和线程的主要区别是：", 
                          listOf("进程拥有独立的内存空间，线程共享内存空间", "没有区别", "线程比进程大", "进程是线程的一部分"), "进程拥有独立的内存空间，线程共享内存空间"),
                    Triple("死锁产生的必要条件包括：", 
                          listOf("互斥、请求与保持、不可剥夺、循环等待", "只需要互斥条件", "只需要循环等待", "任意两个条件"), "互斥、请求与保持、不可剥夺、循环等待"),
                    Triple("虚拟内存技术的主要优点是：", 
                          listOf("扩大了内存容量，提高了内存利用率", "提高了CPU速度", "减少了硬盘容量", "简化了编程"), "扩大了内存容量，提高了内存利用率")
                )
                val (question, options, answer) = questions[index % questions.size]
                
                AIGeneratedQuestion(
                    id = "computer_os_${System.currentTimeMillis()}_$index",
                    subject = "计算机",
                    topic = topic,
                    question = question,
                    options = options,
                    correctAnswer = answer,
                    explanation = "操作系统是计算机系统的核心软件，管理硬件资源并为应用程序提供服务。",
                    difficulty = "高级",
                    questionType = "选择题",
                    knowledgePoints = listOf("操作系统原理", "进程管理", "内存管理"),
                    estimatedTime = 150,
                    adaptiveReason = "操作系统核心概念题",
                    creativityLevel = "标准"
                )
            }
            else -> generateFallbackRealQuestion("计算机", topic, grade, index)
        }
    }
    
    /**
     * 🎯 生物科目具体题目
     */
    private fun generateBiologyQuestion(topic: String, grade: String, index: Int): AIGeneratedQuestion {
        val questions = listOf(
            Triple("DNA分子的双螺旋结构是由哪两位科学家发现的？", 
                  listOf("沃森和克里克", "孟德尔和达尔文", "巴斯德和弗莱明", "哈维和盖伦"), "沃森和克里克"),
            Triple("人体细胞中，负责蛋白质合成的细胞器是：", 
                  listOf("核糖体", "线粒体", "内质网", "高尔基体"), "核糖体"),
            Triple("光合作用的反应式是：", 
                  listOf("6CO₂ + 6H₂O + 光能 → C₆H₁₂O₆ + 6O₂", "C₆H₁₂O₆ + 6O₂ → 6CO₂ + 6H₂O + ATP", "2H₂O → 2H₂ + O₂", "N₂ + 3H₂ → 2NH₃"), "6CO₂ + 6H₂O + 光能 → C₆H₁₂O₆ + 6O₂")
        )
        val (question, options, answer) = questions[index % questions.size]
        
        return AIGeneratedQuestion(
            id = "biology_${System.currentTimeMillis()}_$index",
            subject = "生物",
            topic = topic,
            question = question,
            options = options,
            correctAnswer = answer,
            explanation = "生物学研究生命现象和生命活动规律，这些是生物学的基础知识。",
            difficulty = if (grade.contains("高中") || grade.contains("大学")) "中级" else "基础",
            questionType = "选择题",
            knowledgePoints = listOf("细胞生物学", "分子生物学", "生物化学"),
            estimatedTime = 120,
            adaptiveReason = "生物学基础概念题",
            creativityLevel = "标准"
        )
    }
    
    /**
     * 🎯 地理科目具体题目
     */
    private fun generateGeographyQuestion(topic: String, grade: String, index: Int): AIGeneratedQuestion {
        val questions = listOf(
            Triple("地球上最长的山脉是：", 
                  listOf("安第斯山脉", "喜马拉雅山脉", "阿尔卑斯山脉", "落基山脉"), "安第斯山脉"),
            Triple("世界上面积最大的沙漠是：", 
                  listOf("撒哈拉沙漠", "戈壁沙漠", "阿拉伯沙漠", "塔克拉玛干沙漠"), "撒哈拉沙漠"),
            Triple("地球的自转周期约为：", 
                  listOf("24小时", "365天", "12小时", "30天"), "24小时")
        )
        val (question, options, answer) = questions[index % questions.size]
        
        return AIGeneratedQuestion(
            id = "geography_${System.currentTimeMillis()}_$index",
            subject = "地理",
            topic = topic,
            question = question,
            options = options,
            correctAnswer = answer,
            explanation = "地理学研究地球表面的自然现象和人文现象，这些是地理学的基础知识。",
            difficulty = "基础",
            questionType = "选择题",
            knowledgePoints = listOf("自然地理", "世界地理"),
            estimatedTime = 90,
            adaptiveReason = "地理基础知识题",
            creativityLevel = "标准"
        )
    }
    
    /**
     * 🎯 历史科目具体题目
     */
    private fun generateHistoryQuestion(topic: String, grade: String, index: Int): AIGeneratedQuestion {
        val questions = listOf(
            Triple("中国古代四大发明包括：", 
                  listOf("造纸术、印刷术、指南针、火药", "造纸术、丝绸、瓷器、茶叶", "书法、绘画、诗歌、音乐", "儒学、道学、佛学、法学"), "造纸术、印刷术、指南针、火药"),
            Triple("第一次世界大战的爆发时间是：", 
                  listOf("1914年", "1918年", "1939年", "1945年"), "1914年"),
            Triple("中国历史上第一个统一的中央集权国家是：", 
                  listOf("秦朝", "汉朝", "唐朝", "宋朝"), "秦朝")
        )
        val (question, options, answer) = questions[index % questions.size]
        
        return AIGeneratedQuestion(
            id = "history_${System.currentTimeMillis()}_$index",
            subject = "历史",
            topic = topic,
            question = question,
            options = options,
            correctAnswer = answer,
            explanation = "历史学研究人类社会发展的过程，这些是历史学的基础知识。",
            difficulty = "基础",
            questionType = "选择题",
            knowledgePoints = listOf("中国古代史", "世界历史"),
            estimatedTime = 120,
            adaptiveReason = "历史基础知识题",
            creativityLevel = "标准"
        )
    }
    
    /**
     * 🎯 最后备选方案 - 确保都是真实具体的题目
     */
    private fun generateFallbackRealQuestion(subject: String, topic: String, grade: String, index: Int): AIGeneratedQuestion {
        val realQuestions = listOf(
                // 移除容易造成跨科目误判的固定数学题，避免在英语/其他科目下作为兜底题出现
            Triple("下列哪个是质数？", 
                  listOf("17", "15", "21", "25"), "17"),
            Triple("一个圆的半径是5cm，它的面积是多少平方厘米？（π取3.14）", 
                  listOf("78.5", "31.4", "15.7", "157"), "78.5")
        )
        val (question, options, answer) = realQuestions[index % realQuestions.size]
        
        return AIGeneratedQuestion(
            id = "fallback_real_${System.currentTimeMillis()}_$index",
            subject = subject,
            topic = topic,
            question = question,
            options = options,
            correctAnswer = answer,
            explanation = "这是一道基础的计算题，考查基本的数学运算能力。",
            difficulty = "基础",
            questionType = "选择题",
            knowledgePoints = listOf("基础运算"),
            estimatedTime = 90,
            adaptiveReason = "基础知识巩固",
            creativityLevel = "标准"
        )
    }
    
    /**
     * 保留原有的fallback方法作为最后备选
     */
    private fun generateFallbackQuestions(subject: String, topic: String, count: Int): List<AIGeneratedQuestion> {
        return generateSmartDefaultQuestions(subject, topic, "基础", count)
    }
    
    // 辅助方法
    private fun extractFieldValue(lines: List<String>, fieldName: String, default: String = ""): String {
        return lines.find { it.startsWith("$fieldName：") || it.startsWith("$fieldName:") }
            ?.substringAfter("：")
            ?.substringAfter(":")
            ?.trim() ?: default
    }
    
    private fun parseOptions(optionsLine: String): List<String> {
        return Regex("[A-D]\\.[^A-D]*").findAll(optionsLine)
            .map { it.value.substringAfter(".").trim() }
            .toList()
    }
    
    private fun identifyCommonMistakes(records: List<LearningRecord>): List<String> {
        // 简化实现：基于得分识别问题
        return if (records.any { it.score < 70 }) {
            listOf("基础概念理解", "计算准确性", "应用能力")
        } else {
            emptyList()
        }
    }
    
    private fun identifyStrongAreas(records: List<LearningRecord>): List<String> {
        // 简化实现：基于高分识别优势
        return if (records.any { it.score > 85 }) {
            listOf("理论理解", "逻辑思维")
        } else {
            emptyList()
        }
    }
    
    private fun mapStrategyToDifficulty(strategy: String): String {
        return when (strategy) {
            "挑战提升" -> "高级"
            "巩固拓展" -> "中级"
            "强化练习" -> "基础"
            "基础巩固" -> "基础"
            "基础入门" -> "入门"
            "减压练习" -> "入门"
            "轻松巩固" -> "基础"
            "趣味激发" -> "基础"
            "挑战进阶" -> "高级"
            else -> "中级"
        }
    }
    
    private fun increaseDifficulty(currentDifficulty: String): String {
        return when (currentDifficulty) {
            "入门" -> "基础"
            "基础" -> "中级"
            "中级" -> "高级"
            "高级" -> "挑战"
            else -> currentDifficulty
        }
    }
    
    private fun decreaseDifficulty(currentDifficulty: String): String {
        return when (currentDifficulty) {
            "挑战" -> "高级"
            "高级" -> "中级"
            "中级" -> "基础"
            "基础" -> "入门"
            else -> currentDifficulty
        }
    }
    
    private fun parseDifficultyAdjustment(response: String): String {
        val difficulties = listOf("挑战", "高级", "中级", "基础", "入门")
        return difficulties.find { response.contains(it) } ?: "中级"
    }
    
    // 数据类定义
    data class KnowledgeAnalysis(
        val masteryLevel: String,
        val averageScore: Float,
        val improvementTrend: Float,
        val commonMistakes: List<String>,
        val strongAreas: List<String>,
        val practiceCount: Int
    )
    
    data class AdaptiveStrategy(
        val strategy: String,
        val targetDifficulty: String,
        val emphasizeWeakPoints: Boolean,
        val includeCreative: Boolean,
        val timeConstraint: Int?
    )
}

