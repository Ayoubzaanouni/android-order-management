package com.example.projet_android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.projet_android.model.Container
import com.example.projet_android.ui.screens.color
import com.example.projet_android.ui.screens.label
import com.example.projet_android.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun ContainerCard(
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
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, if (container.enabled) Surface2 else Surface0),
        colors = CardDefaults.cardColors(
            containerColor = if (container.enabled) Surface0 else Base
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "> ${container.name}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (container.enabled) Blue else Subtext0
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Badge(
                        containerColor = container.status.color().copy(alpha = 0.1f),
                    ) {
                        Text(
                            text = "[ ${container.status.label()} ]",
                            color = container.status.color(),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Switch(
                    checked = container.enabled,
                    onCheckedChange = { onToggleEnabled() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Green,
                        checkedTrackColor = Surface1,
                        uncheckedThumbColor = Overlay0,
                        uncheckedTrackColor = Surface0
                    )
                )
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Sapphire
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Red
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Surface1
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ContainerStat("WEIGHT", "${container.maxWeight}kg", alpha)
                ContainerStat("VOLUME", "${container.maxVolume}m3", alpha)
                ContainerStat(
                    "FILL",
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
            color = Overlay2.copy(alpha = alpha)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Text.copy(alpha = alpha)
        )
    }
}
