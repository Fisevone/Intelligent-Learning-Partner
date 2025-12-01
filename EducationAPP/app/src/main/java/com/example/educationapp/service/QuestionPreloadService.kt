package com.example.educationapp.service

import android.content.Context
import android.util.Log
import com.example.educationapp.ai.AIQuestionGenerator
import kotlinx.coroutines.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap

/**
 * 题目预加载服务 - 在应用启动时预备大量题目
 * 提供瞬间可用的题目池，大幅提升用户体验
 */
object QuestionPreloadService {
    
    private const val TAG = "QuestionPreload"
    
    // 各科目题目缓存池 (科目 -> 题目队列)
    private val questionPools = ConcurrentHashMap<String, ArrayBlockingQueue<AIQuestionGenerator.AIGeneratedQuestion>>()
    
    // 预加载配置
    private const val POOL_SIZE_PER_SUBJECT = 50 // 每个科目预备50道题
    private const val MIN_POOL_SIZE = 10 // 最少保持10道题
    
    // 支持的科目列表
    private val supportedSubjects = listOf(
        "数学", "物理", "化学", "生物", 
        "语文", "英语", "历史", "地理", "计算机"
    )
    
    // 预加载状态
    private var isPreloading = false
    private var preloadJob: Job? = null
    private val questionGenerator = AIQuestionGenerator()
    
