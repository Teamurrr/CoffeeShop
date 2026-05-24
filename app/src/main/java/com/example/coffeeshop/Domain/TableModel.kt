package com.example.coffeeshop.Domain

data class TableModel(
    var id: String = "",
    var number: Int = 0,
    var qrCode: String = "",
    var status: String = "Free",
    var reservationId: String = "",
    var reservedByName: String = "",
    var reservationTime: String = ""
)
