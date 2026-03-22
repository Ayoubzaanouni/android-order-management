package com.example.projet_android.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.projet_android.dao.ContainerDao
import com.example.projet_android.model.Container
import kotlinx.coroutines.launch

@Composable
fun ContainerDialogs(
    showAddDialog: Boolean,
    editingContainer: Container?,
    deletingContainer: Container?,
    onDismissAdd: () -> Unit,
    onDismissEdit: () -> Unit,
    onDismissDelete: () -> Unit,
    containerDao: ContainerDao
) {
    val scope = rememberCoroutineScope()

    if (showAddDialog) {
        ContainerFormDialog(
            title = "Add Container",
            initialContainer = null,
            onDismiss = onDismissAdd,
            onSave = { newContainer ->
                scope.launch { containerDao.insert(newContainer) }
                onDismissAdd()
            }
        )
    }

    editingContainer?.let { container ->
        ContainerFormDialog(
            title = "Edit Container",
            initialContainer = container,
            onDismiss = onDismissEdit,
            onSave = { updatedContainer ->
                scope.launch { containerDao.update(updatedContainer) }
                onDismissEdit()
            }
        )
    }

    deletingContainer?.let { container ->
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("Delete Container") },
            text = { Text("Are you sure you want to delete '${container.name}'? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { containerDao.delete(container) }
                    onDismissDelete()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ContainerFormDialog(
    title: String,
    initialContainer: Container?,
    onDismiss: () -> Unit,
    onSave: (Container) -> Unit
) {
    var name by remember { mutableStateOf(initialContainer?.name ?: "") }
    var maxWeight by remember { mutableStateOf(initialContainer?.maxWeight?.toString() ?: "") }
    var maxVolume by remember { mutableStateOf(initialContainer?.maxVolume?.toString() ?: "") }
    var fillThreshold by remember { mutableFloatStateOf(initialContainer?.fillThreshold?.toFloat() ?: 0.7f) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = maxWeight,
                    onValueChange = { maxWeight = it },
                    label = { Text("Max Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = maxVolume,
                    onValueChange = { maxVolume = it },
                    label = { Text("Max Volume (m³)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Fill Threshold: ${(fillThreshold * 100).toInt()}%", 
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = fillThreshold,
                    onValueChange = { fillThreshold = it },
                    valueRange = 0f..1f
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val weight = maxWeight.toDoubleOrNull() ?: return@TextButton
                    val volume = maxVolume.toDoubleOrNull() ?: return@TextButton
                    val threshold = fillThreshold.toDouble()
                    if (name.isNotBlank()) {
                        val containerToSave = initialContainer?.copy(
                            name = name,
                            maxWeight = weight,
                            maxVolume = volume,
                            fillThreshold = threshold
                        ) ?: Container(
                            name = name,
                            maxWeight = weight,
                            maxVolume = volume,
                            fillThreshold = threshold
                        )
                        onSave(containerToSave)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
