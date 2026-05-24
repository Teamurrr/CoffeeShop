package com.example.coffeeshop.Activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshop.Adapter.OrderAdapter
import com.example.coffeeshop.Domain.OrderModel
import com.example.coffeeshop.Domain.OrderStatus
import com.example.coffeeshop.ViewModel.MainViewModel
import com.example.coffeeshop.databinding.ActivityOrdersBinding

class OrdersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrdersBinding
    private val viewModel = MainViewModel()
    private var roleMode = OrderAdapter.MODE_CASHIER
    private var allOrders: List<OrderModel> = emptyList()
    private lateinit var orderAdapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        binding.cashierTab.setOnClickListener {
            roleMode = OrderAdapter.MODE_CASHIER
            setupRecycler()
            renderRoleUi()
            renderOrders()
        }
        binding.baristaTab.setOnClickListener {
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
}
