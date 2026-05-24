package com.example.coffeeshop.Domain

data class OrderItemModel(
    var itemId: String = "",
    var title: String = "",
    var quantity: Int = 0,
    var price: Double = 0.0,
    var imageUrl: String = ""
)
