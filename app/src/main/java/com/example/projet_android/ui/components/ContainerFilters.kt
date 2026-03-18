package com.example.projet_android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.projet_android.model.ContainerStatus
import com.example.projet_android.ui.screens.ContainerSortOrder
import com.example.projet_android.ui.screens.label
import com.example.projet_android.ui.theme.*

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
            TerminalFilterChip(
                selected = filterStatus == null,
                onClick = { onStatusChange(null) },
                label = "ALL"
            )

            ContainerStatus.entries.forEach { status ->
                TerminalFilterChip(
                    selected = filterStatus == status,
                    onClick = {
                        onStatusChange(
                            if (filterStatus == status) null else status
                        )
                    },
                    label = status.label().uppercase()
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
                    onCheckedChange = onEnabledOnlyChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Green,
                        checkedTrackColor = Surface1,
                        uncheckedThumbColor = Overlay0,
                        uncheckedTrackColor = Surface0
                    )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "ENABLED_ONLY",
                    fontFamily = FontFamily.Monospace,
                    color = Text,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Box {
                OutlinedButton(
                    onClick = { showSortMenu = true },
                    shape = RectangleShape,
                    border = BorderStroke(1.dp, Overlay1)
                ) {
                    Text(
                        sortOrder.label.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        color = Text
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier.background(Surface0)
                ) {
                    ContainerSortOrder.entries.forEach {
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    it.label.uppercase(), 
                                    fontFamily = FontFamily.Monospace,
                                    color = Text
                                ) 
                            },
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

@Composable
fun TerminalFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { 
            Text(
                if (selected) "* $label" else "  $label",
                fontFamily = FontFamily.Monospace
            ) 
        },
        shape = RectangleShape,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Surface1,
            selectedLabelColor = Mauve,
            labelColor = Subtext1
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = if (selected) Mauve else Surface1,
            selectedBorderColor = Mauve,
            borderWidth = 1.dp,
            selectedBorderWidth = 1.dp
        )
    )
}