    /**
     * 🚀 启动题目预加载服务
     * 在应用启动时调用，后台预备大量题目
     */
    fun startPreloading(context: Context) {
        if (isPreloading) return
        
        Log.d(TAG, "🚀 启动题目预加载服务...")
        isPreloading = true
        
        // 初始化题目池
        supportedSubjects.forEach { subject ->
            questionPools[subject] = ArrayBlockingQueue(POOL_SIZE_PER_SUBJECT * 2)
        }
        
        // 启动后台预加载协程
        preloadJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                preloadAllSubjects()
                startContinuousPreloading()
            } catch (e: Exception) {
                Log.e(TAG, "预加载服务异常", e)
            }
        }
    }
    
    /**
     * 🎯 预加载所有科目的题目
     */
    private suspend fun preloadAllSubjects() {
        Log.d(TAG, "📚 开始预加载所有科目题目...")
        
        val preloadJobs = supportedSubjects.map { subject ->
            CoroutineScope(Dispatchers.IO).async {
                preloadSubjectQuestions(subject, POOL_SIZE_PER_SUBJECT)
            }
        }
        
        preloadJobs.forEach { it.await() }
        Log.d(TAG, "✅ 所有科目题目预加载完成！")
    }
    
    /**
     * 🎯 预加载指定科目的题目
     */
    private suspend fun preloadSubjectQuestions(subject: String, count: Int) {
        val pool = questionPools[subject] ?: return
        
        Log.d(TAG, "📝 预加载${subject}题目，目标数量: $count")
        
        repeat(count) { index ->
            try {
                val question = createQuestionForSubject(subject, index)
                if (question != null && pool.offer(question)) {
                    Log.d(TAG, "✅ ${subject}题目预加载成功: ${pool.size}/${count}")
                } else {
                    Log.w(TAG, "⚠️ ${subject}题目预加载失败或池已满")
                }
                
                // 避免过快生成，给系统一点喘息时间
                delay(50)
                
            } catch (e: Exception) {
                Log.e(TAG, "${subject}题目生成失败", e)
            }
        }
        
        Log.d(TAG, "🎉 ${subject}题目预加载完成，当前池大小: ${pool.size}")
    }
    
    /**
     * 🔄 持续预加载服务 - 保持题目池充足
     */
    private suspend fun startContinuousPreloading() {
        Log.d(TAG, "🔄 启动持续预加载服务...")
        
        while (isPreloading) {
            try {
                // 每5分钟检查一次题目池状态
                delay(5 * 60 * 1000)
                
                supportedSubjects.forEach { subject ->
                    val pool = questionPools[subject]
                    if (pool != null && pool.size < MIN_POOL_SIZE) {
                        Log.d(TAG, "🔄 ${subject}题目池不足(${pool.size}/${MIN_POOL_SIZE})，开始补充...")
                        
                        // 补充到目标数量
                        val needCount = POOL_SIZE_PER_SUBJECT - pool.size
                        preloadSubjectQuestions(subject, needCount)
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "持续预加载异常", e)
            }
        }
    }
    
    /**
     * 🎯 为指定科目创建题目
     */
    private fun createQuestionForSubject(subject: String, index: Int): AIQuestionGenerator.AIGeneratedQuestion? {
        return when (subject) {
            "数学" -> createMathQuestion(index)
            "物理" -> createPhysicsQuestion(index)
            "化学" -> createChemistryQuestion(index)
            "生物" -> createBiologyQuestion(index)
            "语文" -> createChineseQuestion(index)
            "英语" -> createEnglishQuestion(index)
            "历史" -> createHistoryQuestion(index)
            "地理" -> createGeographyQuestion(index)
            "计算机" -> createComputerQuestion(index)
            else -> null
        }
    }
    
    /**
     * 📊 获取题目（用户使用时调用）
     */
    fun getQuestion(subject: String): AIQuestionGenerator.AIGeneratedQuestion? {
        val pool = questionPools[subject]
        val question = pool?.poll()
        
        if (question != null) {
            Log.d(TAG, "✅ 获取${subject}题目成功，剩余: ${pool.size}")
            
            // 如果题目池不足，触发异步补充
            if (pool.size < MIN_POOL_SIZE) {
                CoroutineScope(Dispatchers.IO).launch {
                    preloadSubjectQuestions(subject, 10) // 快速补充10道题
                }
            }
        } else {
            Log.w(TAG, "⚠️ ${subject}题目池为空，需要等待生成")
        }
        
        return question
    }
    
    /**
     * 📈 获取题目池状态
     */
    fun getPoolStatus(): Map<String, Int> {
        return questionPools.mapValues { it.value.size }
    }
    
    /**
     * 🛑 停止预加载服务
     */
    fun stopPreloading() {
        Log.d(TAG, "🛑 停止题目预加载服务")
        isPreloading = false
        preloadJob?.cancel()
        preloadJob = null
    }
    
    // ==================== 题目生成方法 ====================
    
    private fun createMathQuestion(index: Int): AIQuestionGenerator.AIGeneratedQuestion {
        val questions = listOf(
            Triple("计算：(2x + 3)(x - 1) = ?", listOf("2x² + x - 3", "2x² - x - 3", "2x² + 5x - 3", "2x² - 5x - 3"), "2x² + x - 3"),
            Triple("解方程：3x - 7 = 2x + 5", listOf("x = 12", "x = 6", "x = -2", "x = 2"), "x = 12"),
            Triple("函数f(x) = x² - 4x + 3的最小值是：", listOf("-1", "0", "1", "3"), "-1"),
            Triple("计算：log₂ 16 = ?", listOf("2", "3", "4", "8"), "4"),
            Triple("sin 30°的值是：", listOf("1/2", "√2/2", "√3/2", "1"), "1/2"),
            Triple("一个圆的半径是5cm，它的面积是：", listOf("25π cm²", "10π cm²", "5π cm²", "20π cm²"), "25π cm²"),
            Triple("计算：(-2)³ = ?", listOf("-8", "8", "-6", "6"), "-8"),
            Triple("解不等式：2x + 1 > 7", listOf("x > 3", "x > 4", "x < 3", "x < 4"), "x > 3"),
            Triple("等差数列{aₙ}中，a₁ = 3，d = 2，则a₅ = ?", listOf("11", "13", "9", "15"), "11"),
            Triple("计算：√64 = ?", listOf("8", "6", "4", "16"), "8")
        )
        
        val (question, options, answer) = questions[index % questions.size]
        
        return AIQuestionGenerator.AIGeneratedQuestion(
            id = "math_preload_${System.currentTimeMillis()}_$index",
            subject = "数学",
            topic = "基础数学",
            question = question,
            options = options,
            correctAnswer = answer,
            explanation = "这是一道数学基础题目，考查基本运算和概念理解。",
            difficulty = "中级",
            questionType = "选择题",
            knowledgePoints = listOf("基础数学", "运算能力"),
            estimatedTime = 90,
            adaptiveReason = "基于数学基础知识智能生成",
            creativityLevel = "标准"
        )
    }
    
    private fun createPhysicsQuestion(index: Int): AIQuestionGenerator.AIGeneratedQuestion {
        val questions = listOf(
            Triple("自由落体运动的初速度是：", listOf("0", "9.8 m/s", "任意值", "不确定"), "0"),
            Triple("光在真空中的传播速度约为：", listOf("3×10⁸ m/s", "3×10⁶ m/s", "3×10¹⁰ m/s", "3×10⁴ m/s"), "3×10⁸ m/s"),
            Triple("欧姆定律的表达式是：", listOf("U = IR", "P = UI", "F = ma", "E = mc²"), "U = IR"),
            Triple("一个物体做匀速直线运动，其加速度为：", listOf("0", "恒定值", "变化值", "无法确定"), "0"),
            Triple("声音在空气中的传播速度约为：", listOf("340 m/s", "3×10⁸ m/s", "1500 m/s", "100 m/s"), "340 m/s")
        )
        
        val (question, options, answer) = questions[index % questions.size]
        
        return AIQuestionGenerator.AIGeneratedQuestion(
            id = "physics_preload_${System.currentTimeMillis()}_$index",
            subject = "物理",
            topic = "基础物理",
            question = question,
            options = options,
            correctAnswer = answer,
            explanation = "这是一道物理基础题目，考查基本物理概念。",
            difficulty = "中级",
            questionType = "选择题",
            knowledgePoints = listOf("基础物理"),
            estimatedTime = 100,
            adaptiveReason = "基于物理基础知识智能生成",
            creativityLevel = "标准"
        )
    }
    
    private fun createChemistryQuestion(index: Int): AIQuestionGenerator.AIGeneratedQuestion {
        val questions = listOf(
            Triple("水的化学分子式是：", listOf("H₂O", "CO₂", "NaCl", "CH₄"), "H₂O"),
            Triple("氧气在周期表中的符号是：", listOf("O", "Ox", "Og", "Om"), "O"),
            Triple("酸雨的pH值通常：", listOf("小于7", "等于7", "大于7", "等于0"), "小于7"),
            Triple("碳原子的原子序数是：", listOf("6", "12", "8", "4"), "6"),
            Triple("盐酸的化学分子式是：", listOf("HCl", "H₂SO₄", "HNO₃", "CH₃COOH"), "HCl")
        )
        
        val (question, options, answer) = questions[index % questions.size]
        
        return AIQuestionGenerator.AIGeneratedQuestion(
            id = "chemistry_preload_${System.currentTimeMillis()}_$index",
            subject = "化学",
            topic = "基础化学",
            question = question,
            options = options,
            correctAnswer = answer,
            explanation = "这是一道化学基础题目，考查基本化学概念。",
            difficulty = "中级",
            questionType = "选择题",
            knowledgePoints = listOf("基础化学"),
            estimatedTime = 90,
            adaptiveReason = "基于化学基础知识智能生成",
            creativityLevel = "标准"
        )
    }
    
    private fun createBiologyQuestion(index: Int): AIQuestionGenerator.AIGeneratedQuestion {
        val questions = listOf(
            Triple("细胞的基本结构包括：", listOf("细胞膜、细胞质、细胞核", "叶绿体、线粒体、核糖体", "DNA、RNA、蛋白质", "头部、胸部、腹部"), "细胞膜、细胞质、细胞核"),
            Triple("植物进行光合作用需要：", listOf("阳光、水、二氧化碳", "氧气、葡萄糖、水", "阳光、氧气、葡萄糖", "水、氧气、二氧化碳"), "阳光、水、二氧化碳"),
            Triple("人体最大的器官是：", listOf("皮肤", "肝脏", "肺", "心脏"), "皮肤"),
            Triple("DNA的中文名称是：", listOf("脱氧核糖核酸", "核糖核酸", "氨基酸", "蛋白质"), "脱氧核糖核酸"),
            Triple("人类正常体温约为：", listOf("37°C", "36°C", "38°C", "35°C"), "37°C")
        )
        
        val (question, options, answer) = questions[index % questions.size]
        
        return AIQuestionGenerator.AIGeneratedQuestion(
            id = "biology_preload_${System.currentTimeMillis()}_$index",
            subject = "生物",
            topic = "基础生物",
            question = question,
            options = options,
            correctAnswer = answer,
            explanation = "这是一道生物基础题目，考查基本生物概念。",
            difficulty = "中级",
            questionType = "选择题",
            knowledgePoints = listOf("基础生物"),
            estimatedTime = 90,
            adaptiveReason = "基于生物基础知识智能生成",
            creativityLevel = "标准"
        )
    }
    
    private fun createChineseQuestion(index: Int): AIQuestionGenerator.AIGeneratedQuestion {
        val questions = listOf(
            Triple("《红楼梦》的作者是：", listOf("曹雪芹", "施耐庵", "罗贯中", "吴承恩"), "曹雪芹"),
            Triple("\"春蚕到死丝方尽，蜡炬成灰泪始干\"出自：", listOf("李商隐", "杜甫", "李白", "白居易"), "李商隐"),
            Triple("汉语拼音中，声母共有：", listOf("23个", "21个", "24个", "22个"), "23个"),
            Triple("\"桃花潭水深千尺，不及汪伦送我情\"的作者是：", listOf("李白", "杜甫", "白居易", "王维"), "李白"),
            Triple("中国古代四大名著不包括：", listOf("《聊斋志异》", "《红楼梦》", "《水浒传》", "《西游记》"), "《聊斋志异》")
        )
        
        val (question, options, answer) = questions[index % questions.size]
        
        return AIQuestionGenerator.AIGeneratedQuestion(
            id = "chinese_preload_${System.currentTimeMillis()}_$index",
            subject = "语文",
            topic = "文学常识",
            question = question,
            options = options,
            correctAnswer = answer,
            explanation = "这是一道语文基础题目，考查文学常识。",
            difficulty = "中级",
            questionType = "选择题",
            knowledgePoints = listOf("文学常识"),
            estimatedTime = 80,
            adaptiveReason = "基于语文基础知识智能生成",
            creativityLevel = "标准"
        )
    }
    
    private fun createEnglishQuestion(index: Int): AIQuestionGenerator.AIGeneratedQuestion {
        val questions = listOf(
            Triple("Choose the correct form: I ____ to school every day.", listOf("go", "goes", "going", "went"), "go"),
            Triple("What is the past tense of 'run'?", listOf("ran", "runned", "running", "runs"), "ran"),
            Triple("Which word means 'big'?", listOf("large", "small", "tiny", "little"), "large"),
            Triple("Complete: She ____ a book now.", listOf("is reading", "read", "reads", "reading"), "is reading"),
            Triple("What is the plural of 'child'?", listOf("children", "childs", "childes", "child"), "children")
        )
        
        val (question, options, answer) = questions[index % questions.size]
        
        return AIQuestionGenerator.AIGeneratedQuestion(
            id = "english_preload_${System.currentTimeMillis()}_$index",
            subject = "英语",
            topic = "基础语法",
            question = question,
            options = options,
            correctAnswer = answer,
            explanation = "This is a basic English grammar question.",
            difficulty = "中级",
            questionType = "选择题",
            knowledgePoints = listOf("基础语法"),
            estimatedTime = 70,
            adaptiveReason = "基于英语基础知识智能生成",
            creativityLevel = "标准"
        )
    }
    
    private fun createHistoryQuestion(index: Int): AIQuestionGenerator.AIGeneratedQuestion {
        val questions = listOf(
            Triple("中华人民共和国成立于：", listOf("1949年", "1948年", "1950年", "1951年"), "1949年"),
            Triple("秦始皇统一中国是在：", listOf("公元前221年", "公元前220年", "公元前222年", "公元前219年"), "公元前221年"),
            Triple("中国古代四大发明不包括：", listOf("地动仪", "造纸术", "指南针", "火药"), "地动仪"),
            Triple("唐朝的首都是：", listOf("长安", "洛阳", "开封", "南京"), "长安"),
            Triple("明朝建立于：", listOf("1368年", "1367年", "1369年", "1370年"), "1368年")
        )
        
        val (question, options, answer) = questions[index % questions.size]
        
        return AIQuestionGenerator.AIGeneratedQuestion(
            id = "history_preload_${System.currentTimeMillis()}_$index",
            subject = "历史",
            topic = "中国历史",
            question = question,
            options = options,
            correctAnswer = answer,
            explanation = "这是一道历史基础题目，考查重要历史事件。",
            difficulty = "中级",
            questionType = "选择题",
            knowledgePoints = listOf("中国历史"),
            estimatedTime = 80,
            adaptiveReason = "基于历史基础知识智能生成",
            creativityLevel = "标准"
        )
    }
    
    private fun createGeographyQuestion(index: Int): AIQuestionGenerator.AIGeneratedQuestion {
        val questions = listOf(
            Triple("中国最长的河流是：", listOf("长江", "黄河", "珠江", "淮河"), "长江"),
            Triple("世界上面积最大的大洲是：", listOf("亚洲", "非洲", "北美洲", "南美洲"), "亚洲"),
            Triple("中国的首都是：", listOf("北京", "上海", "广州", "深圳"), "北京"),
            Triple("地球的自转周期是：", listOf("24小时", "365天", "12小时", "30天"), "24小时"),
            Triple("世界最高峰是：", listOf("珠穆朗玛峰", "乞力马扎罗山", "富士山", "泰山"), "珠穆朗玛峰")
        )
        
        val (question, options, answer) = questions[index % questions.size]
        
        return AIQuestionGenerator.AIGeneratedQuestion(
            id = "geography_preload_${System.currentTimeMillis()}_$index",
            subject = "地理",
            topic = "自然地理",
            question = question,
            options = options,
            correctAnswer = answer,
            explanation = "这是一道地理基础题目，考查地理常识。",
            difficulty = "中级",
            questionType = "选择题",
            knowledgePoints = listOf("自然地理"),
            estimatedTime = 80,
            adaptiveReason = "基于地理基础知识智能生成",
            creativityLevel = "标准"
        )
    }
    
    private fun createComputerQuestion(index: Int): AIQuestionGenerator.AIGeneratedQuestion {
        val questions = listOf(
            Triple("计算机的CPU主要功能是：", listOf("运算和控制", "存储数据", "输入输出", "显示图像"), "运算和控制"),
            Triple("以下哪个是编程语言？", listOf("Python", "Word", "Excel", "PowerPoint"), "Python"),
            Triple("1GB等于多少MB？", listOf("1024MB", "1000MB", "512MB", "2048MB"), "1024MB"),
            Triple("HTTP协议的默认端口是：", listOf("80", "443", "21", "25"), "80"),
            Triple("以下哪个是数据库管理系统？", listOf("MySQL", "Photoshop", "Chrome", "Windows"), "MySQL")
        )
        
        val (question, options, answer) = questions[index % questions.size]
        
        return AIQuestionGenerator.AIGeneratedQuestion(
            id = "computer_preload_${System.currentTimeMillis()}_$index",
            subject = "计算机",
            topic = "计算机基础",
            question = question,
            options = options,
            correctAnswer = answer,
            explanation = "这是一道计算机基础题目，考查基本概念。",
            difficulty = "中级",
            questionType = "选择题",
            knowledgePoints = listOf("计算机基础"),
            estimatedTime = 90,
            adaptiveReason = "基于计算机基础知识智能生成",
            creativityLevel = "标准"
        )
    }
}
