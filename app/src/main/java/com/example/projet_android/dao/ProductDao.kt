package com.example.projet_android.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.projet_android.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product): Long
    @Update
    suspend fun update(product: Product): Int
    @Delete
    suspend fun delete(product: Product): Int
    @Query("DELETE FROM product")
    fun deleteAll()
    @Query("SELECT * FROM product WHERE id = :id")
    fun getProduct(id: Int): Flow<Product>
    @Query("SELECT * FROM product")
    fun getAllProducts(): Flow<List<Product>>

    @Query("""
        SELECT p.* FROM product p
        INNER JOIN order_product op ON p.id = op.productId
        WHERE op.orderId = :orderId
    """)
    fun getProductsForOrder(orderId: Int): Flow<List<Product>>
}
