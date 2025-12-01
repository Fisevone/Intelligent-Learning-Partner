package com.example.educationapp.data

import android.content.Context
import com.example.educationapp.utils.PreferenceManager

/**
 * 演示数据初始化器
 * 为student和teacher账号创建真实的演示数据
 */
class DemoDataInitializer(private val context: Context) {
    
    private val preferenceManager = PreferenceManager(context)
    
    /**
     * 初始化所有演示数据
     */
    fun initializeDemoData() {
        val currentUser = preferenceManager.getUserName().lowercase()
        android.util.Log.d("DemoDataInit", "初始化数据，当前用户: $currentUser")
        
        when {
            currentUser.contains("student") || currentUser == "张小明" -> initializeStudentData()
            currentUser.contains("teacher") || currentUser == "李老师" -> initializeTeacherData()
            else -> initializeStudentData() // 默认为学生数据
        }
    }
    
    /**
     * 初始化学生演示数据
     */
    private fun initializeStudentData() {
        // 保存学生基本信息
        saveStudentProfile()
        
        // 保存学习记录
        saveLearningRecords()
        
        // 保存学习进度
        saveLearningProgress()
        
        // 保存学习统计
        saveLearningStats()
        
        // 保存情绪监测记录
        saveEmotionRecords()
        
        // 保存学习笔记
        saveLearningNotes()
    }
    
    /**
     * 初始化教师演示数据
     */
    private fun initializeTeacherData() {
        // 保存教师基本信息
        saveTeacherProfile()
        
        // 保存班级学生数据
        saveClassStudents()
        
        // 保存教学分析数据
        saveTeachingAnalysis()
        
        // 保存学生进度数据
        saveStudentProgress()
        
        // 保存课堂氛围数据
        saveClassroomAtmosphere()
        
        // 保存题目管理数据
        saveQuestionBank()
    }
    
    private fun saveStudentProfile() {
        val editor = context.getSharedPreferences("student_profile", Context.MODE_PRIVATE).edit()
        
        editor.putString("name", "张小明")
        editor.putString("grade", "七年级")
        editor.putString("class", "七年级3班")
        editor.putString("student_id", "2024001")
        editor.putString("school", "实验中学")
        editor.putInt("age", 13)
        editor.putString("avatar", "student_avatar_1")
        
        // 学习偏好
        editor.putString("learning_style", "视觉学习者")
        editor.putString("favorite_subjects", "数学,物理,英语")
        editor.putString("weak_subjects", "语文,历史")
        
        editor.apply()
    }
    
    private fun saveLearningRecords() {
        val editor = context.getSharedPreferences("learning_records", Context.MODE_PRIVATE).edit()
        
        // 最近30天的学习记录
        val records = listOf(
            // 数学学习记录
            "2024-01-15|数学|一元一次方程|45|85|视频学习|已完成一元一次方程的基本解法",
            "2024-01-16|数学|二元一次方程组|60|78|练习|掌握了代入法和加减法",
            "2024-01-17|数学|不等式|40|92|文章阅读|理解了不等式的性质",
            "2024-01-18|数学|函数概念|50|88|视频学习|初步理解了函数的定义",
            
            // 英语学习记录
            "2024-01-19|英语|现在完成时|35|82|语法练习|掌握了have/has的用法",
            "2024-01-20|英语|阅读理解|40|75|阅读练习|提高了阅读速度",
            "2024-01-21|英语|单词记忆|25|90|单词练习|记住了50个新单词",
            
            // 物理学习记录
            "2024-01-22|物理|力的概念|45|86|实验观察|理解了力的作用效果",
            "2024-01-23|物理|牛顿第一定律|55|91|视频学习|掌握了惯性的概念",
            
            // 语文学习记录
            "2024-01-24|语文|古诗词鉴赏|50|79|文章学习|学会了鉴赏技巧",
            "2024-01-25|语文|作文写作|60|73|写作练习|提高了描写能力"
        )
        
        editor.putStringSet("records", records.toSet())
        editor.putLong("last_update", System.currentTimeMillis())
        
        editor.apply()
    }
    
