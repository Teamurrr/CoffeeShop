package com.example.coffeeshop.Domain

data class ReservationContext(
    val reservationId: String,
    val tableId: String,
    val tableNumber: Int,
    val reservationTime: String
)
