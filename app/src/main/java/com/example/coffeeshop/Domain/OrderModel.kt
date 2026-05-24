package com.example.coffeeshop.Domain

data class OrderModel(
    var id: String = "",
    var customerId: String = "",
    var customerName: String = "",
    var customerEmail: String = "",
    var reservationId: String = "",
    var reservedTableNumber: Int = 0,
    var reservationTime: String = "",
    var preorder: Boolean = false,
    var status: String = "",
    var paymentMethod: String = "",
    var paymentStatus: String = "",
    var totalAmount: Double = 0.0,
    var createdAt: Long = 0L,
    var paidAt: Long = 0L,
    var completedAt: Long = 0L,
    var customerNote: String = "",
    var items: List<OrderItemModel> = emptyList()
)
