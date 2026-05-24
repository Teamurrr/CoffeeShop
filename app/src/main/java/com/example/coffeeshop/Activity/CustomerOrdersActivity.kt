package com.example.coffeeshop.Activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshop.Adapter.OrderAdapter
import com.example.coffeeshop.Repository.SessionManager
import com.example.coffeeshop.databinding.ActivityCustomerOrdersBinding

class CustomerOrdersActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomerOrdersBinding
    private lateinit var orderAdapter: OrderAdapter
    private val viewModel = com.example.coffeeshop.ViewModel.MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCustomerOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = SessionManager(this).getSession()
        if (session == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
            return
        }

        binding.backBtn.setOnClickListener { finish() }
        binding.logoutBtn.setOnClickListener {
            SessionManager(this).clear()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
        binding.customerNameTxt.text = session.name

        orderAdapter = OrderAdapter(emptyList(), OrderAdapter.MODE_CUSTOMER) { }
        binding.ordersRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.ordersRecyclerView.adapter = orderAdapter

        viewModel.loadOrdersForCustomer(session.userId).observe(this) { orders ->
            orderAdapter.updateItems(orders)
            binding.emptyOrdersTxt.visibility =
                if (orders.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }
}
