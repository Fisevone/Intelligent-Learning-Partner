package com.example.educationapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.educationapp.databinding.ItemQuizBinding
import com.example.educationapp.ui.interaction.QuizItem

class QuizAdapter(
    private val onItemClick: (QuizItem) -> Unit
) : ListAdapter<QuizItem, QuizAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQuizBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemQuizBinding,
        private val onItemClick: (QuizItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(quiz: QuizItem) {
            binding.apply {
                tvTitle.text = quiz.title
                tvDescription.text = quiz.description
                tvQuestionCount.text = "题目数量: ${quiz.questionCount}"
                tvTimeLimit.text = "时间限制: ${quiz.timeLimit}分钟"
                tvDifficulty.text = "难度: ${quiz.difficulty}"
                
                root.setOnClickListener {
                    onItemClick(quiz)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<QuizItem>() {
        override fun areItemsTheSame(oldItem: QuizItem, newItem: QuizItem): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: QuizItem, newItem: QuizItem): Boolean {
            return oldItem == newItem
        }
    }
}
