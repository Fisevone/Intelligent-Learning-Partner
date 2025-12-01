package com.example.educationapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.educationapp.databinding.ItemQaBinding
import com.example.educationapp.ui.interaction.QAItem

class QAAdapter : ListAdapter<QAItem, QAAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemQaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemQaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(qaItem: QAItem) {
            binding.apply {
                tvQuestion.text = qaItem.question
                tvAnswer.text = qaItem.answer
                tvAsker.text = qaItem.asker
                tvAnswerer.text = qaItem.answerer
                tvTimestamp.text = qaItem.timestamp
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<QAItem>() {
        override fun areItemsTheSame(oldItem: QAItem, newItem: QAItem): Boolean {
            return oldItem.question == newItem.question
        }

        override fun areContentsTheSame(oldItem: QAItem, newItem: QAItem): Boolean {
            return oldItem == newItem
        }
    }
}
