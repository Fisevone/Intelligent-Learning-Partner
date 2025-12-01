package com.example.educationapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.educationapp.databinding.ItemVoteBinding
import com.example.educationapp.ui.interaction.VoteItem

class VoteAdapter(
    private val onItemClick: (VoteItem) -> Unit
) : ListAdapter<VoteItem, VoteAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemVoteBinding,
        private val onItemClick: (VoteItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(vote: VoteItem) {
            binding.apply {
                tvTitle.text = vote.title
                tvDescription.text = vote.description
                tvEndTime.text = "截止时间: ${vote.endTime}"
                
                // 显示选项
                val optionsText = vote.options.joinToString("\n") { "• $it" }
                tvOptions.text = optionsText
                
                root.setOnClickListener {
                    onItemClick(vote)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<VoteItem>() {
        override fun areItemsTheSame(oldItem: VoteItem, newItem: VoteItem): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: VoteItem, newItem: VoteItem): Boolean {
            return oldItem == newItem
        }
    }
}
