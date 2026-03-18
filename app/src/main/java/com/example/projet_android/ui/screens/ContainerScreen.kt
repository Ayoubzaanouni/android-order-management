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
import androidx.compose.ui.graphics.Color
import com.example.projet_android.dao.ContainerDao
import com.example.projet_android.model.Container
import com.example.projet_android.model.ContainerStatus
import com.example.projet_android.ui.components.ContainerDialogs
import com.example.projet_android.ui.components.ContainerEmptyState
import com.example.projet_android.ui.components.ContainerFilters
import com.example.projet_android.ui.components.ContainerList
import kotlinx.coroutines.launch

enum class ContainerSortOrder(val label: String) {
    NAME_ASC("Name A➜Z"),
    NAME_DESC("Name Z➜A"),
    WEIGHT_ASC("Weight ⬆"),
    WEIGHT_DESC("Weight ⬇"),
    VOLUME_ASC("Volume ⬆"),
    VOLUME_DESC("Volume ⬇"),
}

fun ContainerStatus.label() = when (this) {
    ContainerStatus.ACTIVE      -> "Active"
    ContainerStatus.MAINTENANCE -> "Maintenance"
    ContainerStatus.FULL        -> "Full"
    ContainerStatus.DISABLED    -> "Inactive"
    ContainerStatus.SHIPPED     -> "Shipped"
}

fun ContainerStatus.color() = when (this) {
    ContainerStatus.ACTIVE      -> Color(0xFF2E7D32)
    ContainerStatus.MAINTENANCE -> Color(0xFFE65100)
    ContainerStatus.FULL        -> Color(0xFF1565C0)
    ContainerStatus.DISABLED    -> Color(0xFF757575)
    ContainerStatus.SHIPPED     -> Color(0xFF6A1B9A)
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
        floatingActionButton = {
            if (!readOnly) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Container")
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

            HorizontalDivider()

            if (displayed.isEmpty()) {
                ContainerEmptyState(
                    isCompletelyEmpty = containers.isEmpty()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Containers (${displayed.size}/${allContainers.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(displayed, key = { it.id }) { container ->
                        ContainerCard(
                            container = container,
                            readOnly  = readOnly,
                            onEdit = { if (!readOnly) editingContainer = container },
                            onDelete = { if (!readOnly) deletingContainer = container },
                            onToggleEnabled = {
                                if (!readOnly) scope.launch {
                                    val newEnabled = !container.enabled
                                    val newStatus = when {
                                        newEnabled && container.status == ContainerStatus.DISABLED -> ContainerStatus.ACTIVE
                                        !newEnabled && container.status == ContainerStatus.ACTIVE -> ContainerStatus.DISABLED
                                        else -> container.status
                                    }
                                    containerDao.update(container.copy(enabled = newEnabled, status = newStatus))
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        ContainerFormDialog(
            title = "Add Container",
            confirmLabel = "Add",
            initial = null,
            onConfirm = { name, maxWeight, maxVolume, fillThreshold, status, enabled ->
                scope.launch {
                    containerDao.insert(
                        Container(
                            name = name,
                            maxWeight = maxWeight,
                            maxVolume = maxVolume,
                            fillThreshold = fillThreshold,
                            status = status,
                            enabled = enabled
                        )
                    )
                }
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    editingContainer?.let { container ->
        ContainerFormDialog(
            title = "Edit Container",
            confirmLabel = "Save",
            initial = container,
            onConfirm = { name, maxWeight, maxVolume, fillThreshold, status, enabled ->
                scope.launch {
                    containerDao.update(
                        container.copy(
                            name = name,
                            maxWeight = maxWeight,
                            maxVolume = maxVolume,
                            fillThreshold = fillThreshold,
                            status = status,
                            enabled = enabled
                        )
                    )
                }
                editingContainer = null
            },
            onDismiss = { editingContainer = null }
        )
    }

    deletingContainer?.let { container ->
        AlertDialog(
            onDismissRequest = { deletingContainer = null },
            title = { Text("Delete Container") },
            text = {
                Text("Are you sure you want to delete \"${container.name}\"? This cannot be undone.")
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { containerDao.delete(container) }
                    deletingContainer = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingContainer = null }) { Text("Cancel") }
            }
        )
    }
}
@Composable
private fun ContainerCard(
    container: Container,
    readOnly: Boolean = false,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleEnabled: () -> Unit
) {
    val alpha = if (container.enabled) 1f else 0.5f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!readOnly) Modifier.clickable { onEdit() } else Modifier),
        elevation = CardDefaults.cardElevation(defaultElevation = if (container.enabled) 2.dp else 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = alpha)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = container.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Badge(
                        containerColor = container.status.color().copy(alpha = 0.15f),
                    ) {
                        Text(
                            text = container.status.label(),
                            color = container.status.color(),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (!readOnly) {
                    Switch(
                        checked = container.enabled,
                        onCheckedChange = { onToggleEnabled() }
                    )
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ContainerStat("Max Weight", "${container.maxWeight} kg", alpha)
                ContainerStat("Max Volume", "${container.maxVolume} m³", alpha)
                ContainerStat(
                    "Fill Threshold",
                    "${(container.fillThreshold * 100).roundToInt()}%",
                    alpha
                )
            }
        }
    }
}

@Composable
private fun ContainerStat(label: String, value: String, alpha: Float = 1f) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
        )
    }
}
// add / edit
@Composable
private fun ContainerFormDialog(
    title: String,
    confirmLabel: String,
    initial: Container?,
    onConfirm: (
        name: String,
        maxWeight: Double,
        maxVolume: Double,
        fillThreshold: Double,
        status: ContainerStatus,
        enabled: Boolean
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var maxWeightStr by remember { mutableStateOf(initial?.maxWeight?.toString() ?: "") }
    var maxVolumeStr by remember { mutableStateOf(initial?.maxVolume?.toString() ?: "") }
    var fillThreshold by remember { mutableFloatStateOf(initial?.fillThreshold?.toFloat() ?: 0.7f) }
    var status by remember { mutableStateOf(initial?.status ?: ContainerStatus.ACTIVE) }
    var enabled by remember { mutableStateOf(initial?.enabled ?: true) }

    val maxWeight = maxWeightStr.toDoubleOrNull()
    val maxVolume = maxVolumeStr.toDoubleOrNull()
    val isValid = name.isNotBlank() && maxWeight != null && maxWeight > 0
            && maxVolume != null && maxVolume > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Container Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = maxWeightStr,
                    onValueChange = { maxWeightStr = it },
                    label = { Text("Max Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = maxWeightStr.isNotEmpty() && maxWeight == null
                )
                OutlinedTextField(
                    value = maxVolumeStr,
                    onValueChange = { maxVolumeStr = it },
                    label = { Text("Max Volume (m³)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = maxVolumeStr.isNotEmpty() && maxVolume == null
                )
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Fill Threshold", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${(fillThreshold * 100).roundToInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = fillThreshold,
                        onValueChange = { fillThreshold = it },
                        valueRange = 0.1f..1.0f,
                        steps = 17,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("10%", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("100%", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Column {
                    Text("Status", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ContainerStatus.entries.forEach { s ->
                            FilterChip(
                                selected = status == s,
                                onClick = {
                                    status = s
                                    when (s) {
                                        ContainerStatus.ACTIVE   -> enabled = true
                                        ContainerStatus.DISABLED -> enabled = false
                                        else -> Unit
                                    }
                                },
                                label = { Text(s.label()) }
                            )
                        }
                    }
                )
            }
        }
    }

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
