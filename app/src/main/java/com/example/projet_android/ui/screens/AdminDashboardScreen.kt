package com.example.projet_android.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projet_android.dao.CompanyDao
import com.example.projet_android.model.Company
import kotlinx.coroutines.launch

private val DarkBg = Color(0xFF0F1117)
private val CardBg = Color(0xFF1A1D26)
private val AccentBlue = Color(0xFF4F8EF7)
private val AccentGreen = Color(0xFF34D399)
private val AccentRed = Color(0xFFFF6B6B)
private val AccentAmber = Color(0xFFFBBF24)
private val TextPrimary = Color(0xFFE8EAF0)
private val TextSecondary = Color(0xFF9095A8)

private enum class Filter { ALL, PENDING, APPROVED, REJECTED }

@Composable
fun AdminDashboardScreen(
    companyDao: CompanyDao,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val allCompanies by companyDao.getAllCompanies().collectAsState(initial = emptyList())
    var activeFilter by remember { mutableStateOf(Filter.ALL) }
    var confirmDialog by remember { mutableStateOf<Pair<Company, String>?>(null) }

    val filtered = when (activeFilter) {
        Filter.ALL -> allCompanies
        Filter.PENDING -> allCompanies.filter { it.status == "PENDING" }
        Filter.APPROVED -> allCompanies.filter { it.status == "APPROVED" }
        Filter.REJECTED -> allCompanies.filter { it.status == "REJECTED" }
    }

    val pending = allCompanies.count { it.status == "PENDING" }
    val approved = allCompanies.count { it.status == "APPROVED" }
    val rejected = allCompanies.count { it.status == "REJECTED" }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)))
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Column {
                    Text("Admin Dashboard", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("System Management", fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                }
                TextButton(
                    onClick = onLogout,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text("Logout", color = Color.White.copy(alpha = 0.85f))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatChip(label = "Total", value = allCompanies.size, color = AccentBlue, modifier = Modifier.weight(1f))
                StatChip(label = "Pending", value = pending, color = AccentAmber, modifier = Modifier.weight(1f))
                StatChip(label = "Approved", value = approved, color = AccentGreen, modifier = Modifier.weight(1f))
                StatChip(label = "Rejected", value = rejected, color = AccentRed, modifier = Modifier.weight(1f))
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(Filter.entries) { filter ->
                    FilterChip(
                        selected = activeFilter == filter,
                        onClick = { activeFilter = filter },
                        label = { Text(filter.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No companies", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { company ->
                        CompanyAdminCard(
                            company = company,
                            onApprove = { confirmDialog = Pair(company, "APPROVED") },
                            onReject = { confirmDialog = Pair(company, "REJECTED") }
                        )
                    }
                    item { Spacer(modifier = Modifier.navigationBarsPadding()) }
                }
            }
        }
    }

    confirmDialog?.let { (company, newStatus) ->
        val verb = if (newStatus == "APPROVED") "approve" else "reject"
        AlertDialog(
            onDismissRequest = { confirmDialog = null },
            containerColor = CardBg,
            title = { Text("Confirm", color = TextPrimary) },
            text = { Text("Are you sure you want to $verb ${company.name}?", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch { companyDao.updateStatus(company.id, newStatus) }
                        confirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (newStatus == "APPROVED") AccentGreen else AccentRed
                    )
                ) { Text(verb.replaceFirstChar { it.uppercase() }) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDialog = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
@Composable
private fun StatChip(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$value", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun CompanyAdminCard(
    company: Company,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val statusColor by animateColorAsState(
        targetValue = when (company.status) {
            "APPROVED" -> AccentGreen
            "REJECTED" -> AccentRed
            else -> AccentAmber
        },
        animationSpec = tween(300),
        label = "statusColor"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AccentBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(company.name, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 15.sp)
                    Text(company.email, color = TextSecondary, fontSize = 12.sp)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        company.status,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (company.phone.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(company.phone, color = TextSecondary, fontSize = 12.sp)
                }
            }

            if (company.status == "PENDING") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve", fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = AccentRed)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", fontSize = 13.sp, color = AccentRed)
                    }
                }
            }
        }
    }
}
