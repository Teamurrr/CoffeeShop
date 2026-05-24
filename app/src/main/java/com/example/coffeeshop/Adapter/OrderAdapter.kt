package com.example.coffeeshop.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.Domain.OrderModel
import com.example.coffeeshop.Domain.OrderStatus
import com.example.coffeeshop.databinding.ViewholderOrderBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderAdapter(
    private var items: List<OrderModel>,
    private val mode: String,
    private val onPrimaryAction: (OrderModel) -> Unit
) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {

    class ViewHolder(val binding: ViewholderOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = items[position]
        val formatter = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())

        holder.binding.orderIdTxt.text = "#${order.id.takeLast(6)}"
        holder.binding.statusTxt.text = order.status
        holder.binding.paymentTxt.text = "${order.paymentMethod} • ${order.paymentStatus}"
        holder.binding.totalTxt.text = "$%.2f".format(order.totalAmount)
        holder.binding.itemsTxt.text = "${order.items.sumOf { it.quantity }} items"
        holder.binding.timeTxt.text = formatter.format(Date(order.createdAt))
        holder.binding.noteTxt.text = buildNote(order)

        when (mode) {
            MODE_CASHIER -> {
                holder.binding.primaryActionBtn.text =
                    if (order.status == OrderStatus.PENDING) "Confirm payment" else "Payment done"
                holder.binding.primaryActionBtn.isEnabled = order.status == OrderStatus.PENDING
            }

            MODE_BARISTA -> {
                holder.binding.primaryActionBtn.text =
                    if (order.status == OrderStatus.PAID) "Mark completed" else "Waiting for payment"
                holder.binding.primaryActionBtn.isEnabled = order.status == OrderStatus.PAID
            }

            MODE_CUSTOMER -> {
                holder.binding.primaryActionBtn.text = "Status tracked automatically"
                holder.binding.primaryActionBtn.isEnabled = false
            }

            else -> {
                holder.binding.primaryActionBtn.text = "Track order"
                holder.binding.primaryActionBtn.isEnabled = false
            }
        }

        holder.binding.primaryActionBtn.setOnClickListener {
            onPrimaryAction(order)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<OrderModel>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun buildNote(order: OrderModel): String {
        val customerLine = if (mode == MODE_CUSTOMER) "" else "${order.customerName} ${order.customerEmail}".trim()
        val noteLine = order.customerNote.ifBlank { "No customer note" }
        return listOf(customerLine, noteLine).filter { it.isNotBlank() }.joinToString("\n")
    }

    companion object {
        const val MODE_CASHIER = "cashier"
        const val MODE_BARISTA = "barista"
        const val MODE_CUSTOMER = "customer"
    }
}
