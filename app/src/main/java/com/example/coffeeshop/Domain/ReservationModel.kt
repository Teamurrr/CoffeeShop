package com.example.coffeeshop.Domain

data class ReservationModel(
    var id: String = "",
    var customerId: String = "",
    var customerName: String = "",
    var customerEmail: String = "",
    var tableId: String = "",
    var tableNumber: Int = 0,
    var reservationTime: String = "",
    var status: String = "Reserved",
    var preorderRequested: Boolean = false,
    var createdAt: Long = 0L
)
