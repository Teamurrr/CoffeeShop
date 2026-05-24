package com.example.coffeeshop.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.coffeeshop.Domain.UserRole
import com.example.coffeeshop.Repository.SessionManager
import com.example.coffeeshop.ViewModel.MainViewModel
import com.example.coffeeshop.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private val viewModel = MainViewModel()
    private var selectedRole: String = UserRole.CUSTOMER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupActions()
        renderRoleUi()
    }

    private fun setupActions() {
        binding.customerTab.setOnClickListener {
            selectedRole = UserRole.CUSTOMER
            renderRoleUi()
        }
        binding.cashierTab.setOnClickListener {
            selectedRole = UserRole.CASHIER
            renderRoleUi()
        }
        binding.baristaTab.setOnClickListener {
            selectedRole = UserRole.BARISTA
            renderRoleUi()
        }
        binding.adminTab.setOnClickListener {
            selectedRole = UserRole.ADMIN
            renderRoleUi()
        }
        binding.loginBtn.setOnClickListener {
            submit()
        }
    }

    private fun renderRoleUi() {
        val customer = selectedRole == UserRole.CUSTOMER
        binding.customerFieldsGroup.visibility = if (customer) android.view.View.VISIBLE else android.view.View.GONE
        binding.staffFieldsGroup.visibility = if (customer) android.view.View.GONE else android.view.View.VISIBLE
        binding.loginTitleTxt.text = if (customer) "Customer sign in" else "Staff access"
        binding.loginHintTxt.text = when (selectedRole) {
            UserRole.CASHIER -> "Use cashier PIN 1111 for demo access"
            UserRole.BARISTA -> "Use barista PIN 2222 for demo access"
            UserRole.ADMIN -> "Use admin PIN 3333 for demo access"
            else -> "Use your name and email to keep your orders linked to your profile"
        }

        binding.customerTab.setBackgroundResource(if (selectedRole == UserRole.CUSTOMER) com.example.coffeeshop.R.drawable.orange_bg else com.example.coffeeshop.R.drawable.brown_bg)
        binding.cashierTab.setBackgroundResource(if (selectedRole == UserRole.CASHIER) com.example.coffeeshop.R.drawable.orange_bg else com.example.coffeeshop.R.drawable.brown_bg)
        binding.baristaTab.setBackgroundResource(if (selectedRole == UserRole.BARISTA) com.example.coffeeshop.R.drawable.orange_bg else com.example.coffeeshop.R.drawable.brown_bg)
        binding.adminTab.setBackgroundResource(if (selectedRole == UserRole.ADMIN) com.example.coffeeshop.R.drawable.orange_bg else com.example.coffeeshop.R.drawable.brown_bg)
    }

    private fun submit() {
        binding.loginBtn.isEnabled = false
        if (selectedRole == UserRole.CUSTOMER) {
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            if (name.length < 2 || !email.contains("@")) {
                binding.loginBtn.isEnabled = true
                Toast.makeText(this, "Enter a valid name and email", Toast.LENGTH_LONG).show()
                return
            }
            viewModel.signInCustomer(name, email) { result ->
                runOnUiThread {
                    binding.loginBtn.isEnabled = true
                    if (!result.success || result.session == null) {
                        Toast.makeText(this, result.errorMessage ?: "Unable to sign in", Toast.LENGTH_LONG).show()
                        return@runOnUiThread
                    }
                    sessionManager.saveSession(result.session)
                    routeByRole()
                }
            }
        } else {
            val staffName = binding.staffNameEditText.text.toString().trim().ifBlank {
                selectedRole.replaceFirstChar { it.titlecase() }
            }
            val pin = binding.pinEditText.text.toString().trim()
            if (pin.isBlank()) {
                binding.loginBtn.isEnabled = true
                Toast.makeText(this, "Enter PIN for selected role", Toast.LENGTH_LONG).show()
                return
            }
            viewModel.signInStaff(selectedRole, pin, staffName) { result ->
                runOnUiThread {
                    binding.loginBtn.isEnabled = true
                    if (!result.success || result.session == null) {
                        Toast.makeText(this, result.errorMessage ?: "Unable to sign in", Toast.LENGTH_LONG).show()
                        return@runOnUiThread
                    }
                    sessionManager.saveSession(result.session)
                    routeByRole()
                }
            }
        }
    }

    private fun routeByRole() {
        val session = sessionManager.getSession() ?: return
        val target = when (session.role) {
            UserRole.CUSTOMER -> MainActivity::class.java
            UserRole.CASHIER, UserRole.BARISTA -> OrdersActivity::class.java
            UserRole.ADMIN -> AdminActivity::class.java
            else -> MainActivity::class.java
        }
        startActivity(Intent(this, target))
        finishAffinity()
    }
}
