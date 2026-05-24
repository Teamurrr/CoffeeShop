package com.example.coffeeshop.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.coffeeshop.Domain.CategoryModel
import com.example.coffeeshop.Domain.DataActionResult
import com.example.coffeeshop.Domain.ItemModel
import com.example.coffeeshop.Domain.OrderModel
import com.example.coffeeshop.Domain.OrderResult
import com.example.coffeeshop.Domain.UserSession
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
        session: UserSession,
        onComplete: (OrderResult) -> Unit
    ) {
        repository.createOrder(items, customerNote, paymentMethod, session, onComplete)
    }

    fun loadOrders(): LiveData<MutableList<OrderModel>> {
        return repository.loadOrders()
    }

    fun loadOrdersForCustomer(customerId: String): LiveData<MutableList<OrderModel>> {
        return repository.loadOrdersForCustomer(customerId)
    }

    fun confirmPayment(orderId: String, onComplete: (OrderResult) -> Unit) {
        repository.confirmPayment(orderId, onComplete)
    }

    fun completeOrder(orderId: String, onComplete: (OrderResult) -> Unit) {
        repository.completeOrder(orderId, onComplete)
    }

    fun saveItem(item: ItemModel, onComplete: (DataActionResult) -> Unit) {
        repository.saveItem(item, onComplete)
    }

    fun deleteItem(itemId: String, onComplete: (DataActionResult) -> Unit) {
        repository.deleteItem(itemId, onComplete)
    }

    fun saveCategory(category: CategoryModel, onComplete: (DataActionResult) -> Unit) {
        repository.saveCategory(category, onComplete)
    }

    fun deleteCategory(categoryId: Int, onComplete: (DataActionResult) -> Unit) {
        repository.deleteCategory(categoryId, onComplete)
    }
}
