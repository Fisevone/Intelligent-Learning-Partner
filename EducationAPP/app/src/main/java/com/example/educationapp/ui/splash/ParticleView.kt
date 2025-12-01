package com.example.educationapp.ui.splash

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*
import kotlin.random.Random

/**
 * 🌟 粒子效果视图 - 高级动画背景
 */
class ParticleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val particles = mutableListOf<Particle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var animator: ValueAnimator? = null
    private var animationTime = 0f

    private val particleCount = 50
    private val colors = intArrayOf(
        0xFF4CAF50.toInt(), // 绿色
        0xFF2196F3.toInt(), // 蓝色
        0xFFFF9800.toInt(), // 橙色
        0xFFFFC107.toInt(), // 黄色
        0xFFE91E63.toInt()  // 粉色
    )

    init {
        initParticles()
    }

    private fun initParticles() {
        particles.clear()
        repeat(particleCount) {
            particles.add(createRandomParticle())
        }
    }

    private fun createRandomParticle(): Particle {
        return Particle(
            x = Random.nextFloat() * width,
            y = Random.nextFloat() * height,
            vx = Random.nextFloat() * 4 - 2, // -2 to 2
            vy = Random.nextFloat() * 4 - 2,
            radius = Random.nextFloat() * 6 + 2, // 2 to 8
            color = colors[Random.nextInt(colors.size)],
            alpha = Random.nextFloat() * 0.8f + 0.2f, // 0.2 to 1.0
            rotationSpeed = Random.nextFloat() * 4 - 2, // -2 to 2
            pulseSpeed = Random.nextFloat() * 2 + 1, // 1 to 3
            initialPhase = Random.nextFloat() * 2 * PI.toFloat()
        )
    }

    fun startParticleAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, Float.MAX_VALUE).apply {
            duration = Long.MAX_VALUE
            addUpdateListener { valueAnimator ->
                animationTime = valueAnimator.animatedValue as Float
                updateParticles()
                invalidate()
            }
            start()
        }
    }

    fun stopParticleAnimation() {
        animator?.cancel()
    }

    private fun updateParticles() {
        val time = animationTime * 0.001f // 转换为秒

        particles.forEach { particle ->
            // 更新位置
            particle.x += particle.vx
            particle.y += particle.vy

            // 边界检测和反弹
            if (particle.x < 0 || particle.x > width) {
                particle.vx = -particle.vx
                particle.x = particle.x.coerceIn(0f, width.toFloat())
            }
            if (particle.y < 0 || particle.y > height) {
                particle.vy = -particle.vy
                particle.y = particle.y.coerceIn(0f, height.toFloat())
            }

            // 更新旋转
            particle.rotation += particle.rotationSpeed

            // 更新脉冲效果
            val pulsePhase = time * particle.pulseSpeed + particle.initialPhase
            particle.currentRadius = particle.radius * (1f + 0.3f * sin(pulsePhase))
            particle.currentAlpha = particle.alpha * (0.7f + 0.3f * sin(pulsePhase * 1.5f))

            // 添加轻微的引力效果（向中心聚拢）
            val centerX = width / 2f
            val centerY = height / 2f
            val dx = centerX - particle.x
            val dy = centerY - particle.y
            val distance = sqrt(dx * dx + dy * dy)
            
            if (distance > 0) {
                val gravity = 0.001f
                particle.vx += (dx / distance) * gravity
                particle.vy += (dy / distance) * gravity
                
                // 限制速度
                val speed = sqrt(particle.vx * particle.vx + particle.vy * particle.vy)
                if (speed > 3f) {
                    particle.vx = (particle.vx / speed) * 3f
                    particle.vy = (particle.vy / speed) * 3f
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        particles.forEach { particle ->
            paint.color = particle.color
            paint.alpha = (particle.currentAlpha * 255).toInt()

            canvas.save()
            canvas.translate(particle.x, particle.y)
            canvas.rotate(particle.rotation)

            // 绘制粒子（星形）
            drawStar(canvas, particle.currentRadius)

            canvas.restore()

            // 绘制连接线（附近的粒子之间）
            drawConnections(canvas, particle)
        }
    }

    private fun drawStar(canvas: Canvas, radius: Float) {
        val path = Path()
        val angleStep = (2 * PI / 5).toFloat()
        val innerRadius = radius * 0.5f

        for (i in 0..9) {
            val angle = i * angleStep / 2
            val r = if (i % 2 == 0) radius else innerRadius
            val x = cos(angle) * r
            val y = sin(angle) * r

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()

        canvas.drawPath(path, paint)
    }

    private fun drawConnections(canvas: Canvas, particle: Particle) {
        particles.forEach { otherParticle ->
            if (particle != otherParticle) {
                val dx = particle.x - otherParticle.x
                val dy = particle.y - otherParticle.y
                val distance = sqrt(dx * dx + dy * dy)

                if (distance < 150f) { // 连接距离阈值
                    paint.color = 0x30FFFFFF.toInt() // 半透明白色
                    paint.strokeWidth = (150f - distance) / 150f * 2f
                    canvas.drawLine(
                        particle.x, particle.y,
                        otherParticle.x, otherParticle.y,
                        paint
                    )
                }
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            initParticles()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopParticleAnimation()
    }

    data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        val radius: Float,
        val color: Int,
        val alpha: Float,
        var rotation: Float = 0f,
        val rotationSpeed: Float,
        val pulseSpeed: Float,
        val initialPhase: Float,
        var currentRadius: Float = radius,
        var currentAlpha: Float = alpha
    )
}
