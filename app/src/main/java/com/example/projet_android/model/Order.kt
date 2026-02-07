package com.example.projet_android.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = Company::class,
            parentColumns = ["id"],
            childColumns = ["companyId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val companyId: Int,
    val totalWeight: Double,
    val totalVolume: Double,
    val totalPrice: Double,
    val priority: Int, // e.g. 1 for high, 0 for normal
    val status: String // e.g. "PENDING", "SHIPPED"
)
