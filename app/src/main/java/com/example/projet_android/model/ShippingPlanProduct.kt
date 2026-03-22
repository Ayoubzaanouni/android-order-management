package com.example.projet_android.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

import androidx.room.Index

@Entity(
    tableName = "shipping_plan_product",
    foreignKeys = [
        ForeignKey(
            entity = ShippingPlan::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Container::class,
            parentColumns = ["id"],
            childColumns = ["containerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = OrderProduct::class,
            parentColumns = ["id"],
            childColumns = ["orderProductId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("planId"),
        Index("containerId"),
        Index("orderProductId")
    ]
)
data class ShippingPlanProduct(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val planId: Int,
    val containerId: Int,
    val orderProductId: Int,
    val shipped: Boolean
)
