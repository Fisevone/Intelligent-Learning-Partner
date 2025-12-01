package com.example.educationapp.ui.knowledge

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.educationapp.R
import com.example.educationapp.ai.AIKnowledgeGraphBuilder
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

/**
 * 🕸️ 真正的可视化知识图谱界面
 */
class KnowledgeGraphActivity : AppCompatActivity() {
    
    private lateinit var knowledgeGraphView: KnowledgeGraphView
    private val knowledgeGraphBuilder = AIKnowledgeGraphBuilder()
    
    // Toast防抖机制
    private var lastToastTime = 0L
    private val TOAST_DEBOUNCE_INTERVAL = 2000L // 2秒内不重复显示Toast
    
    /**
     * 防抖Toast - 避免频繁显示相同消息
     */
    private fun showDebouncedToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastToastTime > TOAST_DEBOUNCE_INTERVAL) {
            Toast.makeText(this, message, duration).show()
            lastToastTime = currentTime
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knowledge_graph)
        
        initViews()
        setupClickListeners()
        
        // 生成示例知识图谱
        generateSampleKnowledgeGraph()
    }
    
    private fun initViews() {
        knowledgeGraphView = findViewById(R.id.knowledgeGraphView)
        
        // 设置工具栏
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }
    
    private fun setupClickListeners() {
        // 返回按钮
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar).setNavigationOnClickListener {
            finish()
        }
        
        // 刷新按钮
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabRefresh).setOnClickListener {
            showDebouncedToast("🔄 刷新知识图谱")
            generateSampleKnowledgeGraph()
        }
        
        // 居中按钮
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabCenter).setOnClickListener {
            showDebouncedToast("📍 居中显示")
            knowledgeGraphView.centerGraph()
        }
        
        // 缩放按钮
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.fabZoom).setOnClickListener {
            showDebouncedToast("🔍 调整缩放")
            knowledgeGraphView.toggleZoom()
        }
    }
    
    private fun generateSampleKnowledgeGraph() {
        // 直接使用年级适配的知识图谱，确保显示正确内容
        android.util.Log.d("KnowledgeGraph", "直接使用年级适配的知识图谱")
                    generateFallbackKnowledgeGraph()
    }
    
    private fun generateFallbackKnowledgeGraph() {
        // 根据用户年级生成合适的知识图谱
        val preferenceManager = com.example.educationapp.utils.PreferenceManager(this)
        val userGrade = preferenceManager.getUserGrade() ?: "七年级"
        
        val sampleNodes = when {
            userGrade.contains("七年级") || userGrade.contains("初一") -> getGrade7KnowledgeNodes()
            userGrade.contains("八年级") || userGrade.contains("初二") -> getGrade8KnowledgeNodes()
            userGrade.contains("九年级") || userGrade.contains("初三") -> getGrade9KnowledgeNodes()
            userGrade.contains("高一") -> getHighSchool1KnowledgeNodes()
            userGrade.contains("高二") -> getHighSchool2KnowledgeNodes()
            userGrade.contains("高三") -> getHighSchool3KnowledgeNodes()
            userGrade.contains("大学") -> getUniversityKnowledgeNodes()
            else -> getGrade7KnowledgeNodes() // 默认七年级
        }
        
        val sampleEdges = getKnowledgeEdgesForGrade(userGrade)
        
        knowledgeGraphView.setGraphData(sampleNodes, sampleEdges)
    }
    
    /**
     * 🎯 显示节点详细信息
     */
    fun showNodeDetails(node: KnowledgeNode) {
        val statusText = when (node.type) {
            NodeType.MASTERED -> "✅ 已掌握"
            NodeType.LEARNING -> "📚 学习中"
            NodeType.TODO -> "🎯 待学习"
            NodeType.LOCKED -> "🔒 未解锁"
        }
        
        val message = """
            📖 知识点: ${node.name}
            
            📊 掌握程度: ${(node.masteryLevel * 100).toInt()}%
            
            🎯 状态: $statusText
            
            💡 建议: ${getStudyAdvice(node)}
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🧠 知识点详情")
            .setMessage(message)
            .setPositiveButton("开始学习") { _, _ ->
                startStudying(node)
            }
            .setNegativeButton("关闭", null)
            .show()
    }
    
    private fun getStudyAdvice(node: KnowledgeNode): String {
        return when {
            node.masteryLevel >= 0.8f -> "继续保持，可以挑战更高难度的题目"
            node.masteryLevel >= 0.5f -> "正在进步中，建议多做练习巩固理解"
            node.masteryLevel >= 0.2f -> "需要更多时间学习，建议从基础开始"
            else -> "建议先学习前置知识点，为学习此内容做准备"
        }
    }
    
    private fun startStudying(node: KnowledgeNode) {
        when {
            // 如果是已掌握的知识点，进行强化练习
            node.type == NodeType.MASTERED -> {
                showDebouncedToast("🎯 开始「${node.name}」强化练习")
                startAIQuestionPractice(node.name, "高级")
            }
            
            // 如果是学习中的知识点，继续练习
            node.type == NodeType.LEARNING -> {
                showDebouncedToast("📚 继续学习「${node.name}」")
                startAIQuestionPractice(node.name, "中级")
            }
            
            // 如果是待学习的知识点，从基础开始
            node.type == NodeType.TODO -> {
                showDebouncedToast("🌟 开始学习「${node.name}」基础知识")
                // 可以跳转到学习资料或基础练习
                startLearningMaterials(node.name)
            }
            
            // 如果是未解锁的知识点，提示学习前置条件
            node.type == NodeType.LOCKED -> {
                showDebouncedToast("🔒 请先完成前置知识点的学习")
                // 可以显示前置条件或引导用户
                showPrerequisites(node.name)
            }
        }
    }
    
    /**
     * 启动AI题目练习
     */
    private fun startAIQuestionPractice(knowledgePoint: String, difficulty: String) {
        try {
            val intent = android.content.Intent(this, com.example.educationapp.ui.ai.SubjectSelectionActivity::class.java)
            intent.putExtra("knowledge_point", knowledgePoint)
            intent.putExtra("difficulty", difficulty)
            intent.putExtra("focus_mode", true) // 专注于特定知识点
            startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("KnowledgeGraph", "启动AI练习失败: ${e.message}")
            showDebouncedToast("启动AI练习时出现问题，请稍后重试")
        }
    }
    
    /**
     * 启动学习资料 - 根据知识点类型提供不同的学习内容
     */
    private fun startLearningMaterials(knowledgePoint: String) {
        try {
            val intent = android.content.Intent(this, com.example.educationapp.ui.learning.LearningZoneActivity::class.java)
            val materialType = getLearningMaterialType(knowledgePoint)
            
            intent.putExtra("knowledge_point", knowledgePoint)
            intent.putExtra("content_type", materialType)
            intent.putExtra("learning_goal", "掌握「$knowledgePoint」的基础概念和应用")
            intent.putExtra("from_knowledge_graph", true)
            
            // 根据知识点推荐学习方式
            val recommendedLearningMethod = when {
                knowledgePoint.contains("基础") -> "video_first" // 基础概念优先看视频
                knowledgePoint.contains("应用") -> "practice_first" // 应用类优先做练习
                knowledgePoint.contains("运算") -> "interactive_first" // 运算类优先交互学习
                else -> "balanced" // 平衡学习
            }
            intent.putExtra("learning_method", recommendedLearningMethod)
            
            startActivity(intent)
            
            // 记录学习路径
            recordLearningPathEntry(knowledgePoint, materialType)
            
        } catch (e: Exception) {
            android.util.Log.e("KnowledgeGraph", "启动学习专区失败: ${e.message}")
            showDebouncedToast("启动学习资料时出现问题，请稍后重试")
        }
    }
    
    /**
     * 记录学习路径，用于个性化推荐
     */
    private fun recordLearningPathEntry(knowledgePoint: String, materialType: String) {
        android.util.Log.d("LearningPath", "用户从知识图谱开始学习: $knowledgePoint (类型: $materialType)")
        
        // 这里可以记录到数据库或分析系统
        // 用于后续的个性化学习推荐
    }
    
    /**
     * 显示前置条件并提供智能学习路径
     */
    private fun showPrerequisites(knowledgePoint: String) {
        val prerequisites = getPrerequisitesForKnowledge(knowledgePoint)
        
        // 检查是否所有前置条件都是基础知识点
        val hasBasicPrerequisites = prerequisites.any { isBasicKnowledgePoint(it) }
        
        val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔒 学习前置条件")
            .setMessage("要学习「$knowledgePoint」，需要先掌握以下知识点：\n\n${prerequisites.joinToString("\n• ", "• ")}")
            .setNegativeButton("关闭", null)
        
        if (hasBasicPrerequisites) {
            // 如果有基础知识点，提供直接学习选项
            dialogBuilder
                .setPositiveButton("开始基础学习") { _, _ ->
                    startBasicLearningPath(prerequisites)
                }
                .setNeutralButton("查看知识点关系") { _, _ ->
                    highlightPrerequisiteNodes(prerequisites)
                }
        } else {
            // 如果没有基础知识点，只提供查看选项
            dialogBuilder
                .setPositiveButton("查看前置知识点") { _, _ ->
                    highlightPrerequisiteNodes(prerequisites)
                }
        }
        
        dialogBuilder.show()
    }
    
    /**
     * 判断是否为基础知识点（没有前置条件或前置条件很少）
     */
    private fun isBasicKnowledgePoint(knowledgePoint: String): Boolean {
        val prereqs = getPrerequisitesForKnowledge(knowledgePoint)
        return prereqs.size <= 1 || prereqs.contains("基础数学知识")
    }
    
    /**
     * 开始基础学习路径 - 从最基础的知识点开始
     */
    private fun startBasicLearningPath(prerequisites: List<String>) {
        // 找到最基础的知识点（前置条件最少的）
        val basicKnowledgePoint = prerequisites.minByOrNull { 
            getPrerequisitesForKnowledge(it).size 
        } ?: prerequisites.first()
        
        showDebouncedToast("🌟 开始学习最基础的知识点：「$basicKnowledgePoint」")
        
        // 高亮显示学习路径
        highlightLearningPath(basicKnowledgePoint, prerequisites)
        
        // 延迟2秒后跳转到学习专区，让用户看到高亮效果
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            startLearningMaterials(basicKnowledgePoint)
        }, 2000)
    }
    
    /**
     * 高亮显示完整学习路径
     */
    private fun highlightLearningPath(startPoint: String, allPoints: List<String>) {
        // 创建学习路径顺序
        val learningPath = createLearningPathOrder(startPoint, allPoints)
        
        showDebouncedToast("📚 推荐学习顺序：${learningPath.joinToString(" → ")}", Toast.LENGTH_LONG)
        
        // 高亮显示所有相关节点
        highlightPrerequisiteNodes(learningPath)
        
        // 显示学习路径指导
        showLearningPathGuidance(learningPath)
    }
    
    /**
     * 创建智能学习路径顺序
     */
    private fun createLearningPathOrder(startPoint: String, allPoints: List<String>): List<String> {
        val sortedPoints = allPoints.sortedBy { getPrerequisitesForKnowledge(it).size }
        
        // 确保起始点在最前面
        val result = mutableListOf<String>()
        if (sortedPoints.contains(startPoint)) {
            result.add(startPoint)
            result.addAll(sortedPoints.filter { it != startPoint })
        } else {
            result.add(startPoint)
            result.addAll(sortedPoints)
        }
        
        return result
    }
    
    /**
     * 显示学习路径指导对话框
     */
    private fun showLearningPathGuidance(learningPath: List<String>) {
        val pathDescription = learningPath.mapIndexed { index, point ->
            "${index + 1}. $point"
        }.joinToString("\n")
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("📚 智能学习路径")
            .setMessage("系统为您规划了最优学习路径：\n\n$pathDescription\n\n建议按此顺序逐步学习，每个知识点掌握后再进入下一个。")
            .setPositiveButton("开始第一步") { _, _ ->
                startLearningMaterials(learningPath.first())
            }
            .setNegativeButton("我知道了", null)
            .show()
    }
    
    /**
     * 获取知识点的前置条件 - 根据年级适配的知识依赖图谱
     */
    private fun getPrerequisitesForKnowledge(knowledgePoint: String): List<String> {
        return when (knowledgePoint) {
            // 七年级（初一）数学
            "有理数运算" -> listOf("有理数")
            "整式" -> listOf("有理数运算")
            "整式加减" -> listOf("整式")
            "一元一次方程" -> listOf("整式加减", "有理数运算")
            "几何图形初步" -> listOf("有理数")
            "相交线与平行线" -> listOf("几何图形初步")
            "实数" -> listOf("有理数", "一元一次方程")
            
            // 八年级（初二）数学
            "全等三角形" -> listOf("三角形")
            "轴对称" -> listOf("三角形")
            "勾股定理" -> listOf("三角形")
            "平行四边形" -> listOf("全等三角形", "勾股定理")
            "一次函数" -> listOf("三角形")
            "数据的分析" -> listOf("轴对称")
            
            // 九年级（初三）数学
            "二次函数" -> listOf("一元二次方程")
            "旋转" -> listOf("二次函数")
            "圆" -> listOf("旋转")
            "概率初步" -> listOf("圆")
            "反比例函数" -> listOf("二次函数")
            "相似" -> listOf("圆")
            "锐角三角函数" -> listOf("相似")
            
            // 高中数学
            "函数概念" -> listOf("集合")
            "基本初等函数" -> listOf("函数概念")
            "函数应用" -> listOf("基本初等函数")
            "空间几何体" -> listOf("函数概念")
            "点线面位置关系" -> listOf("空间几何体")
            "直线与方程" -> listOf("点线面位置关系")
            "圆的方程" -> listOf("直线与方程")
            
            // 大学高等数学
            "导数与微分" -> listOf("函数与极限")
            "微分中值定理" -> listOf("导数与微分")
            "不定积分" -> listOf("微分中值定理")
            "定积分" -> listOf("不定积分")
            "微分方程" -> listOf("定积分")
            "无穷级数" -> listOf("微分方程")
            "多元函数微积分" -> listOf("无穷级数")
            
            // 基础知识点（各年级的学习起点）
            "有理数" -> listOf("小学数学基础")
            "三角形" -> listOf("七年级几何基础")
            "一元二次方程" -> listOf("八年级代数基础")
            "集合" -> listOf("初中数学基础")
            "函数与极限" -> listOf("高中数学基础")
            
            // 其他知识点
            else -> listOf("数学基础知识")
        }
    }
    
    /**
     * 获取知识点的学习资料类型
     */
    private fun getLearningMaterialType(knowledgePoint: String): String {
        return when (knowledgePoint) {
            // 七年级（初一）
            "有理数", "有理数运算" -> "初一数学基础"
            "整式", "整式加减" -> "初一代数入门"
            "一元一次方程" -> "初一方程基础"
            "几何图形初步", "相交线与平行线" -> "初一几何基础"
            "实数" -> "初一数系扩展"
            
            // 八年级（初二）
            "三角形", "全等三角形" -> "初二几何基础"
            "轴对称", "勾股定理" -> "初二几何进阶"
            "平行四边形" -> "初二四边形"
            "一次函数" -> "初二函数入门"
            "数据的分析" -> "初二统计基础"
            
            // 九年级（初三）
            "一元二次方程" -> "初三方程进阶"
            "二次函数", "反比例函数" -> "初三函数深化"
            "旋转", "相似" -> "初三几何变换"
            "圆" -> "初三圆的性质"
            "概率初步" -> "初三概率统计"
            "锐角三角函数" -> "初三三角函数"
            
            // 高中数学
            "集合" -> "高一数学基础"
            "函数概念", "基本初等函数", "函数应用" -> "高中函数"
            "空间几何体", "点线面位置关系" -> "高中立体几何"
            "直线与方程", "圆的方程" -> "高中解析几何"
            
            // 大学数学
            "函数与极限", "导数与微分" -> "高等数学基础"
            "微分中值定理", "不定积分", "定积分" -> "微积分理论"
            "微分方程", "无穷级数" -> "高等数学应用"
            "多元函数微积分" -> "高等数学进阶"
            
            else -> "数学基础知识"
        }
    }
    
    /**
     * 高亮显示前置知识点
     */
    private fun highlightPrerequisiteNodes(prerequisites: List<String>) {
        // 可以在知识图谱中高亮显示前置条件节点
        knowledgeGraphView.highlightNodes(prerequisites)
        showDebouncedToast("已高亮显示前置知识点")
    }
}

/**
 * 知识节点数据类
 */
data class KnowledgeNode(
    val id: String,
    val name: String,
    var x: Float,
    var y: Float,
    val masteryLevel: Float, // 0.0 - 1.0
    val type: NodeType
)

/**
 * 知识连接数据类
 */
data class KnowledgeEdge(
    val fromId: String,
    val toId: String,
    val type: EdgeType
)

enum class NodeType {
    MASTERED,   // 已掌握 - 绿色
    LEARNING,   // 学习中 - 黄色
    TODO,       // 待学习 - 蓝色
    LOCKED      // 未解锁 - 灰色
}

enum class EdgeType {
    PREREQUISITE, // 前置依赖 - 实线箭头
    RELATED,      // 相关知识 - 虚线
    APPLICATION   // 应用关系 - 粗线箭头
}

/**
 * 🎨 自定义知识图谱可视化View
 */
class KnowledgeGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private var nodes = listOf<KnowledgeNode>()
    private var edges = listOf<KnowledgeEdge>()
    
    // 绘制相关
    private val nodePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    // 交互相关
    private var selectedNode: KnowledgeNode? = null
    private var isDragging = false
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    
    // 高亮相关
    private var highlightedNodes = listOf<String>()
    private var zoomLevel = 1.0f
    
    // 动画相关
    private var animationProgress = 0f
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 2000
        addUpdateListener { animation ->
            animationProgress = animation.animatedValue as Float
            invalidate()
        }
    }
    
    init {
        setupPaints()
        startAnimation()
    }
    
    private fun setupPaints() {
        // 节点画笔
        nodePaint.style = Paint.Style.FILL
        
        // 连线画笔
        edgePaint.style = Paint.Style.STROKE
        edgePaint.strokeWidth = 4f
        
        // 文字画笔
        textPaint.color = Color.BLACK
        textPaint.textSize = 32f
        textPaint.textAlign = Paint.Align.CENTER
        
        // 发光效果画笔
        glowPaint.style = Paint.Style.STROKE
        glowPaint.strokeWidth = 8f
    }
    
    fun setGraphData(nodes: List<KnowledgeNode>, edges: List<KnowledgeEdge>) {
        this.nodes = nodes
        this.edges = edges
        invalidate()
    }
    
    private fun startAnimation() {
        animator.start()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // 绘制背景网格
        drawGrid(canvas)
        
        // 绘制边
        edges.forEach { edge ->
            drawEdge(canvas, edge)
        }
        
        // 绘制节点
        nodes.forEach { node ->
            drawNode(canvas, node)
        }
        
        // 绘制选中节点的详细信息
        selectedNode?.let { node ->
            drawNodeDetails(canvas, node)
        }
        
        // 绘制图例
        drawLegend(canvas)
    }
    
    private fun drawGrid(canvas: Canvas) {
        val gridPaint = Paint().apply {
            color = Color.parseColor("#E0E0E0")
            strokeWidth = 1f
            alpha = 100
        }
        
        val gridSize = 100f
        
        // 绘制垂直线
        var x = 0f
        while (x < width) {
            canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
            x += gridSize
        }
        
        // 绘制水平线
        var y = 0f
        while (y < height) {
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
            y += gridSize
        }
    }
    
    private fun drawEdge(canvas: Canvas, edge: KnowledgeEdge) {
        val fromNode = nodes.find { it.id == edge.fromId } ?: return
        val toNode = nodes.find { it.id == edge.toId } ?: return
        
        // 设置连线样式
        when (edge.type) {
            EdgeType.PREREQUISITE -> {
                edgePaint.color = Color.parseColor("#FF6B6B")
                edgePaint.strokeWidth = 6f
                edgePaint.pathEffect = null
            }
            EdgeType.RELATED -> {
                edgePaint.color = Color.parseColor("#4ECDC4")
                edgePaint.strokeWidth = 4f
                edgePaint.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
            }
            EdgeType.APPLICATION -> {
                edgePaint.color = Color.parseColor("#45B7D1")
                edgePaint.strokeWidth = 8f
                edgePaint.pathEffect = null
            }
        }
        
        // 应用动画效果
        val animatedAlpha = (255 * animationProgress).toInt()
        edgePaint.alpha = animatedAlpha
        
        // 绘制连线
        canvas.drawLine(fromNode.x, fromNode.y, toNode.x, toNode.y, edgePaint)
        
        // 绘制箭头
        if (edge.type == EdgeType.PREREQUISITE || edge.type == EdgeType.APPLICATION) {
            drawArrow(canvas, fromNode.x, fromNode.y, toNode.x, toNode.y)
        }
    }
    
    private fun drawArrow(canvas: Canvas, startX: Float, startY: Float, endX: Float, endY: Float) {
        val arrowLength = 30f
        val arrowAngle = Math.PI / 6
        
        val angle = atan2((endY - startY).toDouble(), (endX - startX).toDouble())
        
        val arrowX1 = endX - arrowLength * cos(angle - arrowAngle).toFloat()
        val arrowY1 = endY - arrowLength * sin(angle - arrowAngle).toFloat()
        
        val arrowX2 = endX - arrowLength * cos(angle + arrowAngle).toFloat()
        val arrowY2 = endY - arrowLength * sin(angle + arrowAngle).toFloat()
        
        canvas.drawLine(endX, endY, arrowX1, arrowY1, edgePaint)
        canvas.drawLine(endX, endY, arrowX2, arrowY2, edgePaint)
    }
    
    private fun drawNode(canvas: Canvas, node: KnowledgeNode) {
        val nodeRadius = 50f + node.masteryLevel * 30f // 根据掌握程度调整大小
        
        // 设置节点颜色
        val nodeColor = when (node.type) {
            NodeType.MASTERED -> Color.parseColor("#4CAF50")   // 绿色
            NodeType.LEARNING -> Color.parseColor("#FF9800")   // 橙色
            NodeType.TODO -> Color.parseColor("#2196F3")       // 蓝色
            NodeType.LOCKED -> Color.parseColor("#9E9E9E")     // 灰色
        }
        
        nodePaint.color = nodeColor
        
        // 应用动画效果
        val animatedRadius = nodeRadius * animationProgress
        val animatedAlpha = (255 * animationProgress).toInt()
        nodePaint.alpha = animatedAlpha
        
        // 绘制选中效果
        if (node == selectedNode) {
            glowPaint.color = nodeColor
            glowPaint.alpha = 100
            canvas.drawCircle(node.x, node.y, animatedRadius + 20f, glowPaint)
        }
        
        // 绘制高亮效果
        if (highlightedNodes.contains(node.name)) {
            glowPaint.color = Color.parseColor("#FFD700") // 金色高亮
            glowPaint.alpha = 150
            canvas.drawCircle(node.x, node.y, animatedRadius + 30f, glowPaint)
            
            // 闪烁效果
            val pulseRadius = animatedRadius + 30f + sin(System.currentTimeMillis() / 200.0).toFloat() * 10f
            glowPaint.alpha = 80
            canvas.drawCircle(node.x, node.y, pulseRadius, glowPaint)
        }
        
        // 绘制节点圆圈
        canvas.drawCircle(node.x, node.y, animatedRadius, nodePaint)
        
        // 绘制掌握程度环
        drawMasteryRing(canvas, node, animatedRadius)
        
        // 绘制节点文字
        textPaint.alpha = animatedAlpha
        canvas.drawText(node.name, node.x, node.y + 10f, textPaint)
        
        // 绘制掌握百分比
        val masteryText = "${(node.masteryLevel * 100).toInt()}%"
        textPaint.textSize = 24f
        canvas.drawText(masteryText, node.x, node.y + 40f, textPaint)
        textPaint.textSize = 32f
    }
    
    private fun drawMasteryRing(canvas: Canvas, node: KnowledgeNode, radius: Float) {
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 8f
            color = Color.WHITE
            alpha = 200
        }
        
        val rect = RectF(
            node.x - radius - 10f,
            node.y - radius - 10f,
            node.x + radius + 10f,
            node.y + radius + 10f
        )
        
        // 绘制背景环
        canvas.drawCircle(node.x, node.y, radius + 10f, ringPaint)
        
        // 绘制进度环
        ringPaint.color = Color.parseColor("#FFC107")
        ringPaint.strokeWidth = 6f
        val sweepAngle = 360f * node.masteryLevel * animationProgress
        canvas.drawArc(rect, -90f, sweepAngle, false, ringPaint)
    }
    
    private fun drawNodeDetails(canvas: Canvas, node: KnowledgeNode) {
        val detailsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFFFFF")
            style = Paint.Style.FILL
        }
        
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E0E0E0")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        
        val detailsRect = RectF(50f, height - 300f, width - 50f, height - 50f)
        
        // 绘制详情背景
        canvas.drawRoundRect(detailsRect, 20f, 20f, detailsPaint)
        canvas.drawRoundRect(detailsRect, 20f, 20f, borderPaint)
        
        // 绘制详情文字
        val detailTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 36f
            textAlign = Paint.Align.LEFT
        }
        
        val startY = height - 250f
        canvas.drawText("📚 ${node.name}", 80f, startY, detailTextPaint)
        canvas.drawText("📊 掌握程度: ${(node.masteryLevel * 100).toInt()}%", 80f, startY + 50f, detailTextPaint)
        canvas.drawText("🎯 状态: ${getStatusText(node.type)}", 80f, startY + 100f, detailTextPaint)
        canvas.drawText("💡 点击其他节点探索关联知识", 80f, startY + 150f, detailTextPaint)
    }
    
    private fun getStatusText(type: NodeType): String {
        return when (type) {
            NodeType.MASTERED -> "已掌握"
            NodeType.LEARNING -> "学习中"
            NodeType.TODO -> "待学习"
            NodeType.LOCKED -> "未解锁"
        }
    }
    
    private fun drawLegend(canvas: Canvas) {
        val legendPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFFFFF")
            style = Paint.Style.FILL
            alpha = 230
        }
        
        val legendRect = RectF(width - 300f, 50f, width - 50f, 350f)
        canvas.drawRoundRect(legendRect, 15f, 15f, legendPaint)
        
        val legendTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 28f
            textAlign = Paint.Align.LEFT
        }
        
        canvas.drawText("🕸️ 知识图谱", width - 280f, 100f, legendTextPaint)
        canvas.drawText("🟢 已掌握", width - 280f, 150f, legendTextPaint)
        canvas.drawText("🟡 学习中", width - 280f, 180f, legendTextPaint)
        canvas.drawText("🔵 待学习", width - 280f, 210f, legendTextPaint)
        canvas.drawText("⚫ 未解锁", width - 280f, 240f, legendTextPaint)
        canvas.drawText("━━ 前置依赖", width - 280f, 280f, legendTextPaint)
        canvas.drawText("┅┅ 相关知识", width - 280f, 310f, legendTextPaint)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 检查是否在FAB区域内，如果是则不处理触摸事件
        val fabMargin = 100f // FAB区域边距
        if (event.x > width - fabMargin * 3 && event.y > height - fabMargin * 4) {
            return false // 让父View处理FAB的触摸事件
        }
        
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                
                // 检查是否点击了节点
                val clickedNode = findNodeAt(event.x, event.y)
                if (clickedNode != null) {
                    selectedNode = clickedNode
                    isDragging = true
                    invalidate()
                    
                    // 显示节点点击反馈
                    performNodeClick(clickedNode)
                    return true
                }
            }
            
            MotionEvent.ACTION_MOVE -> {
                if (isDragging && selectedNode != null) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    
                    selectedNode!!.x += dx
                    selectedNode!!.y += dy
                    
                    lastTouchX = event.x
                    lastTouchY = event.y
                    invalidate()
                    return true
                }
            }
            
            MotionEvent.ACTION_UP -> {
                isDragging = false
                return true
            }
        }
        
        return super.onTouchEvent(event)
    }
    
    /**
     * 🎯 处理节点点击事件
     */
    private fun performNodeClick(node: KnowledgeNode) {
        // 显示节点详细信息
        context?.let { ctx ->
            if (ctx is KnowledgeGraphActivity) {
                ctx.showNodeDetails(node)
            }
        }
    }
    
    private fun findNodeAt(x: Float, y: Float): KnowledgeNode? {
        return nodes.find { node ->
            val distance = sqrt((x - node.x).pow(2) + (y - node.y).pow(2))
            distance <= 80f // 触摸范围
        }
    }
    
    /**
     * 🎯 居中显示所有节点
     */
    fun centerGraph() {
        if (nodes.isEmpty()) return
        
        val centerX = width / 2f
        val centerY = height / 2f
        
        // 计算节点的重心
        val avgX = nodes.map { it.x }.average().toFloat()
        val avgY = nodes.map { it.y }.average().toFloat()
        
        // 移动所有节点到中心
        val offsetX = centerX - avgX
        val offsetY = centerY - avgY
        
        nodes.forEach { node ->
            node.x += offsetX
            node.y += offsetY
        }
        
        invalidate()
    }
    
    /**
     * 🔍 切换缩放状态
     */
    fun toggleZoom() {
        // 简单的缩放实现
        val scaleFactor = if (nodes.isNotEmpty()) {
            val avgDistance = nodes.map { node ->
                sqrt((node.x - width/2f).pow(2) + (node.y - height/2f).pow(2))
            }.average()
            
            if (avgDistance > 200) 0.7f else 1.3f
        } else 1.0f
        
        val centerX = width / 2f
        val centerY = height / 2f
        
        nodes.forEach { node ->
            val deltaX = node.x - centerX
            val deltaY = node.y - centerY
            node.x = centerX + deltaX * scaleFactor
            node.y = centerY + deltaY * scaleFactor
        }
        
        invalidate()
    }
    
    /**
     * 高亮显示指定的节点
     */
    fun highlightNodes(nodeNames: List<String>) {
        highlightedNodes = nodeNames
        invalidate()
    }
    
}

// ==================== 年级专用知识节点生成方法 ====================

/**
 * 七年级数学知识图谱（初一）- 张小明的学习进度
 */
private fun getGrade7KnowledgeNodes(): List<KnowledgeNode> {
    return listOf(
        KnowledgeNode("1", "有理数", 300f, 200f, 0.95f, NodeType.MASTERED),
        KnowledgeNode("2", "有理数运算", 600f, 200f, 0.88f, NodeType.MASTERED),
        KnowledgeNode("3", "整式", 900f, 200f, 0.75f, NodeType.LEARNING),
        KnowledgeNode("4", "整式加减", 300f, 400f, 0.65f, NodeType.LEARNING),
        KnowledgeNode("5", "一元一次方程", 600f, 400f, 0.35f, NodeType.TODO),
        KnowledgeNode("6", "几何图形初步", 900f, 400f, 0.25f, NodeType.TODO),
        KnowledgeNode("7", "相交线与平行线", 300f, 600f, 0.05f, NodeType.LOCKED),
        KnowledgeNode("8", "实数", 600f, 600f, 0.0f, NodeType.LOCKED)
    )
}

/**
 * 八年级数学知识图谱（初二）
 */
private fun getGrade8KnowledgeNodes(): List<KnowledgeNode> {
    return listOf(
        KnowledgeNode("1", "三角形", 300f, 200f, 0.90f, NodeType.MASTERED),
        KnowledgeNode("2", "全等三角形", 600f, 200f, 0.78f, NodeType.LEARNING),
        KnowledgeNode("3", "轴对称", 900f, 200f, 0.68f, NodeType.LEARNING),
        KnowledgeNode("4", "实数", 300f, 400f, 0.55f, NodeType.TODO),
        KnowledgeNode("5", "勾股定理", 600f, 400f, 0.42f, NodeType.TODO),
        KnowledgeNode("6", "平行四边形", 900f, 400f, 0.18f, NodeType.LOCKED),
        KnowledgeNode("7", "一次函数", 300f, 600f, 0.08f, NodeType.LOCKED),
        KnowledgeNode("8", "数据的分析", 600f, 600f, 0.0f, NodeType.LOCKED)
    )
}

/**
 * 九年级数学知识图谱（初三）
 */
private fun getGrade9KnowledgeNodes(): List<KnowledgeNode> {
    return listOf(
        KnowledgeNode("1", "一元二次方程", 300f, 200f, 0.82f, NodeType.MASTERED),
        KnowledgeNode("2", "二次函数", 600f, 200f, 0.71f, NodeType.LEARNING),
        KnowledgeNode("3", "旋转", 900f, 200f, 0.58f, NodeType.TODO),
        KnowledgeNode("4", "圆", 300f, 400f, 0.45f, NodeType.TODO),
        KnowledgeNode("5", "概率初步", 600f, 400f, 0.32f, NodeType.TODO),
        KnowledgeNode("6", "反比例函数", 900f, 400f, 0.22f, NodeType.LOCKED),
        KnowledgeNode("7", "相似", 300f, 600f, 0.12f, NodeType.LOCKED),
        KnowledgeNode("8", "锐角三角函数", 600f, 600f, 0.0f, NodeType.LOCKED)
    )
}

/**
 * 高一数学知识图谱
 */
private fun getHighSchool1KnowledgeNodes(): List<KnowledgeNode> {
    return listOf(
        KnowledgeNode("1", "集合", 300f, 200f, 0.8f, NodeType.MASTERED),
        KnowledgeNode("2", "函数概念", 600f, 200f, 0.7f, NodeType.LEARNING),
        KnowledgeNode("3", "基本初等函数", 900f, 200f, 0.6f, NodeType.LEARNING),
        KnowledgeNode("4", "函数应用", 300f, 400f, 0.4f, NodeType.TODO),
        KnowledgeNode("5", "空间几何体", 600f, 400f, 0.3f, NodeType.TODO),
        KnowledgeNode("6", "点线面位置关系", 900f, 400f, 0.2f, NodeType.LOCKED),
        KnowledgeNode("7", "直线与方程", 300f, 600f, 0.1f, NodeType.LOCKED),
        KnowledgeNode("8", "圆的方程", 600f, 600f, 0.0f, NodeType.LOCKED)
    )
}

/**
 * 高二数学知识图谱
 */
private fun getHighSchool2KnowledgeNodes(): List<KnowledgeNode> {
    return listOf(
        KnowledgeNode("1", "三角函数", 300f, 200f, 0.7f, NodeType.LEARNING),
        KnowledgeNode("2", "平面向量", 600f, 200f, 0.6f, NodeType.LEARNING),
        KnowledgeNode("3", "三角恒等变换", 900f, 200f, 0.5f, NodeType.TODO),
        KnowledgeNode("4", "解三角形", 300f, 400f, 0.4f, NodeType.TODO),
        KnowledgeNode("5", "数列", 600f, 400f, 0.3f, NodeType.TODO),
        KnowledgeNode("6", "不等式", 900f, 400f, 0.2f, NodeType.LOCKED),
        KnowledgeNode("7", "立体几何", 300f, 600f, 0.1f, NodeType.LOCKED),
        KnowledgeNode("8", "解析几何", 600f, 600f, 0.0f, NodeType.LOCKED)
    )
}

/**
 * 高三数学知识图谱
 */
private fun getHighSchool3KnowledgeNodes(): List<KnowledgeNode> {
    return listOf(
        KnowledgeNode("1", "导数概念", 300f, 200f, 0.6f, NodeType.LEARNING),
        KnowledgeNode("2", "导数应用", 600f, 200f, 0.5f, NodeType.TODO),
        KnowledgeNode("3", "统计", 900f, 200f, 0.4f, NodeType.TODO),
        KnowledgeNode("4", "概率", 300f, 400f, 0.3f, NodeType.TODO),
        KnowledgeNode("5", "复数", 600f, 400f, 0.2f, NodeType.LOCKED),
        KnowledgeNode("6", "推理与证明", 900f, 400f, 0.1f, NodeType.LOCKED),
        KnowledgeNode("7", "极坐标", 300f, 600f, 0.0f, NodeType.LOCKED),
        KnowledgeNode("8", "参数方程", 600f, 600f, 0.0f, NodeType.LOCKED)
    )
}

/**
 * 大学数学知识图谱（高等数学）
 */
private fun getUniversityKnowledgeNodes(): List<KnowledgeNode> {
    return listOf(
        KnowledgeNode("1", "函数与极限", 300f, 200f, 0.5f, NodeType.TODO),
        KnowledgeNode("2", "导数与微分", 600f, 200f, 0.4f, NodeType.TODO),
        KnowledgeNode("3", "微分中值定理", 900f, 200f, 0.3f, NodeType.LOCKED),
        KnowledgeNode("4", "不定积分", 300f, 400f, 0.2f, NodeType.LOCKED),
        KnowledgeNode("5", "定积分", 600f, 400f, 0.1f, NodeType.LOCKED),
        KnowledgeNode("6", "微分方程", 900f, 400f, 0.0f, NodeType.LOCKED),
        KnowledgeNode("7", "无穷级数", 300f, 600f, 0.0f, NodeType.LOCKED),
        KnowledgeNode("8", "多元函数微积分", 600f, 600f, 0.0f, NodeType.LOCKED)
    )
}

/**
 * 根据年级获取知识点连接关系
 */
private fun getKnowledgeEdgesForGrade(userGrade: String): List<KnowledgeEdge> {
    return when {
        userGrade.contains("七年级") || userGrade.contains("初一") -> listOf(
            KnowledgeEdge("1", "2", EdgeType.PREREQUISITE),
            KnowledgeEdge("2", "3", EdgeType.PREREQUISITE),
            KnowledgeEdge("3", "4", EdgeType.PREREQUISITE),
            KnowledgeEdge("4", "5", EdgeType.PREREQUISITE),
            KnowledgeEdge("2", "6", EdgeType.APPLICATION),
            KnowledgeEdge("6", "7", EdgeType.PREREQUISITE),
            KnowledgeEdge("5", "8", EdgeType.PREREQUISITE)
        )
        userGrade.contains("八年级") || userGrade.contains("初二") -> listOf(
            KnowledgeEdge("1", "2", EdgeType.PREREQUISITE),
            KnowledgeEdge("2", "3", EdgeType.APPLICATION),
            KnowledgeEdge("1", "4", EdgeType.PREREQUISITE),
            KnowledgeEdge("1", "5", EdgeType.PREREQUISITE),
            KnowledgeEdge("5", "6", EdgeType.APPLICATION),
            KnowledgeEdge("4", "7", EdgeType.PREREQUISITE),
            KnowledgeEdge("3", "8", EdgeType.APPLICATION)
        )
        userGrade.contains("九年级") || userGrade.contains("初三") -> listOf(
            KnowledgeEdge("1", "2", EdgeType.PREREQUISITE),
            KnowledgeEdge("2", "3", EdgeType.APPLICATION),
            KnowledgeEdge("3", "4", EdgeType.PREREQUISITE),
            KnowledgeEdge("4", "5", EdgeType.APPLICATION),
            KnowledgeEdge("2", "6", EdgeType.PREREQUISITE),
            KnowledgeEdge("4", "7", EdgeType.PREREQUISITE),
            KnowledgeEdge("7", "8", EdgeType.PREREQUISITE)
        )
        else -> listOf(
            KnowledgeEdge("1", "2", EdgeType.PREREQUISITE),
            KnowledgeEdge("2", "3", EdgeType.PREREQUISITE),
            KnowledgeEdge("3", "4", EdgeType.PREREQUISITE),
            KnowledgeEdge("4", "5", EdgeType.PREREQUISITE),
            KnowledgeEdge("5", "6", EdgeType.PREREQUISITE),
            KnowledgeEdge("6", "7", EdgeType.APPLICATION),
            KnowledgeEdge("7", "8", EdgeType.PREREQUISITE)
        )
    }
}
