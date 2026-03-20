package com.example.projet_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projet_android.database.AppDatabase
import com.example.projet_android.model.Company
import com.example.projet_android.ui.components.AppNavigationWrapper
import com.example.projet_android.ui.components.NavigationDestination
import com.example.projet_android.ui.screens.*
import com.example.projet_android.ui.theme.ProjetandroidTheme
import com.example.projet_android.ui.view_model.OrderViewModel
import com.example.projet_android.util.PasswordUtils
import com.example.projet_android.util.SessionManager
import com.example.projet_android.util.SessionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val sessionManager by lazy { SessionManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjetandroidTheme {
                ProjetandroidApp(database, sessionManager)
            }
        }
    }
}

enum class AuthScreen { LOGIN, SIGNUP }

enum class CompanyDestination(val label: String, val icon: ImageVector) {
    HOME("Orders", Icons.Default.Home),
    CONTAINERS("Containers", Icons.Default.Settings),
    EMPLOYEES("Employees", Icons.Default.Group),
    PROFILE("Profile", Icons.Default.AccountBox),
}
enum class EmployeeDestination(val label: String, val icon: ImageVector) {
    HOME("Orders", Icons.Default.Home),
    CONTAINERS("Containers", Icons.Default.Settings),
    PROFILE("Profile", Icons.Default.AccountBox),
}

@Composable
fun ProjetandroidApp(database: AppDatabase, sessionManager: SessionManager) {
    val session by sessionManager.sessionFlow.collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    var currentAuthScreen by rememberSaveable { mutableStateOf(AuthScreen.LOGIN) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val s = session) {
            null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is SessionState.LoggedInAdmin -> {
                AdminDashboardScreen(
                    companyDao = database.companyDao(),
                    onLogout = { scope.launch { sessionManager.clearSession() } }
                )
            }

            is SessionState.LoggedInCompany -> {
                CompanyAppContent(
                    companyId = s.companyId,
                    database = database,
                    onLogout = { scope.launch { sessionManager.clearSession() } }
                )
            }

            is SessionState.LoggedInEmployee -> {
                EmployeeAppContent(
                    companyId = s.companyId,
                    database = database,
                    onLogout = { scope.launch { sessionManager.clearSession() } }
                )
            }

            is SessionState.NoSession -> {
                when (currentAuthScreen) {
                    AuthScreen.LOGIN -> {
                        LoginScreen(
                            onLoginSuccess = { role, id, companyId ->
                                scope.launch {
                                    when (role) {
                                        LoginRole.ADMIN -> sessionManager.saveAdminSession(id)
                                        LoginRole.COMPANY -> sessionManager.saveCompanySession(id)
                                        LoginRole.EMPLOYEE -> sessionManager.saveEmployeeSession(id, companyId)
                                    }
                                }
                            },
                            onNavigateToSignUp = { currentAuthScreen = AuthScreen.SIGNUP },
                            onLogin = { identifier, password ->
                                withContext(Dispatchers.IO) {
                                    val admin = database.adminDao().getByUsername(identifier)
                                    if (admin != null && PasswordUtils.verify(password, admin.password)) {
                                        return@withContext LoginResult(LoginRole.ADMIN, admin.id, admin.id)
                                    }
                                    val company = database.companyDao().getCompanyByEmail(identifier)
                                    if (company != null
                                        && PasswordUtils.verify(password, company.password)
                                        && company.status == "APPROVED"
                                    ) {
                                        return@withContext LoginResult(LoginRole.COMPANY, company.id, company.id)
                                    }
                                    val emp = database.employeeDao().getByEmail(identifier)
                                    if (emp != null && PasswordUtils.verify(password, emp.password)) {
                                        return@withContext LoginResult(LoginRole.EMPLOYEE, emp.id, emp.companyId)
                                    }
                                    null
                                }
                            }
                        )
                    }


                    AuthScreen.SIGNUP -> {
                        SignUpScreen(
                            onSignUpSuccess = {
                                currentAuthScreen = AuthScreen.LOGIN
                            },
                            onNavigateBack = { currentAuthScreen = AuthScreen.LOGIN },
                            onSignUp = { name, email, phone, password ->
                                withContext(Dispatchers.IO) {
                                    val existing = database.companyDao().getCompanyByEmail(email)
                                    if (existing == null) {
                                        database.companyDao().insert(
                                            Company(
                                                name = name,
                                                email = email,
                                                phone = phone,
                                                password = PasswordUtils.hash(password),
                                                status = "PENDING"
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
            }
        }
    }
}

@Composable
fun CompanyAppContent(companyId: Int, database: AppDatabase, onLogout: () -> Unit) {
    var currentDestination by rememberSaveable { mutableStateOf(CompanyDestination.HOME) }

    val destinations = CompanyDestination.entries.map {
        NavigationDestination(
            label = it.label,
            icon = it.icon,
            isSelected = it == currentDestination,
            onClick = { currentDestination = it }
        )
    }

    AppNavigationWrapper(destinations = destinations) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentDestination) {
                    CompanyDestination.HOME -> {
                        OrderScreen(
                            viewModel = viewModel(factory = OrderViewModel.factory(database, companyId)),
                            canDelete = true,
                            canChangeStatus = true
                        )
                    }
                    CompanyDestination.CONTAINERS -> {
                        ContainerScreen(
                            containerDao = database.containerDao(),
                            paddingValues = PaddingValues(0.dp),
                            readOnly = false
                        )
                    }
                    CompanyDestination.EMPLOYEES -> {
                        CompanyEmployeesScreen(
                            companyId = companyId,
                            employeeDao = database.employeeDao()
                        )
                    }
                    CompanyDestination.PROFILE -> {
                        ProfileTab(onLogout = onLogout, paddingValues = PaddingValues(0.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EmployeeAppContent(companyId: Int, database: AppDatabase, onLogout: () -> Unit) {
    var currentDestination by rememberSaveable { mutableStateOf(EmployeeDestination.HOME) }

    val destinations = EmployeeDestination.entries.map {
        NavigationDestination(
            label = it.label,
            icon = it.icon,
            isSelected = it == currentDestination,
            onClick = { currentDestination = it }
        )
    }

    AppNavigationWrapper(destinations = destinations) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentDestination) {
                    EmployeeDestination.HOME -> {
                        OrderScreen(
                            viewModel = viewModel(factory = OrderViewModel.factory(database, companyId)),
                            canDelete = false,
                            canChangeStatus = true
                        )
                    }
                    EmployeeDestination.CONTAINERS -> {
                        ContainerScreen(
                            containerDao = database.containerDao(),
                            paddingValues = PaddingValues(0.dp),
                            readOnly = true
                        )
                    }
                    EmployeeDestination.PROFILE -> {
                        ProfileTab(onLogout = onLogout, paddingValues = PaddingValues(0.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTab(onLogout: () -> Unit, paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onLogout) { Text("Disconnect") }
    }
}
