package com.example.coffeeshop.Domain

data class OrderResult(
    val success: Boolean,
    val orderId: String? = null,
    val errorMessage: String? = null
)