    private fun saveLearningProgress() {
        val editor = context.getSharedPreferences("learning_progress", Context.MODE_PRIVATE).edit()
        
        // 各科目掌握程度
        editor.putFloat("数学_progress", 0.75f)
        editor.putFloat("英语_progress", 0.68f)
        editor.putFloat("物理_progress", 0.82f)
        editor.putFloat("语文_progress", 0.61f)
        editor.putFloat("化学_progress", 0.45f)
        editor.putFloat("生物_progress", 0.71f)
        
        // 知识点掌握情况
        val knowledgePoints = mapOf(
            "一元一次方程" to 0.95f,
            "二元一次方程组" to 0.78f,
            "不等式" to 0.92f,
            "函数" to 0.65f,
            "现在完成时" to 0.82f,
            "阅读理解" to 0.75f,
            "力学基础" to 0.86f,
            "古诗词鉴赏" to 0.79f
        )
        
        knowledgePoints.forEach { (topic, progress) ->
            editor.putFloat("knowledge_$topic", progress)
        }
        
        editor.apply()
    }
    
    private fun saveLearningStats() {
        val editor = context.getSharedPreferences("learning_stats", Context.MODE_PRIVATE).edit()
        
        // 学习统计数据
        editor.putInt("total_study_days", 45)
        editor.putInt("current_streak", 7)
        editor.putInt("longest_streak", 15)
        editor.putLong("total_study_time", 28 * 60 * 60 * 1000L) // 28小时
        editor.putFloat("average_score", 83.2f)
        editor.putInt("completed_contents", 67)
        editor.putInt("total_contents", 120)
        
        // 本周学习数据
        editor.putFloat("weekly_progress", 0.72f)
        editor.putInt("weekly_goal", 10) // 10小时
        editor.putLong("this_week_time", 7 * 60 * 60 * 1000L) // 7小时
        
        // 各科目时间分配
        editor.putLong("数学_time", 8 * 60 * 60 * 1000L)
        editor.putLong("英语_time", 6 * 60 * 60 * 1000L)
        editor.putLong("物理_time", 7 * 60 * 60 * 1000L)
        editor.putLong("语文_time", 4 * 60 * 60 * 1000L)
        editor.putLong("化学_time", 2 * 60 * 60 * 1000L)
        editor.putLong("生物_time", 1 * 60 * 60 * 1000L)
        
        editor.apply()
    }
    
    private fun saveEmotionRecords() {
        val editor = context.getSharedPreferences("emotion_records", Context.MODE_PRIVATE).edit()
        
        // 最近的情绪监测记录
        val emotionData = listOf(
            "2024-01-25|专注|8.5|2.1|7.8|学习状态良好，注意力集中",
            "2024-01-24|轻松|7.2|1.8|8.1|心情愉快，学习效率高",
            "2024-01-23|紧张|6.8|4.2|6.5|考试前有些紧张",
            "2024-01-22|专注|8.8|1.5|8.9|数学课听讲很认真",
            "2024-01-21|疲惫|5.5|3.8|5.2|昨晚睡眠不足",
            "2024-01-20|兴奋|8.0|2.0|7.5|学会了新的解题方法"
        )
        
        editor.putStringSet("emotion_records", emotionData.toSet())
        
        // 当前情绪状态
        editor.putString("current_emotion", "专注")
        editor.putFloat("current_focus", 8.5f)
        editor.putFloat("current_stress", 2.1f)
        editor.putFloat("current_confidence", 7.8f)
        
        editor.apply()
    }
    
    private fun saveLearningNotes() {
        val editor = context.getSharedPreferences("learning_notes", Context.MODE_PRIVATE).edit()
        
        val notes = mapOf(
            "一元一次方程" to "解题步骤：1.移项 2.合并同类项 3.系数化为1。要注意移项时要变号！",
            "古诗词鉴赏" to "鉴赏要点：1.理解诗意 2.分析手法 3.体会情感。特别注意意象的象征意义。",
            "现在完成时" to "结构：have/has + 过去分词。表示过去发生的动作对现在的影响。",
            "力学基础" to "牛顿第一定律：物体在不受外力时保持静止或匀速直线运动状态。"
        )
        
        notes.forEach { (topic, note) ->
            editor.putString("note_$topic", note)
        }
        
        editor.apply()
    }
    
    private fun saveTeacherProfile() {
        val editor = context.getSharedPreferences("teacher_profile", Context.MODE_PRIVATE).edit()
        
        editor.putString("name", "李老师")
        editor.putString("subject", "数学")
        editor.putString("title", "高级教师")
        editor.putString("teacher_id", "T2024001")
        editor.putString("school", "实验中学")
        editor.putInt("experience_years", 12)
        editor.putString("education", "华东师范大学数学系硕士")
        editor.putString("classes", "七年级1班,七年级2班,七年级3班")
        editor.putInt("total_students", 45) // 3个班，每班15人
        
        editor.apply()
    }
    
