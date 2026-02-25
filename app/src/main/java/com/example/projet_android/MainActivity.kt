package com.example.projet_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projet_android.database.AppDatabase
import com.example.projet_android.model.Company
import com.example.projet_android.ui.screens.LoginScreen
import com.example.projet_android.ui.screens.OrderScreen
import com.example.projet_android.ui.screens.SignUpScreen
import com.example.projet_android.ui.theme.ProjetandroidTheme
import com.example.projet_android.ui.view_model.OrderViewModel
import com.example.projet_android.util.PasswordUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjetandroidTheme {
                ProjetandroidApp(database)
            }
        }
    }
}

enum class AuthScreen {
    LOGIN, SIGNUP
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Commands", Icons.Default.Home),
    CONTAINERS("Containers", Icons.Default.Settings),
    PROFILE("Profile", Icons.Default.AccountBox),
}

@Composable
fun ProjetandroidApp(database: AppDatabase) {
    var isLoggedIn by rememberSaveable { mutableStateOf(false) }
    var currentAuthScreen by rememberSaveable { mutableStateOf(AuthScreen.LOGIN) }
    var loggedInCompanyId by rememberSaveable { mutableIntStateOf(-1) }

    if (!isLoggedIn) {
        when (currentAuthScreen) {
            AuthScreen.LOGIN -> {
                LoginScreen(
                    onLoginSuccess = { companyId ->
                        loggedInCompanyId = companyId
                        isLoggedIn = true
                    },
                    onNavigateToSignUp = { currentAuthScreen = AuthScreen.SIGNUP },
                    onLogin = { email, password ->
                        withContext(Dispatchers.IO) {
                            val company = database.companyDao().getCompanyByEmail(email)
                            if (company != null && PasswordUtils.verify(password, company.password)) {
                                company.id
                            } else {
                                null
                            }
                        }
                    }
                )
            }
            AuthScreen.SIGNUP -> {
                SignUpScreen(
                    onSignUpSuccess = { currentAuthScreen = AuthScreen.LOGIN },
                    onNavigateBack = { currentAuthScreen = AuthScreen.LOGIN },
                    onSignUp = { name, email, password ->
                        withContext(Dispatchers.IO) {
                            val existing = database.companyDao().getCompanyByEmail(email)
                            if (existing == null) {
                                database.companyDao().insert(
                                    Company(
                                        name = name,
                                        email = email,
                                        password = PasswordUtils.hash(password)
                                    )
                                )
                                true
                            } else {
                                false
                            }
                        }
                    }
                )
            }
        }
    } else {
        MainAppContent(
            companyId = loggedInCompanyId,
            database,
            onLogout = {
                isLoggedIn = false
                loggedInCompanyId = -1
            }
        )
    }
}

@Composable
fun MainAppContent(companyId: Int, database: AppDatabase, onLogout: () -> Unit) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = { Icon(it.icon, contentDescription = it.label) },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            when (currentDestination) {
                AppDestinations.HOME -> {
                    OrderScreen(
                        viewModel = viewModel(factory = OrderViewModel.factory(database))
                    )
                }
                AppDestinations.CONTAINERS -> {
                    Text(
                        text = "Container Configuration",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                AppDestinations.PROFILE -> {
                    Column(
                        modifier = Modifier.padding(innerPadding).fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Profile", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(onClick = onLogout) { Text("Disconnect") }
                    }
                }
            }
        }
    }
}