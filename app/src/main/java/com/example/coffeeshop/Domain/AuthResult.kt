package com.example.coffeeshop.Domain

data class AuthResult(
    val success: Boolean,
    val session: UserSession? = null,
    val errorMessage: String? = null
)
