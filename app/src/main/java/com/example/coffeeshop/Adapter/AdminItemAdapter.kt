package com.example.coffeeshop.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.Domain.ItemModel
import com.example.coffeeshop.databinding.ViewholderAdminItemBinding

class AdminItemAdapter(
    private var items: List<ItemModel>,
    private val categoryNameProvider: (String) -> String,
    private val onEditClicked: (ItemModel) -> Unit,
    private val onDeleteClicked: (ItemModel) -> Unit
) : RecyclerView.Adapter<AdminItemAdapter.ViewHolder>() {

    class ViewHolder(val binding: ViewholderAdminItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderAdminItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.titleTxt.text = item.title
        holder.binding.categoryTxt.text = categoryNameProvider(item.categoryId)
        holder.binding.priceTxt.text = "$%.2f".format(item.price)
        holder.binding.imageTxt.text = item.firstImageUrl().ifBlank { "No image URL" }
        holder.binding.editBtn.setOnClickListener { onEditClicked(item) }
        holder.binding.deleteBtn.setOnClickListener { onDeleteClicked(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<ItemModel>) {
        items = newItems
        notifyDataSetChanged()
    }
}
