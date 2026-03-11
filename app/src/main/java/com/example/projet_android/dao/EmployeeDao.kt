package com.example.projet_android.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.projet_android.model.Employee
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(employee: Employee): Long

    @Update
    suspend fun update(employee: Employee): Int

    @Delete
    suspend fun delete(employee: Employee): Int

    @Query("SELECT * FROM employee WHERE companyId = :companyId")
    fun getAllByCompany(companyId: Int): Flow<List<Employee>>

    @Query("SELECT * FROM employee WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): Employee?

    @Query("SELECT * FROM employee WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Employee?
}
