package com.example.educationapp.ui.splash

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

/**
 * 🌊 波纹效果视图 - 高级动画背景
 */
class RippleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val ripples = mutableListOf<Ripple>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var animator: ValueAnimator? = null
    private var animationTime = 0f

    private val maxRipples = 5
    private val rippleColors = intArrayOf(
        0x404CAF50.toInt(), // 半透明绿色
        0x402196F3.toInt(), // 半透明蓝色
        0x40FF9800.toInt(), // 半透明橙色
        0x40FFC107.toInt()  // 半透明黄色
    )

    init {
        paint.style = Paint.Style.STROKE
    }

    fun startRippleAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, Float.MAX_VALUE).apply {
            duration = Long.MAX_VALUE
            addUpdateListener { valueAnimator ->
                animationTime = valueAnimator.animatedValue as Float
                updateRipples()
                invalidate()
            }
            start()
        }

        // 定期创建新的波纹
        createRipplesPeriodically()
    }

    fun stopRippleAnimation() {
        animator?.cancel()
    }

    private fun createRipplesPeriodically() {
        // 每1.5秒创建一个新波纹
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (animator?.isRunning == true) {
                createNewRipple()
                createRipplesPeriodically()
            }
        }, 1500)
    }

    private fun createNewRipple() {
        if (ripples.size >= maxRipples) {
            ripples.removeAt(0) // 移除最老的波纹
        }

        val centerX = width / 2f
        val centerY = height / 2f
        val maxRadius = sqrt((centerX * centerX + centerY * centerY).toDouble()).toFloat() * 1.5f

        ripples.add(
            Ripple(
                centerX = centerX,
                centerY = centerY,
                maxRadius = maxRadius,
                color = rippleColors.random(),
                startTime = animationTime,
                duration = 3000f // 3秒
            )
        )
    }

    private fun updateRipples() {
        ripples.removeAll { ripple ->
            val elapsed = animationTime - ripple.startTime
            elapsed > ripple.duration
        }

        ripples.forEach { ripple ->
            val elapsed = animationTime - ripple.startTime
            val progress = (elapsed / ripple.duration).coerceIn(0f, 1f)

            // 使用缓动函数创建更自然的扩散效果
            val easedProgress = easeOutCubic(progress)
            ripple.currentRadius = ripple.maxRadius * easedProgress

            // 透明度随时间衰减
            ripple.currentAlpha = (1f - progress) * 0.6f

            // 波纹宽度变化
            ripple.currentStrokeWidth = (1f - progress) * 8f + 2f
        }
    }

    private fun easeOutCubic(t: Float): Float {
        val t1 = t - 1f
        return t1 * t1 * t1 + 1f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        ripples.forEach { ripple ->
            paint.color = ripple.color
            paint.alpha = (ripple.currentAlpha * 255).toInt()
            paint.strokeWidth = ripple.currentStrokeWidth

            // 绘制主波纹
            canvas.drawCircle(
                ripple.centerX,
                ripple.centerY,
                ripple.currentRadius,
                paint
            )

            // 绘制内部小波纹（创造层次感）
            if (ripple.currentRadius > 50f) {
                paint.alpha = (ripple.currentAlpha * 128).toInt()
                paint.strokeWidth = ripple.currentStrokeWidth * 0.5f
                canvas.drawCircle(
                    ripple.centerX,
                    ripple.centerY,
                    ripple.currentRadius * 0.7f,
                    paint
                )
            }

            // 绘制外部辅助波纹
            if (ripple.currentRadius > 100f) {
                paint.alpha = (ripple.currentAlpha * 64).toInt()
                paint.strokeWidth = ripple.currentStrokeWidth * 0.3f
                canvas.drawCircle(
                    ripple.centerX,
                    ripple.centerY,
                    ripple.currentRadius * 1.2f,
                    paint
                )
            }
        }

        // 绘制中心发光点
        drawCenterGlow(canvas)
    }

    private fun drawCenterGlow(canvas: Canvas) {
        val centerX = width / 2f
        val centerY = height / 2f

        // 创建径向渐变
        val glowRadius = 60f + 20f * sin(animationTime * 0.003f)
        val gradient = RadialGradient(
            centerX, centerY, glowRadius,
            intArrayOf(
                0x80FFFFFF.toInt(),
                0x404CAF50.toInt(),
                0x00FFFFFF.toInt()
            ),
            floatArrayOf(0f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )

        paint.shader = gradient
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, glowRadius, paint)

        // 重置画笔
        paint.shader = null
        paint.style = Paint.Style.STROKE
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopRippleAnimation()
    }

    data class Ripple(
        val centerX: Float,
        val centerY: Float,
        val maxRadius: Float,
        val color: Int,
        val startTime: Float,
        val duration: Float,
        var currentRadius: Float = 0f,
        var currentAlpha: Float = 1f,
        var currentStrokeWidth: Float = 8f
    )
}
