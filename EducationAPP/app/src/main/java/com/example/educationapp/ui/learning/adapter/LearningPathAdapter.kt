package com.example.educationapp.ui.learning.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.educationapp.R
import com.example.educationapp.data.SimpleLearningPath
import com.google.android.material.progressindicator.LinearProgressIndicator

/**
 * 学习路径适配器
 */
class LearningPathAdapter(
    private val onPathClick: (SimpleLearningPath) -> Unit
) : RecyclerView.Adapter<LearningPathAdapter.PathViewHolder>() {

    private var paths = listOf<SimpleLearningPath>()

    fun updatePaths(newPaths: List<SimpleLearningPath>) {
        paths = newPaths
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PathViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_learning_path, parent, false)
        return PathViewHolder(view)
    }

    override fun onBindViewHolder(holder: PathViewHolder, position: Int) {
        holder.bind(paths[position])
    }

    override fun getItemCount() = paths.size

    inner class PathViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardContainer: CardView = itemView.findViewById(R.id.card_container)
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
        private val tvSubject: TextView = itemView.findViewById(R.id.tv_subject)
        private val tvDuration: TextView = itemView.findViewById(R.id.tv_duration)
        private val tvDifficulty: TextView = itemView.findViewById(R.id.tv_difficulty)
        private val progressPath: LinearProgressIndicator = itemView.findViewById(R.id.progress_path)
        private val tvProgress: TextView = itemView.findViewById(R.id.tv_progress)
        private val tvContentCount: TextView = itemView.findViewById(R.id.tv_content_count)

        fun bind(path: SimpleLearningPath) {
            tvTitle.text = path.title
            tvDescription.text = path.description
            tvSubject.text = "📚 ${path.subject}"
            tvDuration.text = "⏱️ ${path.estimatedDuration}小时"
            tvDifficulty.text = path.difficulty
            tvContentCount.text = "${path.contentCount}个内容"

            // 设置难度颜色
            val difficultyColor = when (path.difficulty) {
                "入门" -> "#4CAF50"
                "中级" -> "#FF9800"
                "高级" -> "#F44336"
                "专家" -> "#9C27B0"
                else -> "#666666"
            }
            tvDifficulty.setTextColor(Color.parseColor(difficultyColor))

            // 设置完成进度
            val progressPercent = (path.completionRate * 100).toInt()
            progressPath.progress = progressPercent
            tvProgress.text = "${progressPercent}% 完成"

            // 根据完成进度设置卡片样式
            when {
                path.completionRate == 0f -> {
                    // 未开始 - 蓝色边框
                    cardContainer.setCardBackgroundColor(Color.parseColor("#E3F2FD"))
                }
                path.completionRate < 1f -> {
                    // 进行中 - 橙色边框
                    cardContainer.setCardBackgroundColor(Color.parseColor("#FFF3E0"))
                }
                else -> {
                    // 已完成 - 绿色边框
                    cardContainer.setCardBackgroundColor(Color.parseColor("#E8F5E8"))
                }
            }

            // 点击事件
            cardContainer.setOnClickListener {
                // 添加点击动画
                it.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction {
                        it.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .withEndAction {
                                onPathClick(path)
                            }
                    }
            }
        }
    }
}
