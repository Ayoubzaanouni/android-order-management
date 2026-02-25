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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SeedFab(
    isLoading : Boolean,
    onClick   : () -> Unit
) {
    ExtendedFloatingActionButton(
        onClick           = { if (!isLoading) onClick() },
        containerColor    = MaterialTheme.colorScheme.primaryContainer,
        contentColor      = MaterialTheme.colorScheme.onPrimaryContainer,
        icon = {
            Crossfade(
                targetState = isLoading,
                label       = "fab_icon"
            ) { loading ->
                if (loading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = MaterialTheme.colorScheme.onPrimaryContainer,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector        = Icons.Default.Refresh,
                        contentDescription = "Seed orders"
                    )
                }
            }
        },
        text = {
            Crossfade(
                targetState = isLoading,
                label       = "fab_text"
            ) { loading ->
                Text(if (loading) "Seeding…" else "Seed Orders")
            }
        }
    )
}

