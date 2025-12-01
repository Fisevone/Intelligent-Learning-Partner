package com.example.educationapp.ui.interaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.educationapp.R
import com.example.educationapp.databinding.FragmentQuizBinding
import com.example.educationapp.ui.adapter.QuizAdapter

class QuizFragment : Fragment() {
    
    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var quizAdapter: QuizAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        loadSampleData()
    }
    
    private fun setupRecyclerView() {
        quizAdapter = QuizAdapter { quizItem ->
            Toast.makeText(context, "开始测验：${quizItem.title}", Toast.LENGTH_SHORT).show()
        }
        binding.rvQuizzes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = quizAdapter
        }
    }
    
    private fun loadSampleData() {
        val sampleQuizzes = listOf(
            QuizItem(
                title = "函数基础测验",
                description = "测试你对函数基本概念的理解",
                questionCount = 10,
                timeLimit = 30,
                difficulty = "中等"
            ),
            QuizItem(
                title = "导数应用测验",
                description = "检验导数在实际问题中的应用能力",
                questionCount = 15,
                timeLimit = 45,
                difficulty = "困难"
            ),
            QuizItem(
                title = "三角函数测验",
                description = "测试三角函数的基本性质和计算",
                questionCount = 8,
                timeLimit = 25,
                difficulty = "简单"
            )
        )
        quizAdapter.submitList(sampleQuizzes)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class QuizItem(
    val title: String,
    val description: String,
    val questionCount: Int,
    val timeLimit: Int,
    val difficulty: String
)
