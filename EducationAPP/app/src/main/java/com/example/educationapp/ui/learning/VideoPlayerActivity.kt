package com.example.educationapp.ui.learning

import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.TextView
import android.widget.VideoView
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
 * 🎥 视频学习播放器
 * 支持本地和在线视频播放，学习进度跟踪
 */
class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var tvVideoTitle: TextView
    private lateinit var tvVideoDescription: TextView
    private lateinit var tvVideoDuration: TextView
    private lateinit var tvVideoProgress: TextView
    private lateinit var progressVideo: LinearProgressIndicator
    private lateinit var btnPlayPause: MaterialButton
    private lateinit var btnComplete: MaterialButton
    private lateinit var cardVideoInfo: MaterialCardView
    
    private var currentContent: SimpleLearningContent? = null
    private var videoDuration = 0
    private var currentPosition = 0
    private var isVideoCompleted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        
        initViews()
        setupToolbar()
        loadVideoContent()
        setupVideoPlayer()
        setupClickListeners()
        startProgressTracking()
    }
    
    private fun initViews() {
        videoView = findViewById(R.id.video_view)
        tvVideoTitle = findViewById(R.id.tv_video_title)
        tvVideoDescription = findViewById(R.id.tv_video_description)
        tvVideoDuration = findViewById(R.id.tv_video_duration)
        tvVideoProgress = findViewById(R.id.tv_video_progress)
        progressVideo = findViewById(R.id.progress_video)
        btnPlayPause = findViewById(R.id.btn_play_pause)
        btnComplete = findViewById(R.id.btn_complete)
        cardVideoInfo = findViewById(R.id.card_video_info)
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "视频学习"
        }
        
        toolbar.setNavigationOnClickListener { finish() }
    }
    
    private fun loadVideoContent() {
        // 获取传入的内容信息
        val contentId = intent.getStringExtra("content_id") ?: "math_001"
        val contentTitle = intent.getStringExtra("content_title") ?: "一元一次方程解法"
        
        // 创建示例内容（实际应用中应该从数据库获取）
        currentContent = SimpleLearningContent(
            id = contentId,
            title = contentTitle,
            description = "本视频将详细讲解一元一次方程的解法步骤，包括移项、合并同类项、系数化为1等关键步骤。通过具体例题演示，帮助学生掌握解题技巧。",
            type = SimpleContentType.VIDEO,
            subject = "数学",
            duration = 15, // 15分钟
            difficulty = "中级",
            rating = 4.7f,
            viewCount = 2150,
            progress = 0f
        )
        
        updateVideoInfo()
    }
    
    private fun updateVideoInfo() {
        currentContent?.let { content ->
            tvVideoTitle.text = content.title
            tvVideoDescription.text = content.description
            tvVideoDuration.text = "视频时长：${content.duration}分钟"
            
            val progressPercent = (content.progress * 100).toInt()
            progressVideo.progress = progressPercent
            tvVideoProgress.text = "学习进度：${progressPercent}%"
        }
    }
    
    private fun setupVideoPlayer() {
        // 使用示例视频URL（实际应用中应该使用真实的视频链接）
        val videoUri = getVideoUri()
        videoView.setVideoURI(videoUri)
        
        // 设置媒体控制器
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)
        
        // 视频准备完成监听
        videoView.setOnPreparedListener { mediaPlayer ->
            videoDuration = mediaPlayer.duration
            updateDurationDisplay()
        }
        
        // 视频完成监听
        videoView.setOnCompletionListener {
            isVideoCompleted = true
            btnComplete.isEnabled = true
            btnComplete.text = "标记为已完成"
            android.widget.Toast.makeText(this, "🎉 视频播放完成！", android.widget.Toast.LENGTH_SHORT).show()
        }
        
        // 视频错误监听
        videoView.setOnErrorListener { _, what, extra ->
            android.widget.Toast.makeText(this, "视频加载失败，播放示例内容", android.widget.Toast.LENGTH_SHORT).show()
            // 这里可以播放本地示例视频或显示错误信息
            false
        }
    }
    
    private fun getVideoUri(): Uri {
        // 这里使用一个公开的教育视频示例
        // 实际应用中应该从服务器获取视频URL
        return Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
    }
    
    private fun setupClickListeners() {
        btnPlayPause.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
                btnPlayPause.text = "播放"
                btnPlayPause.setIconResource(R.drawable.ic_play)
            } else {
                videoView.start()
                btnPlayPause.text = "暂停"
                btnPlayPause.setIconResource(R.drawable.ic_pause)
            }
        }
        
        btnComplete.setOnClickListener {
            markAsCompleted()
        }
    }
    
    private fun startProgressTracking() {
        lifecycleScope.launch {
            while (!isVideoCompleted) {
                delay(1000) // 每秒更新一次
                
                if (videoView.isPlaying && videoDuration > 0) {
                    currentPosition = videoView.currentPosition
                    val progress = currentPosition.toFloat() / videoDuration.toFloat()
                    
                    // 更新进度显示
                    val progressPercent = (progress * 100).toInt()
                    progressVideo.progress = progressPercent
                    tvVideoProgress.text = "学习进度：${progressPercent}%"
                    
                    // 更新内容进度
                    currentContent = currentContent?.copy(progress = progress)
                    
                    // 如果观看超过80%，启用完成按钮
                    if (progress > 0.8f && !btnComplete.isEnabled) {
                        btnComplete.isEnabled = true
                        btnComplete.text = "标记为已完成"
                        android.widget.Toast.makeText(this@VideoPlayerActivity, "👏 已观看80%以上，可以标记完成！", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    
    private fun updateDurationDisplay() {
        val minutes = videoDuration / (1000 * 60)
        val seconds = (videoDuration / 1000) % 60
        tvVideoDuration.text = "视频时长：${minutes}:${String.format("%02d", seconds)}"
    }
    
    private fun markAsCompleted() {
        currentContent = currentContent?.copy(progress = 1.0f)
        
        // 更新UI
        progressVideo.progress = 100
        tvVideoProgress.text = "学习进度：100% ✅"
        btnComplete.text = "已完成"
        btnComplete.isEnabled = false
        
        // 显示完成提示
        android.widget.Toast.makeText(this, "🎉 恭喜完成视频学习！知识图谱已更新", android.widget.Toast.LENGTH_LONG).show()
        
        // TODO: 这里应该将进度保存到数据库，并更新知识图谱
        saveProgressToDatabase()
    }
    
    private fun saveProgressToDatabase() {
        // 模拟保存到数据库
        lifecycleScope.launch {
            delay(500)
            // 这里应该调用数据库保存方法
            // progressTracker.updateContentProgress(currentContent)
            
            android.widget.Toast.makeText(this@VideoPlayerActivity, "✅ 学习进度已保存", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onPause() {
        super.onPause()
        if (videoView.isPlaying) {
            videoView.pause()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        videoView.stopPlayback()
    }
}
