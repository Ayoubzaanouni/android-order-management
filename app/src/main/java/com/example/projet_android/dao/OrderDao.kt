package com.example.projet_android.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.projet_android.model.Order
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: Order): Long
    @Update
    suspend fun update(order: Order): Int
    @Delete
    suspend fun delete(order: Order): Int
    @Query("DELETE FROM orders")
    fun deleteAll()
    @Query("SELECT * FROM orders WHERE id = :id")
    fun getOrder(id: Int): Flow<Order>
    @Query("SELECT * FROM orders")
    fun getAllOrders(): Flow<List<Order>>
    @Query("UPDATE orders SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)
}

