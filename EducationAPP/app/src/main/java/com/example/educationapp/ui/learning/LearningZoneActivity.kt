package com.example.educationapp.ui.learning

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.educationapp.R
import com.example.educationapp.data.SimpleLearningContent
import com.example.educationapp.data.SimpleLearningPath
import com.example.educationapp.data.SimpleContentType
import com.example.educationapp.ui.learning.adapter.LearningContentAdapter
import com.example.educationapp.ui.learning.adapter.LearningPathAdapter
import com.example.educationapp.utils.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch

/**
 * 🎓 学习专区 - 个性化学习内容中心
 * 功能：视频学习、文章阅读、练习测验、学习路径、进度跟踪
 */
class LearningZoneActivity : AppCompatActivity() {

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var contentAdapter: LearningContentAdapter
    private lateinit var pathAdapter: LearningPathAdapter
    
    // UI组件
    private lateinit var tvWelcome: TextView
    private lateinit var tvCurrentGrade: TextView
    private lateinit var tvLearningStats: TextView
    private lateinit var progressWeeklyGoal: LinearProgressIndicator
    private lateinit var tvWeeklyProgress: TextView
    
    // 筛选组件
    private lateinit var chipGroupSubjects: ChipGroup
    private lateinit var chipGroupContentTypes: ChipGroup
    private lateinit var chipGroupDifficulty: ChipGroup
    
    // RecyclerView
    private lateinit var rvLearningPaths: RecyclerView
    private lateinit var rvRecommendedContent: RecyclerView
    private lateinit var rvContinueLearning: RecyclerView
    
    // 快速访问卡片
    private lateinit var cardVideoLibrary: MaterialCardView
    private lateinit var cardArticleLibrary: MaterialCardView
    private lateinit var cardPracticeZone: MaterialCardView
    private lateinit var cardMyProgress: MaterialCardView
    
