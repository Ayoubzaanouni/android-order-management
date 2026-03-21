package com.example.projet_android.ui.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.projet_android.dao.ContainerDao
import com.example.projet_android.dao.OrderProductDao
import com.example.projet_android.dao.ShippingPlanDao
import com.example.projet_android.dao.OrderDao
import com.example.projet_android.database.AppDatabase
import com.example.projet_android.domain.ShippingOptimizer
import com.example.projet_android.model.ShippingPlan
import com.example.projet_android.model.ShippingPlanProduct
import com.example.projet_android.model.ShippingPlanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface ShippingOptimizationState {
    data object Idle : ShippingOptimizationState
    data object Loading : ShippingOptimizationState
    data class Success(val result: ShippingPlanResult) : ShippingOptimizationState
    data class Error(val message: String) : ShippingOptimizationState
}

class ShippingViewModel(
    private val containerDao: ContainerDao,
    private val orderProductDao: OrderProductDao,
    private val shippingPlanDao: ShippingPlanDao,
    private val orderDao: OrderDao
) : ViewModel() {

    companion object {
        fun factory(database: AppDatabase): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    ShippingViewModel(
                        containerDao = database.containerDao(),
                        orderProductDao = database.orderProductDao(),
                        shippingPlanDao = database.shippingPlanDao(),
                        orderDao = database.orderDao()
                    )
                }
            }
    }

    private val _optimizationState = MutableStateFlow<ShippingOptimizationState>(ShippingOptimizationState.Idle)
    val optimizationState: StateFlow<ShippingOptimizationState> = _optimizationState.asStateFlow()

    fun runOptimization(weightScore: Double = 0.4, priceScore: Double = 0.4, wasteScore: Double = 0.2) {
        viewModelScope.launch {
            _optimizationState.value = ShippingOptimizationState.Loading
            try {
                val result = withContext(Dispatchers.IO) {
                    val containers = containerDao.getActiveContainersSync()
                    val pendingProducts = orderProductDao.getAllPendingProductItems()
                    
                    if (priceScore > weightScore) {

                        val optValue = ShippingOptimizer(containers, weightScore, priceScore, wasteScore).optimize(pendingProducts)
                        val optFill = ShippingOptimizer(containers, 0.8, 0.1, 0.1).optimize(pendingProducts)
                        
                        if (optFill.totalRevenue > optValue.totalRevenue) optFill else optValue
                    } else {
                        ShippingOptimizer(
                            containers = containers,
                            weightFillScore = weightScore,
                            priceDensityScore = priceScore,
                            wasteScore = wasteScore
                        ).optimize(pendingProducts)
                    }
                }
                _optimizationState.value = ShippingOptimizationState.Success(result)
            } catch (e: Exception) {
                _optimizationState.value = ShippingOptimizationState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun saveShippingPlan(result: ShippingPlanResult, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val plan = ShippingPlan(
                        totalRevenue = result.totalRevenue,
                        totalProductsShipped = result.totalProductsShipped,
                        totalProductsPostponed = result.totalProductsPostponed
                    )
                    
                    val planProducts = mutableListOf<ShippingPlanProduct>()
                    
                    result.shippedSlots.forEach { slot ->
                        slot.assignedProducts.forEach { product ->
                            planProducts.add(
                                ShippingPlanProduct(
                                    planId = 0, // Assigned by DAO
                                    containerId = slot.container.id,
                                    orderProductId = product.orderProductId,
                                    shipped = true
                                )
                            )
                        }
                    }
                    
                    result.postponedSlots.forEach { slot ->
                        slot.assignedProducts.forEach { product ->
                            planProducts.add(
                                ShippingPlanProduct(
                                    planId = 0,
                                    containerId = slot.container.id,
                                    orderProductId = product.orderProductId,
                                    shipped = false
                                )
                            )
                        }
                    }
                    
                    shippingPlanDao.insertCompletePlan(plan, planProducts)
                    
                    // Update order statuses
                    val remainingPendingIds = orderProductDao.getAllPendingProductItems().map { it.orderId }.toSet()
                    val shippedOrderIds = result.shippedSlots.flatMap { it.assignedProducts }.map { it.orderId }.toSet()
                    
                    val fullyShippedOrderIds = shippedOrderIds - remainingPendingIds
                    val partiallyShippedOrderIds = shippedOrderIds.intersect(remainingPendingIds)
                    
                    fullyShippedOrderIds.forEach { orderId ->
                        orderDao.updateStatus(orderId, "SHIPPED")
                    }
                    partiallyShippedOrderIds.forEach { orderId ->
                        orderDao.updateStatus(orderId, "PROCESSING")
                    }
                }
                onComplete()
                runOptimization()
            } catch (e: Exception) {
            }
        }
    }
}
