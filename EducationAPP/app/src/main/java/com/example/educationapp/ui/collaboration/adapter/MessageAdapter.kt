package com.example.educationapp.ui.collaboration.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.educationapp.R
import com.example.educationapp.ui.collaboration.data.DiscussionMessage
import com.example.educationapp.ui.collaboration.data.MessageType
import java.text.SimpleDateFormat
import java.util.*

/**
 * 讨论消息适配器
 */
class MessageAdapter(
    private val messages: List<DiscussionMessage>,
    private val currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    companion object {
        private const val VIEW_TYPE_MY_MESSAGE = 1
        private const val VIEW_TYPE_OTHER_MESSAGE = 2
        private const val VIEW_TYPE_SYSTEM_MESSAGE = 3
        private const val VIEW_TYPE_AI_MESSAGE = 4
    }
    
    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return when {
            message.type == MessageType.SYSTEM_MESSAGE -> VIEW_TYPE_SYSTEM_MESSAGE
            message.type == MessageType.AI_MESSAGE -> VIEW_TYPE_AI_MESSAGE
            message.senderId == currentUserId -> VIEW_TYPE_MY_MESSAGE
            else -> VIEW_TYPE_OTHER_MESSAGE
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        
        return when (viewType) {
            VIEW_TYPE_MY_MESSAGE -> {
                val view = inflater.inflate(R.layout.item_message_mine, parent, false)
                MyMessageViewHolder(view)
            }
            VIEW_TYPE_OTHER_MESSAGE -> {
                val view = inflater.inflate(R.layout.item_message_other, parent, false)
                OtherMessageViewHolder(view)
            }
            VIEW_TYPE_SYSTEM_MESSAGE -> {
                val view = inflater.inflate(R.layout.item_message_system, parent, false)
                SystemMessageViewHolder(view)
            }
            VIEW_TYPE_AI_MESSAGE -> {
                val view = inflater.inflate(R.layout.item_message_ai, parent, false)
                AIMessageViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        
        when (holder) {
            is MyMessageViewHolder -> holder.bind(message)
            is OtherMessageViewHolder -> holder.bind(message)
            is SystemMessageViewHolder -> holder.bind(message)
            is AIMessageViewHolder -> holder.bind(message)
        }
    }
    
    override fun getItemCount(): Int = messages.size
    
    // 我的消息ViewHolder
    class MyMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        
        fun bind(message: DiscussionMessage) {
            tvContent.text = message.content
            tvTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
        }
    }
    
    // 其他人消息ViewHolder
    class OtherMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSenderName: TextView = itemView.findViewById(R.id.tvSenderName)
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        
        fun bind(message: DiscussionMessage) {
            tvSenderName.text = message.senderName
            tvContent.text = message.content
            tvTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
        }
    }
    
    // 系统消息ViewHolder
    class SystemMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        
        fun bind(message: DiscussionMessage) {
            tvContent.text = message.content
            tvTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
        }
    }
    
    // AI消息ViewHolder
    class AIMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        
        fun bind(message: DiscussionMessage) {
            tvContent.text = message.content
            tvTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
        }
    }
}

