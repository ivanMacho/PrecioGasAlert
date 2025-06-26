package com.machoapps.preciogasalert

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val icon = findViewById<ImageView>(R.id.splashIcon)
        val title = findViewById<TextView>(R.id.splashTitle)

        // Animación Material: Fade-in
        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.duration = 800
        icon.startAnimation(fadeIn)
        title.startAnimation(fadeIn)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2000)
    }
} 