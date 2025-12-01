package com.example.educationapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.educationapp.R
import com.example.educationapp.data.Recommendation
import com.example.educationapp.databinding.ItemRecommendationBinding

class RecommendationAdapter(
    private val onItemClick: (Recommendation) -> Unit
) : ListAdapter<Recommendation, RecommendationAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecommendationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemRecommendationBinding,
        private val onItemClick: (Recommendation) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(recommendation: Recommendation) {
            binding.apply {
                tvRecommendation.text = recommendation.reason
                
                root.setOnClickListener {
                    onItemClick(recommendation)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Recommendation>() {
        override fun areItemsTheSame(oldItem: Recommendation, newItem: Recommendation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Recommendation, newItem: Recommendation): Boolean {
            return oldItem == newItem
        }
    }
}
