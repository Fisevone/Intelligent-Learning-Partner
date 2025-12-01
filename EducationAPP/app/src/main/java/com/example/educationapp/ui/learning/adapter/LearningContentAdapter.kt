package com.example.educationapp.ui.learning.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.educationapp.R
import com.example.educationapp.data.SimpleLearningContent
import com.example.educationapp.data.SimpleContentType
import com.google.android.material.progressindicator.LinearProgressIndicator

/**
 * 学习内容适配器
 */
class LearningContentAdapter(
    private val onContentClick: (SimpleLearningContent) -> Unit
) : RecyclerView.Adapter<LearningContentAdapter.ContentViewHolder>() {

    private var contents = listOf<SimpleLearningContent>()

    fun updateContent(newContents: List<SimpleLearningContent>) {
        contents = newContents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_learning_content, parent, false)
        return ContentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContentViewHolder, position: Int) {
        holder.bind(contents[position])
    }

    override fun getItemCount() = contents.size

    inner class ContentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardContainer: CardView = itemView.findViewById(R.id.card_container)
        private val ivThumbnail: ImageView = itemView.findViewById(R.id.iv_thumbnail)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
        private val tvType: TextView = itemView.findViewById(R.id.tv_type)
        private val tvDuration: TextView = itemView.findViewById(R.id.tv_duration)
        private val tvDifficulty: TextView = itemView.findViewById(R.id.tv_difficulty)
        private val tvRating: TextView = itemView.findViewById(R.id.tv_rating)
        private val tvViewCount: TextView = itemView.findViewById(R.id.tv_view_count)
        private val progressLearning: LinearProgressIndicator = itemView.findViewById(R.id.progress_learning)
        private val tvProgress: TextView = itemView.findViewById(R.id.tv_progress)

        fun bind(content: SimpleLearningContent) {
            tvTitle.text = content.title
            tvDescription.text = content.description
            tvType.text = "${content.type.icon} ${content.type.displayName}"
            tvDuration.text = "${content.duration}分钟"
            tvDifficulty.text = content.difficulty
            tvRating.text = "⭐ ${String.format("%.1f", content.rating)}"
            tvViewCount.text = "${content.viewCount}人学习"

            // 设置学习进度
            val progressPercent = (content.progress * 100).toInt()
            progressLearning.progress = progressPercent
            
            if (content.progress > 0) {
                tvProgress.text = "${progressPercent}% 已完成"
                tvProgress.visibility = View.VISIBLE
                progressLearning.visibility = View.VISIBLE
            } else {
                tvProgress.visibility = View.GONE
                progressLearning.visibility = View.GONE
            }

            // 设置缩略图（这里使用类型图标）
            val thumbnailRes = when (content.type) {
                SimpleContentType.VIDEO -> R.drawable.ic_play
                SimpleContentType.ARTICLE -> R.drawable.ic_article
                SimpleContentType.EXERCISE -> R.drawable.ic_exercise
                SimpleContentType.QUIZ -> R.drawable.ic_quiz
                SimpleContentType.INTERACTIVE -> R.drawable.ic_interactive
            }
            ivThumbnail.setImageResource(thumbnailRes)

            // 点击事件
            cardContainer.setOnClickListener {
                onContentClick(content)
            }

            // 添加点击动画
            cardContainer.setOnClickListener { view ->
                view.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction {
                        view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .withEndAction {
                                onContentClick(content)
                            }
                    }
            }
        }
    }
}
