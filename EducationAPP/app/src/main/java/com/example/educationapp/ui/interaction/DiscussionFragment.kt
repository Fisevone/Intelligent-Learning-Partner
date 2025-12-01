package com.example.educationapp.ui.interaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.educationapp.R
import com.example.educationapp.databinding.FragmentDiscussionBinding
import com.example.educationapp.ui.adapter.DiscussionAdapter

class DiscussionFragment : Fragment() {
    
    private var _binding: FragmentDiscussionBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var discussionAdapter: DiscussionAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDiscussionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupClickListeners()
        loadSampleData()
    }
    
    private fun setupRecyclerView() {
        discussionAdapter = DiscussionAdapter()
        binding.rvDiscussions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = discussionAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.btnPostDiscussion.setOnClickListener {
            val content = binding.etDiscussion.text.toString().trim()
            if (content.isNotEmpty()) {
                Toast.makeText(context, "讨论已发布", Toast.LENGTH_SHORT).show()
                binding.etDiscussion.text?.clear()
            } else {
                Toast.makeText(context, "请输入讨论内容", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun loadSampleData() {
        val sampleDiscussions = listOf(
            DiscussionItem(
                title = "函数图像变换的规律",
                content = "大家觉得函数图像变换有什么规律吗？我总结了几点...",
                author = "学生A",
                timestamp = "2024-01-15 14:30",
                likeCount = 12,
                commentCount = 5
            ),
            DiscussionItem(
                title = "导数在实际生活中的应用",
                content = "导数不仅在数学中有用，在生活中也有很多应用，比如...",
                author = "学生B",
                timestamp = "2024-01-15 15:20",
                likeCount = 8,
                commentCount = 3
            ),
            DiscussionItem(
                title = "如何提高数学解题速度",
                content = "最近做题速度比较慢，大家有什么好的方法吗？",
                author = "学生C",
                timestamp = "2024-01-15 16:10",
                likeCount = 15,
                commentCount = 8
            )
        )
        discussionAdapter.submitList(sampleDiscussions)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class DiscussionItem(
    val title: String,
    val content: String,
    val author: String,
    val timestamp: String,
    val likeCount: Int,
    val commentCount: Int
)
