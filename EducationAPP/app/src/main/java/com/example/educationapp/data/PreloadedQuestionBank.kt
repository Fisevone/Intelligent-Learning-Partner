package com.example.educationapp.data

import com.example.educationapp.ai.AIQuestionGenerator

/**
 * 预备题库：为每个科目准备题目，在AI出题缓慢时直接取用
 */
object PreloadedQuestionBank {

    fun getQuestions(subject: String, topics: List<String>, grade: String): List<AIQuestionGenerator.AIGeneratedQuestion> {
        return when (subject.lowercase()) {
            "英语", "english" -> generateEnglishQuestions(topics)
            "语文", "chinese" -> generateChineseQuestions(topics)
            "数学", "math" -> generateMathQuestions(topics)
            "物理", "physics" -> generatePhysicsQuestions(topics)
            "化学", "chemistry" -> generateChemistryQuestions(topics)
            else -> generateGeneralQuestions(subject, topics)
        }
    }

    private fun generateEnglishQuestions(topics: List<String>): List<AIQuestionGenerator.AIGeneratedQuestion> {
        data class Verb(val base: String, val third: String, val past: String, val ing: String)
        val subjects = listOf("I", "You", "He", "She", "They", "We", "Tom", "Lucy", "My classmates")
        val verbs = listOf(
            Verb("go", "goes", "went", "going"),
            Verb("study", "studies", "studied", "studying"),
            Verb("play", "plays", "played", "playing"),
            Verb("watch", "watches", "watched", "watching"),
            Verb("read", "reads", "read", "reading"),
            Verb("write", "writes", "wrote", "writing"),
            Verb("listen", "listens", "listened", "listening"),
            Verb("cook", "cooks", "cooked", "cooking"),
            Verb("exercise", "exercises", "exercised", "exercising"),
            Verb("swim", "swims", "swam", "swimming")
        )
        val tenses = listOf("present", "past", "continuous")
        val topic = topics.randomOrNull() ?: "语法"
        val questions = mutableListOf<AIQuestionGenerator.AIGeneratedQuestion>()
        var id = 0

        outer@for (sub in subjects) {
            val third = sub.lowercase() in listOf("he", "she", "tom", "lucy")
            for (verb in verbs) {
                for (tense in tenses) {
                    val sentence = when (tense) {
                        "present" -> "$sub ____ ${verb.base} every day."
                        "past" -> "$sub ____ ${verb.base} yesterday."
                        else -> "$sub is ____ ${verb.base} now."
                    }
                    val (answer, options, knowledge) = when (tense) {
                        "present" -> {
                            val correct = if (third) verb.third else verb.base
                            Triple(correct, listOf(correct, verb.base, verb.ing, verb.past), listOf("一般现在时", "主谓一致"))
                        }
                        "past" -> Triple(verb.past, listOf(verb.past, verb.base, if (third) verb.third else verb.base + "s", verb.ing), listOf("一般过去时"))
                        else -> Triple(verb.ing, listOf(verb.ing, verb.base, if (third) verb.third else verb.base + "s", verb.past), listOf("现在进行时"))
                    }
                    questions.add(
                        AIQuestionGenerator.AIGeneratedQuestion(
                            id = "pre_eng_${id++}",
                            subject = "英语",
                            topic = topic,
                            question = "Choose the correct word to complete the sentence: $sentence",
                            options = options.shuffled(),
                            correctAnswer = answer,
                            explanation = "考查时态和主谓一致",
                            difficulty = "基础",
                            questionType = "选择题",
                            knowledgePoints = knowledge,
                            estimatedTime = 90,
                            adaptiveReason = "预备题库",
                            creativityLevel = "标准",
                            scenarioContext = "预备题"
                        )
                    )
                    if (questions.size >= 50) break@outer
                }
            }
        }
        return questions
    }

    private fun generateChineseQuestions(topics: List<String>): List<AIQuestionGenerator.AIGeneratedQuestion> {
        data class Polyphone(val correct: String, val options: List<String>)
        val dataset = listOf(
            Polyphone("载(zài)重", listOf("载(zài)重", "载(zǎi)重", "载(zāi)重", "载(zāi)体")),
            Polyphone("乐(yuè)曲", listOf("乐(yuè)曲", "乐(lè)曲", "乐(yào)曲", "乐(lè)趣")),
            Polyphone("行(háng)业", listOf("行(háng)业", "行(xíng)业", "行(hàng)业", "行(xiàng)业")),
            Polyphone("模(mú)样", listOf("模(mú)样", "模(mó)样", "模(mò)样", "模(méi)样")),
            Polyphone("称(chēng)心", listOf("称(chēng)心", "称(chèn)心", "称(chéng)心", "称(chén)心"))
        )
        val topic = topics.randomOrNull() ?: "多音字"
        val questions = mutableListOf<AIQuestionGenerator.AIGeneratedQuestion>()
        var idx = 0
        while (questions.size < 50) {
            val poly = dataset[idx % dataset.size]
            questions.add(
                AIQuestionGenerator.AIGeneratedQuestion(
                    id = "pre_chi_${questions.size}",
                    subject = "语文",
                    topic = topic,
                    question = "下列词语中，读音完全正确的是：",
                    options = poly.options.shuffled(),
                    correctAnswer = poly.correct,
                    explanation = "注意词语语境，辨析多音字",
                    difficulty = "基础",
                    questionType = "选择题",
                    knowledgePoints = listOf("多音字", "语言运用"),
                    estimatedTime = 120,
                    adaptiveReason = "预备题库",
                    creativityLevel = "标准",
                    scenarioContext = "预备题"
                )
            )
            idx++
        }
        return questions
    }

