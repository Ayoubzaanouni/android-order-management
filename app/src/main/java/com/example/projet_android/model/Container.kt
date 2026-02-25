package com.example.projet_android.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ContainerStatus {
    ACTIVE, SHIPPED, FULL, DISABLED, MAINTENANCE
}

@Entity(tableName = "container")
data class Container(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val maxWeight: Double,
    val maxVolume: Double,
    val fillThreshold: Double = 0.7,
    val status: ContainerStatus = ContainerStatus.ACTIVE,
    val enabled: Boolean = true
)
