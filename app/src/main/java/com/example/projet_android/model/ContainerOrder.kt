package com.example.projet_android.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "container_order",
    foreignKeys = [
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
    ]
)
data class ContainerOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val containerId: Int,
    val orderProductId: Int
)
