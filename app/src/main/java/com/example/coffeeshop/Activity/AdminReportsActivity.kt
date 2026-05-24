package com.example.coffeeshop.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshop.Adapter.ReservationAdapter
import com.example.coffeeshop.Adapter.TableAdapter
import com.example.coffeeshop.Domain.UserRole
import com.example.coffeeshop.Repository.SessionManager
import com.example.coffeeshop.ViewModel.MainViewModel
import com.example.coffeeshop.databinding.ActivityAdminReportsBinding

class AdminReportsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminReportsBinding
    private val viewModel = MainViewModel()
    private lateinit var reservationAdapter: ReservationAdapter
    private lateinit var tableAdapter: TableAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdminReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = SessionManager(this).getSession()
        if (session == null || session.role != UserRole.ADMIN) {
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
            return
        }

        binding.backBtn.setOnClickListener { finish() }
        reservationAdapter = ReservationAdapter(emptyList(), true) { reservation ->
            viewModel.cancelReservation(reservation) { result ->
                runOnUiThread {
                    if (result.success) {
                        Toast.makeText(this, "Reservation cancelled", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, result.errorMessage ?: "Unable to cancel reservation", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        binding.reservationsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.reservationsRecyclerView.adapter = reservationAdapter

        tableAdapter = TableAdapter(emptyList()) { }
        binding.tablesRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.tablesRecyclerView.adapter = tableAdapter

        viewModel.loadOrders().observe(this) { orders ->
            val summary = viewModel.buildReportSummary(orders)
            binding.revenueTxt.text = "$%.2f".format(summary.totalRevenue)
            binding.ordersTxt.text = "Orders: ${summary.totalOrders}"
            binding.averageBillTxt.text = "Average bill: $%.2f".format(summary.averageBill)
            binding.statusBreakdownTxt.text =
                "Pending ${summary.pendingOrders} - Paid ${summary.paidOrders} - Completed ${summary.completedOrders}"
        }

        viewModel.loadTables().observe(this) { tables ->
            tableAdapter.updateItems(tables)
        }

        viewModel.loadReservations().observe(this) { reservations ->
            reservationAdapter.updateItems(reservations)
            binding.emptyReservationsTxt.visibility =
                if (reservations.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }
}
