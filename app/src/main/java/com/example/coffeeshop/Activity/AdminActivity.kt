package com.example.coffeeshop.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshop.Repository.SessionManager
import com.example.coffeeshop.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.openOrderBoardBtn.setOnClickListener {
            startActivity(Intent(this, OrdersActivity::class.java).apply {
                putExtra(OrdersActivity.EXTRA_ROLE_MODE, OrdersActivity.ROLE_ADMIN)
            })
        }
        binding.openMenuManagementBtn.setOnClickListener {
            startActivity(Intent(this, AdminMenuPreviewActivity::class.java))
        }
        binding.logoutBtn.setOnClickListener {
            SessionManager(this).clear()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }
}
