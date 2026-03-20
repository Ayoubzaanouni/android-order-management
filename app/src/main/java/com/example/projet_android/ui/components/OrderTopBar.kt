package com.example.projet_android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.projet_android.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTopBar(
    orderCount : Int,
    isLoading  : Boolean
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text  = "ORDER_MANAGER",
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Mauve
                )
                AnimatedVisibility(visible = !isLoading) {
                    Text(
                        text  = "BUFFER: $orderCount ITEMS",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = Overlay2
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Crust,
            titleContentColor = Text
        )
    )
}
