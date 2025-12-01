package com.example.educationapp.ai

import com.example.educationapp.ai.AIQuestionGenerator.AIGeneratedQuestion

/**
 * 预备题库：为每个科目准备约50道高质量题目，供AI出题延迟或失败时使用
 */
object PreloadedQuestionBank {

    fun getQuestions(subject: String, topics: List<String>, grade: String): List<AIGeneratedQuestion> {
        return when (subject.lowercase()) {
            "英语", "english" -> generateEnglishQuestions(topics, grade)
            "语文", "chinese" -> generateChineseQuestions(topics)
            "数学", "math" -> generateMathQuestions(topics)
            "物理", "physics" -> generatePhysicsQuestions(topics)
            "化学", "chemistry" -> generateChemistryQuestions(topics)
            else -> generateGeneralKnowledgeQuestions(subject, topics)
        }
    }

    /**
     * 英语题库：50道语法/词汇题
     */
    private fun generateEnglishQuestions(topics: List<String>, grade: String): List<AIGeneratedQuestion> {
        data class VerbForm(val base: String, val third: String, val past: String, val ing: String)

        val subjects = listOf(
            "I", "You", "He", "She", "They", "We", "The students", "My brother", "Alice", "Tom and Jerry"
        )

        val verbForms = listOf(
            VerbForm("go", "goes", "went", "going"),
            VerbForm("eat", "eats", "ate", "eating"),
            VerbForm("watch", "watches", "watched", "watching"),
            VerbForm("study", "studies", "studied", "studying"),
            VerbForm("play", "plays", "played", "playing"),
            VerbForm("read", "reads", "read", "reading"),
            VerbForm("write", "writes", "wrote", "writing"),
            VerbForm("listen", "listens", "listened", "listening"),
            VerbForm("visit", "visits", "visited", "visiting"),
            VerbForm("exercise", "exercises", "exercised", "exercising")
        )

        val tenses = listOf("present", "past", "continuous")

        val questions = mutableListOf<AIGeneratedQuestion>()
        var idCounter = 0

        outer@for (subject in subjects) {
            val isThirdPerson = subject.lowercase() in listOf("he", "she", "it", "my brother", "alice")
            for (verb in verbForms) {
                for (tense in tenses) {
                    val sentence = when (tense) {
                        "present" -> "$subject ____ ${verb.base} every day."
                        "past" -> "$subject ____ ${verb.base} yesterday."
                        else -> "$subject is ____ ${verb.base} now."
                    }

                    val (correct, distractors, knowledge) = when (tense) {
                        "present" -> {
                            val correct = if (isThirdPerson) verb.third else verb.base
                            val options = listOf(
                                correct,
                                if (isThirdPerson) verb.base else verb.third,
                                verb.past,
                                verb.ing
                            )
                            Triple(correct, options, listOf("一般现在时", "主谓一致"))
                        }
                        "past" -> {
                            val options = listOf(
                                verb.past,
                                verb.base,
                                if (isThirdPerson) verb.third else verb.base + "s",
                                verb.ing
                            )
                            Triple(verb.past, options, listOf("一般过去时"))
                        }
                        else -> {
                            val options = listOf(
                                verb.ing,
                                verb.base,
                                if (isThirdPerson) verb.third else verb.base + "s",
                                verb.past
                            )
                            Triple(verb.ing, options, listOf("现在进行时"))
                        }
                    }

                    questions.add(
                        AIGeneratedQuestion(
                            id = "pre_english_${idCounter++}",
                            subject = "英语",
                            topic = topics.randomOrNull() ?: "语法",
                            question = "Choose the correct word to complete the sentence: $sentence",
                            options = distractors.shuffled(),
                            correctAnswer = correct,
                            explanation = "subject-verb agreement / 时态搭配",
                            difficulty = "基础",
                            questionType = "选择题",
                            knowledgePoints = knowledge,
                            estimatedTime = 90,
                            adaptiveReason = "预备题库 - 英语语法练习",
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

    /**
     * 语文题库：多音字、成语辨析等 50 题
     */
    private fun generateChineseQuestions(topics: List<String>): List<AIGeneratedQuestion> {
        data class Polyphone(val phrase: String, val correct: String, val distractors: List<String>)

        val polyphones = listOf(
            Polyphone("载(zài)重", "载(zài)重", listOf("载(zǎi)重", "载(zāi)重", "载(zāi)体")),
            Polyphone("乐(yuè)曲", "乐(yuè)曲", listOf("乐(lè)曲", "乐(yào)曲", "乐(lè)趣")),
            Polyphone("行(háng)业", "行(háng)业", listOf("行(xíng)业", "行(hàng)业", "行(xiàng)业")),
            Polyphone("模(mú)样", "模(mú)样", listOf("模(mó)样", "模(mò)样", "模(méi)样")),
            Polyphone("称(chēng)心", "称(chēng)心", listOf("称(chèn)心", "称(chéng)心", "称(chén)心")),
            Polyphone("调(diào)查", "调(diào)查", listOf("调(tiáo)查", "调(zhōu)查", "调(dào)查")),
            Polyphone("种(zhǒng)类", "种(zhǒng)类", listOf("种(zhòng)类", "种(zhāng)类", "种(zhuàng)类")),
            Polyphone("处(chù)理", "处(chù)理", listOf("处(chǔ)理", "处(chòu)理", "处(cù)处理")),
            Polyphone("露(lù)面", "露(lù)面", listOf("露(lòu)面", "露(lú)面", "露(lóu)面")),
            Polyphone("嚼(jiáo)劲", "嚼(jiáo)劲", listOf("嚼(jué)劲", "嚼(jiào)劲", "嚼(jī)劲"))
        )

        val questions = mutableListOf<AIGeneratedQuestion>()
        var index = 0
        val topic = topics.randomOrNull() ?: "汉字基础"

        while (questions.size < 50) {
            val poly = polyphones[index % polyphones.size]
            val variants = listOf(
                poly.correct,
                poly.distractors[0],
                poly.distractors[1],
                poly.distractors[2]
            )
            questions.add(
                AIGeneratedQuestion(
                    id = "pre_chinese_${questions.size}",
                    subject = "语文",
                    topic = topic,
                    question = "下列词语中，读音完全正确的是（第${questions.size + 1}题变式）：",
                    options = variants.shuffled(),
                    correctAnswer = poly.correct,
                    explanation = "辨析多音字，注意语境",
                    difficulty = "基础",
                    questionType = "选择题",
                    knowledgePoints = listOf("多音字", "语言运用"),
                    estimatedTime = 120,
                    adaptiveReason = "预备题库 - 语文多音字",
                    creativityLevel = "标准",
                    scenarioContext = "预备题"
                )
            )
            index++
        }
        return questions
    }

    private fun generateMathQuestions(topics: List<String>): List<AIGeneratedQuestion> {
        val questions = mutableListOf<AIGeneratedQuestion>()
        val topic = topics.randomOrNull() ?: "基础运算"
        var counter = 0

        // 🎯 多样化数学题目：函数、三角、代数、几何等
        val mathQuestionBank = listOf(
            // 三角函数
            Triple("sin²x + cos²x的值等于：", listOf("1", "0", "2", "sin x"), "1"),
            Triple("tan(π/4)的值是：", listOf("1", "0", "√3", "1/√3"), "1"),
            Triple("cos(0)的值是：", listOf("1", "0", "-1", "π"), "1"),
            Triple("sin(π/2)的值是：", listOf("1", "0", "-1", "π/2"), "1"),
            Triple("函数y = sin x的周期是：", listOf("2π", "π", "π/2", "4π"), "2π"),
            
            // 对数和指数
            Triple("log₂ 8的值是：", listOf("3", "4", "2", "8"), "3"),
            Triple("ln e的值是：", listOf("1", "0", "e", "2"), "1"),
            Triple("2³的值是：", listOf("8", "6", "9", "4"), "8"),
            Triple("√16的值是：", listOf("4", "8", "2", "16"), "4"),
            Triple("log₁₀ 100的值是：", listOf("2", "10", "100", "1"), "2"),
            
            // 函数与导数
            Triple("函数f(x) = x²的导数是：", listOf("2x", "x", "x²", "2"), "2x"),
            Triple("函数f(x) = x³的导数是：", listOf("3x²", "x²", "3x", "x³"), "3x²"),
            Triple("函数f(x) = sin x的导数是：", listOf("cos x", "sin x", "-cos x", "-sin x"), "cos x"),
            Triple("函数f(x) = cos x的导数是：", listOf("-sin x", "sin x", "cos x", "-cos x"), "-sin x"),
            Triple("函数f(x) = eˣ的导数是：", listOf("eˣ", "xeˣ⁻¹", "ln x", "x"), "eˣ"),
            
            // 极限
            Triple("lim(x→0) (sin x / x) 的值是：", listOf("1", "0", "∞", "不存在"), "1"),
            Triple("lim(x→∞) (1/x) 的值是：", listOf("0", "1", "∞", "不存在"), "0"),
            Triple("lim(x→1) (x² - 1)/(x - 1) 的值是：", listOf("2", "1", "0", "不存在"), "2"),
            
            // 代数运算
            Triple("(x + 2)(x - 3) 展开后的结果是：", listOf("x² - x - 6", "x² + x - 6", "x² - 5x - 6", "x² + 5x + 6"), "x² - x - 6"),
            Triple("方程 x² - 5x + 6 = 0 的解是：", listOf("x = 2 或 x = 3", "x = 1 或 x = 6", "x = -2 或 x = -3", "x = 0 或 x = 5"), "x = 2 或 x = 3"),
            Triple("如果 2x + 3 = 11，那么 x = ?", listOf("4", "3", "5", "7"), "4"),
            
            // 几何
            Triple("圆的面积公式是：", listOf("πr²", "2πr", "πd", "r²"), "πr²"),
            Triple("直角三角形中，勾股定理表示为：", listOf("a² + b² = c²", "a + b = c", "a² - b² = c²", "ab = c"), "a² + b² = c²"),
            Triple("正方形边长为5，其面积是：", listOf("25", "20", "10", "15"), "25"),
            Triple("长方形长为8，宽为3，其周长是：", listOf("22", "24", "11", "16"), "22"),
            
            // 概率统计
            Triple("抛掷一枚硬币，正面朝上的概率是：", listOf("1/2", "1/3", "1/4", "1"), "1/2"),
            Triple("从52张牌中抽取一张红桃的概率是：", listOf("1/4", "1/2", "1/13", "1/52"), "1/4"),
            Triple("掷一个骰子，得到偶数的概率是：", listOf("1/2", "1/3", "1/6", "2/3"), "1/2"),
            
            // 数列
            Triple("等差数列 2, 5, 8, 11, ... 的公差是：", listOf("3", "2", "4", "5"), "3"),
            Triple("等比数列 2, 6, 18, 54, ... 的公比是：", listOf("3", "2", "4", "6"), "3"),
            Triple("斐波那契数列的前几项是 1, 1, 2, 3, 5, 8, ...，下一项是：", listOf("13", "11", "10", "12"), "13"),
            
            // 复数
            Triple("复数 i² 的值是：", listOf("-1", "1", "i", "0"), "-1"),
            Triple("复数 (2 + 3i) + (1 - i) 的结果是：", listOf("3 + 2i", "3 - 2i", "1 + 4i", "3 + 4i"), "3 + 2i"),
            
            // 矩阵
            Triple("2×2单位矩阵的对角线元素都是：", listOf("1", "0", "2", "-1"), "1"),
            Triple("矩阵乘法满足：", listOf("结合律", "交换律", "分配律和结合律", "所有运算律"), "结合律"),
            
            // 微积分应用
            Triple("函数f(x) = x²在x = 2处的切线斜率是：", listOf("4", "2", "8", "1"), "4"),
            Triple("∫x dx 的结果是：", listOf("x²/2 + C", "x + C", "x²", "2x + C"), "x²/2 + C"),
            
            // 应用题
            Triple("某商品原价100元，打8折后的价格是：", listOf("80元", "20元", "120元", "90元"), "80元"),
            Triple("以每小时60公里的速度行驶，3小时能行驶多少公里？", listOf("180公里", "20公里", "63公里", "57公里"), "180公里"),
            Triple("一个班有40名学生，其中60%是女生，女生有多少人？", listOf("24人", "16人", "20人", "30人"), "24人"),
            
            // 基础运算
            Triple("计算：15 + 28 = ?", listOf("43", "42", "44", "41"), "43"),
            Triple("计算：144 ÷ 12 = ?", listOf("12", "10", "14", "16"), "12"),
            Triple("计算：7 × 8 = ?", listOf("56", "54", "58", "49"), "56"),
            Triple("计算：100 - 37 = ?", listOf("63", "67", "73", "57"), "63"),
            
            // 分数运算
            Triple("1/2 + 1/3 的结果是：", listOf("5/6", "2/5", "1/5", "3/5"), "5/6"),
            Triple("3/4 - 1/4 的结果是：", listOf("1/2", "2/4", "1/4", "3/8"), "1/2"),
            Triple("2/3 × 3/4 的结果是：", listOf("1/2", "5/7", "6/12", "2/3"), "1/2"),
            
            // 百分比
            Triple("25%转换为分数是：", listOf("1/4", "1/3", "1/5", "2/5"), "1/4"),
            Triple("0.75转换为百分比是：", listOf("75%", "7.5%", "0.75%", "750%"), "75%")
        )

        // 随机选择50道不重复的题目
        val selectedQuestions = mathQuestionBank.shuffled().take(50)
        
        selectedQuestions.forEachIndexed { index, (question, options, answer) ->
            questions.add(
                AIGeneratedQuestion(
                    id = "pre_math_${counter++}",
                    subject = "数学",
                    topic = topic,
                    question = question,
                    options = options.shuffled(),
                    correctAnswer = answer,
                    explanation = "数学基础题目，考查相关知识点的理解和计算能力",
                    difficulty = when {
                        question.contains("导数") || question.contains("极限") || question.contains("积分") -> "高级"
                        question.contains("函数") || question.contains("方程") || question.contains("概率") -> "中级"
                        else -> "基础"
                    },
                    questionType = "选择题",
                    knowledgePoints = listOf(
                        when {
                            question.contains("sin") || question.contains("cos") || question.contains("tan") -> "三角函数"
                            question.contains("log") || question.contains("ln") -> "对数函数"
                            question.contains("导数") -> "微分学"
                            question.contains("极限") -> "极限理论"
                            question.contains("积分") -> "积分学"
                            question.contains("概率") -> "概率统计"
                            question.contains("矩阵") -> "线性代数"
                            question.contains("复数") -> "复数运算"
                            else -> "基础数学"
                        }
                    ),
                    estimatedTime = when {
                        question.contains("导数") || question.contains("极限") -> 150
                        question.contains("函数") || question.contains("方程") -> 120
                        else -> 90
                    },
                    adaptiveReason = "预备题库 - 数学综合",
                    creativityLevel = "标准",
                    scenarioContext = "预备题"
                )
            )
        }
        
        return questions
    }

    private fun generatePhysicsQuestions(topics: List<String>): List<AIGeneratedQuestion> {
        val topic = topics.randomOrNull() ?: "力学"
        val questions = mutableListOf<AIGeneratedQuestion>()
        val accelerations = listOf(2, 3, 4, 5, 6)
        val times = listOf(2, 3, 4, 5)
        var counter = 0
        outer@for (a in accelerations) {
            for (t in times) {
                val velocity = a * t
                val options = listOf(
                    "$velocity m/s",
                    "${velocity + 2} m/s",
                    "${velocity - 2} m/s",
                    "${velocity + 4} m/s"
                )
                questions.add(
                    AIGeneratedQuestion(
                        id = "pre_physics_$counter",
                        subject = "物理",
                        topic = topic,
                        question = "一物体从静止开始做匀加速直线运动，加速度为${a} m/s²，经过${t}秒后的速度是多少？(a=${a}, t=${t})",
                        options = options.shuffled(),
                        correctAnswer = "$velocity m/s",
                        explanation = "匀加速直线运动公式 v = at",
                        difficulty = "基础",
                        questionType = "选择题",
                        knowledgePoints = listOf("匀加速直线运动"),
                        estimatedTime = 120,
                        adaptiveReason = "预备题库 - 力学基础",
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

    private fun generateChemistryQuestions(topics: List<String>): List<AIGeneratedQuestion> {
        val topic = topics.randomOrNull() ?: "化学方程式"
        val reactions = listOf(
            "2H₂ + O₂ → 2H₂O",
            "N₂ + 3H₂ → 2NH₃",
            "2Na + Cl₂ → 2NaCl",
            "CaCO₃ → CaO + CO₂",
            "Fe + S → FeS"
        )
        val questions = mutableListOf<AIGeneratedQuestion>()
        var counter = 0
        while (questions.size < 50) {
            val equation = reactions[counter % reactions.size]
            val answer = equation
            val options = listOf(
                answer,
                "${equation}↑",
                "${equation.replace("→", "+")}",
                "${equation.replace("2", "3")}"
            )
            questions.add(
                AIGeneratedQuestion(
                    id = "pre_chem_${counter}",
                    subject = "化学",
                    topic = topic,
                    question = "下列化学方程式书写正确的是（第${counter + 1}题变式）：",
                    options = options.shuffled(),
                    correctAnswer = answer,
                    explanation = "注意化学方程式配平",
                    difficulty = "基础",
                    questionType = "选择题",
                    knowledgePoints = listOf("化学方程式"),
                    estimatedTime = 120,
                    adaptiveReason = "预备题库 - 化学基础",
                    creativityLevel = "标准",
                    scenarioContext = "预备题"
                )
            )
            counter++
        }
        return questions
    }

    private fun generateGeneralKnowledgeQuestions(subject: String, topics: List<String>): List<AIGeneratedQuestion> {
        val topic = topics.randomOrNull() ?: "通用知识"
        val questions = mutableListOf<AIGeneratedQuestion>()
        val statements = listOf(
            "太阳从东边升起", "水在100摄氏度沸腾", "地球围绕太阳转", "氧气支持燃烧", "雨后会出现彩虹"
        )
        var counter = 0
        while (questions.size < 50) {
            val statement = statements[counter % statements.size]
            questions.add(
                AIGeneratedQuestion(
                    id = "pre_general_${counter}",
                    subject = subject,
                    topic = topic,
                    question = "判断题：${statement}，下列哪项描述正确？",
                    options = listOf("正确", "错误", "无法判断", "与题目无关"),
                    correctAnswer = "正确",
                    explanation = "常识判断",
                    difficulty = "基础",
                    questionType = "选择题",
                    knowledgePoints = listOf("常识"),
                    estimatedTime = 90,
                    adaptiveReason = "预备题库 - 通识",
                    creativityLevel = "标准",
                    scenarioContext = "预备题"
                )
            )
            counter++
        }
        return questions
    }
}

