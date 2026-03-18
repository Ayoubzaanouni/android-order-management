package com.example.projet_android.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import com.example.projet_android.dao.ContainerDao
import com.example.projet_android.model.Container
import com.example.projet_android.model.ContainerStatus
import com.example.projet_android.ui.components.ContainerDialogs
import com.example.projet_android.ui.components.ContainerEmptyState
import com.example.projet_android.ui.components.ContainerFilters
import com.example.projet_android.ui.components.ContainerList
import com.example.projet_android.ui.theme.*
import kotlinx.coroutines.launch

enum class ContainerSortOrder(val label: String) {
    NAME_ASC("NAME_ASC"),
    NAME_DESC("NAME_DESC"),
    WEIGHT_ASC("WGT_ASC"),
    WEIGHT_DESC("WGT_DESC"),
    VOLUME_ASC("VOL_ASC"),
    VOLUME_DESC("VOL_DESC"),
}

fun ContainerStatus.label() = when (this) {
    ContainerStatus.ACTIVE      -> "ACTIVE"
    ContainerStatus.MAINTENANCE -> "MAINT"
    ContainerStatus.FULL        -> "FULL"
    ContainerStatus.DISABLED    -> "DISABLED"
    ContainerStatus.SHIPPED     -> "SHIPPED"
}

fun ContainerStatus.color() = when (this) {
    ContainerStatus.ACTIVE      -> Green
    ContainerStatus.MAINTENANCE -> Yellow
    ContainerStatus.FULL        -> Blue
    ContainerStatus.DISABLED    -> Overlay0
    ContainerStatus.SHIPPED     -> Mauve
}

@Composable
fun ContainerScreen(
    containerDao: ContainerDao,
    paddingValues: PaddingValues,
    readOnly: Boolean = false
) {
    val containers by containerDao.getAllContainers()
        .collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    var filterStatus by remember { mutableStateOf<ContainerStatus?>(null) }
    var filterEnabledOnly by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf(ContainerSortOrder.NAME_ASC) }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingContainer by remember { mutableStateOf<Container?>(null) }
    var deletingContainer by remember { mutableStateOf<Container?>(null) }

    val displayed = remember(containers, filterStatus, filterEnabledOnly, sortOrder) {
        containers
            .filter { filterStatus == null || it.status == filterStatus }
            .filter { !filterEnabledOnly || it.enabled }
            .let { list ->
                when (sortOrder) {
                    ContainerSortOrder.NAME_ASC -> list.sortedBy { it.name.lowercase() }
                    ContainerSortOrder.NAME_DESC -> list.sortedByDescending { it.name.lowercase() }
                    ContainerSortOrder.WEIGHT_ASC -> list.sortedBy { it.maxWeight }
                    ContainerSortOrder.WEIGHT_DESC -> list.sortedByDescending { it.maxWeight }
                    ContainerSortOrder.VOLUME_ASC -> list.sortedBy { it.maxVolume }
                    ContainerSortOrder.VOLUME_DESC -> list.sortedByDescending { it.maxVolume }
                }
            }
    }

    Scaffold(
        modifier = Modifier.padding(paddingValues),
        containerColor = Base,
        floatingActionButton = {
            if (!readOnly) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Mauve,
                    contentColor = Base,
                    shape = RectangleShape
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            ContainerFilters(
                filterStatus = filterStatus,
                onStatusChange = { filterStatus = it },
                filterEnabledOnly = filterEnabledOnly,
                onEnabledOnlyChange = { filterEnabledOnly = it },
                sortOrder = sortOrder,
                onSortChange = { sortOrder = it }
            )

            HorizontalDivider(color = Surface1)

            if (displayed.isEmpty()) {
                ContainerEmptyState(
                    isCompletelyEmpty = containers.isEmpty()
                )
            } else {
                ContainerList(
                    containers = displayed,
                    totalCount = containers.size,
                    onEdit = if (readOnly) ({}) else ({ editingContainer = it }),
                    onDelete = if (readOnly) ({}) else ({ deletingContainer = it }),
                    onToggleEnabled = { container ->
                        if (!readOnly) {
                            scope.launch {
                                val newEnabled = !container.enabled
                                val newStatus =
                                    if (newEnabled) ContainerStatus.ACTIVE
                                    else ContainerStatus.DISABLED

                                containerDao.update(
                                    container.copy(
                                        enabled = newEnabled,
                                        status = newStatus
                                    )
                                )
                            }
                        }
                    }
                )
            }
        }
    }

    if (!readOnly) {
        ContainerDialogs(
            showAddDialog = showAddDialog,
            editingContainer = editingContainer,
            deletingContainer = deletingContainer,
            onDismissAdd = { showAddDialog = false },
            onDismissEdit = { editingContainer = null },
            onDismissDelete = { deletingContainer = null },
            containerDao = containerDao
        )
    }
}
