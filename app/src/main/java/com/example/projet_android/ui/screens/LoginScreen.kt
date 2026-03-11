package com.example.projet_android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

enum class LoginRole { COMPANY, EMPLOYEE, ADMIN }

typealias LoginResult = Triple<LoginRole, Int, Int>

@Composable
fun LoginScreen(
    onLoginSuccess: (role: LoginRole, id: Int, companyId: Int) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onLogin: suspend (identifier: String, password: String) -> LoginResult?
) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Order Management",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Sign in to continue",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = identifier,
            onValueChange = { identifier = it; errorMessage = null },
            label = { Text("Email or Username") },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("Password") },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { isLoading = true; errorMessage = null },
            enabled = !isLoading && identifier.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Sign In")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onNavigateToSignUp) {
            Text("Don't have an account? Register Company")
        }
    }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            val result = onLogin(identifier, password)
            isLoading = false
            if (result != null) {
                onLoginSuccess(result.first, result.second, result.third)
            } else {
                errorMessage = "Invalid credentials or account not approved"
            }
        }
    }
}

