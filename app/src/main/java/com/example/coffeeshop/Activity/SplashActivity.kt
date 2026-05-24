package com.example.coffeeshop.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshop.Domain.UserRole
import com.example.coffeeshop.Repository.SessionManager
import com.example.coffeeshop.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startBtn.setOnClickListener {
            val session = SessionManager(this).getSession()
            val target = when (session?.role) {
                UserRole.CUSTOMER -> MainActivity::class.java
                UserRole.CASHIER, UserRole.BARISTA -> OrdersActivity::class.java
                UserRole.ADMIN -> AdminActivity::class.java
                else -> LoginActivity::class.java
            }
            startActivity(Intent(this, target))
        }
    }
}
