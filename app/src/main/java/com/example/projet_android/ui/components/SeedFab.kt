package com.example.projet_android.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SeedFab(
    isLoading: Boolean,
    onClick: (ordersPerCompany: Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        SeedOrdersDialog(
            onConfirm = { count ->
                showDialog = false
                onClick(count)
            },
            onDismiss = { showDialog = false }
        )
    }

    ExtendedFloatingActionButton(
        onClick        = { if (!isLoading) showDialog = true }, // ✅ open dialog instead
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor   = MaterialTheme.colorScheme.onPrimaryContainer,
        icon = {
            Crossfade(targetState = isLoading, label = "fab_icon") { loading ->
                if (loading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = MaterialTheme.colorScheme.onPrimaryContainer,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "Seed orders")
                }
            }
        },
        text = {
            Crossfade(targetState = isLoading, label = "fab_text") { loading ->
                Text(if (loading) "Seeding…" else "Seed Orders")
            }
        }
    )
}
