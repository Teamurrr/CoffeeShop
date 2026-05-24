package com.example.coffeeshop.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeshop.Domain.ItemModel
import com.example.coffeeshop.databinding.ViewholderCartBinding

class CartAdapter(
    private var items: List<ItemModel>,
    private val onQuantityChanged: (ItemModel, Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    class ViewHolder(val binding: ViewholderCartBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.titleTxt.text = item.title
        holder.binding.extraTxt.text = item.extra
        holder.binding.priceTxt.text = "$%.2f".format(item.price * item.numberInCart)
        holder.binding.quantityTxt.text = item.numberInCart.toString()

        Glide.with(holder.itemView.context)
            .load(item.firstImageUrl())
            .placeholder(com.example.coffeeshop.R.drawable.coffee)
            .error(com.example.coffeeshop.R.drawable.coffee)
            .into(holder.binding.pic)

        holder.binding.minusBtn.setOnClickListener {
            onQuantityChanged(item, item.numberInCart - 1)
        }
        holder.binding.plusBtn.setOnClickListener {
            onQuantityChanged(item, item.numberInCart + 1)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<ItemModel>) {
        items = newItems
        notifyDataSetChanged()
    }
}
