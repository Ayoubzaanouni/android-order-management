package com.example.projet_android.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.projet_android.model.OrderProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(orderProduct: OrderProduct): Long
    @Update
    suspend fun update(orderProduct: OrderProduct): Int
    @Delete
    suspend fun delete(orderProduct: OrderProduct): Int
    @Query("SELECT * FROM order_product WHERE orderId = :orderId")
    fun getProductsForOrder(orderId: Int): Flow<List<OrderProduct>>
}
