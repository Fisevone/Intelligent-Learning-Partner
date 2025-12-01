package com.example.educationapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.educationapp.R
import com.example.educationapp.data.LearningRecord
import com.example.educationapp.databinding.ItemRecentLearningBinding
import java.text.SimpleDateFormat
import java.util.*

class RecentLearningAdapter : ListAdapter<LearningRecord, RecentLearningAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentLearningBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemRecentLearningBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(record: LearningRecord) {
            binding.apply {
                tvSubject.text = record.subject
                tvTopic.text = record.topic
                tvDuration.text = "${record.duration}分钟"
                tvScore.text = "${record.score}"
                
                val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                tvTimestamp.text = dateFormat.format(Date(record.timestamp))
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<LearningRecord>() {
        override fun areItemsTheSame(oldItem: LearningRecord, newItem: LearningRecord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LearningRecord, newItem: LearningRecord): Boolean {
            return oldItem == newItem
        }
    }
}
