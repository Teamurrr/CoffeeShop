package com.example.coffeeshop.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeshop.Domain.ItemModel
import com.example.coffeeshop.databinding.ViewholderSpecialBinding

class SpecialAdapter(
    private val items: MutableList<ItemModel>,
    private val onItemClicked: (ItemModel) -> Unit
)
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
        val item = items[position]
        holder.binding.titleTxt.text=item.title
        holder.binding.priceTxt.text="$%.2f".format(item.price)
        holder.binding.ratingBar.rating=item.rating.toFloat()

        Glide.with(holder.itemView.context)
            .load(item.firstImageUrl())
            .placeholder(com.example.coffeeshop.R.drawable.coffee)
            .error(com.example.coffeeshop.R.drawable.coffee)
            .into(holder.binding.picMain)

        holder.binding.root.setOnClickListener {
            onItemClicked(item)
        }
    }

    override fun getItemCount(): Int = items.size
}
