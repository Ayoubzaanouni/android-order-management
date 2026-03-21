package com.example.projet_android.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import com.example.projet_android.ui.theme.Mantle
import com.example.projet_android.ui.theme.Text

data class NavigationDestination(
    val label: String,
    val icon: ImageVector,
    val isSelected: Boolean,
    val hasBadge: Boolean = false,
    val onClick: () -> Unit
)

@Composable
fun AppNavigationWrapper(
    destinations: List<NavigationDestination>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    NavigationSuiteScaffold(
        modifier = modifier,
        navigationSuiteItems = {
            destinations.forEach { destination ->
                item(
                    icon = { 
                        if (destination.hasBadge) {
                            androidx.compose.material3.BadgedBox(
                                badge = { androidx.compose.material3.Badge() }
                            ) {
                                Icon(destination.icon, contentDescription = destination.label)
                            }
                        } else {
                            Icon(destination.icon, contentDescription = destination.label)
                        }
                    },
                    label = { 
                        Text(
                            text = destination.label, 
                            style = MaterialTheme.typography.labelSmall, 
                            fontSize = 8.sp,
                            lineHeight = 10.sp,
                            maxLines = 1
                        ) 
                    },
                    selected = destination.isSelected,
                    onClick = destination.onClick
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationBarContainerColor = Mantle,
            navigationBarContentColor = Text,
            navigationRailContainerColor = Mantle,
            navigationRailContentColor = Text
        ),
        content = content
    )
}
