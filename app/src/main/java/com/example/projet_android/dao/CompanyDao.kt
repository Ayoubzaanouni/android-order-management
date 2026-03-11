package com.example.projet_android.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.projet_android.model.Company
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(company: Company): Long

    @Update
    suspend fun update(company: Company): Int

    @Delete
    suspend fun delete(company: Company): Int

    @Query("SELECT * FROM company WHERE id = :id")
    fun getCompany(id: Int): Flow<Company>

    @Query("SELECT * FROM company")
    fun getAllCompanies(): Flow<List<Company>>

    @Query("SELECT * FROM company WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): Company?

    @Query("SELECT * FROM company WHERE email = :email LIMIT 1")
    suspend fun getCompanyByEmail(email: String): Company?

    @Query("SELECT * FROM company WHERE status = 'PENDING'")
    fun getPendingCompanies(): Flow<List<Company>>

    @Query("UPDATE company SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)
}

