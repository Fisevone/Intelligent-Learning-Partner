package com.example.educationapp.ui.interaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.educationapp.R
import com.example.educationapp.databinding.FragmentVoteBinding
import com.example.educationapp.ui.adapter.VoteAdapter

class VoteFragment : Fragment() {
    
    private var _binding: FragmentVoteBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var voteAdapter: VoteAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVoteBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadSampleData()
    }
    
    private fun setupRecyclerView() {
        voteAdapter = VoteAdapter { voteItem ->
            Toast.makeText(context, "已投票：${voteItem.title}", Toast.LENGTH_SHORT).show()
        }
        binding.rvVotes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = voteAdapter
        }
    }
    
    private fun loadSampleData() {
        val sampleVotes = listOf(
            VoteItem(
                title = "最喜欢的数学学习方法",
                description = "请选择你最喜欢的数学学习方法",
                options = listOf("视频学习", "练习做题", "讨论交流", "自主学习"),
                endTime = "2024-01-20 18:00"
            ),
            VoteItem(
                title = "希望增加的课程内容",
                description = "你希望在下学期增加哪些课程内容？",
                options = listOf("编程基础", "数据分析", "人工智能", "机器学习"),
                endTime = "2024-01-25 12:00"
            )
        )
        voteAdapter.submitList(sampleVotes)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class VoteItem(
    val title: String,
    val description: String,
    val options: List<String>,
    val endTime: String
)
