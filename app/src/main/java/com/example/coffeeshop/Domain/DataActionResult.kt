package com.example.coffeeshop.Domain

data class DataActionResult(
    val success: Boolean,
    val id: String? = null,
    val errorMessage: String? = null
)
