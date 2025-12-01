package com.example.educationapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.educationapp.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * 简化的AI聊天适配器 - 支持用户和AI消息
 */
class AIChatAdapter(
    private val messages: MutableList<ChatMessage>
) : RecyclerView.Adapter<AIChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val layoutUserMessage: LinearLayout = itemView.findViewById(R.id.layoutUserMessage)
        private val layoutAiMessage: LinearLayout = itemView.findViewById(R.id.layoutAiMessage)
        private val tvUserMessage: TextView = itemView.findViewById(R.id.tvUserMessage)
        private val tvAiMessage: TextView = itemView.findViewById(R.id.tvAiMessage)
        private val tvTimestamp: TextView = itemView.findViewById(R.id.tvTimestamp)

        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(message: ChatMessage) {
            if (message.isUser) {
                // 显示用户消息
                layoutUserMessage.visibility = View.VISIBLE
                layoutAiMessage.visibility = View.GONE
                
                tvUserMessage.text = message.text
            } else {
                // 显示AI消息
                layoutUserMessage.visibility = View.GONE
                layoutAiMessage.visibility = View.VISIBLE
                
                tvAiMessage.text = message.text
            }
            
            tvTimestamp.text = timeFormat.format(Date(message.timestamp))
        }
    }
}

/**
 * 聊天消息数据类
 */
data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
