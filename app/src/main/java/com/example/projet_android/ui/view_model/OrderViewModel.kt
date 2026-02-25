package com.example.projet_android.ui.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.projet_android.dao.CompanyDao
import com.example.projet_android.dao.OrderDao
import com.example.projet_android.database.AppDatabase
import com.example.projet_android.model.Company
import com.example.projet_android.model.Order
import com.example.projet_android.util.OrderSeeder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SeedingState {
    data object Idle    : SeedingState
    data object Loading : SeedingState
    data class  Success(val message: String) : SeedingState
    data class  Error  (val message: String) : SeedingState
}

class OrderViewModel(
    private val orderDao: OrderDao,
    private val companyDao: CompanyDao,
) : ViewModel() {
    companion object {
        fun factory(database: AppDatabase): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    OrderViewModel(
                        orderDao  = database.orderDao(),
                        companyDao = database.companyDao()
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
                .collect { performSeed(triggeredByUser = false) }
        }
    }

    fun onSeedButtonClicked() {
        if (_seedingState.value is SeedingState.Loading) return
        viewModelScope.launch { performSeed(triggeredByUser = true) }
    }

    fun resetSeedingState() {
        _seedingState.update { SeedingState.Idle }
    }

    private suspend fun performSeed(triggeredByUser: Boolean) {
        _seedingState.update { SeedingState.Loading }

        runCatching {
            // 1. Fetch available companies to link orders against
            val companies = companyDao.getAllCompanies().first()

            check(companies.count() != 0) {
                "No companies found. Please seed companies first."
            }

            // 2. If re-seeding manually, wipe existing orders first
            if (triggeredByUser) orderDao.deleteAll()

            // 3. Generate and insert fake orders
            val generatedOrders = OrderSeeder.generateOrders(
                companyIds       = companies.map { it.id },
                ordersPerCompany = 2..6
            )

            generatedOrders.forEach { order -> orderDao.insert(order) }

            generatedOrders.size
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

