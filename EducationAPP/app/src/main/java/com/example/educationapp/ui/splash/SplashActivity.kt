package com.example.educationapp.ui.splash

import android.animation.*
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import com.example.educationapp.R
import com.example.educationapp.ui.auth.LoginActivity
import com.example.educationapp.ui.splash.ParticleView
import com.example.educationapp.utils.PreferenceManager
import kotlin.math.sin
import kotlin.random.Random

/**
 * 🌟 开屏动画页面 - 智学伙伴
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var logoImageView: ImageView
    private lateinit var appNameTextView: TextView
    private lateinit var sloganTextView: TextView
    private lateinit var loadingTextView: TextView
    private lateinit var particleView: ParticleView
    private lateinit var rippleView: RippleView
    private lateinit var preferenceManager: PreferenceManager
    
    private val animatorSet = AnimatorSet()
    private var currentPhase = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        
        // 隐藏状态栏，全屏显示
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        supportActionBar?.hide()
        
        initViews()
        preferenceManager = PreferenceManager(this)
        startSplashAnimation()
    }

    private fun initViews() {
        logoImageView = findViewById(R.id.iv_logo)
        appNameTextView = findViewById(R.id.tv_app_name)
        sloganTextView = findViewById(R.id.tv_slogan)
        loadingTextView = findViewById(R.id.tv_loading)
        particleView = findViewById(R.id.particle_view)
        rippleView = findViewById(R.id.ripple_view)
    }

    private fun startSplashAnimation() {
        // 启动高级动画序列
        startAdvancedAnimationSequence()
    }

    private fun startAdvancedAnimationSequence() {
        // 阶段1：粒子背景启动
        startParticleBackground()
        
        // 阶段2：3D Logo入场 (延迟500ms)
        Handler(Looper.getMainLooper()).postDelayed({ animate3DLogo() }, 500)
        
        // 阶段3：波纹扩散效果 (延迟1200ms)
        Handler(Looper.getMainLooper()).postDelayed({ startRippleEffect() }, 1200)
        
        // 阶段4：文字炫酷出现 (延迟2000ms)
        Handler(Looper.getMainLooper()).postDelayed({ animateTextsAdvanced() }, 2000)
        
        // 阶段5：全息加载效果 (延迟3500ms)
        Handler(Looper.getMainLooper()).postDelayed({ animateHolographicLoading() }, 3500)
        
        // 阶段6：转场特效 (延迟5500ms)
        Handler(Looper.getMainLooper()).postDelayed({ animateTransition() }, 5500)
    }

    private fun startParticleBackground() {
        particleView.visibility = View.VISIBLE
        particleView.startParticleAnimation()
        
        // 粒子视图淡入
        val particleAlpha = ObjectAnimator.ofFloat(particleView, "alpha", 0f, 1f)
        particleAlpha.duration = 1000
        particleAlpha.interpolator = DecelerateInterpolator()
        particleAlpha.start()
    }

    private fun animate3DLogo() {
        // 初始状态：logo完全隐藏并缩小
        logoImageView.alpha = 0f
        logoImageView.scaleX = 0.3f
        logoImageView.scaleY = 0.3f
        logoImageView.rotationY = -90f
        logoImageView.translationZ = -100f

        // 3D翻转入场动画
        val rotationAnimator = ObjectAnimator.ofFloat(logoImageView, "rotationY", -90f, 15f, 0f)
        rotationAnimator.duration = 1200
        rotationAnimator.interpolator = OvershootInterpolator(1.2f)

        // 3D深度动画
        val translationZAnimator = ObjectAnimator.ofFloat(logoImageView, "translationZ", -100f, 20f, 0f)
        translationZAnimator.duration = 1200
        translationZAnimator.interpolator = DecelerateInterpolator()

        // 弹性缩放
        val scaleXAnimator = ObjectAnimator.ofFloat(logoImageView, "scaleX", 0.3f, 1.3f, 1f)
        scaleXAnimator.duration = 1200
        scaleXAnimator.interpolator = OvershootInterpolator(1.5f)

        val scaleYAnimator = ObjectAnimator.ofFloat(logoImageView, "scaleY", 0.3f, 1.3f, 1f)
        scaleYAnimator.duration = 1200
        scaleYAnimator.interpolator = OvershootInterpolator(1.5f)

        // 透明度渐现
        val alphaAnimator = ObjectAnimator.ofFloat(logoImageView, "alpha", 0f, 1f)
        alphaAnimator.duration = 800
        alphaAnimator.interpolator = AccelerateDecelerateInterpolator()

        // 组合动画
        val logoAnimatorSet = AnimatorSet()
        logoAnimatorSet.playTogether(
            rotationAnimator, translationZAnimator, scaleXAnimator, scaleYAnimator, alphaAnimator
        )
        logoAnimatorSet.start()
    }

    private fun startRippleEffect() {
        rippleView.visibility = View.VISIBLE
        rippleView.startRippleAnimation()
        
        // 波纹视图淡入
        val rippleAlpha = ObjectAnimator.ofFloat(rippleView, "alpha", 0f, 0.8f, 0f)
        rippleAlpha.duration = 2000
        rippleAlpha.interpolator = AccelerateDecelerateInterpolator()
        rippleAlpha.start()
    }

    private fun animateTextsAdvanced() {
        // 应用名称：打字机效果 + 发光
        animateTypewriterEffect(appNameTextView, "智学伙伴", 0) {
            // 名称完成后，开始标语
            animateTypewriterEffect(sloganTextView, "AI陪伴，智慧学习", 300) {
                // 添加文字发光效果
                addTextGlowEffect(appNameTextView)
                addTextGlowEffect(sloganTextView)
            }
        }
    }

    private fun animateTypewriterEffect(textView: TextView, fullText: String, delay: Long, onComplete: () -> Unit) {
        textView.alpha = 0f
        textView.translationY = 50f
        
        // 先显示TextView
        val showAnimator = AnimatorSet()
        val alphaShow = ObjectAnimator.ofFloat(textView, "alpha", 0f, 1f)
        val translateShow = ObjectAnimator.ofFloat(textView, "translationY", 50f, 0f)
        alphaShow.duration = 300
        translateShow.duration = 300
        showAnimator.playTogether(alphaShow, translateShow)
        
        showAnimator.doOnEnd {
            // 打字机效果
            Handler(Looper.getMainLooper()).postDelayed({
                startTypewriterAnimation(textView, fullText, onComplete)
            }, delay)
        }
        
        showAnimator.start()
    }

    private fun startTypewriterAnimation(textView: TextView, fullText: String, onComplete: () -> Unit) {
        var currentIndex = 0
        textView.text = ""
        
        val typewriterHandler = Handler(Looper.getMainLooper())
        val typewriterRunnable = object : Runnable {
            override fun run() {
                if (currentIndex < fullText.length) {
                    textView.text = fullText.substring(0, currentIndex + 1)
                    currentIndex++
                    
                    // 添加随机延迟，模拟真实打字
                    val delay = Random.nextLong(50, 150)
                    typewriterHandler.postDelayed(this, delay)
                } else {
                    onComplete()
                }
            }
        }
        typewriterRunnable.run()
    }

    private fun addTextGlowEffect(textView: TextView) {
        // 文字发光动画
        val glowAnimator = ValueAnimator.ofFloat(0f, 10f, 0f)
        glowAnimator.duration = 2000
        glowAnimator.repeatCount = ValueAnimator.INFINITE
        glowAnimator.repeatMode = ValueAnimator.REVERSE
        
        glowAnimator.addUpdateListener { animator ->
            val glowRadius = animator.animatedValue as Float
            textView.setShadowLayer(glowRadius, 0f, 0f, 0xFFFFFFFF.toInt())
        }
        
        glowAnimator.start()
    }

    private fun animateHolographicLoading() {
        loadingTextView.alpha = 0f
        loadingTextView.visibility = View.VISIBLE
        
        // 全息投影效果
        val holographicAnimator = ValueAnimator.ofFloat(0f, 1f)
        holographicAnimator.duration = 2000
        holographicAnimator.repeatCount = ValueAnimator.INFINITE
        
        holographicAnimator.addUpdateListener { animator ->
            val progress = animator.animatedValue as Float
            
            // 透明度波动
            val alpha = 0.3f + 0.7f * sin(progress * Math.PI * 4).toFloat()
            loadingTextView.alpha = alpha
            
            // 颜色变化（模拟全息效果）
            val hue = (progress * 360) % 360
            val color = android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.8f, 1f))
            loadingTextView.setTextColor(color)
            
            // 轻微缩放
            val scale = 0.95f + 0.05f * sin(progress * Math.PI * 8).toFloat()
            loadingTextView.scaleX = scale
            loadingTextView.scaleY = scale
        }
        
        holographicAnimator.start()
    }

    private fun animateTransition() {
        // 创建炫酷的转场动画
        val containerView = findViewById<ViewGroup>(R.id.splash_container)
        
        // 整体缩放淡出
        val scaleOutX = ObjectAnimator.ofFloat(containerView, "scaleX", 1f, 0.8f)
        val scaleOutY = ObjectAnimator.ofFloat(containerView, "scaleY", 1f, 0.8f)
        val alphaOut = ObjectAnimator.ofFloat(containerView, "alpha", 1f, 0f)
        
        scaleOutX.duration = 800
        scaleOutY.duration = 800
        alphaOut.duration = 800
        
        val transitionSet = AnimatorSet()
        transitionSet.playTogether(scaleOutX, scaleOutY, alphaOut)
        transitionSet.interpolator = AccelerateInterpolator()
        
        transitionSet.doOnEnd {
            navigateToLogin()
        }
        
        transitionSet.start()
    }

    private fun navigateToLogin() {
        // 延迟500ms后跳转，让用户看到完整动画
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = if (preferenceManager.isLoggedIn()) {
                // 如果已登录，根据用户类型跳转
                val userRole = preferenceManager.getUserRole()
                if (userRole == "TEACHER") {
                    Intent(this, com.example.educationapp.ui.teacher.TeacherMainActivity::class.java)
                } else {
                    Intent(this, com.example.educationapp.ui.main.MainActivity::class.java)
                }
            } else {
                Intent(this, LoginActivity::class.java)
            }
            
            startActivity(intent)
            
            // 添加淡出转场动画
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 500)
    }

    override fun onBackPressed() {
        // 防止用户在开屏时按返回键
        // 不调用 super.onBackPressed()
    }
}
