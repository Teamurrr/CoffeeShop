package com.example.coffeeshop.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.Domain.ReservationModel
import com.example.coffeeshop.databinding.ViewholderReservationBinding

class ReservationAdapter(
    private var items: List<ReservationModel>,
    private val showCancel: Boolean,
    private val onCancelClicked: (ReservationModel) -> Unit
) : RecyclerView.Adapter<ReservationAdapter.ViewHolder>() {

    class ViewHolder(val binding: ViewholderReservationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderReservationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.titleTxt.text = "Table ${item.tableNumber}"
        holder.binding.customerTxt.text = "${item.customerName} • ${item.customerEmail}"
        holder.binding.timeTxt.text = item.reservationTime
        holder.binding.statusTxt.text = item.status
        holder.binding.preorderTxt.text = if (item.preorderRequested) "Preorder: yes" else "Preorder: no"
        holder.binding.cancelBtn.visibility = if (showCancel && item.status == "Reserved") android.view.View.VISIBLE else android.view.View.GONE
        holder.binding.cancelBtn.setOnClickListener { onCancelClicked(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<ReservationModel>) {
        items = newItems
        notifyDataSetChanged()
    }
}
