package com.example.coffeeshop.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.Domain.CategoryModel
import com.example.coffeeshop.databinding.ViewholderAdminCategoryBinding

class AdminCategoryAdapter(
    private var items: List<CategoryModel>,
    private val onEditClicked: (CategoryModel) -> Unit,
    private val onDeleteClicked: (CategoryModel) -> Unit
) : RecyclerView.Adapter<AdminCategoryAdapter.ViewHolder>() {

    class ViewHolder(val binding: ViewholderAdminCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderAdminCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.titleTxt.text = item.title
        holder.binding.idTxt.text = "ID ${item.id}"
        holder.binding.editBtn.setOnClickListener { onEditClicked(item) }
        holder.binding.deleteBtn.setOnClickListener { onDeleteClicked(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<CategoryModel>) {
        items = newItems
        notifyDataSetChanged()
    }
}