    private fun generateMathQuestions(topics: List<String>): List<AIQuestionGenerator.AIGeneratedQuestion> {
        val topic = topics.randomOrNull() ?: "基础运算"
        val questions = mutableListOf<AIQuestionGenerator.AIGeneratedQuestion>()
        var counter = 0
        for (a in 2..11) {
            for (b in 2..11) {
                val correct = a * b
                val options = listOf(correct, correct + 2, correct - 3, correct + 5).map { it.toString() }
                questions.add(
                    AIQuestionGenerator.AIGeneratedQuestion(
                        id = "pre_math_${counter}",
                        subject = "数学",
                        topic = topic,
                        question = "计算：$a × $b = ?",
                        options = options.shuffled(),
                        correctAnswer = correct.toString(),
                        explanation = "基础乘法计算",
                        difficulty = "基础",
                        questionType = "选择题",
                        knowledgePoints = listOf("整数运算"),
                        estimatedTime = 60,
                        adaptiveReason = "预备题库",
                        creativityLevel = "标准",
                        scenarioContext = "预备题"
                    )
                )
                counter++
                if (questions.size >= 50) return questions
            }
        }
        return questions
    }

    private fun generatePhysicsQuestions(topics: List<String>): List<AIQuestionGenerator.AIGeneratedQuestion> {
        val topic = topics.randomOrNull() ?: "力学"
        val questions = mutableListOf<AIQuestionGenerator.AIGeneratedQuestion>()
        val accelerations = listOf(2, 3, 4, 5, 6)
        val times = listOf(2, 3, 4, 5)
        var counter = 0
        outer@for (a in accelerations) {
            for (t in times) {
                val velocity = a * t
                val options = listOf("$velocity m/s", "${velocity + 2} m/s", "${velocity - 2} m/s", "${velocity + 4} m/s")
                questions.add(
                    AIQuestionGenerator.AIGeneratedQuestion(
                        id = "pre_phys_${counter}",
                        subject = "物理",
                        topic = topic,
                        question = "一物体从静止开始做匀加速直线运动，加速度为${a} m/s²，经过${t}秒后的速度是多少？",
                        options = options.shuffled(),
                        correctAnswer = "$velocity m/s",
                        explanation = "匀加速直线运动公式 v = at",
                        difficulty = "基础",
                        questionType = "选择题",
                        knowledgePoints = listOf("匀加速直线运动"),
                        estimatedTime = 120,
                        adaptiveReason = "预备题库",
                        creativityLevel = "标准",
                        scenarioContext = "预备题"
                    )
                )
                counter++
                if (questions.size >= 50) break@outer
            }
        }
        return questions
    }

    private fun generateChemistryQuestions(topics: List<String>): List<AIQuestionGenerator.AIGeneratedQuestion> {
        val topic = topics.randomOrNull() ?: "化学方程式"
        val reactions = listOf(
            "2H₂ + O₂ → 2H₂O",
            "N₂ + 3H₂ → 2NH₃",
            "2Na + Cl₂ → 2NaCl",
            "CaCO₃ → CaO + CO₂",
            "Fe + S → FeS"
        )
        val questions = mutableListOf<AIQuestionGenerator.AIGeneratedQuestion>()
        var counter = 0
        while (questions.size < 50) {
            val equation = reactions[counter % reactions.size]
            val options = listOf(
                equation,
                "${equation}↑",
                equation.replace("→", "+"),
                equation.replace("2", "3")
            )
            questions.add(
                AIQuestionGenerator.AIGeneratedQuestion(
                    id = "pre_chem_${counter}",
                    subject = "化学",
                    topic = topic,
                    question = "下列化学方程式书写正确的是：",
                    options = options.shuffled(),
                    correctAnswer = equation,
                    explanation = "注意化学方程式配平",
                    difficulty = "基础",
                    questionType = "选择题",
                    knowledgePoints = listOf("化学方程式"),
                    estimatedTime = 120,
                    adaptiveReason = "预备题库",
                    creativityLevel = "标准",
                    scenarioContext = "预备题"
                )
            )
            counter++
        }
        return questions
    }

    private fun generateGeneralQuestions(subject: String, topics: List<String>): List<AIQuestionGenerator.AIGeneratedQuestion> {
        val topic = topics.randomOrNull() ?: "通识"
        val statements = listOf(
            "The sun rises in the east.",
            "Water boils at 100°C under standard pressure.",
            "Earth orbits the sun.",
            "Plants need sunlight to grow.",
            "Exercise is good for health."
        )
        val questions = mutableListOf<AIQuestionGenerator.AIGeneratedQuestion>()
        var counter = 0
        while (questions.size < 50) {
            val statement = statements[counter % statements.size]
            questions.add(
                AIQuestionGenerator.AIGeneratedQuestion(
                    id = "pre_general_${counter}",
                    subject = subject,
                    topic = topic,
                    question = "Judge whether the following statement is correct: $statement",
                    options = listOf("Correct", "Incorrect", "Not sure", "Irrelevant"),
                    correctAnswer = "Correct",
                    explanation = "常识判断",
                    difficulty = "基础",
                    questionType = "选择题",
                    knowledgePoints = listOf("常识"),
                    estimatedTime = 90,
                    adaptiveReason = "预备题库",
                    creativityLevel = "标准",
                    scenarioContext = "预备题"
                )
            )
            counter++
        }
        return questions
    }
}

