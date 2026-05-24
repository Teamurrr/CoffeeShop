package com.example.coffeeshop.ViewModel

import com.example.coffeeshop.Domain.AuthResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.coffeeshop.Domain.CategoryModel
import com.example.coffeeshop.Domain.DataActionResult
import com.example.coffeeshop.Domain.ItemModel
import com.example.coffeeshop.Domain.OrderModel
import com.example.coffeeshop.Domain.OrderResult
import com.example.coffeeshop.Domain.ReportSummary
import com.example.coffeeshop.Domain.ReservationContext
import com.example.coffeeshop.Domain.ReservationModel
import com.example.coffeeshop.Domain.TableModel
import com.example.coffeeshop.Domain.UserSession
import com.example.coffeeshop.Repository.MainRepository

class MainViewModel : ViewModel() {
    private val repository = MainRepository()

    fun loadCategory(): LiveData<MutableList<CategoryModel>> {
        return repository.loadCategory()
    }

    fun signInCustomer(name: String, email: String, onComplete: (AuthResult) -> Unit) {
        repository.signInCustomer(name, email, onComplete)
    }

    fun signInStaff(role: String, pin: String, displayName: String, onComplete: (AuthResult) -> Unit) {
        repository.signInStaff(role, pin, displayName, onComplete)
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
        reservationContext: ReservationContext?,
        onComplete: (OrderResult) -> Unit
    ) {
        repository.createOrder(items, customerNote, paymentMethod, session, reservationContext, onComplete)
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

    fun loadTables(): LiveData<MutableList<TableModel>> {
        return repository.loadTables()
    }

    fun saveTable(table: TableModel, onComplete: (DataActionResult) -> Unit) {
        repository.saveTable(table, onComplete)
    }

    fun deleteTable(tableId: String, onComplete: (DataActionResult) -> Unit) {
        repository.deleteTable(tableId, onComplete)
    }

    fun loadReservations(): LiveData<MutableList<ReservationModel>> {
        return repository.loadReservations()
    }

    fun createReservation(
        table: TableModel,
        reservationTime: String,
        session: UserSession,
        preorderRequested: Boolean,
        onComplete: (DataActionResult) -> Unit
    ) {
        repository.createReservation(table, reservationTime, session, preorderRequested, onComplete)
    }

    fun buildReportSummary(orders: List<OrderModel>): ReportSummary {
        return repository.buildReportSummary(orders)
    }

    fun cancelReservation(reservation: ReservationModel, onComplete: (DataActionResult) -> Unit) {
        repository.cancelReservation(reservation, onComplete)
    }
}
