package com.example.projet_android.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "company")
data class Company(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String,
    val password: String,
    val phone: String = "",
    val status: String = "PENDING" // PENDING | APPROVED | REJECTED
)
