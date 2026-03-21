package com.example.projet_android.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.projet_android.model.ProductItem
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.projet_android.model.ContainerSlot
import com.example.projet_android.model.SlotStatus
import com.example.projet_android.ui.theme.*
import com.example.projet_android.ui.view_model.ShippingOptimizationState
import com.example.projet_android.ui.view_model.ShippingViewModel

@Composable
fun ShippingPlanScreen(viewModel: ShippingViewModel, paddingValues: PaddingValues) {
    val state by viewModel.optimizationState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.runOptimization()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
    ) {
        Text(
            text = "SHIPPING OPTIMIZATION ENGINE",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Lavender
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        when (val currState = state) {
            is ShippingOptimizationState.Idle, is ShippingOptimizationState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Blue)
                }
            }
            is ShippingOptimizationState.Error -> {
                Text(
                    text = "ERROR: ${currState.message}",
                    color = Red,
                    fontFamily = FontFamily.Monospace
                )
                Button(
                    onClick = { viewModel.runOptimization() },
                    colors = ButtonDefaults.buttonColors(containerColor = Red),
                    shape = RectangleShape
                ) {
                    Text("RETRY", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
            is ShippingOptimizationState.Success -> {
                val result = currState.result

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Surface1),
                    shape = RectangleShape
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "TOTAL REVENUE: $${"%.2f".format(result.totalRevenue)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Green
                        )
                        Spacer(Modifier.height(8.dp))
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("SHIPPED ITEMS: ${result.totalProductsShipped}", color = Text, fontFamily = FontFamily.Monospace)
                            Spacer(Modifier.height(4.dp))
                            Text("POSTPONED ITEMS: ${result.totalProductsPostponed}", color = Peach, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(
                        onClick = { 
                            viewModel.runOptimization(weightScore = 0.8, priceScore = 0.1, wasteScore = 0.1) 
                        },
                        shape = RectangleShape,
                        border = BorderStroke(1.dp, Blue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("MAX FILL", color = Blue, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = { 
                            viewModel.runOptimization(weightScore = 0.1, priceScore = 0.8, wasteScore = 0.1) 
                        },
                        shape = RectangleShape,
                        border = BorderStroke(1.dp, Yellow),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("MAX VALUE", color = Yellow, fontFamily = FontFamily.Monospace)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                var showWarningDialog by remember { mutableStateOf(false) }
                var selectedTab by remember { mutableIntStateOf(0) }
                val postponedOrdersCount = result.postponedSlots.flatMap { it.assignedProducts }.map { it.orderId }
                    .union(result.waitingProducts.map { it.orderId }).size
                val tabs = listOf("SHIPPED\n (${result.shippedSlots.size} CONTAINERS)", "POSTPONED\n ($postponedOrdersCount ORDERS)")

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Base,
                    contentColor = Blue
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Text(
                                    title, 
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                ) 
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val displaySlots = if (selectedTab == 0) result.shippedSlots else result.postponedSlots
                    
                    items(displaySlots) { slot ->
                        ContainerSlotCard(slot)
                    }

                    if (selectedTab == 1 && result.waitingProducts.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "POSTPONED PRODUCTS (${result.waitingProducts.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Peach,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        val groupedWaiting = result.waitingProducts.groupBy { it.orderId }.values.toList()
                        items(groupedWaiting) { products ->
                            val first = products.first()
                            ExpandableWaitingOrderCard(
                                orderNumber = first.orderNumber,
                                products = products
                            )
                        }
                    }
                    
                    if (selectedTab == 0 && result.shippedSlots.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    if (result.postponedSlots.isNotEmpty()) {
                                        showWarningDialog = true
                                    } else {
                                        viewModel.saveShippingPlan(result) {
                                        }
                                    }
                                },
                                shape = RectangleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = Green),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("CONFIRM & SAVE SHIPMENT PLAN", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = Base)
                            }
                            Spacer(Modifier.height(32.dp))
                        }
                    }
                }
                if (showWarningDialog) {
                    AlertDialog(
                        onDismissRequest = { showWarningDialog = false },
                        containerColor = Base,
                        titleContentColor = Red,
                        title = { Text("Warning: Thresholds Not Met!", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
                        text = { Text("There are postponed containers that did not meet their strict fill threshold. Do you want to forcibly ship them anyway and bypass the limits?", color = Text, fontFamily = FontFamily.Monospace) },
                        confirmButton = {
                            Button(
                                colors = ButtonDefaults.buttonColors(containerColor = Red),
                                shape = RectangleShape,
                                onClick = { 
                                    showWarningDialog = false
                                    val newResult = result.copy(
                                        shippedSlots = result.shippedSlots + result.postponedSlots.map { it.apply { status = SlotStatus.SHIPPED } },
                                        postponedSlots = emptyList()
                                    )
                                    viewModel.saveShippingPlan(newResult) {}
                                }
                            ) { Text("FORCE SHIP ALL", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) }
                        },
                        dismissButton = {
                            OutlinedButton(
                                border = BorderStroke(1.dp, Blue),
                                shape = RectangleShape,
                                onClick = { 
                                    showWarningDialog = false 
                                    viewModel.saveShippingPlan(result) {}
                                }
                            ) { Text("SHIP STRICT ONLY", color = Blue, fontFamily = FontFamily.Monospace) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ContainerSlotCard(slot: ContainerSlot) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = Surface0),
        border = BorderStroke(1.dp, if (slot.status == SlotStatus.SHIPPED) Teal else Peach)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "[${slot.container.name.uppercase()}]",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Text
                )
                
                val statusColor = if (slot.status == SlotStatus.SHIPPED) Teal else Peach
                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, statusColor),
                    shape = RectangleShape
                ) {
                    Text(
                        text = slot.status.name,
                        color = statusColor,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            
            FillBar("WEIGHT", slot.fillRateWeight, slot.container.fillThreshold)
            Spacer(Modifier.height(8.dp))
            FillBar("VOLUME", slot.fillRateVolume, slot.container.fillThreshold)

            Spacer(Modifier.height(12.dp))

            Text(
                text = "CONTAINER VAL: $${"%.2f".format(slot.totalPrice)}",
                color = Green,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium
            )
            if (slot.status == SlotStatus.SHIPPED && maxOf(slot.fillRateWeight, slot.fillRateVolume) < slot.container.fillThreshold && slot.hasPriorityItem) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Yellow, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Shipped below threshold due to PRIORITY items",
                        style = MaterialTheme.typography.bodySmall,
                        color = Yellow,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Surface2)
            Spacer(Modifier.height(8.dp))

            val grouped = slot.assignedProducts.groupBy { it.orderId }
            grouped.forEach { (_, products) ->
                val first = products.first()
                Text(
                    text = "▶ ORDER ${first.orderNumber} ${if (first.isPriority) "(PRIORITY)" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (first.isPriority) Red else Blue,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                products.forEach { p ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 2.dp, bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("- ${p.productName}", color = Text, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                        Text("$${p.price} | ${p.weight}kg | ${p.volume}m3", color = Subtext0, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
fun FillBar(label: String, fillRatio: Double, threshold: Double) {
    val pct = (fillRatio * 100).toInt()
    val clampedRatio = fillRatio.coerceIn(0.0, 1.0).toFloat()
    
    val barColor = when {
        fillRatio >= 1.0 -> Red
        fillRatio >= threshold -> Green
        else -> Yellow
    }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(text = label.padEnd(6), fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall, color = Overlay1)
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .background(Surface1, shape = RoundedCornerShape(2.dp))
                .clip(RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = clampedRatio)
                    .background(barColor)
            )
            // Threshold marker
            Row(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.fillMaxWidth(threshold.toFloat()))
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(Text)
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Text(text = "${pct}%", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall, color = Text)
    }
}

@Composable
fun ExpandableWaitingOrderCard(orderNumber: String, products: List<com.example.projet_android.model.ProductItem>) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { expanded = !expanded },
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = Surface0),
        border = BorderStroke(1.dp, Surface2)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "▶ ORDER $orderNumber",
                    color = Peach,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${products.size} postponed items",
                    color = Overlay1,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = Surface2)
                Spacer(Modifier.height(8.dp))
                products.forEach { p ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("- ${p.productName}", color = Text, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Column(horizontalAlignment = Alignment.End) {
                            Text("$${p.price}", color = Green, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            Text("${p.weight}kg | ${p.volume}m3", color = Overlay1, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
