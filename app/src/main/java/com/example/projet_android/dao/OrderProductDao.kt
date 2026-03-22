package com.example.projet_android.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.projet_android.model.OrderProduct
import com.example.projet_android.model.ProductItem
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(orderProduct: OrderProduct): Long
    @Update
    suspend fun update(orderProduct: OrderProduct): Int
    @Delete
    suspend fun delete(orderProduct: OrderProduct): Int
    @Query("DELETE FROM order_product")
    fun deleteAll()
    @Query("SELECT * FROM order_product WHERE orderId = :orderId")
    fun getProductsForOrder(orderId: Int): Flow<List<OrderProduct>>

    @Query("""
        SELECT 
            op.id AS orderProductId,
            o.id AS orderId,
            o.orderNumber AS orderNumber,
            p.id AS productId,
            p.name AS productName,
            p.weight AS weight,
            p.volume AS volume,
            p.price AS price,
            (o.priority >= 4) AS isPriority,
            p.fragility AS isFragile
        FROM order_product op
        INNER JOIN orders o ON op.orderId = o.id
        INNER JOIN product p ON op.productId = p.id
        WHERE o.status IN ('PENDING', 'PROCESSING')
        AND op.id NOT IN (SELECT orderProductId FROM shipping_plan_product WHERE shipped = 1)
    """)
    suspend fun getAllPendingProductItems(): List<ProductItem>
}
