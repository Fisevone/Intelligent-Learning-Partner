package com.example.educationapp.ui.interaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.educationapp.R
import com.example.educationapp.databinding.FragmentQaBinding
import com.example.educationapp.ui.adapter.QAAdapter

class QAFragment : Fragment() {
    
    private var _binding: FragmentQaBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var qaAdapter: QAAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQaBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupClickListeners()
        loadSampleData()
    }
    
    private fun setupRecyclerView() {
        qaAdapter = QAAdapter()
        binding.rvQuestions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = qaAdapter
        }
    }
    
    private fun setupClickListeners() {
        binding.btnAskQuestion.setOnClickListener {
            val question = binding.etQuestion.text.toString().trim()
            if (question.isNotEmpty()) {
                // 这里可以添加提问逻辑
                Toast.makeText(context, "问题已提交", Toast.LENGTH_SHORT).show()
                binding.etQuestion.text?.clear()
            } else {
                Toast.makeText(context, "请输入问题", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun loadSampleData() {
        val sampleQuestions = listOf(
            QAItem(
                question = "如何理解函数的单调性？",
                answer = "函数的单调性是指函数在定义域内，随着自变量的增大，函数值的变化趋势...",
                asker = "学生A",
                answerer = "张老师",
                timestamp = "2024-01-15 14:30"
            ),
            QAItem(
                question = "导数的几何意义是什么？",
                answer = "导数的几何意义是函数图像在某点处的切线斜率...",
                asker = "学生B",
                answerer = "李老师",
                timestamp = "2024-01-15 15:20"
            ),
            QAItem(
                question = "如何求解复合函数的导数？",
                answer = "复合函数求导需要使用链式法则，即外函数的导数乘以内函数的导数...",
                asker = "学生C",
                answerer = "王老师",
                timestamp = "2024-01-15 16:10"
            )
        )
        qaAdapter.submitList(sampleQuestions)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class QAItem(
    val question: String,
    val answer: String,
    val asker: String,
    val answerer: String,
    val timestamp: String
)
