package com.example.coffeeshop.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeshop.Domain.ItemModel
import com.example.coffeeshop.databinding.ViewholderMenuBinding

class MenuAdapter(
    private var items: List<ItemModel>,
    private val onItemClicked: (ItemModel) -> Unit,
    private val onAddClicked: (ItemModel) -> Unit
) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    class ViewHolder(val binding: ViewholderMenuBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.titleTxt.text = item.title
        holder.binding.descriptionTxt.text = item.description
        holder.binding.extraTxt.text = item.extra
        holder.binding.priceTxt.text = "$%.2f".format(item.price)
        holder.binding.ratingTxt.text = item.rating.toString()

        Glide.with(holder.itemView.context)
            .load(item.firstImageUrl())
            .placeholder(com.example.coffeeshop.R.drawable.coffee)
            .error(com.example.coffeeshop.R.drawable.coffee)
            .into(holder.binding.pic)

        holder.binding.root.setOnClickListener { onItemClicked(item) }
        holder.binding.addBtn.setOnClickListener { onAddClicked(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<ItemModel>) {
        items = newItems
        notifyDataSetChanged()
    }
}
