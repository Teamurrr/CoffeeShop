package com.example.coffeeshop.Activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.coffeeshop.Domain.ItemModel
import com.example.coffeeshop.Repository.CartManager
import com.example.coffeeshop.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var item: ItemModel
    private var quantity = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val product = readItemFromIntent()
        if (product == null) {
            finish()
            return
        }
        item = product

        bindItem()
        setupActions()
    }

    private fun bindItem() {
        Glide.with(this)
            .load(item.firstImageUrl())
            .placeholder(com.example.coffeeshop.R.drawable.coffee)
            .error(com.example.coffeeshop.R.drawable.coffee)
            .into(binding.productImage)

        binding.titleTxt.text = item.title
        binding.priceTxt.text = "$%.2f".format(item.price)
        binding.ratingTxt.text = "${item.rating} / 5"
        binding.descriptionTxt.text = item.description
        binding.extraTxt.text = item.extra
        binding.quantityTxt.text = quantity.toString()
    }

    private fun setupActions() {
        binding.backBtn.setOnClickListener { finish() }
        binding.cartBtn.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
        binding.minusBtn.setOnClickListener {
            if (quantity > 1) {
                quantity -= 1
                binding.quantityTxt.text = quantity.toString()
            }
        }
        binding.plusBtn.setOnClickListener {
            quantity += 1
            binding.quantityTxt.text = quantity.toString()
        }
        binding.addToCartBtn.setOnClickListener {
            CartManager.addItem(item, quantity)
            Toast.makeText(this, getString(com.example.coffeeshop.R.string.order_added), Toast.LENGTH_SHORT).show()
        }
    }

    private fun readItemFromIntent(): ItemModel? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_ITEM, ItemModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_ITEM) as? ItemModel
        }
    }

    companion object {
        const val EXTRA_ITEM = "extra_item"
    }
}
