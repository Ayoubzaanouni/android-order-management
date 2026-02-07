package com.example.projet_android.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.projet_android.model.ContainerOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface ContainerOrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(containerOrder: ContainerOrder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(containerOrders: List<ContainerOrder>)

    @Update
    suspend fun update(containerOrder: ContainerOrder): Int

    @Delete
    suspend fun delete(containerOrder: ContainerOrder): Int

    @Query("SELECT * FROM container_order WHERE id = :id")
    fun getContainerOrder(id: Int): Flow<ContainerOrder>

    @Query("SELECT * FROM container_order WHERE containerId = :containerId")
    fun getOrdersByContainer(containerId: Int): Flow<List<ContainerOrder>>

    @Query("SELECT * FROM container_order WHERE orderProductId = :orderProductId")
    fun getContainersByOrderProduct(orderProductId: Int): Flow<List<ContainerOrder>>

    @Query("SELECT * FROM container_order")
    fun getAllContainerOrders(): Flow<List<ContainerOrder>>

    @Query("DELETE FROM container_order WHERE containerId = :containerId")
    suspend fun deleteByContainer(containerId: Int)

    @Query("DELETE FROM container_order")
    suspend fun deleteAll()
}
