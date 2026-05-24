package com.example.coffeeshop.Repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.coffeeshop.Domain.ItemModel

object CartManager {
    private val cartItems = mutableListOf<ItemModel>()
    private val cartLiveData = MutableLiveData<List<ItemModel>>(emptyList())

    fun observeCart(): LiveData<List<ItemModel>> = cartLiveData

    fun getItems(): List<ItemModel> = cartItems.map { it.copy(picUrl = ArrayList(it.picUrl)) }

    fun addItem(item: ItemModel, quantity: Int) {
        val existingItem = cartItems.firstOrNull { it.id == item.id && it.title == item.title }
        if (existingItem != null) {
            existingItem.numberInCart += quantity
        } else {
            cartItems.add(item.copy(picUrl = ArrayList(item.picUrl), numberInCart = quantity))
        }
        publish()
    }

    fun updateQuantity(itemId: String, quantity: Int) {
        val target = cartItems.firstOrNull { it.id == itemId }
        if (target != null) {
            if (quantity <= 0) {
                cartItems.remove(target)
            } else {
                target.numberInCart = quantity
            }
            publish()
        }
    }

    fun clear() {
        cartItems.clear()
        publish()
    }

    fun totalPrice(): Double = cartItems.sumOf { it.price * it.numberInCart }

    fun totalCount(): Int = cartItems.sumOf { it.numberInCart }

    private fun publish() {
        cartLiveData.value = getItems()
    }
}
