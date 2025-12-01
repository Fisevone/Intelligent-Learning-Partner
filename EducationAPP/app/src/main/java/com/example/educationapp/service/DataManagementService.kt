package com.example.educationapp.service

import android.content.Context
import android.util.Log
import com.example.educationapp.data.*
import com.example.educationapp.data.dao.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * 📊 数据管理服务 - 负责生成、存储和管理真实的教育数据
 */
class DataManagementService(
    private val context: Context,
    private val database: EducationDatabase
) {
    
    private val userDao = database.userDao()
    private val learningRecordDao = database.learningRecordDao()
    private val learningProgressDao = database.learningProgressDao()
    
    companion object {
        private const val TAG = "DataManagementService"
        
        @Volatile
        private var INSTANCE: DataManagementService? = null
        
        fun getInstance(context: Context, database: EducationDatabase): DataManagementService {
            return INSTANCE ?: synchronized(this) {
                val instance = DataManagementService(context, database)
                INSTANCE = instance
                instance
            }
        }
    }
    
    /**
     * 🎓 生成李老师班级的真实中学生数据
     */
    suspend fun generateStudentData(count: Int = 45): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🚀 开始生成 $count 个学生数据...")
            Log.d(TAG, "📊 数据库实例: $database")
            Log.d(TAG, "👤 用户DAO: $userDao")
            
            val students = mutableListOf<User>()
            
            // 李老师的真实班级学生名单 (45人)
            val realStudents = listOf(
                // 七年级1班 (15人)
                "张小明|2024001|七年级1班|85.2|数学偏好|专注型学习者",
                "王小红|2024002|七年级1班|78.5|语文偏好|听觉型学习者",
                "李小刚|2024003|七年级1班|92.1|理科偏好|逻辑型学习者",
                "陈小美|2024004|七年级1班|81.7|英语偏好|视觉型学习者",
                "刘小强|2024005|七年级1班|76.3|体育偏好|动手型学习者",
                "张思远|2024016|七年级1班|89.1|数学偏好|理性型学习者",
                "李雨涵|2024017|七年级1班|83.4|语文偏好|感性型学习者",
                "王梓轩|2024018|七年级1班|77.8|英语偏好|交际型学习者",
                "陈若汐|2024019|七年级1班|86.5|理科偏好|探究型学习者",
                "刘子墨|2024020|七年级1班|79.2|艺术偏好|创意型学习者",
                "赵语桐|2024021|七年级1班|88.7|全科均衡|综合型学习者",
                "孙梓涵|2024022|七年级1班|82.3|数学偏好|逻辑型学习者",
                "马若溪|2024023|七年级1班|75.6|体育偏好|活跃型学习者",
                "朱一诺|2024024|七年级1班|91.4|语文偏好|文艺型学习者",
                "胡语嫣|2024025|七年级1班|84.9|英语偏好|社交型学习者",
                
                // 七年级2班 (15人)
                "赵小芳|2024006|七年级2班|88.9|全科均衡|综合型学习者",
                "孙小伟|2024007|七年级2班|79.4|数学偏好|分析型学习者",
                "周小丽|2024008|七年级2班|83.6|文科偏好|创意型学习者",
                "吴小华|2024009|七年级2班|86.2|理科偏好|实验型学习者",
                "郑小军|2024010|七年级2班|74.8|艺术偏好|感性型学习者",
                "林子涵|2024026|七年级2班|87.3|数学偏好|严谨型学习者",
                "何雨泽|2024027|七年级2班|80.1|体育偏好|运动型学习者",
                "高梓豪|2024028|七年级2班|85.7|理科偏好|逻辑型学习者",
                "罗思琪|2024029|七年级2班|82.8|语文偏好|想象型学习者",
                "梁雨轩|2024030|七年级2班|78.5|英语偏好|听觉型学习者",
                "黄子琪|2024031|七年级2班|89.6|全科均衡|勤奋型学习者",
                "谢若涵|2024032|七年级2班|81.2|艺术偏好|审美型学习者",
                "杨梓轩|2024033|七年级2班|76.9|数学偏好|计算型学习者",
                "许语汐|2024034|七年级2班|84.4|文科偏好|表达型学习者",
                "邓子萱|2024035|七年级2班|88.1|理科偏好|实践型学习者",
                
                // 七年级3班 (15人)
                "黄小玲|2024011|七年级3班|90.3|数学偏好|快速型学习者",
                "徐小东|2024012|七年级3班|82.1|科学偏好|探索型学习者",
                "林小雪|2024013|七年级3班|87.5|语文偏好|文艺型学习者",
                "何小龙|2024014|七年级3班|75.9|体育偏好|活跃型学习者",
                "邓小慧|2024015|七年级3班|84.7|英语偏好|交际型学习者",
                "苏雨桐|2024036|七年级3班|86.8|全科均衡|平衡型学习者",
                "徐若溪|2024037|七年级3班|79.7|艺术偏好|感性型学习者",
                "曾思涵|2024038|七年级3班|88.4|数学偏好|理性型学习者",
                "彭子轩|2024039|七年级3班|83.2|理科偏好|实验型学习者",
                "韩雨涵|2024040|七年级3班|77.6|语文偏好|阅读型学习者",
                "江思琪|2024041|七年级3班|85.9|英语偏好|口语型学习者",
                "汪语桐|2024042|七年级3班|81.5|体育偏好|协调型学习者",
                "石梓涵|2024043|七年级3班|89.2|全科优秀|全面型学习者",
                "崔雨轩|2024044|七年级3班|78.3|数学偏好|思维型学习者",
                "金子涵|2024045|七年级3班|84.1|文科偏好|情感型学习者"
            )
            
            for ((index, studentData) in realStudents.withIndex()) {
                val parts = studentData.split("|")
                val name = parts[0]
                val studentId = parts[1]
                val classId = parts[2]
                val avgScore = parts[3].toFloat()
                val subject = parts[4]
                val learningStyle = parts[5]
                
                // 从班级信息提取年级
                val grade = when {
                    classId.contains("七年级") -> "七年级"
                    classId.contains("八年级") -> "八年级"
                    classId.contains("九年级") -> "九年级"
                    else -> "七年级"
                }
                
                val student = User(
                    id = 0, // Room会自动生成
                    username = "stu_${studentId}",
                    email = "${studentId}@school.edu.cn",
                    password = "hashed_password_${index + 1}", 
                    name = name,
                    userType = UserType.STUDENT,
                    grade = grade,
                    learningStyle = learningStyle,
                    interests = subject,
                    school = "实验中学",
                    classId = classId,
                    subjects = "数学,语文,英语,物理,化学,生物,历史,地理,政治",
                    teacherId = "teacher", // 都是李老师的学生
                    isActive = true,
                    lastLoginTime = System.currentTimeMillis() - Random.nextLong(0, 7L * 24 * 60 * 60 * 1000),
                    createdAt = System.currentTimeMillis() - Random.nextLong(30, 180) * 24L * 60 * 60 * 1000
                )
                
                Log.d(TAG, "📝 准备插入学生: ${student.name} (${student.grade})")
                val savedStudent = userDao.insertUser(student)
                Log.d(TAG, "✅ 学生插入成功，ID: $savedStudent")
                
                students.add(student.copy(id = savedStudent))
                
                // 为每个学生生成学习记录
                Log.d(TAG, "📚 开始为学生 ${student.name} 生成学习记录...")
                generateLearningRecordsForStudent(savedStudent)
                
                Log.d(TAG, "✅ 完成学生: ${student.name} (${student.grade}), 总进度: ${index + 1}/${realStudents.size}")
            }
            
            Log.d(TAG, "成功生成 ${students.size} 个学生数据")
            Result.success(students)
            
        } catch (e: Exception) {
            Log.e(TAG, "生成学生数据失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 👨‍🏫 生成真实的教师数据
     */
    suspend fun generateTeacherData(count: Int = 5): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "开始生成 $count 个教师数据...")
            
            val teachers = mutableListOf<User>()
            val teacherSchools = listOf(
                "北京市第四中学", "上海中学", "深圳中学", "成都七中", "西安交大附中",
                "华师大二附中", "人大附中", "清华附中", "北师大实验中学", "复旦附中",
                "南京师大附中", "杭州学军中学", "长沙雅礼中学", "重庆南开中学", "天津南开中学"
            )
            val gradeRanges = listOf("初一,初二", "初二,初三", "初三,高一", "高一,高二", "高二,高三", "初中全年级", "高中全年级")
            val teacherSubjects = listOf(
                "数学", "语文", "英语", "物理", "化学", "生物", "历史", "地理", "政治", 
                "计算机", "音乐", "美术", "体育", "心理学", "通用技术", "信息技术"
            )
            val teacherNames = listOf(
                "张志明", "李慧敏", "王建华", "刘雅芳", "陈国强", "赵丽华", "周文斌", "吴桂香",
                "郑德才", "孙美丽", "马振华", "朱晓东", "胡静雯", "林国庆", "何雅琴", "高建军",
                "罗美玲", "梁志强", "黄秀英", "谢文华", "杨海涛", "许雅丽", "邓建平", "苏慧芳",
                "徐志刚", "曾丽娟", "彭文彬", "韩雅琴", "江建华", "汪美丽", "石志明", "崔慧敏"
            )
            val teacherTitles = listOf("助教", "讲师", "副教授", "教授", "特级教师", "高级教师", "一级教师", "二级教师")
            val teacherDegrees = listOf("学士", "硕士", "博士", "博士后")
            val teachingYears = listOf(1, 3, 5, 8, 12, 15, 20, 25, 30) // 教龄
            
            for (i in 1..count) {
                val selectedSchool = teacherSchools.random()
                val mainSubject = teacherSubjects.random() // 主要教学科目
                val additionalSubjects = teacherSubjects.filter { it != mainSubject }.shuffled().take(Random.nextInt(0, 2))
                val allSubjects = listOf(mainSubject) + additionalSubjects
                val subjects = allSubjects.joinToString(",")
                
                val teachingYear = teachingYears.random()
                val title = when {
                    teachingYear >= 25 -> listOf("特级教师", "教授", "副教授").random()
                    teachingYear >= 15 -> listOf("高级教师", "副教授", "讲师").random()
                    teachingYear >= 8 -> listOf("一级教师", "讲师", "高级教师").random()
                    teachingYear >= 3 -> listOf("二级教师", "一级教师", "助教").random()
                    else -> listOf("助教", "二级教师").random()
                }
                
                val degree = when {
                    title.contains("教授") -> listOf("博士", "博士后").random()
                    title.contains("讲师") || title == "特级教师" -> listOf("硕士", "博士").random()
                    else -> listOf("学士", "硕士").random()
                }
                
                // 基于学校生成邮箱域名
                val emailDomain = when {
                    selectedSchool.contains("北京") -> "bjschool.edu.cn"
                    selectedSchool.contains("上海") -> "shschool.edu.cn"
                    selectedSchool.contains("深圳") -> "szschool.edu.cn"
                    else -> "school.edu.cn"
                }
                
                // 创建真实的班级负责信息
                val classCount = Random.nextInt(1, 4) // 负责1-3个班级
                val classList = mutableListOf<String>()
                repeat(classCount) {
                    val classGrade = gradeRanges.random().split(",").random()
                    val classNumber = Random.nextInt(1, 15)
                    classList.add("${classGrade}${classNumber}班")
                }
                
                val teacher = User(
                    id = 0,
                    username = "teacher${String.format("%03d", i)}_${mainSubject}",
                    email = "teacher${String.format("%03d", i)}@$emailDomain", 
                    password = "hashed_password_teacher_$i",
                    name = "${teacherNames.getOrElse(i - 1) { "教师$i" }}($title)",
                    userType = UserType.TEACHER,
                    grade = gradeRanges.random(),
                    learningStyle = "teaching_${teachingYear}years", // 用教龄标识教学风格
                    interests = "${degree}学位,${teachingYear}年教龄,${title}",
                    school = selectedSchool,
                    classId = classList.joinToString(","),
                    subjects = subjects,
                    teacherId = "", // 教师自己
                    isActive = Random.nextFloat() > 0.05f, // 95%的教师是活跃的
                    lastLoginTime = System.currentTimeMillis() - Random.nextLong(0, 7L * 24 * 60 * 60 * 1000),
                    createdAt = System.currentTimeMillis() - Random.nextLong(teachingYear * 365L, (teachingYear + 5) * 365L) * 24L * 60 * 60 * 1000
                )
                
                val savedTeacher = userDao.insertUser(teacher)
                teachers.add(teacher.copy(id = savedTeacher))
                
                // 为每个教师生成教学记录
                generateTeachingRecordsForTeacher(savedTeacher)
                
                Log.d(TAG, "生成教师: ${teacher.name} (${teacher.subjects})")
            }
            
            Log.d(TAG, "成功生成 ${teachers.size} 个教师数据")
            Result.success(teachers)
            
        } catch (e: Exception) {
            Log.e(TAG, "生成教师数据失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 📚 为学生生成学习记录
     */
    private suspend fun generateLearningRecordsForStudent(studentId: Long) {
        try {
            val student = userDao.getUserById(studentId)
            val studentSubjects = student?.subjects?.split(",")?.map { it.trim() } ?: listOf("数学", "语文", "英语")
            
            // 基于学生的年级和兴趣调整记录数量
            val baseRecordCount = when (student?.grade) {
                "高三", "大三" -> Random.nextInt(80, 150) // 毕业班学习记录更多
                "高二", "大二" -> Random.nextInt(50, 100)
                "高一", "大一" -> Random.nextInt(30, 80)
                else -> Random.nextInt(20, 60)
            }
            
            // 基于学生活跃度调整记录数量
            val recordCount = if (student?.isActive == true) {
                (baseRecordCount * (0.8 + Random.nextFloat() * 0.4)).toInt() // 活跃学生记录更多
            } else {
                (baseRecordCount * (0.3 + Random.nextFloat() * 0.4)).toInt() // 不活跃学生记录较少
            }
            
            for (i in 1..recordCount) {
                val subject = studentSubjects.random()
                
                // 基于学生兴趣和学习风格调整分数分布
                val baseScore = when {
                    student?.interests?.contains(subject) == true -> Random.nextDouble(75.0, 95.0) // 感兴趣的科目分数更高
                    student?.learningStyle == "visual" && subject in listOf("数学", "物理", "化学") -> Random.nextDouble(70.0, 90.0)
                    student?.learningStyle == "auditory" && subject in listOf("语文", "英语", "历史") -> Random.nextDouble(70.0, 90.0)
                    else -> Random.nextDouble(60.0, 85.0)
                }
                
                // 添加一些随机波动
                val randomVariation = (Random.nextDouble(-10.0, 10.0)) // 简单的随机波动
                val score = (baseScore + randomVariation).coerceIn(50.0, 100.0)
                
                // 基于学习风格调整学习时长
                val baseDuration = when (student?.learningStyle) {
                    "kinesthetic" -> Random.nextLong(600, 2400) // 动觉学习者学习时间较短但频繁
                    "reading" -> Random.nextLong(1200, 4800) // 阅读型学习者时间较长
                    "social" -> Random.nextLong(900, 3600) // 社交型学习时间中等
                    else -> Random.nextLong(600, 3600)
                }
                val duration = (baseDuration * (0.7 + Random.nextFloat() * 0.6)).toLong()
                
                val learningRecord = LearningRecord(
                    id = 0,
                    userId = studentId,
                    subject = subject,
                    topic = generateRandomTopic(subject),
                    score = score.toFloat(),
                    duration = duration,
                    difficulty = when {
                        score >= 90 -> "高级"
                        score >= 80 -> "中级" 
                        score >= 70 -> "基础"
                        else -> "入门"
                    },
                    learningStyle = "visual",
                    timestamp = System.currentTimeMillis() - Random.nextLong(0, 30) * 24L * 60 * 60 * 1000
                )
                
                learningRecordDao.insertLearningRecord(learningRecord)
            }
            
            // 生成学习进度数据
            generateLearningProgressForStudent(studentId, studentSubjects)
            
        } catch (e: Exception) {
            Log.e(TAG, "生成学生学习记录失败: $studentId", e)
        }
    }
    
    /**
     * 📊 为学生生成学习进度数据
     */
    private suspend fun generateLearningProgressForStudent(studentId: Long, subjects: List<String>) {
        try {
            for (subject in subjects) {
                val knowledgePoints = generateKnowledgePointsForSubject(subject)
                
                for (knowledgePoint in knowledgePoints) {
                    val masteryLevel = Random.nextFloat()
                    val practiceCount = Random.nextInt(1, 20)
                    val correctCount = (practiceCount * (0.5 + masteryLevel * 0.5)).toInt()
                    
                    val progress = LearningProgress(
                        id = 0,
                        userId = studentId,
                        subject = subject,
                        knowledgePoint = knowledgePoint,
                        masteryLevel = masteryLevel,
                        studyTime = Random.nextLong(600, 7200), // 10分钟到2小时
                        correctAnswers = correctCount,
                        totalAnswers = practiceCount,
                        lastStudyTime = System.currentTimeMillis() - Random.nextLong(0, 7) * 24L * 60 * 60 * 1000,
                        difficultyLevel = when {
                            masteryLevel >= 0.8f -> "高级"
                            masteryLevel >= 0.6f -> "中级"
                            else -> "基础"
                        },
                        studySource = "练习",
                        createdAt = System.currentTimeMillis() - Random.nextLong(7, 30) * 24L * 60 * 60 * 1000,
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    learningProgressDao.insertProgress(progress)
                }
            }
            
            // 生成学习统计数据
            generateLearningStatisticsForStudent(studentId)
            
        } catch (e: Exception) {
            Log.e(TAG, "生成学习进度失败: $studentId", e)
        }
    }
    
    /**
     * 📈 为学生生成学习统计数据
     */
    private suspend fun generateLearningStatisticsForStudent(studentId: Long) {
        try {
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            val statistics = LearningStatistics(
                id = 0,
                userId = studentId,
                date = today,
                totalStudyTime = Random.nextLong(10000, 100000), // 总学习时间(秒)
                questionsAnswered = Random.nextInt(100, 1000), // 总题目数
                correctRate = Random.nextFloat() * 0.3f + 0.7f, // 正确率 70%-100%
                subjectsStudied = listOf("数学", "物理", "语文").shuffled().take(2).joinToString(","),
                aiInteractions = Random.nextInt(20, 100), // AI交互次数
                knowledgePointsLearned = Random.nextInt(10, 50), // 学习的知识点数量
                createdAt = System.currentTimeMillis() - Random.nextLong(30, 90) * 24L * 60 * 60 * 1000
            )
            
            learningProgressDao.insertStatistics(statistics)
            
        } catch (e: Exception) {
            Log.e(TAG, "生成学习统计失败: $studentId", e)
        }
    }
    
    /**
     * 👨‍🏫 为教师生成教学记录
     */
    private suspend fun generateTeachingRecordsForTeacher(teacherId: Long) {
        try {
            // 为教师生成一些教学相关的"学习记录"（实际是教学记录）
            val recordCount = Random.nextInt(20, 100)
            val teacher = userDao.getUserById(teacherId)
            val subjects = teacher?.subjects?.split(",") ?: listOf("数学")
            
            for (i in 1..recordCount) {
                val subject = subjects.random()
                val teachingQuality = Random.nextDouble(80.0, 100.0) // 教学质量评分
                val duration = Random.nextLong(1800, 7200) // 30分钟到2小时的课程
                
                val teachingRecord = LearningRecord(
                    id = 0,
                    userId = teacherId,
                    subject = "教学-$subject",
                    topic = "课程: ${generateRandomTopic(subject)}",
                    score = teachingQuality.toFloat(),
                    duration = duration,
                    difficulty = "教学",
                    learningStyle = "teaching",
                    timestamp = System.currentTimeMillis() - Random.nextLong(0, 60) * 24L * 60 * 60 * 1000
                )
                
                learningRecordDao.insertLearningRecord(teachingRecord)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "生成教师教学记录失败: $teacherId", e)
        }
    }
    
    /**
     * 🎯 根据科目生成随机主题
     */
    private fun generateRandomTopic(subject: String): String {
        return when (subject) {
            "数学" -> listOf(
                "函数与导数", "极限理论", "微积分基础", "线性代数", "概率统计",
                "三角函数", "立体几何", "解析几何", "数列", "不等式"
            ).random()
            "语文" -> listOf(
                "现代文阅读", "古诗词鉴赏", "文言文翻译", "作文写作", "语法分析",
                "修辞手法", "文学常识", "阅读理解", "诗歌赏析", "散文分析"
            ).random()
            "英语" -> listOf(
                "语法时态", "词汇扩展", "阅读理解", "听力训练", "口语表达",
                "写作技巧", "翻译练习", "语音语调", "语言文化", "商务英语"
            ).random()
            "物理" -> listOf(
                "力学基础", "电磁学", "光学", "热力学", "量子物理",
                "波动理论", "相对论", "原子物理", "核物理", "电路分析"
            ).random()
            "化学" -> listOf(
                "原子结构", "化学键", "化学反应", "有机化学", "无机化学",
                "化学平衡", "电化学", "化学动力学", "化学热力学", "分析化学"
            ).random()
            "生物" -> listOf(
                "细胞生物学", "遗传学", "生态学", "进化论", "生理学",
                "分子生物学", "微生物学", "植物学", "动物学", "生物技术"
            ).random()
            else -> "基础知识"
        }
    }
    
    /**
     * 📋 根据科目生成知识点列表
     */
    private fun generateKnowledgePointsForSubject(subject: String): List<String> {
        return when (subject) {
            "数学" -> listOf(
                "函数概念", "导数运算", "极限计算", "积分应用", "线性方程组",
                "矩阵运算", "概率计算", "统计分析", "三角恒等式", "几何证明"
            )
            "语文" -> listOf(
                "字词理解", "句法分析", "修辞识别", "文章结构", "主题思想",
                "语言风格", "表达技巧", "文学常识", "诗歌韵律", "散文特点"
            )
            "英语" -> listOf(
                "动词时态", "名词复数", "形容词比较", "介词使用", "从句结构",
                "词汇搭配", "语音规则", "语法结构", "阅读技巧", "写作方法"
            )
            "物理" -> listOf(
                "牛顿定律", "能量守恒", "动量定理", "电场强度", "磁场方向",
                "波的性质", "光的传播", "热力学定律", "原子模型", "量子效应"
            )
            "化学" -> listOf(
                "元素周期律", "化学键类型", "反应机理", "化学平衡", "酸碱性质",
                "氧化还原", "有机反应", "分子结构", "化学计算", "实验操作"
            )
            "生物" -> listOf(
                "细胞结构", "DNA复制", "蛋白质合成", "遗传规律", "进化机制",
                "生态系统", "生物多样性", "新陈代谢", "免疫反应", "神经调节"
            )
            else -> listOf("基础概念", "基本原理", "应用实践")
        }
    }
    
    /**
     * 📊 获取所有学生数据（用于教师查看）
     */
    suspend fun getAllStudents(): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val students = userDao.getUsersByType(UserType.STUDENT)
            Result.success(students)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 👨‍🏫 获取所有教师数据
     */
    suspend fun getAllTeachers(): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val teachers = userDao.getUsersByType(UserType.TEACHER)
            Result.success(teachers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 📚 获取学生的学习记录
     */
    suspend fun getStudentLearningRecords(studentId: Long): Result<List<LearningRecord>> = withContext(Dispatchers.IO) {
        try {
            val records = learningRecordDao.getRecordsByUserId(studentId)
            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 📈 获取学生的学习进度
     */
    suspend fun getStudentLearningProgress(studentId: Long): Result<List<LearningProgress>> = withContext(Dispatchers.IO) {
        try {
            val progress = learningProgressDao.getProgressByUserId(studentId)
            Result.success(progress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
* 🗑️ 彻底清除所有学生数据（强力清除）
     */
    suspend fun clearAllTestData(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🧹 开始强力清除所有数据...")
            
            // 1. 删除所有学习记录
            learningRecordDao.deleteAllRecords()
            Log.d(TAG, "✅ 清除学习记录完成")
            
            // 2. 删除所有学习进度
            learningProgressDao.deleteAllProgress()
            learningProgressDao.deleteAllStatistics()
            learningProgressDao.deleteAllBehaviors()
            Log.d(TAG, "✅ 清除学习进度完成")
            
            // 3. 强力删除所有用户（除了登录账号）
            val allStudents = userDao.getUsersByType(UserType.STUDENT)
            val allTeachers = userDao.getUsersByType(UserType.TEACHER)
            val allUsers = allStudents + allTeachers
            var deletedCount = 0
            for (user in allUsers) {
                // 只保留当前登录的账号
                if (user.username != "student" && user.username != "teacher") {
                    userDao.deleteUser(user)
                    deletedCount++
                    Log.d(TAG, "删除用户: ${user.name} (${user.username}) - ${user.userType}")
                }
            }
            
            Log.d(TAG, "✅ 强力清除完成，删除了${deletedCount}个用户")
            
            // 4. 验证清除结果
            val remainingStudents = userDao.getUsersByType(UserType.STUDENT)
            val remainingTeachers = userDao.getUsersByType(UserType.TEACHER)
            Log.d(TAG, "📊 清除后剩余: 学生${remainingStudents.size}个，教师${remainingTeachers.size}个")
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ 清除测试数据失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 📊 获取数据统计信息
     */
    suspend fun getDataStatistics(): Result<DataStatistics> = withContext(Dispatchers.IO) {
        try {
            val studentCount = userDao.getUserCountByType(UserType.STUDENT)
            val teacherCount = userDao.getUserCountByType(UserType.TEACHER)
            val learningRecordCount = learningRecordDao.getTotalRecordCount()
            val progressCount = learningProgressDao.getTotalProgressCount()
            
            val statistics = DataStatistics(
                studentCount = studentCount,
                teacherCount = teacherCount,
                learningRecordCount = learningRecordCount,
                progressRecordCount = progressCount,
                lastUpdated = System.currentTimeMillis()
            )
            
            Result.success(statistics)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    data class DataStatistics(
        val studentCount: Int,
        val teacherCount: Int, 
        val learningRecordCount: Int,
        val progressRecordCount: Int,
        val lastUpdated: Long
    )
}
