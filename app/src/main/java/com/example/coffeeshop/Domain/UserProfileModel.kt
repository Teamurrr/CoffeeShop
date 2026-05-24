package com.example.coffeeshop.Domain

data class UserProfileModel(
    var id: String = "",
    var name: String = "",
    var email: String = "",
    var role: String = UserRole.CUSTOMER,
    var pin: String = "",
    var active: Boolean = true,
    var createdAt: Long = 0L
)
