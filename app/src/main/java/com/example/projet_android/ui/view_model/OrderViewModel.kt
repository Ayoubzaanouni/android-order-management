package com.example.projet_android.ui.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.projet_android.dao.OrderDao
import com.example.projet_android.dao.ProductDao
import com.example.projet_android.database.AppDatabase
import com.example.projet_android.model.Order
import com.example.projet_android.model.Product
import com.example.projet_android.dao.OrderProductDao
import com.example.projet_android.model.OrderProduct
import com.example.projet_android.util.OrderSeeder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed interface SeedingState {
    data object Idle    : SeedingState
    data object Loading : SeedingState
    data class  Success(val message: String) : SeedingState
    data class  Error  (val message: String) : SeedingState
}

enum class OrderStatus(val label: String) {
    PENDING("PENDING"),
    PROCESSING("PROCESSING"),
    SHIPPED("SHIPPED"),
    DELIVERED("DELIVERED"),
    CANCELLED("CANCELLED");

    fun next(): OrderStatus? = when (this) {
        PENDING    -> PROCESSING
        PROCESSING -> SHIPPED
        SHIPPED    -> DELIVERED
        else       -> null
    }
}

class OrderViewModel(
    private val orderDao: OrderDao,
    private val productDao: ProductDao,
    private val orderProductDao: OrderProductDao,
    private val companyId: Int
) : ViewModel() {
    companion object {
        fun factory(database: AppDatabase, companyId: Int): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    OrderViewModel(
                        orderDao  = database.orderDao(),
                        productDao = database.productDao(),
                        orderProductDao = database.orderProductDao(),
                        companyId = companyId
                    )
                }
            }
    }

    private val _seedingState = MutableStateFlow<SeedingState>(SeedingState.Idle)
    val seedingState: StateFlow<SeedingState> = _seedingState.asStateFlow()

    fun getProductsForOrder(orderId: Int): Flow<List<Product>> {
        return productDao.getProductsForOrder(orderId)
    }

    val orders: StateFlow<List<Order>> = orderDao
        .getAllOrders()
        .stateIn(
            scope         = viewModelScope,
            started       = SharingStarted.WhileSubscribed(5_000),
            initialValue  = emptyList()
        )

    init {
        viewModelScope.launch {
            orders
                .filter { it.isEmpty() }
                .take(1)
                .collect { performSeed(triggeredByUser = false, companyId, 50) }
        }
    }

    fun onSeedButtonClicked(orderCount: Int) {
        if (_seedingState.value is SeedingState.Loading) return
        viewModelScope.launch { performSeed(triggeredByUser = true, companyId, orderCount) }
    }

    fun resetSeedingState() {
        _seedingState.update { SeedingState.Idle }
    }

    /** Company-only: delete an order */
    fun deleteOrder(order: Order) {
        viewModelScope.launch(Dispatchers.IO) { orderDao.delete(order) }
    }

    /** Employee + Company: advance an order to its next status */
    fun updateOrderStatus(order: Order, newStatus: String) {
        viewModelScope.launch(Dispatchers.IO) { orderDao.updateStatus(order.id, newStatus) }
    }

    private suspend fun performSeed(triggeredByUser: Boolean, companyId: Int, orderCount: Int) {
        _seedingState.update { SeedingState.Loading }

        runCatching {
            withContext(Dispatchers.IO) {
                // 1. If re-seeding manually, wipe existing orders first
                if (triggeredByUser) {
                    orderProductDao.deleteAll()
                    orderDao.deleteAll()
                    productDao.deleteAll()
                }

                // 2. Generate and insert fake orders
                val generatedOrders = OrderSeeder.generateOrders(
                    companyId,
                    orderCount,
                )

                generatedOrders.forEach { (order, products) -> 
                    val orderId = orderDao.insert(order).toInt()
                    products.forEach { product ->
                        val productId = productDao.insert(product).toInt()
                        orderProductDao.insert(OrderProduct(orderId = orderId, productId = productId))
                    }
                }
                generatedOrders.size
            }
        }
            .onSuccess { count ->
                val trigger = if (triggeredByUser) "Manual" else "Auto"
                _seedingState.update {
                    SeedingState.Success("$trigger seed complete — $count orders inserted.")
                }
            }
            .onFailure { error ->
                _seedingState.update {
                    SeedingState.Error(error.message ?: "An unexpected error occurred during seeding.")
                }
            }
    }
}

