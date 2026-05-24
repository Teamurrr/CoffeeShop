package com.example.coffeeshop.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshop.Adapter.OrderAdapter
import com.example.coffeeshop.Domain.OrderModel
import com.example.coffeeshop.Domain.OrderStatus
import com.example.coffeeshop.Domain.UserRole
import com.example.coffeeshop.Repository.SessionManager
import com.example.coffeeshop.ViewModel.MainViewModel
import com.example.coffeeshop.databinding.ActivityOrdersBinding

class OrdersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrdersBinding
    private val viewModel = MainViewModel()
    private var roleMode = OrderAdapter.MODE_CASHIER
    private var allOrders: List<OrderModel> = emptyList()
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var sessionManager: SessionManager
    private var canSwitchTabs = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        val session = sessionManager.getSession()
        if (session == null || (session.role != UserRole.CASHIER && session.role != UserRole.BARISTA && session.role != UserRole.ADMIN)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
            return
        }

        canSwitchTabs = session.role == UserRole.ADMIN || intent.getStringExtra(EXTRA_ROLE_MODE) == ROLE_ADMIN
        roleMode = when {
            intent.getStringExtra(EXTRA_ROLE_MODE) == ROLE_BARISTA -> OrderAdapter.MODE_BARISTA
            session.role == UserRole.BARISTA -> OrderAdapter.MODE_BARISTA
            else -> OrderAdapter.MODE_CASHIER
        }

        setupRecycler()
        setupActions()
        observeOrders()
        renderRoleUi()
    }

    private fun setupRecycler() {
        orderAdapter = OrderAdapter(emptyList(), roleMode) { order ->
            when (roleMode) {
                OrderAdapter.MODE_CASHIER -> confirmPayment(order.id)
                OrderAdapter.MODE_BARISTA -> completeOrder(order.id)
            }
        }
        binding.ordersRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.ordersRecyclerView.adapter = orderAdapter
    }

    private fun setupActions() {
        binding.backBtn.setOnClickListener { finish() }
        binding.logoutBtn.setOnClickListener {
            sessionManager.clear()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
        binding.cashierTab.setOnClickListener {
            if (!canSwitchTabs) return@setOnClickListener
            roleMode = OrderAdapter.MODE_CASHIER
            setupRecycler()
            renderRoleUi()
            renderOrders()
        }
        binding.baristaTab.setOnClickListener {
            if (!canSwitchTabs) return@setOnClickListener
            roleMode = OrderAdapter.MODE_BARISTA
            setupRecycler()
            renderRoleUi()
            renderOrders()
        }
    }

    private fun observeOrders() {
        viewModel.loadOrders().observe(this) { orders ->
            allOrders = orders
            renderOrders()
        }
    }

    private fun renderRoleUi() {
        val cashierMode = roleMode == OrderAdapter.MODE_CASHIER
        binding.cashierTab.setBackgroundResource(
            if (cashierMode) com.example.coffeeshop.R.drawable.orange_bg
            else com.example.coffeeshop.R.drawable.brown_bg
        )
        binding.baristaTab.setBackgroundResource(
            if (cashierMode) com.example.coffeeshop.R.drawable.brown_bg
            else com.example.coffeeshop.R.drawable.orange_bg
        )
        binding.subtitleTxt.text = if (cashierMode) {
            getString(com.example.coffeeshop.R.string.cashier_hint)
        } else {
            getString(com.example.coffeeshop.R.string.barista_hint)
        }
        binding.boardRoleTxt.text = if (cashierMode) "Cashier queue" else "Barista queue"
        val alpha = if (canSwitchTabs) 1f else 0.8f
        binding.cashierTab.alpha = alpha
        binding.baristaTab.alpha = alpha
    }

    private fun renderOrders() {
        val filtered = when (roleMode) {
            OrderAdapter.MODE_CASHIER -> allOrders.filter { it.status == OrderStatus.PENDING }
            OrderAdapter.MODE_BARISTA -> allOrders.filter { it.status == OrderStatus.PAID }
            else -> emptyList()
        }
        orderAdapter.updateItems(filtered)
        binding.emptyOrdersTxt.visibility =
            if (filtered.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun confirmPayment(orderId: String) {
        viewModel.confirmPayment(orderId) { result ->
            runOnUiThread {
                if (!result.success) {
                    Toast.makeText(this, result.errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun completeOrder(orderId: String) {
        viewModel.completeOrder(orderId) { result ->
            runOnUiThread {
                if (!result.success) {
                    Toast.makeText(this, result.errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        const val EXTRA_ROLE_MODE = "extra_role_mode"
        const val ROLE_BARISTA = "role_barista"
        const val ROLE_ADMIN = "role_admin"
    }
}
