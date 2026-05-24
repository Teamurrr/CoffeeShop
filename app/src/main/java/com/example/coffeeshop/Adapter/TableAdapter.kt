package com.example.coffeeshop.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.Domain.TableModel
import com.example.coffeeshop.databinding.ViewholderTableBinding

class TableAdapter(
    private var items: List<TableModel>,
    private val onTableClicked: (TableModel) -> Unit
) : RecyclerView.Adapter<TableAdapter.ViewHolder>() {

    class ViewHolder(val binding: ViewholderTableBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderTableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val table = items[position]
        val isFree = table.status.equals("Free", true)
        holder.binding.tableNumberTxt.text = table.number.toString()
        holder.binding.statusTxt.text = if (isFree) "Free" else "Reserved"
        holder.binding.root.setCardBackgroundColor(
            holder.itemView.context.getColor(
                if (isFree) com.example.coffeeshop.R.color.table_green
                else com.example.coffeeshop.R.color.table_red
            )
        )
        holder.binding.root.setOnClickListener { onTableClicked(table) }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<TableModel>) {
        items = newItems
        notifyDataSetChanged()
    }
}
