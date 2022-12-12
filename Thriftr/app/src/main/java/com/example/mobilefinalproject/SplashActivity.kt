package com.example.mobilefinalproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashActivity : AppCompatActivity() {
    var handler: Handler? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        setContentView(R.layout.activity_splash)
        handler = Handler()
        handler!!.postDelayed({
            val intent = Intent(this@SplashActivity, LogInActivity::class.java)
            startActivity(intent)
            finish()
        }, 5000)
    }
}