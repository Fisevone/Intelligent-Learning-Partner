package com.example.educationapp.ui.learning

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.educationapp.R
import com.example.educationapp.data.SimpleLearningContent
import com.example.educationapp.data.SimpleContentType
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 📖 文章阅读器
 * 支持富文本阅读，阅读进度跟踪，学习笔记
 */
class ArticleReaderActivity : AppCompatActivity() {

    private lateinit var tvArticleTitle: TextView
    private lateinit var tvArticleContent: TextView
    private lateinit var tvArticleInfo: TextView
    private lateinit var tvReadingProgress: TextView
    private lateinit var progressReading: LinearProgressIndicator
    private lateinit var btnComplete: MaterialButton
    private lateinit var btnSaveNotes: MaterialButton
    private lateinit var etNotes: com.google.android.material.textfield.TextInputEditText
    
    private var currentContent: SimpleLearningContent? = null
    private var readingStartTime = 0L
    private var isCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_reader)
        
        initViews()
        setupToolbar()
        loadArticleContent()
        setupClickListeners()
        startReadingTracking()
    }
    
    private fun initViews() {
        tvArticleTitle = findViewById(R.id.tv_article_title)
        tvArticleContent = findViewById(R.id.tv_article_content)
        tvArticleInfo = findViewById(R.id.tv_article_info)
        tvReadingProgress = findViewById(R.id.tv_reading_progress)
        progressReading = findViewById(R.id.progress_reading)
        btnComplete = findViewById(R.id.btn_complete)
        btnSaveNotes = findViewById(R.id.btn_save_notes)
        etNotes = findViewById(R.id.et_notes)
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "文章学习"
        }
        
        toolbar.setNavigationOnClickListener { finish() }
    }
    
    private fun loadArticleContent() {
        val contentId = intent.getStringExtra("content_id") ?: "chinese_001"
        val contentTitle = intent.getStringExtra("content_title") ?: "古诗词鉴赏技巧"
        
        currentContent = SimpleLearningContent(
            id = contentId,
            title = contentTitle,
            description = "掌握古诗词的鉴赏方法和答题技巧",
            type = SimpleContentType.ARTICLE,
            subject = "语文",
            duration = 20, // 20分钟阅读时间
            difficulty = "中级",
            rating = 4.5f,
            viewCount = 1890,
            progress = 0f
        )
        
        updateArticleInfo()
        loadArticleText()
    }
    
    private fun updateArticleInfo() {
        currentContent?.let { content ->
            tvArticleTitle.text = content.title
            tvArticleInfo.text = "预计阅读时间：${content.duration}分钟 | ⭐ ${content.rating} | ${content.viewCount}人已学习"
            
            val progressPercent = (content.progress * 100).toInt()
            progressReading.progress = progressPercent
            tvReadingProgress.text = "阅读进度：${progressPercent}%"
        }
    }
    
    private fun loadArticleText() {
        // 加载文章内容（实际应用中应该从服务器或数据库获取）
        val articleContent = getArticleContent()
        tvArticleContent.text = articleContent
        
        readingStartTime = System.currentTimeMillis()
    }
    
    private fun getArticleContent(): String {
        return """
# 古诗词鉴赏技巧与方法

## 一、古诗词鉴赏的基本步骤

古诗词鉴赏是语文学习中的重要内容，掌握正确的鉴赏方法能够帮助我们更好地理解诗人的情感和作品的艺术价值。

### 1. 读懂诗意
首先要通读全诗，理解诗歌的基本内容：
- **时间**：诗歌创作的时代背景
- **地点**：诗歌描写的场景
- **人物**：诗歌中的抒情主人公
- **事件**：诗歌叙述的主要内容

### 2. 分析意象
意象是诗歌的重要组成部分：
- **自然意象**：山、水、花、鸟等
- **人文意象**：古迹、建筑、器物等
- **典型意象**：具有固定象征意义的意象

## 二、常见的表现手法

### 1. 修辞手法
- **比喻**：增强表达效果，使抽象具体化
- **拟人**：赋予事物人的情感和行为
- **对偶**：形式整齐，音律和谐
- **夸张**：突出特征，强化情感

### 2. 表达技巧
- **借景抒情**：通过描写景物来表达情感
- **托物言志**：借助具体事物表达抽象理念
- **对比衬托**：通过对比突出主题
- **虚实结合**：现实与想象相结合

## 三、情感主题的把握

### 1. 常见情感类型
- **思乡怀人**：对故乡和亲人的思念
- **边塞征战**：对战争的感慨和对和平的向往
- **羁旅愁思**：旅途中的孤独和思考
- **咏史怀古**：对历史的反思和感悟

### 2. 主题表达方式
- **直抒胸臆**：直接表达情感
- **间接抒情**：通过景物、典故等间接表达

## 四、语言特色分析

### 1. 词语选择
- **动词**：体现动态美
- **形容词**：突出事物特征
- **叠词**：增强音韵美和表达效果

### 2. 句式特点
- **长短句结合**：富有节奏感
- **倒装句**：突出重点
- **省略句**：言简意赅

## 五、实战技巧

### 1. 答题步骤
1. **审题**：明确题目要求
2. **定位**：找到相关诗句
3. **分析**：运用鉴赏知识
4. **表达**：组织规范答案

### 2. 答题模板
- **手法题**：运用了...手法，...（具体分析），表达了...情感
- **情感题**：表达了...情感，通过...（具体分析）体现
- **语言题**：...词语，...（作用分析），突出了...

## 六、经典例题解析

让我们通过具体的诗歌来实践这些技巧：

**《春望》杜甫**
国破山河在，城春草木深。
感时花溅泪，恨别鸟惊心。
烽火连三月，家书抵万金。
白头搔更短，浑欲不胜簪。

**分析要点**：
- 时代背景：安史之乱
- 情感主题：忧国思家
- 表现手法：对比、拟人
- 语言特色：朴素深沉

通过系统的学习和练习，我们就能够熟练掌握古诗词鉴赏的方法，提高文学素养和审美能力。

---

**学习小贴士**：
1. 多读多背经典诗词，积累文学底蕴
2. 关注诗人生平和时代背景
3. 培养对语言文字的敏感度
4. 多做练习，熟能生巧

记住，古诗词鉴赏不仅是应试技巧，更是文化传承和精神熏陶的过程。让我们在诗词的海洋中感受中华文化的博大精深！
        """.trimIndent()
    }
    
    private fun setupClickListeners() {
        btnComplete.setOnClickListener {
            markAsCompleted()
        }
        
        btnSaveNotes.setOnClickListener {
            saveNotes()
        }
    }
    
    private fun startReadingTracking() {
        lifecycleScope.launch {
            while (!isCompleted) {
                delay(10000) // 每10秒更新一次进度
                
                val currentTime = System.currentTimeMillis()
                val readingTime = (currentTime - readingStartTime) / 1000 // 秒
                val expectedTime = (currentContent?.duration ?: 20) * 60 // 转换为秒
                
                val progress = (readingTime.toFloat() / expectedTime.toFloat()).coerceAtMost(0.95f)
                
                // 更新进度显示
                val progressPercent = (progress * 100).toInt()
                progressReading.progress = progressPercent
                tvReadingProgress.text = "阅读进度：${progressPercent}%"
                
                // 更新内容进度
                currentContent = currentContent?.copy(progress = progress)
                
                // 如果阅读时间超过预计时间的70%，启用完成按钮
                if (progress > 0.7f && !btnComplete.isEnabled) {
                    btnComplete.isEnabled = true
                    btnComplete.text = "标记为已完成"
                    android.widget.Toast.makeText(this@ArticleReaderActivity, "👏 阅读进度良好，可以标记完成！", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun markAsCompleted() {
        isCompleted = true
        currentContent = currentContent?.copy(progress = 1.0f)
        
        // 更新UI
        progressReading.progress = 100
        tvReadingProgress.text = "阅读进度：100% ✅"
        btnComplete.text = "已完成"
        btnComplete.isEnabled = false
        
        // 显示完成提示
        android.widget.Toast.makeText(this, "🎉 恭喜完成文章学习！知识图谱已更新", android.widget.Toast.LENGTH_LONG).show()
        
        saveProgressToDatabase()
    }
    
    private fun saveNotes() {
        val notes = etNotes.text.toString().trim()
        if (notes.isNotEmpty()) {
            // 模拟保存笔记
            lifecycleScope.launch {
                delay(300)
                android.widget.Toast.makeText(this@ArticleReaderActivity, "📝 学习笔记已保存", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            android.widget.Toast.makeText(this, "请输入学习笔记内容", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun saveProgressToDatabase() {
        lifecycleScope.launch {
            delay(500)
            android.widget.Toast.makeText(this@ArticleReaderActivity, "✅ 学习进度已保存", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}

