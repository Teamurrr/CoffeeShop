package com.example.coffeeshop.Domain

data class ReportSummary(
    val totalRevenue: Double,
    val totalOrders: Int,
    val averageBill: Double,
    val completedOrders: Int,
    val pendingOrders: Int,
    val paidOrders: Int
)
