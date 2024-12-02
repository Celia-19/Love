package com.example.love

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        val logo: ImageView = findViewById(R.id.ivLogo)
        val centerPoint: View = findViewById(R.id.centerPoint)

        // Animación de entrada del logo
        logo.animate().alpha(1f).setDuration(1500).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                startCirclesAnimation(centerPoint)
            }
        })
    }

    private fun startCirclesAnimation(centerPoint: View) {
        val duration = 2000L

        // Crear y animar círculos
        for (i in 0..3) {
            val circle = View(this).apply {
                background = ShapeDrawable(OvalShape()).apply {
                    paint.color = getColor(android.R.color.white)
                }
                layoutParams = centerPoint.layoutParams
                alpha = 0.5f
                scaleX = 0.1f
                scaleY = 0.1f
            }

            (centerPoint.parent as ViewGroup).addView(circle)

            // Configurar animación de círculo
            val scaleX = ObjectAnimator.ofFloat(circle, "scaleX", 0.1f, 3f)
            val scaleY = ObjectAnimator.ofFloat(circle, "scaleY", 0.1f, 3f)
            val translationX = ObjectAnimator.ofFloat(circle, "translationX", getTranslationX(i))
            val translationY = ObjectAnimator.ofFloat(circle, "translationY", getTranslationY(i))

            AnimatorSet().apply {
                playTogether(scaleX, scaleY, translationX, translationY)
                this.duration = duration
                start()
                doOnEnd { (circle.parent as ViewGroup).removeView(circle) }
            }
        }

        // Redirigir al inicio de sesión
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, duration)
    }

    private fun getTranslationX(index: Int) = when (index) {
        0 -> -300f
        1 -> 300f
        2 -> 300f
        3 -> -300f
        else -> 0f
    }

    private fun getTranslationY(index: Int) = when (index) {
        0 -> -300f
        1 -> -300f
        2 -> 300f
        3 -> 300f
        else -> 0f
    }

    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}