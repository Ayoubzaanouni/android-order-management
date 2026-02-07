package com.example.projet_android.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.projet_android.model.Container
import kotlinx.coroutines.flow.Flow

@Dao
interface ContainerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(container: Container): Long
    @Update
    suspend fun update(container: Container): Int

    @Delete
    suspend fun delete(container: Container): Int
    @Query("SELECT * FROM container")
    fun getAllContainers(): Flow<List<Container>>
}
