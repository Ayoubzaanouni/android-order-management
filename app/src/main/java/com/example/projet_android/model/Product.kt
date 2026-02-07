package com.example.projet_android.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val weight: Double,
    val volume: Double,
    val price: Double,
    val fragility: Boolean = false
)
