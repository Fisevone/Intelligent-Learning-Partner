package com.example.educationapp.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.educationapp.databinding.FragmentCollaborationStudentBinding
import com.example.educationapp.utils.PreferenceManager
import com.example.educationapp.data.User
import com.example.educationapp.data.UserType
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class StudentCollaborationFragment : Fragment() {
    private var _binding: FragmentCollaborationStudentBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var preferenceManager: PreferenceManager
    
    // 学生协作数据
    private var myGroupName = "第1组"
    private var myRole = "讨论员"
    private var groupMembers = listOf("张三", "李四", "王五", "我")
    private var currentTask = "完成数学函数综合练习，小组讨论解题思路"
    private var remainingTime = "25分钟"
    private var taskProgress = 60
    private var mySpeechCount = 12
    private var myActivity = 85
    private var myContribution = 78
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCollaborationStudentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeComponents()
        setupUI()
        setupClickListeners()
        loadStudentData()
    }
    
    private fun initializeComponents() {
        preferenceManager = PreferenceManager(requireContext())
    }
    
    private fun setupUI() {
        binding.apply {
            // 设置小组信息
            tvGroupName.text = myGroupName
            tvGroupMembers.text = groupMembers.joinToString("、")
            tvMyRole.text = myRole
            
            // 设置当前任务
            tvCurrentTask.text = currentTask
            tvRemainingTime.text = remainingTime
            tvTaskProgress.text = "${taskProgress}%"
            
            // 设置参与情况
            tvMySpeechCount.text = mySpeechCount.toString()
            tvMyActivity.text = "${myActivity}%"
            tvMyContribution.text = "${myContribution}%"
            
            // 设置反馈信息
            tvParticipationFeedback.text = generateParticipationFeedback()
            
            // 设置活动状态
            tvActivityStatus.text = "💬 等待开始讨论"
        }
    }
    
    private fun setupClickListeners() {
        binding.apply {
            // 小组讨论
            btnGroupDiscussion.setOnClickListener {
                startGroupDiscussion()
            }
            
            // 小组竞赛
            btnGroupQuiz.setOnClickListener {
                startGroupQuiz()
            }
            
            // 同伴评价
            btnPeerReview.setOnClickListener {
                startPeerReview()
            }
        }
    }
    
    private fun loadStudentData() {
        // 模拟加载学生数据
        lifecycleScope.launch {
            // 这里可以从数据库或API加载真实数据
            updateUIWithRealTimeData()
        }
    }
    
    private fun startGroupDiscussion() {
        // 跳转到小组讨论界面
        val intent = android.content.Intent(requireContext(), com.example.educationapp.ui.collaboration.GroupDiscussionActivity::class.java)
        startActivity(intent)
    }
    
    private fun startGroupQuiz() {
        // 跳转到小组竞赛界面
        val intent = android.content.Intent(requireContext(), com.example.educationapp.ui.collaboration.GroupQuizActivity::class.java)
        startActivity(intent)
    }
    
    private fun startPeerReview() {
        binding.tvActivityStatus.text = "📝 开始同伴评价..."
        Toast.makeText(requireContext(), "请为小组成员打分", Toast.LENGTH_SHORT).show()
        
        lifecycleScope.launch {
            delay(2000)
            binding.tvActivityStatus.text = "📝 评价完成，等待其他成员"
            
            // 更新贡献度
            myContribution += 2
            binding.tvMyContribution.text = "${myContribution}%"
        }
    }
    
    private fun updateUIWithRealTimeData() {
        lifecycleScope.launch {
            while (true) {
                delay(30000) // 每30秒更新一次
                
                // 模拟实时数据更新
                if (taskProgress < 100) {
                    taskProgress += (1..3).random()
                    binding.tvTaskProgress.text = "${taskProgress.coerceAtMost(100)}%"
                }
                
                // 更新剩余时间
                updateRemainingTime()
                
                // 随机更新活跃度
                if ((1..10).random() > 7) {
                    myActivity = (myActivity + (-2..3).random()).coerceIn(0, 100)
                    binding.tvMyActivity.text = "${myActivity}%"
                    
                    // 更新反馈
                    binding.tvParticipationFeedback.text = generateParticipationFeedback()
                }
            }
        }
    }
    
    private fun updateRemainingTime() {
        // 简单的时间倒计时逻辑
        val currentMinutes = remainingTime.replace("分钟", "").toIntOrNull() ?: 0
        if (currentMinutes > 0) {
            val newMinutes = (currentMinutes - 1).coerceAtLeast(0)
            remainingTime = "${newMinutes}分钟"
            binding.tvRemainingTime.text = remainingTime
            
            if (newMinutes == 0) {
                binding.tvActivityStatus.text = "⏰ 任务时间已到"
                Toast.makeText(requireContext(), "小组任务时间结束", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun generateParticipationFeedback(): String {
        return when {
            myActivity >= 90 -> "🌟 你在小组中表现非常积极，是小组的核心成员！"
            myActivity >= 80 -> "💡 你在小组中表现积极，建议多主动提出想法"
            myActivity >= 70 -> "👍 你的参与度不错，可以更多地与同伴交流"
            myActivity >= 60 -> "📢 建议更积极地参与小组讨论"
            else -> "🔔 需要提高参与度，多与小组成员互动"
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
