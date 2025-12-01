package com.example.educationapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.educationapp.R
import com.example.educationapp.data.Resource
import com.example.educationapp.databinding.ItemResourceBinding

class ResourceAdapter : ListAdapter<Resource, ResourceAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemResourceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemResourceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(resource: Resource) {
            binding.apply {
                tvTitle.text = resource.title
                tvSubject.text = resource.subject
                tvDifficulty.text = when (resource.difficulty) {
                    "easy" -> "简单"
                    "medium" -> "中等"
                    "hard" -> "困难"
                    else -> resource.difficulty
                }
                tvDuration.text = "${resource.duration}分钟"
                tvType.text = when (resource.type) {
                    "video" -> "视频"
                    "article" -> "文章"
                    "quiz" -> "测验"
                    "exercise" -> "练习"
                    else -> resource.type
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Resource>() {
        override fun areItemsTheSame(oldItem: Resource, newItem: Resource): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Resource, newItem: Resource): Boolean {
            return oldItem == newItem
        }
    }
}
