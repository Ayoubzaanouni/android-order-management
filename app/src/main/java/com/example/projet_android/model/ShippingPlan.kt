package com.example.projet_android.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shipping_plan")
data class ShippingPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val totalRevenue: Double,
    val totalProductsShipped: Int,
    val totalProductsPostponed: Int,
    val createdAt: Long = System.currentTimeMillis()
)
