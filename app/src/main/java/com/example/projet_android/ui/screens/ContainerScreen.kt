package com.example.projet_android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.projet_android.dao.ContainerDao
import com.example.projet_android.model.Container
import com.example.projet_android.model.ContainerStatus
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
enum class ContainerSortOrder(val label: String) {
    NAME_ASC("Name A➜Z"),
    NAME_DESC("Name Z➜A"),
    WEIGHT_ASC("Weight ⬆"),
    WEIGHT_DESC("Weight ⬇"),
    VOLUME_ASC("Volume ⬆"),
    VOLUME_DESC("Volume ⬇"),
}
private fun ContainerStatus.label() = when (this) {
    ContainerStatus.ACTIVE      -> "Active"
    ContainerStatus.MAINTENANCE -> "Maintenance"
    ContainerStatus.FULL        -> "Full"
    ContainerStatus.DISABLED    -> "Inactive"
    ContainerStatus.SHIPPED     -> "Shipped"
}

private fun ContainerStatus.color() = when (this) {
    ContainerStatus.ACTIVE      -> Color(0xFF2E7D32)
    ContainerStatus.MAINTENANCE -> Color(0xFFE65100)
    ContainerStatus.FULL        -> Color(0xFF1565C0)
    ContainerStatus.DISABLED    -> Color(0xFF757575)
    ContainerStatus.SHIPPED     -> Color(0xFF6A1B9A)
}
@Composable
fun ContainerScreen(
    containerDao: ContainerDao,
    paddingValues: PaddingValues
) {
    val allContainers by containerDao.getAllContainers().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var filterStatus by remember { mutableStateOf<ContainerStatus?>(null) }
    var filterEnabledOnly by remember { mutableStateOf(false) }
    var sortOrder by remember { mutableStateOf(ContainerSortOrder.NAME_ASC) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingContainer by remember { mutableStateOf<Container?>(null) }
    var deletingContainer by remember { mutableStateOf<Container?>(null) }

    val displayed = allContainers
        .filter { c -> filterStatus == null || c.status == filterStatus }
        .filter { c -> !filterEnabledOnly || c.enabled }
        .let { list ->
            when (sortOrder) {
                ContainerSortOrder.NAME_ASC    -> list.sortedBy { it.name.lowercase() }
                ContainerSortOrder.NAME_DESC   -> list.sortedByDescending { it.name.lowercase() }
                ContainerSortOrder.WEIGHT_ASC  -> list.sortedBy { it.maxWeight }
                ContainerSortOrder.WEIGHT_DESC -> list.sortedByDescending { it.maxWeight }
                ContainerSortOrder.VOLUME_ASC  -> list.sortedBy { it.maxVolume }
                ContainerSortOrder.VOLUME_DESC -> list.sortedByDescending { it.maxVolume }
            }
        }

    Scaffold(
        modifier = Modifier.padding(paddingValues),
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Container")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = filterStatus == null,
                        onClick = { filterStatus = null },
                        label = { Text("All") }
                    )
                    ContainerStatus.entries.forEach { status ->
                        FilterChip(
                            selected = filterStatus == status,
                            onClick = {
                                filterStatus = if (filterStatus == status) null else status
                            },
                            label = { Text(status.label()) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = filterEnabledOnly,
                            onCheckedChange = { filterEnabledOnly = it },
                            modifier = Modifier.size(40.dp, 24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Enabled only",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    //sort
                    Box {
                        OutlinedButton(
                            onClick = { showSortMenu = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                sortOrder.label,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                if (showSortMenu) Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            ContainerSortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(
                                                selected = sortOrder == order,
                                                onClick = null
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(order.label)
                                        }
                                    },
                                    onClick = {
                                        sortOrder = order
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider()
            if (displayed.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (allContainers.isEmpty()) "No containers yet"
                                   else "No containers match the filter",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (allContainers.isEmpty()) "Tap + to add your first container"
                                   else "Try adjusting your filters",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
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
                            onEdit = { editingContainer = container },
                            onDelete = { deletingContainer = container },
                            onToggleEnabled = {
                                scope.launch {
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
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleEnabled: () -> Unit
) {
    val alpha = if (container.enabled) 1f else 0.5f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
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
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enabled", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = enabled,
                        onCheckedChange = { newEnabled ->
                            enabled = newEnabled
                            if (status == ContainerStatus.ACTIVE || status == ContainerStatus.DISABLED) {
                                status = if (newEnabled) ContainerStatus.ACTIVE else ContainerStatus.DISABLED
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (isValid) {
                        onConfirm(
                            name.trim(),
                            maxWeight!!,
                            maxVolume!!,
                            fillThreshold.toDouble(),
                            status,
                            enabled
                        )
                    }
                },
                enabled = isValid
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}