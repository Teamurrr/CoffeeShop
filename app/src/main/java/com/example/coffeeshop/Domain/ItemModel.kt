package com.example.coffeeshop.Domain

import java.io.Serializable

data class ItemModel(
    var id: String = "",
    var categoryId: String = "",
    var title: String="",
    var description: String ="0",
    var picUrl: ArrayList<String> = ArrayList(),
    var price: Double = 0.0,
    var rating: Double=0.0,
    var numberInCart: Int = 0,
    var extra: String=""
): Serializable {
    fun firstImageUrl(): String = picUrl.firstOrNull().orEmpty()
}

