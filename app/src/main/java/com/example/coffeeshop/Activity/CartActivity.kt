package com.example.coffeeshop.Activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshop.Adapter.CartAdapter
import com.example.coffeeshop.Domain.PaymentMethod
import com.example.coffeeshop.Domain.UserRole
import com.example.coffeeshop.Repository.CartManager
import com.example.coffeeshop.Repository.SessionManager
import com.example.coffeeshop.ViewModel.MainViewModel
import com.example.coffeeshop.databinding.ActivityCartBinding

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private val viewModel = MainViewModel()
    private lateinit var cartAdapter: CartAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        val session = sessionManager.getSession()
        if (session == null || session.role != UserRole.CUSTOMER) {
            startActivity(android.content.Intent(this, LoginActivity::class.java))
            finishAffinity()
            return
        }

        setupRecycler()
        setupActions()
        observeCart()
        renderReservationContext()
    }

    private fun setupRecycler() {
        cartAdapter = CartAdapter(emptyList()) { item, quantity ->
            CartManager.updateQuantity(item.id, quantity)
        }
        binding.cartRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.cartRecyclerView.adapter = cartAdapter
    }

    private fun setupActions() {
        binding.backBtn.setOnClickListener { finish() }
        binding.checkoutBtn.setOnClickListener {
            val currentItems = CartManager.getItems()
            if (currentItems.isEmpty()) {
                return@setOnClickListener
            }

            binding.checkoutBtn.isEnabled = false
            val paymentMethod = if (binding.qrRadioBtn.isChecked) PaymentMethod.QR else PaymentMethod.CARD
            val session = sessionManager.getSession() ?: return@setOnClickListener
            val reservationContext = sessionManager.getReservationContext()
            viewModel.createOrder(
                currentItems,
                binding.noteEditText.text.toString(),
                paymentMethod,
                session,
                reservationContext
            ) { result ->
                runOnUiThread {
                    binding.checkoutBtn.isEnabled = true
                    if (result.success) {
                        CartManager.clear()
                        sessionManager.clearReservationContext()
                        Toast.makeText(
                            this,
                            getString(com.example.coffeeshop.R.string.order_created, result.orderId ?: ""),
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            result.errorMessage ?: getString(com.example.coffeeshop.R.string.order_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun observeCart() {
        CartManager.observeCart().observe(this) { items ->
            cartAdapter.updateItems(items)
            binding.emptyCartTxt.visibility = if (items.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            binding.itemsCountTxt.text = getString(
                com.example.coffeeshop.R.string.items_in_order,
                CartManager.totalCount()
            )
            binding.totalTxt.text = "$%.2f".format(CartManager.totalPrice())
            binding.checkoutBtn.isEnabled = items.isNotEmpty()
        }
    }

    private fun renderReservationContext() {
        val reservation = sessionManager.getReservationContext()
        if (reservation == null) {
            binding.reservationContextTxt.visibility = android.view.View.GONE
        } else {
            binding.reservationContextTxt.visibility = android.view.View.VISIBLE
            binding.reservationContextTxt.text =
                "Preorder for table ${reservation.tableNumber} at ${reservation.reservationTime}"
        }
    }
}
