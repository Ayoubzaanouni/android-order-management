package com.example.projet_android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projet_android.model.Order
import com.example.projet_android.model.Product
import com.example.projet_android.ui.theme.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderCard(
    order          : Order,
    getProductsForOrder: (Int) -> Flow<List<Product>>,
    modifier       : Modifier = Modifier,
    canDelete      : Boolean = false,
    onDelete       : () -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier  = modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        shape     = RectangleShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = BorderStroke(1.dp, Surface2),
        colors    = CardDefaults.cardColors(containerColor = Surface0)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Header ───────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = "$ ${order.orderNumber}",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color      = Blue
                    )
                    Text(
                        text  = "USR: #${order.companyId}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = Overlay2
                    )
                }
                StatusChip(status = order.status)
                if (canDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete order",
                            tint = Red,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier  = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color     = Surface1
            )

            // ── Metrics ──────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(Icons.Default.Scale,       "WGT", "${order.totalWeight}kg")
                MetricItem(Icons.Default.Straighten,  "VOL", "${order.totalVolume}m3")
                MetricItem(Icons.Default.AttachMoney, "PRC",  "$${"%.2f".format(order.totalPrice)}")
            }

            Spacer(Modifier.height(12.dp))

            // ── Footer ───────────────────────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                PriorityIndicator(priority = order.priority)
                Text(
                    text  = formatTimestamp(order.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = Overlay1
                )
            }
        }
    }

    if (showDialog) {
        val flow = remember(order.id) { getProductsForOrder(order.id) }
        val products by flow.collectAsState(initial = emptyList())

        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = Base,
            shape = RectangleShape,
            title = {
                Text(
                    text = "--- PRODUCTS [${order.orderNumber}] ---",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Yellow
                )
            },
            text = {
                if (products.isEmpty()) {
                    Text(
                        text = "EMPTY_SET",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = Subtext1
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(products) { product ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RectangleShape,
                                border = BorderStroke(1.dp, Surface1),
                                colors = CardDefaults.cardColors(
                                    containerColor = Surface0
                                ),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "+ ${product.name}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Teal
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "ID: ${product.id} | ${if (product.fragility) "FRAGILE" else "ROBUST"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = FontFamily.Monospace,
                                            color = Overlay2
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${product.weight}kg",
                                            fontFamily = FontFamily.Monospace,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Subtext0
                                        )
                                        Text(
                                            text = "${product.volume}m3",
                                            fontFamily = FontFamily.Monospace,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Subtext0
                                        )
                                        Text(
                                            text = "$${"%.2f".format(product.price)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = FontFamily.Monospace,
                                            color = Green
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(
                        "CLOSE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Blue
                    )
                }
            }
        )
    }
}

@Composable
private fun StatusChip(status: String) {
    val color = when (status) {
        "PENDING"   -> Yellow
        "ACCEPTED"  -> Teal
        "DELIVERED" -> Green
        "CANCELLED" -> Red
        else        -> Mauve
    }
    Surface(
        color  = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color),
        shape  = RectangleShape
    ) {
        Text(
            text       = status,
            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style      = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color      = color
        )
    }
}

@Composable
private fun MetricItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Overlay2, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Overlay1, fontFamily = FontFamily.Monospace)
            Text(value, style = MaterialTheme.typography.bodySmall, color = Text, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun PriorityIndicator(priority: Int) {
    val color = when (priority) {
        in 4..5 -> Red
        3       -> Peach
        else    -> Blue
    }
    val label = when (priority) {
        in 4..5 -> "HIGH"
        3       -> "MEDIUM"
        else    -> "LOW"
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clickable(enabled = false) {}
                .padding(2.dp)
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = color, shape = RectangleShape) {}
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = "$label ($priority)",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = color
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
