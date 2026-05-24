package com.example.coffeeshop.Adapter

import android.content.Context
import android.renderscript.ScriptGroup
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coffeeshop.Domain.ItemModel
import com.example.coffeeshop.databinding.ViewholderPopularBinding

class PopularAdapter(
    private val items: MutableList<ItemModel>,
    private val onItemClicked: (ItemModel) -> Unit
):
    RecyclerView.Adapter<PopularAdapter.Viewholder>(){

        lateinit var context: Context

    class Viewholder(val binding: ViewholderPopularBinding ):
        RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PopularAdapter.Viewholder {
        context = parent.context
        val binding = ViewholderPopularBinding.inflate(LayoutInflater.from(context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: PopularAdapter.Viewholder, position: Int) {
        val item = items[position]
        holder.binding.titleTxt.text=item.title
        holder.binding.extraTxt.text=item.extra
        holder.binding.priceTxt.text="$%.2f".format(item.price)

        Glide.with(context)
            .load(item.firstImageUrl())
            .placeholder(com.example.coffeeshop.R.drawable.coffee)
            .error(com.example.coffeeshop.R.drawable.coffee)
            .into(holder.binding.pic)

        holder.binding.root.setOnClickListener {
            onItemClicked(item)
        }
        holder.binding.imageView3.setOnClickListener {
            onItemClicked(item)
        }
    }

    override fun getItemCount(): Int = items.size
}
