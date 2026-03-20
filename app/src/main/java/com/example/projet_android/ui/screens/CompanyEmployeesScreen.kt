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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projet_android.dao.EmployeeDao
import com.example.projet_android.model.Employee
import com.example.projet_android.ui.components.CommonTopBar
import com.example.projet_android.ui.theme.*
import com.example.projet_android.util.PasswordUtils
import kotlinx.coroutines.launch

@Composable
fun CompanyEmployeesScreen(
    companyId: Int,
    employeeDao: EmployeeDao
) {
    val scope = rememberCoroutineScope()
    val employees by employeeDao.getAllByCompany(companyId).collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Employee?>(null) }

    Scaffold(
        topBar = {
            CommonTopBar(title = "EMPLOYEE_DIRECTORY", titleColor = Green)
        },
        containerColor = Base,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Green,
                contentColor = Base,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add employee")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (employees.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Overlay2, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("NO_EMPLOYEES_FOUND", color = Overlay2, fontFamily = FontFamily.Monospace)
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
                }
            }
        }
    }

    if (showAddDialog) {
        AddEmployeeDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, email, password ->
                val existingEmployee = employeeDao.getByEmail(email)
                if (existingEmployee != null) {
                    return@AddEmployeeDialog "Email already registered"
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
            containerColor = Surface0,
            title = { Text("DELETE_CONFIRMATION", color = Text, fontFamily = FontFamily.Monospace) },
            text = { Text("Remove ${emp.name}? This action is irreversible.", color = Subtext1) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch { employeeDao.delete(emp) }
                        deleteTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red, contentColor = Base)
                ) { Text("DELETE") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("CANCEL", color = Overlay2)
                }
            }
        )
    }
}

@Composable
private fun EmployeeCard(employee: Employee, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Surface0)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Green.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    employee.name.take(1).uppercase(),
                    color = Green,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(employee.name, fontWeight = FontWeight.SemiBold, color = Text, fontSize = 15.sp)
                Text(employee.email, color = Subtext0, fontSize = 12.sp)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Red)
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
        containerColor = Surface0,
        title = { Text("ADD_NEW_EMPLOYEE", color = Text, fontFamily = FontFamily.Monospace) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it; error = null },
                    label = { Text("Full Name", color = Subtext1) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green,
                        unfocusedBorderColor = Surface2,
                        focusedLabelColor = Green,
                        cursorColor = Green
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email, onValueChange = { email = it; error = null },
                    label = { Text("Email Address", color = Subtext1) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green,
                        unfocusedBorderColor = Surface2,
                        focusedLabelColor = Green,
                        cursorColor = Green
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password, onValueChange = { password = it; error = null },
                    label = { Text("Password", color = Subtext1) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green,
                        unfocusedBorderColor = Surface2,
                        focusedLabelColor = Green,
                        cursorColor = Green
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(error!!, color = Red, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        name.isBlank() -> error = "Name required"
                        email.isBlank() -> error = "Email required"
                        password.length < 6 -> error = "Min. 6 chars"
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
                colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Base)
            ) { Text("ADD_USER") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Overlay2)
            }
        }
    )
}
