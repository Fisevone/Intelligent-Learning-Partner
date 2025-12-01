package com.example.educationapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.educationapp.databinding.ItemDiscussionBinding
import com.example.educationapp.ui.interaction.DiscussionItem

class DiscussionAdapter : ListAdapter<DiscussionItem, DiscussionAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDiscussionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemDiscussionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(discussion: DiscussionItem) {
            binding.apply {
                tvTitle.text = discussion.title
                tvContent.text = discussion.content
                tvAuthor.text = discussion.author
                tvTimestamp.text = discussion.timestamp
                tvLikeCount.text = discussion.likeCount.toString()
                tvCommentCount.text = discussion.commentCount.toString()
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<DiscussionItem>() {
        override fun areItemsTheSame(oldItem: DiscussionItem, newItem: DiscussionItem): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: DiscussionItem, newItem: DiscussionItem): Boolean {
            return oldItem == newItem
        }
    }
}
