package com.example.educationapp.ui.ai

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.educationapp.R
import com.example.educationapp.ai.AILearningAssistant
import com.example.educationapp.data.EducationDatabase
import com.example.educationapp.databinding.ActivitySmartAnalysisBinding
import com.example.educationapp.utils.PreferenceManager
import kotlinx.coroutines.launch

class SmartAnalysisActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySmartAnalysisBinding
    private lateinit var aiAssistant: AILearningAssistant
    private lateinit var preferenceManager: PreferenceManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySmartAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupAI()
        loadAnalysis()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "智能学习分析"
    }
    
    private fun setupAI() {
        aiAssistant = AILearningAssistant()
        preferenceManager = PreferenceManager(this)
    }
    
    private fun loadAnalysis() {
        val user = preferenceManager.getUser()
        if (user != null) {
            lifecycleScope.launch {
                try {
                    val database = EducationDatabase.getDatabase(this@SmartAnalysisActivity)
                    val learningRecords = database.learningRecordDao().getLearningRecordsByUser(user.id)
                    
                    // 显示加载状态
                    showLoadingState()
                    
                    // 获取学习记录
                    learningRecords.collect { records ->
                        if (records.isNotEmpty()) {
                            // 生成AI分析
                            generateAIAnalysis(user, records)
                        } else {
                            showEmptyState()
                        }
                    }
                } catch (e: Exception) {
                    showErrorState()
                }
            }
        }
    }
    
    private suspend fun generateAIAnalysis(user: com.example.educationapp.data.User, records: List<com.example.educationapp.data.LearningRecord>) {
        // 学习建议分析
        aiAssistant.generateLearningAdvice(user, records).collect { advice ->
            updateAdviceSection(advice)
        }
        
        // 错题分析
        aiAssistant.analyzeMistakes(records).collect { mistakeAnalysis ->
            updateMistakeSection(mistakeAnalysis)
        }
        
        // 学习情绪分析
        aiAssistant.analyzeLearningMood(user, records).collect { mood ->
            updateMoodSection(mood)
        }
        
        // 隐藏加载状态
        hideLoadingState()
    }
    
    private fun updateAdviceSection(advice: com.example.educationapp.ai.LearningAdvice) {
        binding.apply {
            tvCurrentLevel.text = advice.currentLevel
            tvMotivationalMessage.text = advice.motivationalMessage
            
            // 优势
            val strengthsText = advice.strengths.joinToString("、")
            tvStrengths.text = strengthsText
            
            // 需要改进的地方
            val weaknessesText = advice.weaknesses.joinToString("、")
            tvWeaknesses.text = weaknessesText
            
            // 建议
            val recommendationsText = advice.recommendations.joinToString("\n• ", "• ")
            tvRecommendations.text = recommendationsText
            
            // 下一步
            val nextStepsText = advice.nextSteps.joinToString("\n• ", "• ")
            tvNextSteps.text = nextStepsText
        }
    }
    
    private fun updateMistakeSection(mistakeAnalysis: com.example.educationapp.ai.MistakeAnalysis) {
        binding.apply {
            // 常见错误
            val commonMistakesText = mistakeAnalysis.commonMistakes.joinToString("\n") { mistake ->
                "• ${mistake.type} (${(mistake.frequency * 100).toInt()}%)"
            }
            tvCommonMistakes.text = commonMistakesText
            
            // 根本原因
            val rootCausesText = mistakeAnalysis.rootCauses.joinToString("\n• ", "• ")
            tvRootCauses.text = rootCausesText
            
            // 改进策略
            val strategiesText = mistakeAnalysis.improvementStrategies.joinToString("\n• ", "• ")
            tvImprovementStrategies.text = strategiesText
            
            // 练习建议
            val practiceText = mistakeAnalysis.practiceRecommendations.joinToString("\n• ", "• ")
            tvPracticeRecommendations.text = practiceText
        }
    }
    
    private fun updateMoodSection(mood: com.example.educationapp.ai.LearningMood) {
        binding.apply {
            tvCurrentMood.text = mood.currentMood
            tvMoodTrend.text = mood.moodTrend
            tvStressLevel.text = mood.stressLevel
            tvEngagementLevel.text = mood.engagementLevel
            
            // 情绪建议
            val moodRecommendationsText = mood.recommendations.joinToString("\n• ", "• ")
            tvMoodRecommendations.text = moodRecommendationsText
        }
    }
    
    private fun showLoadingState() {
        binding.apply {
            loadingContainer.visibility = View.VISIBLE
            contentContainer.visibility = View.GONE
            emptyStateContainer.visibility = View.GONE
            errorStateContainer.visibility = View.GONE
        }
    }
    
    private fun hideLoadingState() {
        binding.apply {
            loadingContainer.visibility = View.GONE
            contentContainer.visibility = View.VISIBLE
        }
    }
    
    private fun showEmptyState() {
        binding.apply {
            loadingContainer.visibility = View.GONE
            contentContainer.visibility = View.GONE
            emptyStateContainer.visibility = View.VISIBLE
            errorStateContainer.visibility = View.GONE
        }
    }
    
    private fun showErrorState() {
        binding.apply {
            loadingContainer.visibility = View.GONE
            contentContainer.visibility = View.GONE
            emptyStateContainer.visibility = View.GONE
            errorStateContainer.visibility = View.VISIBLE
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
