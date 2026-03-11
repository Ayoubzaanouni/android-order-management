package com.example.projet_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projet_android.dao.EmployeeDao
import com.example.projet_android.model.Employee
import com.example.projet_android.util.PasswordUtils
import kotlinx.coroutines.launch

private val CEDarkBg = Color(0xFF0F1117)
private val CECardBg = Color(0xFF1A1D26)
private val CEAccent = Color(0xFF4F8EF7)
private val CETextPrimary = Color(0xFFE8EAF0)
private val CETextSecondary = Color(0xFF9095A8)
private val CERed = Color(0xFFFF6B6B)

@Composable
fun CompanyEmployeesScreen(
    companyId: Int,
    employeeDao: EmployeeDao
) {
    val scope = rememberCoroutineScope()
    val employees by employeeDao.getAllByCompany(companyId).collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Employee?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CEDarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)))
                    )
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Column {
                    Text("Employees", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${employees.size} member(s)", fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                }
            }

            if (employees.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = CETextSecondary, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No employees yet", color = CETextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tap + to add the first one", color = CETextSecondary, fontSize = 12.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(employees, key = { it.id }) { emp ->
                        EmployeeCard(emp, onDelete = { deleteTarget = emp })
                    }
                    item { Spacer(modifier = Modifier.navigationBarsPadding()) }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .navigationBarsPadding(),
            containerColor = CEAccent
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add employee", tint = Color.White)
        }
    }
    if (showAddDialog) {
        AddEmployeeDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, email, password ->
                val existingEmployee = employeeDao.getByEmail(email)
                if (existingEmployee != null) {
                    return@AddEmployeeDialog "This email is already registered to an employee"
                }

                employeeDao.insert(
                    Employee(
                        companyId = companyId,
                        name = name,
                        email = email,
                        password = PasswordUtils.hash(password)
                    )
                )
                null
            }
        )
    }
    deleteTarget?.let { emp ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = CECardBg,
            title = { Text("Delete employee", color = CETextPrimary) },
            text = { Text("Remove ${emp.name}? This cannot be undone.", color = CETextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch { employeeDao.delete(emp) }
                        deleteTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CERed)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel", color = CETextSecondary) }
            }
        )
    }
}

@Composable
private fun EmployeeCard(employee: Employee, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CECardBg)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(CEAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    employee.name.take(1).uppercase(),
                    color = CEAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(employee.name, fontWeight = FontWeight.SemiBold, color = CETextPrimary, fontSize = 15.sp)
                Text(employee.email, color = CETextSecondary, fontSize = 12.sp)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CERed)
            }
        }
    }
}

@Composable
private fun AddEmployeeDialog(
    onDismiss: () -> Unit,
    onConfirm: suspend (String, String, String) -> String?
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CECardBg,
        title = { Text("Add Employee", color = CETextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it; error = null },
                    label = { Text("Full Name", color = CETextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email, onValueChange = { email = it; error = null },
                    label = { Text("Email", color = CETextSecondary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password, onValueChange = { password = it; error = null },
                    label = { Text("Password", color = CETextSecondary) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(error!!, color = CERed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        name.isBlank() -> error = "Name is required"
                        email.isBlank() -> error = "Email is required"
                        password.length < 6 -> error = "Password must be at least 6 characters"
                        else -> {
                            scope.launch {
                                val resultError = onConfirm(name.trim(), email.trim(), password)
                                if (resultError != null) {
                                    error = resultError
                                } else {
                                    onDismiss()
                                }
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CEAccent)
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = CETextSecondary) }
        }
    )
}
