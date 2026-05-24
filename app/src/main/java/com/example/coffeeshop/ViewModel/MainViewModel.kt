package com.example.coffeeshop.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.coffeeshop.Domain.CategoryModel
import com.example.coffeeshop.Domain.ItemModel
import com.example.coffeeshop.Domain.OrderModel
import com.example.coffeeshop.Domain.OrderResult
import com.example.coffeeshop.Repository.MainRepository

class MainViewModel : ViewModel() {
    private val repository = MainRepository()

    fun loadCategory(): LiveData<MutableList<CategoryModel>> {
        return repository.loadCategory()
    }

    fun loadPopular(): LiveData<MutableList<ItemModel>> {
        return repository.loadPopular()
    }

    fun loadSpecial(): LiveData<MutableList<ItemModel>> {
        return repository.loadSpecial()
    }

    fun loadItems(): LiveData<MutableList<ItemModel>> {
        return repository.loadItems()
    }

    fun createOrder(
        items: List<ItemModel>,
        customerNote: String,
        paymentMethod: String,
        onComplete: (OrderResult) -> Unit
    ) {
        repository.createOrder(items, customerNote, paymentMethod, onComplete)
    }

    fun loadOrders(): LiveData<MutableList<OrderModel>> {
        return repository.loadOrders()
    }

    fun confirmPayment(orderId: String, onComplete: (OrderResult) -> Unit) {
        repository.confirmPayment(orderId, onComplete)
    }

    fun completeOrder(orderId: String, onComplete: (OrderResult) -> Unit) {
        repository.completeOrder(orderId, onComplete)
    }
}
