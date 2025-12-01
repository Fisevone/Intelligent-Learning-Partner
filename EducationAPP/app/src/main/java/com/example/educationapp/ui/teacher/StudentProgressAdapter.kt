package com.example.educationapp.ui.teacher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.educationapp.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator

/**
 * 📊 学生进度适配器
 */
class StudentProgressAdapter(
    private val onStudentClick: (StudentProgressData) -> Unit
) : RecyclerView.Adapter<StudentProgressAdapter.StudentViewHolder>() {

    private var students = listOf<StudentProgressData>()

    fun updateStudents(newStudents: List<StudentProgressData>) {
        students = newStudents
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_progress, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(students[position])
    }

    override fun getItemCount() = students.size

    inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardStudent)
        private val tvName: TextView = itemView.findViewById(R.id.tvStudentName)
        private val tvGrade: TextView = itemView.findViewById(R.id.tvGrade)
        private val tvSubjects: TextView = itemView.findViewById(R.id.tvSubjects)
        private val tvMastery: TextView = itemView.findViewById(R.id.tvMastery)
        private val progressMastery: LinearProgressIndicator = itemView.findViewById(R.id.progressMastery)
        private val tvScore: TextView = itemView.findViewById(R.id.tvScore)
        private val tvStudyTime: TextView = itemView.findViewById(R.id.tvStudyTime)
        private val tvRecordCount: TextView = itemView.findViewById(R.id.tvRecordCount)
        private val tvRecentActivity: TextView = itemView.findViewById(R.id.tvRecentActivity)

        fun bind(data: StudentProgressData) {
            val student = data.student

            tvName.text = student.name
            tvGrade.text = student.grade
            tvSubjects.text = student.subjects

            // 掌握度显示
            val masteryPercent = (data.avgMastery * 100).toInt()
            tvMastery.text = "${masteryPercent}%"
            progressMastery.progress = masteryPercent

            // 根据掌握度设置颜色
            val color = when {
                masteryPercent >= 80 -> android.graphics.Color.parseColor("#4CAF50") // 绿色
                masteryPercent >= 60 -> android.graphics.Color.parseColor("#FF9800") // 橙色
                else -> android.graphics.Color.parseColor("#F44336") // 红色
            }
            progressMastery.setIndicatorColor(color)

            // 平均得分
            tvScore.text = "${String.format("%.1f", data.avgScore)}分"

            // 学习时长
            tvStudyTime.text = formatDuration(data.totalStudyTime)

            // 记录数量
            tvRecordCount.text = "${data.recordCount}条记录"

            // 最近活动
            tvRecentActivity.text = data.recentActivity

            // 点击事件
            cardView.setOnClickListener {
                onStudentClick(data)
            }

            // 根据学习状态设置卡片背景
            when {
                data.recordCount == 0 -> {
                    cardView.strokeColor = android.graphics.Color.parseColor("#F44336")
                    cardView.strokeWidth = 4
                }
                masteryPercent >= 80 -> {
                    cardView.strokeColor = android.graphics.Color.parseColor("#4CAF50")
                    cardView.strokeWidth = 2
                }
                else -> {
                    cardView.strokeColor = android.graphics.Color.parseColor("#E0E0E0")
                    cardView.strokeWidth = 1
                }
            }
        }

        private fun formatDuration(seconds: Long): String {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            return when {
                hours > 0 -> "${hours}h${minutes}m"
                minutes > 0 -> "${minutes}m"
                else -> "${seconds}s"
            }
        }
    }
}

