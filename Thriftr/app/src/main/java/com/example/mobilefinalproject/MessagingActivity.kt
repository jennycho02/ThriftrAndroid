package com.example.mobilefinalproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mobilefinalproject.databinding.ActivityMessagingBinding

class MessagingActivity : AppCompatActivity() {

    lateinit var binding: ActivityMessagingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}