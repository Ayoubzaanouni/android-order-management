package com.example.projet_android.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "container")
data class Container(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val maxWeight: Double,
    val maxVolume: Double
)
