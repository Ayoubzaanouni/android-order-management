package com.example.projet_android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.projet_android.model.Order

@Composable
fun OrderEditDialog(
    order     : Order,
    onConfirm : (Order) -> Unit,
    onDismiss : () -> Unit,
) {
    var weight   by remember { mutableStateOf(order.totalWeight.toString()) }
    var volume   by remember { mutableStateOf(order.totalVolume.toString()) }
    var price    by remember { mutableStateOf(order.totalPrice.toString()) }
    var priority by remember { mutableStateOf(order.priority.toString()) }

    val isValid = weight.toDoubleOrNull() != null
            && volume.toDoubleOrNull() != null
            && price.toDoubleOrNull()  != null
            && priority.toIntOrNull()?.let { it in 1..5 } == true

    AlertDialog(
        onDismissRequest = onDismiss,
        shape            = RoundedCornerShape(16.dp),
        title = {
            Text(
                text  = "Edit ${order.orderNumber}",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                DialogField(
                    value         = weight,
                    onValueChange = { weight = it },
                    label         = "Weight (kg)",
                    icon          = Icons.Default.Scale,
                    keyboardType  = KeyboardType.Decimal
                )
                DialogField(
                    value         = volume,
                    onValueChange = { volume = it },
                    label         = "Volume (m³)",
                    icon          = Icons.Default.Straighten,
                    keyboardType  = KeyboardType.Decimal
                )
                DialogField(
                    value         = price,
                    onValueChange = { price = it },
                    label         = "Price ($)",
                    icon          = Icons.Default.AttachMoney,
                    keyboardType  = KeyboardType.Decimal
                )
                DialogField(
                    value         = priority,
                    onValueChange = { priority = it },
                    label         = "Priority (1–5)",
                    icon          = Icons.Default.Flag,
                    keyboardType  = KeyboardType.Number,
                    isError       = priority.toIntOrNull()?.let { it !in 1..5 } ?: true,
                    supportingText = "Must be between 1 and 5"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        order.copy(
                            totalWeight = weight.toDouble(),
                            totalVolume = volume.toDouble(),
                            totalPrice  = price.toDouble(),
                            priority    = priority.toInt()
                        )
                    )
                },
                enabled = isValid
            ) { Text("Save") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun DialogField(
    value          : String,
    onValueChange  : (String) -> Unit,
    label          : String,
    icon           : ImageVector,
    keyboardType   : KeyboardType,
    isError        : Boolean = false,
    supportingText : String? = null
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label) },
        leadingIcon   = { Icon(icon, contentDescription = null) },
        keyboardOptions   = KeyboardOptions(keyboardType = keyboardType),
        singleLine    = true,
        isError       = isError,
        supportingText = if (isError && supportingText != null) {
            { Text(supportingText, color = MaterialTheme.colorScheme.error) }
        } else null,
        modifier      = Modifier.fillMaxWidth()
    )
}
