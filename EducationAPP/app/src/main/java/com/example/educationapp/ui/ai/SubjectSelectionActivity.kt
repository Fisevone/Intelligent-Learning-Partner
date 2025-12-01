package com.example.educationapp.ui.ai

import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.educationapp.R

/**
 * 学科选择Activity - 选择AI出题的学科
 */
class SubjectSelectionActivity : AppCompatActivity() {
    
    private val subjects = listOf(
        Subject("数学", "📊", "#FF6B6B", "mathematics"),
        Subject("物理", "⚛️", "#4ECDC4", "physics"),
        Subject("化学", "🧪", "#45B7D1", "chemistry"),
        Subject("生物", "🧬", "#96CEB4", "biology"),
        Subject("语文", "📝", "#FFEAA7", "chinese"),
        Subject("英语", "🌍", "#DDA0DD", "english"),
        Subject("历史", "🏛️", "#F39C12", "history"),
        Subject("地理", "🌎", "#27AE60", "geography")
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject_selection)
        
        setupToolbar()
        setupSubjectCards()
    }
    
    private fun setupToolbar() {
        supportActionBar?.apply {
            title = "选择学科"
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun setupSubjectCards() {
        val cardIds = listOf(
            R.id.card_mathematics,
            R.id.card_physics,
            R.id.card_chemistry,
            R.id.card_biology,
            R.id.card_chinese,
            R.id.card_english,
            R.id.card_history,
            R.id.card_geography
        )
        
        cardIds.forEachIndexed { index, cardId ->
            findViewById<CardView>(cardId)?.setOnClickListener {
                animateCardClick(it as CardView) {
                    startAIQuestions(subjects[index])
                }
            }
        }
    }
    
    private fun animateCardClick(card: CardView, action: () -> Unit) {
        val originalScaleX = card.scaleX
        val originalScaleY = card.scaleY
        
        card.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                card.animate()
                    .scaleX(originalScaleX)
                    .scaleY(originalScaleY)
                    .setDuration(100)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withEndAction {
                        action()
                    }
                    .start()
            }
            .start()
    }
    
    private fun startAIQuestions(subject: Subject) {
        val intent = Intent(this, AISmartQuestionActivity::class.java).apply {
            putExtra("subject", subject.id)
            putExtra("subject_name", subject.name)
            putExtra("subject_emoji", subject.emoji)
        }
        startActivity(intent)
        finish()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    
    data class Subject(
        val name: String,
        val emoji: String,
        val color: String,
        val id: String
    )
}