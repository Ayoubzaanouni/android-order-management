package com.example.projet_android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    onDelete       : () -> Unit = {},
    onStatusChange : ((newStatus: String) -> Unit)? = null
) {
    var showDialog by remember { mutableStateOf(false) }
    val currentStatus = try {
        com.example.projet_android.ui.view_model.OrderStatus.valueOf(order.status)
    } catch (_: Exception) { null }
    val nextStatus = currentStatus?.next()

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
            if (onStatusChange != null && nextStatus != null) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick  = { onStatusChange(nextStatus.label) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RectangleShape,
                    border = BorderStroke(1.dp, Mauve),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Mauve,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "NEXT: ${nextStatus.label}",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace,
                        color = Mauve
                    )
                }
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
                    Text("[ CLOSE ]", fontFamily = FontFamily.Monospace, color = Sky)
                }
            }
        )
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
            tint               = Sapphire,
            modifier           = Modifier.size(18.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text  = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = Text
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = Overlay1
        )
    }
}

@Composable
private fun StatusChip(status: String) {
    val color = when (status) {
        "DELIVERED"  -> Green
        "SHIPPED"    -> Mauve
        "PROCESSING" -> Yellow
        "CANCELLED"  -> Red
        else         -> Subtext0
    }
    Surface(
        shape = RectangleShape,
        color = Color.Transparent,
        border = BorderStroke(1.dp, color)
    ) {
        Text(
            text     = "<$status>",
            color    = color,
            style    = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PriorityIndicator(priority: Int) {
    val color = when (priority) {
        5    -> Red
        4    -> Peach
        3    -> Yellow
        2    -> Green
        else -> Blue
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
            text  = "PRIO:$priority",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
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
            CircularProgressIndicator(color = Blue)
            Spacer(Modifier.height(16.dp))
            Text(
                text  = "INITIALIZING_DATA...",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = Sapphire
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
            tint               = Surface2
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text      = "ERR: NO_ORDERS_FOUND",
            style     = MaterialTheme.typography.titleMedium,
            fontFamily = FontFamily.Monospace,
            color     = Red
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text      = "EXEC: SEED_DATA TO CONTINUE",
            style     = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color     = Overlay1,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(horizontal = 32.dp)
        )
    }
}

private fun formatTimestamp(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        .format(Date(millis))
