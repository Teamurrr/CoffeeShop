package com.example.coffeeshop.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeshop.Domain.ItemModel
import com.example.coffeeshop.databinding.ViewholderSpecialBinding

class SpecialAdapter(val items: MutableList<ItemModel>)
    : RecyclerView.Adapter<SpecialAdapter.Viewholder>() {
    class Viewholder(val binding: ViewholderSpecialBinding):
        RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SpecialAdapter.Viewholder {
        val binding = ViewholderSpecialBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return Viewholder(binding)

    }

    override fun onBindViewHolder(holder: SpecialAdapter.Viewholder, position: Int) {
        holder.binding.titleTxt.text=items[position].title
        holder.binding.priceTxt.text="$"+items[position].price.toString()
        holder.binding.ratingBar.rating=items[position].rating.toFloat()

        Glide.with(holder.itemView.context)
            .load(items[position].picUrl[0])
            .into(holder.binding.picMain)
    }

    override fun getItemCount(): Int = items.size
}