package com.example.projet_android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.projet_android.model.Order
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderCard(
    order    : Order,
    modifier : Modifier = Modifier
) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text       = order.orderNumber,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text  = "Company #${order.companyId}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(status = order.status)
            }

            HorizontalDivider(
                modifier  = Modifier.padding(vertical = 12.dp),
                thickness = 0.5.dp,
                color     = MaterialTheme.colorScheme.outlineVariant
            )

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    icon  = Icons.Default.Scale,
                    label = "Weight",
                    value = "${order.totalWeight} kg"
                )
                MetricItem(
                    icon  = Icons.Default.Straighten,
                    label = "Volume",
                    value = "${order.totalVolume} m³"
                )
                MetricItem(
                    icon  = Icons.Default.AttachMoney,
                    label = "Price",
                    value = "$${"%.2f".format(order.totalPrice)}"
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                PriorityIndicator(priority = order.priority)
                Text(
                    text  = formatTimestamp(order.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetricItem(
    icon  : ImageVector,
    label : String,
    value : String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector        = icon,
            contentDescription = label,
            tint               = MaterialTheme.colorScheme.primary,
            modifier           = Modifier.size(18.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text  = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusChip(status: String) {
    val (bg, fg) = when (status) {
        "DELIVERED"  -> MaterialTheme.colorScheme.primaryContainer  to MaterialTheme.colorScheme.onPrimaryContainer
        "SHIPPED"    -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        "PROCESSING" -> MaterialTheme.colorScheme.tertiaryContainer  to MaterialTheme.colorScheme.onTertiaryContainer
        "CANCELLED"  -> MaterialTheme.colorScheme.errorContainer     to MaterialTheme.colorScheme.onErrorContainer
        else         -> MaterialTheme.colorScheme.surfaceVariant     to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = bg
    ) {
        Text(
            text     = status,
            color    = fg,
            style    = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PriorityIndicator(priority: Int) {
    val color = when (priority) {
        5    -> Color(0xFFD32F2F)
        4    -> Color(0xFFF57C00)
        3    -> Color(0xFFFBC02D)
        2    -> Color(0xFF388E3C)
        else -> Color(0xFF1976D2)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector        = Icons.Default.Flag,
            contentDescription = "Priority $priority",
            tint               = color,
            modifier           = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text  = "P$priority",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FullScreenLoading(modifier: Modifier = Modifier) {
    Box(
        modifier        = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(
                text  = "Seeding orders…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyOrdersPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier             = modifier.fillMaxSize(),
        horizontalAlignment  = Alignment.CenterHorizontally,
        verticalArrangement  = Arrangement.Center
    ) {
        Icon(
            imageVector        = Icons.Default.Inbox,
            contentDescription = null,
            modifier           = Modifier.size(72.dp),
            tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text      = "No orders yet",
            style     = MaterialTheme.typography.titleMedium,
            color     = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text      = "Tap \"Seed Orders\" to generate sample data",
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 32.dp)
        )
    }
}

private fun formatTimestamp(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        .format(Date(millis))


