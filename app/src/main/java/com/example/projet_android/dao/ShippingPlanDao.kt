package com.example.projet_android.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.projet_android.model.ShippingPlan
import com.example.projet_android.model.ShippingPlanProduct

@Dao
interface ShippingPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: ShippingPlan): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanProducts(products: List<ShippingPlanProduct>)

    @Query("SELECT * FROM shipping_plan ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestShippingPlan(): ShippingPlan?

    @Query("SELECT * FROM shipping_plan_product WHERE planId = :planId")
    suspend fun getProductsForPlan(planId: Int): List<ShippingPlanProduct>

    @Transaction
    suspend fun insertCompletePlan(plan: ShippingPlan, products: List<ShippingPlanProduct>) {
        val planId = insertPlan(plan).toInt()
        val planProductsWithId = products.map { it.copy(planId = planId) }
        insertPlanProducts(planProductsWithId)
    }

    @Query("DELETE FROM shipping_plan")
    suspend fun clearAllPlans()
}
