package com.example.coffeeshop.Domain

data class UserSession(
    val userId: String,
    val role: String,
    val name: String,
    val email: String
)
