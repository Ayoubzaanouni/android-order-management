package com.example.projet_android.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.projet_android.model.Admin

@Dao
interface AdminDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(admin: Admin): Long

    @Query("SELECT * FROM admin WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): Admin?
}
