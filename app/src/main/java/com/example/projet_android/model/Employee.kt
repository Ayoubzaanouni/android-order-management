package com.example.projet_android.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "employee",
    foreignKeys = [
        ForeignKey(
            entity = Company::class,
            parentColumns = ["id"],
            childColumns = ["companyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("companyId")]
)
data class Employee(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val companyId: Int,
    val name: String,
    val email: String,
    val password: String
)
