package com.example.projet_android.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projet_android.model.ContainerStatus
import com.example.projet_android.ui.screens.ContainerSortOrder
import com.example.projet_android.ui.screens.label

@Composable
fun ContainerFilters(
    filterStatus: ContainerStatus?,
    onStatusChange: (ContainerStatus?) -> Unit,
    filterEnabledOnly: Boolean,
    onEnabledOnlyChange: (Boolean) -> Unit,
    sortOrder: ContainerSortOrder,
    onSortChange: (ContainerSortOrder) -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // Status filter chips
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filterStatus == null,
                onClick = { onStatusChange(null) },
                label = { Text("All") }
            )

            ContainerStatus.entries.forEach { status ->
                FilterChip(
                    selected = filterStatus == status,
                    onClick = {
                        onStatusChange(
                            if (filterStatus == status) null else status
                        )
                    },
                    label = { Text(status.label()) }
                )
            }
        }

        // Enabled + Sort
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = filterEnabledOnly,
                    onCheckedChange = onEnabledOnlyChange
                )
                Spacer(Modifier.width(8.dp))
                Text("Enabled only")
            }

            Box {
                OutlinedButton(onClick = { showSortMenu = true }) {
                    Text(sortOrder.label)
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    ContainerSortOrder.entries.forEach {
                        DropdownMenuItem(
                            text = { Text(it.label) },
                            onClick = {
                                onSortChange(it)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

