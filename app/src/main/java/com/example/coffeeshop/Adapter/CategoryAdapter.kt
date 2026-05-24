package com.example.coffeeshop.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.coffeeshop.Domain.CategoryModel
import com.example.coffeeshop.databinding.ViewholderCategoryBinding
import android.content.Context
import com.example.coffeeshop.R


class CategoryAdapter(
    private val items: MutableList<CategoryModel>,
    private val onCategorySelected: (CategoryModel?) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.Viewholder>() {

    private lateinit var context: Context
    private var selectedPosition = -1
    private var lastSelectedPosition = -1

    class Viewholder(val binding: ViewholderCategoryBinding): RecyclerView.ViewHolder(binding.root)



    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CategoryAdapter.Viewholder {
        context=parent.context
        val binding = ViewholderCategoryBinding.inflate(LayoutInflater.from(context), parent, false)

        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: CategoryAdapter.Viewholder, position: Int) {
        val item = items[position]
        holder.binding.titleCat.text = item.title

        holder.binding.root.setOnClickListener  {
            if (selectedPosition == position) {
                lastSelectedPosition = selectedPosition
                selectedPosition = -1
                notifyItemChanged(lastSelectedPosition)
                onCategorySelected(null)
                return@setOnClickListener
            }

            lastSelectedPosition =  selectedPosition
            selectedPosition = position
            if (lastSelectedPosition >= 0) {
                notifyItemChanged(lastSelectedPosition)
            }
            notifyItemChanged(selectedPosition)
            onCategorySelected(item)
        }

        if(selectedPosition==position){
            holder.binding.titleCat.setBackgroundResource(R.drawable.brown_bg)
        }
        else{
            holder.binding.titleCat.setBackgroundResource(R.drawable.dark_brown_bg)
        }
    }

    override fun getItemCount(): Int = items.size
}
