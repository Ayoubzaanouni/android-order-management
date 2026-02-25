package com.example.projet_android.ui.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.projet_android.dao.OrderDao
import com.example.projet_android.database.AppDatabase
import com.example.projet_android.model.Order
import com.example.projet_android.util.OrderSeeder
import kotlinx.coroutines.Dispatchers
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

class OrderViewModel(
    private val orderDao: OrderDao,
    private val companyId: Int
) : ViewModel() {
    companion object {
        fun factory(database: AppDatabase, companyId: Int): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    OrderViewModel(
                        orderDao  = database.orderDao(),
                        companyId
                    )
                }
            }
    }

    private val _seedingState = MutableStateFlow<SeedingState>(SeedingState.Idle)
    val seedingState: StateFlow<SeedingState> = _seedingState.asStateFlow()

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

    private suspend fun performSeed(triggeredByUser: Boolean, companyId: Int, orderCount: Int) {
        _seedingState.update { SeedingState.Loading }

        runCatching {
            withContext(Dispatchers.IO) {
                // 1. If re-seeding manually, wipe existing orders first
                if (triggeredByUser) orderDao.deleteAll()

                // 2. Generate and insert fake orders
                val generatedOrders = OrderSeeder.generateOrders(
                    companyId,
                    orderCount,
                )

                generatedOrders.forEach { order -> orderDao.insert(order) }

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
                    SeedingState.Error(
                        error.message ?: "An unexpected error occurred during seeding."
                    )
                }
            }
    }
}

