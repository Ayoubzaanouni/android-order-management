package com.example.projet_android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SeedOrdersDialog(
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var rawInput by remember { mutableStateOf("50") }
    val count = rawInput.toIntOrNull()
    val isValid = count != null && count in 1..999

    AlertDialog(
        onDismissRequest = onDismiss,
        icon    = { Icon(Icons.Default.Refresh, contentDescription = null) },
        title   = { Text("Seed Orders") },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("How many orders per company do you want to generate?")
                OutlinedTextField(
                    value         = rawInput,
                    onValueChange = { rawInput = it.filter(Char::isDigit) },
                    label         = { Text("Orders per company") },
                    isError       = !isValid,
                    supportingText = {
                        if (!isValid) Text("Enter a number between 1 and 999")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine    = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = { if (isValid) onConfirm(count) },
                enabled  = isValid
            ) {
                Text("Generate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