    private fun saveClassStudents() {
        val editor = context.getSharedPreferences("class_students", Context.MODE_PRIVATE).edit()
        
        val students = listOf(
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
        
        editor.putStringSet("students_list", students.toSet())
        editor.putInt("total_students", students.size)
        
        editor.apply()
    }
    
    private fun saveTeachingAnalysis() {
        val editor = context.getSharedPreferences("teaching_analysis", Context.MODE_PRIVATE).edit()
        
        // 教学效果分析
        editor.putFloat("class_average_score", 83.4f)
        editor.putFloat("improvement_rate", 0.15f) // 15%提升
        editor.putFloat("attendance_rate", 0.96f) // 96%出勤率
        editor.putFloat("homework_completion", 0.89f) // 89%作业完成率
        
        // 各班级表现
        editor.putFloat("class1_average", 81.2f)
        editor.putFloat("class2_average", 84.1f)
        editor.putFloat("class3_average", 85.0f)
        
        // 知识点掌握情况
        editor.putFloat("algebra_mastery", 0.78f)
        editor.putFloat("geometry_mastery", 0.82f)
        editor.putFloat("functions_mastery", 0.65f)
        editor.putFloat("statistics_mastery", 0.71f)
        
        // 学生分层情况 (总共45人)
        editor.putInt("excellent_students", 12) // 优秀 (26.7%)
        editor.putInt("good_students", 18) // 良好 (40%)
        editor.putInt("average_students", 12) // 一般 (26.7%)
        editor.putInt("struggling_students", 3) // 需要帮助 (6.6%)
        
        editor.apply()
    }
    
    private fun saveStudentProgress() {
        val editor = context.getSharedPreferences("student_progress", Context.MODE_PRIVATE).edit()
        
        // 重点关注学生的详细进度
        val progressData = mapOf(
            "张小明" to "85.2|78|92|15|一元一次方程掌握良好，需加强几何",
            "王小红" to "78.5|65|85|12|语文表达能力强，数学逻辑需提升",
            "李小刚" to "92.1|95|88|18|理科天赋突出，可适当增加难度",
            "陈小美" to "81.7|82|79|14|学习稳定，各科发展均衡",
            "刘小强" to "76.3|70|82|11|动手能力强，理论学习需加强"
        )
        
        progressData.forEach { (student, data) ->
            editor.putString("progress_$student", data)
        }
        
        editor.apply()
    }
    
    private fun saveClassroomAtmosphere() {
        val editor = context.getSharedPreferences("classroom_atmosphere", Context.MODE_PRIVATE).edit()
        
        // 课堂氛围数据
        editor.putFloat("engagement_level", 8.2f)
        editor.putFloat("participation_rate", 0.73f)
        editor.putFloat("attention_score", 7.8f)
        editor.putFloat("interaction_frequency", 6.5f)
        
        // 各时段表现
        editor.putFloat("morning_engagement", 8.5f)
        editor.putFloat("afternoon_engagement", 7.8f)
        editor.putFloat("evening_engagement", 7.2f)
        
        // 课堂活动效果
        editor.putFloat("group_discussion", 8.1f)
        editor.putFloat("individual_work", 7.6f)
        editor.putFloat("presentation", 7.9f)
        editor.putFloat("quiz_activity", 8.3f)
        
        editor.apply()
    }
    
    private fun saveQuestionBank() {
        val editor = context.getSharedPreferences("question_bank", Context.MODE_PRIVATE).edit()
        
        // 题库统计
        editor.putInt("total_questions", 1250)
        editor.putInt("easy_questions", 450)
        editor.putInt("medium_questions", 550)
        editor.putInt("hard_questions", 250)
        
        // 各章节题目数量
        editor.putInt("algebra_questions", 380)
        editor.putInt("geometry_questions", 420)
        editor.putInt("functions_questions", 280)
        editor.putInt("statistics_questions", 170)
        
        // 最近出题记录
        val recentQuestions = listOf(
            "2024-01-25|一元一次方程综合练习|中等|45分钟|已布置",
            "2024-01-24|几何证明专项|困难|60分钟|已完成",
            "2024-01-23|函数概念理解|简单|30分钟|已完成",
            "2024-01-22|代数式化简|中等|40分钟|已完成"
        )
        
        editor.putStringSet("recent_questions", recentQuestions.toSet())
        
        editor.apply()
    }
    
    private fun initializeDefaultData() {
        // 为其他用户提供基础数据
        val editor = context.getSharedPreferences("default_data", Context.MODE_PRIVATE).edit()
        editor.putBoolean("initialized", true)
        editor.putLong("init_time", System.currentTimeMillis())
        editor.apply()
    }
}
