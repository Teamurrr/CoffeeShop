package com.example.coffeeshop.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.coffeeshop.Adapter.TableAdapter
import com.example.coffeeshop.Domain.ReservationContext
import com.example.coffeeshop.Domain.UserRole
import com.example.coffeeshop.Repository.SessionManager
import com.example.coffeeshop.ViewModel.MainViewModel
import com.example.coffeeshop.databinding.ActivityReservationBinding
import com.example.coffeeshop.databinding.DialogReservationBinding

class ReservationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReservationBinding
    private val viewModel = MainViewModel()
    private lateinit var sessionManager: SessionManager
    private lateinit var tableAdapter: TableAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReservationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        val session = sessionManager.getSession()
        if (session == null || session.role != UserRole.CUSTOMER) {
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
            return
        }

        binding.backBtn.setOnClickListener { finish() }
        tableAdapter = TableAdapter(emptyList()) { table ->
            if (!table.status.equals("Free", true)) {
                Toast.makeText(this, "This table is already reserved", Toast.LENGTH_LONG).show()
                return@TableAdapter
            }
            openReservationDialog(table)
        }
        binding.tablesRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.tablesRecyclerView.adapter = tableAdapter

        viewModel.loadTables().observe(this) { tables ->
            tableAdapter.updateItems(tables)
            binding.emptyTablesTxt.visibility = if (tables.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun openReservationDialog(table: com.example.coffeeshop.Domain.TableModel) {
        val dialogBinding = DialogReservationBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

        dialogBinding.dialogTitleTxt.text = "Reserve table ${table.number}"
        dialogBinding.cancelBtn.setOnClickListener { dialog.dismiss() }
        dialogBinding.confirmBtn.setOnClickListener {
            val date = dialogBinding.dateEditText.text.toString().trim()
            val time = dialogBinding.timeEditText.text.toString().trim()
            if (date.isBlank() || time.isBlank()) {
                Toast.makeText(this, "Enter date and time", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val reservationTime = "$date, $time"
            val preorderRequested = dialogBinding.preorderSwitch.isChecked
            val session = sessionManager.getSession() ?: return@setOnClickListener
            viewModel.createReservation(table, reservationTime, session, preorderRequested) { result ->
                runOnUiThread {
                    if (result.success) {
                        val context = ReservationContext(
                            reservationId = result.id.orEmpty(),
                            tableId = table.id,
                            tableNumber = table.number,
                            reservationTime = reservationTime
                        )
                        sessionManager.saveReservationContext(context)
                        dialog.dismiss()
                        if (preorderRequested) {
                            Toast.makeText(this, "Reservation created. Add preorder items, then pay in cart.", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Reservation created", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this, result.errorMessage ?: "Unable to reserve table", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        dialog.show()
    }
}