    private var selectedSubject = "全部"
    private var selectedContentType = SimpleContentType.VIDEO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learning_zone)
        
        initServices()
        initViews()
        setupToolbar()
        setupRecyclerViews()
        setupClickListeners()
        loadLearningData()
    }
    
    private fun initServices() {
        preferenceManager = PreferenceManager(this)
    }
    
    private fun initViews() {
        // 欢迎区域
        tvWelcome = findViewById(R.id.tv_welcome)
        tvCurrentGrade = findViewById(R.id.tv_current_grade)
        tvLearningStats = findViewById(R.id.tv_learning_stats)
        progressWeeklyGoal = findViewById(R.id.progress_weekly_goal)
        tvWeeklyProgress = findViewById(R.id.tv_weekly_progress)
        
        // 筛选组件
        chipGroupSubjects = findViewById(R.id.chip_group_subjects)
        chipGroupContentTypes = findViewById(R.id.chip_group_content_types)
        chipGroupDifficulty = findViewById(R.id.chip_group_difficulty)
        
        // RecyclerView
        rvLearningPaths = findViewById(R.id.rv_learning_paths)
        rvRecommendedContent = findViewById(R.id.rv_recommended_content)
        rvContinueLearning = findViewById(R.id.rv_continue_learning)
        
        // 快速访问卡片
        cardVideoLibrary = findViewById(R.id.card_video_library)
        cardArticleLibrary = findViewById(R.id.card_article_library)
        cardPracticeZone = findViewById(R.id.card_practice_zone)
        cardMyProgress = findViewById(R.id.card_my_progress)
        
        // 初始化欢迎信息
        updateWelcomeInfo()
        setupFilterChips()
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "🎓 学习专区"
        }
        
        toolbar.setNavigationOnClickListener { finish() }
    }
    
    private fun setupRecyclerViews() {
        // 学习路径
        pathAdapter = LearningPathAdapter { path ->
            openLearningPath(path)
        }
        rvLearningPaths.apply {
            layoutManager = LinearLayoutManager(this@LearningZoneActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = pathAdapter
        }
        
        // 推荐内容
        contentAdapter = LearningContentAdapter { content ->
            openLearningContent(content)
        }
        rvRecommendedContent.apply {
            layoutManager = LinearLayoutManager(this@LearningZoneActivity)
            adapter = contentAdapter
        }
        
        // 继续学习
        val continueAdapter = LearningContentAdapter { content ->
            openLearningContent(content)
        }
        rvContinueLearning.apply {
            layoutManager = LinearLayoutManager(this@LearningZoneActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = continueAdapter
        }
    }
    
    private fun setupClickListeners() {
        // 快速访问卡片
        cardVideoLibrary.setOnClickListener {
            openContentLibrary(SimpleContentType.VIDEO)
        }
        
        cardArticleLibrary.setOnClickListener {
            openContentLibrary(SimpleContentType.ARTICLE)
        }
        
        cardPracticeZone.setOnClickListener {
            openContentLibrary(SimpleContentType.EXERCISE)
        }
        
        cardMyProgress.setOnClickListener {
            openProgressDashboard()
        }
        
        // 筛选芯片点击事件
        setupChipGroupListeners()
    }
    
    private fun setupFilterChips() {
        // 学科筛选
        val subjects = listOf("全部", "数学", "语文", "英语", "物理", "化学", "生物", "历史", "地理")
        subjects.forEach { subject ->
            val chip = Chip(this).apply {
                text = subject
                isCheckable = true
                isChecked = subject == selectedSubject
                setOnClickListener {
                    selectedSubject = subject
                    filterContent()
                }
            }
            chipGroupSubjects.addView(chip)
        }
        
        // 内容类型筛选
        SimpleContentType.values().forEach { type ->
            val chip = Chip(this).apply {
                text = "${type.icon} ${type.displayName}"
                isCheckable = true
                isChecked = type == selectedContentType
                setOnClickListener {
                    selectedContentType = type
                    filterContent()
                }
            }
            chipGroupContentTypes.addView(chip)
        }
        
        // 难度筛选
        val difficulties = listOf("入门", "中级", "高级", "专家")
        difficulties.forEach { difficulty ->
            val chip = Chip(this).apply {
                text = difficulty
                isCheckable = true
                setOnClickListener {
                    filterContent()
                }
            }
            chipGroupDifficulty.addView(chip)
        }
    }
    
    private fun setupChipGroupListeners() {
        chipGroupSubjects.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = findViewById<Chip>(checkedIds[0])
                selectedSubject = chip.text.toString()
                filterContent()
            }
        }
    }
    
    private fun updateWelcomeInfo() {
        val userName = preferenceManager.getUserName()
        tvWelcome.text = "你好，$userName"
        tvCurrentGrade.text = "当前年级：七年级"
        
        // 模拟学习统计
        tvLearningStats.text = """
            本周已学习 6 小时
            完成内容 45/120
            连续学习 7 天
        """.trimIndent()
        
        progressWeeklyGoal.progress = 60
        tvWeeklyProgress.text = "60% 完成本周目标"
    }
    
    
    private fun loadLearningData() {
        lifecycleScope.launch {
            try {
                // 加载学习路径
                val learningPaths = generateSampleLearningPaths()
                pathAdapter.updatePaths(learningPaths)
                
                // 加载推荐内容
                val recommendedContent = generateSampleContent()
                contentAdapter.updateContent(recommendedContent)
                
            } catch (e: Exception) {
                android.widget.Toast.makeText(this@LearningZoneActivity, "加载学习内容失败", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun filterContent() {
        // 根据选择的筛选条件重新加载内容
        loadLearningData()
    }
    
    private fun openLearningPath(path: SimpleLearningPath) {
        android.widget.Toast.makeText(this, "开始学习路径：${path.title}", android.widget.Toast.LENGTH_SHORT).show()
        // TODO: 实现学习路径详情页面
    }
    
    private fun openLearningContent(content: SimpleLearningContent) {
        val intent = when (content.type) {
            SimpleContentType.VIDEO -> Intent(this, VideoPlayerActivity::class.java)
            SimpleContentType.ARTICLE -> Intent(this, ArticleReaderActivity::class.java)
            SimpleContentType.EXERCISE -> Intent(this, ExerciseActivity::class.java)
            SimpleContentType.QUIZ -> Intent(this, ExerciseActivity::class.java) // 复用练习Activity
            SimpleContentType.INTERACTIVE -> Intent(this, VideoPlayerActivity::class.java) // 复用视频Activity
        }.apply {
            putExtra("content_id", content.id)
            putExtra("content_title", content.title)
            putExtra("content_type", content.type.name)
        }
        startActivity(intent)
    }
    
    private fun openContentLibrary(type: SimpleContentType) {
        android.widget.Toast.makeText(this, "打开${type.displayName}库", android.widget.Toast.LENGTH_SHORT).show()
        // TODO: 实现内容库页面
    }
    
    private fun openProgressDashboard() {
        android.widget.Toast.makeText(this, "查看学习进度", android.widget.Toast.LENGTH_SHORT).show()
        // TODO: 实现进度仪表板
    }
    
    private fun generateSampleLearningPaths(): List<SimpleLearningPath> {
        return listOf(
            SimpleLearningPath(
                id = "path_math_algebra",
                title = "代数基础",
                description = "从基础代数概念到复杂方程求解",
                subject = "数学",
                contentCount = 8,
                estimatedDuration = 8,
                difficulty = "中级",
                completionRate = 0.3f
            ),
            SimpleLearningPath(
                id = "path_chinese_composition",
                title = "作文写作技巧",
                description = "提升写作水平的系统训练",
                subject = "语文",
                contentCount = 6,
                estimatedDuration = 6,
                difficulty = "中级",
                completionRate = 0.7f
            )
        )
    }
    
    private fun generateSampleContent(): List<SimpleLearningContent> {
        return listOf(
            SimpleLearningContent(
                id = "math_001",
                title = "一元一次方程解法",
                description = "学习如何解一元一次方程的基本方法",
                type = SimpleContentType.VIDEO,
                subject = "数学",
                duration = 25,
                difficulty = "中级",
                rating = 4.5f,
                viewCount = 1250,
                progress = 0.0f
            ),
            SimpleLearningContent(
                id = "chinese_001",
                title = "古诗词鉴赏技巧",
                description = "掌握古诗词的鉴赏方法和答题技巧",
                type = SimpleContentType.ARTICLE,
                subject = "语文",
                duration = 30,
                difficulty = "中级",
                rating = 4.3f,
                viewCount = 890,
                progress = 0.6f
            )
        )
    }
}
