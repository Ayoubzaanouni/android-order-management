package com.example.projet_android.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projet_android.ui.components.*
import com.example.projet_android.ui.theme.Base
import com.example.projet_android.ui.view_model.OrderViewModel
import com.example.projet_android.ui.view_model.SeedingState

@Composable
fun OrderScreen(
    viewModel       : OrderViewModel = viewModel(),
    canDelete       : Boolean = true,
    canChangeStatus : Boolean = true,
    onOrdersSeeded  : () -> Unit = {}
) {
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val seedingState by viewModel.seedingState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(seedingState) {
        when (seedingState) {
            is SeedingState.Success -> {
                snackbarHostState.showSnackbar((seedingState as SeedingState.Success).message)
                onOrdersSeeded()
                viewModel.resetSeedingState()
            }
            is SeedingState.Error -> {
                snackbarHostState.showSnackbar((seedingState as SeedingState.Error).message)
                viewModel.resetSeedingState()
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            OrderTopBar(
                orderCount = orders.size,
                isLoading  = seedingState is SeedingState.Loading
            )
        },
        containerColor = Base,
        floatingActionButton = {
            if (canDelete) {
                SeedFab(
                    isLoading = seedingState is SeedingState.Loading,
                    onClick   = viewModel::onSeedButtonClicked
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                seedingState is SeedingState.Loading && orders.isEmpty() -> {
                    FullScreenLoading()
                }
                orders.isEmpty() -> {
                    EmptyOrdersPlaceholder()
                }
                else -> {
                    OrderList(
                        orders         = orders,
                        isLoading      = seedingState is SeedingState.Loading,
                        getProductsForOrder = viewModel::getProductsForOrder,
                        canDelete      = canDelete,
                        onDelete       = { order -> viewModel.deleteOrder(order) }
                    )
                }
            }
        }
    }
}
