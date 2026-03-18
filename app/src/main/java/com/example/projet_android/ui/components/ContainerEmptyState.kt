package com.example.projet_android.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.projet_android.ui.theme.*

@Composable
fun ContainerEmptyState(
    isCompletelyEmpty: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = if (isCompletelyEmpty)
                    "ERR: NO_CONTAINERS_FOUND"
                else
                    "WRN: FILTER_RETURNED_EMPTY_SET",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                color = if (isCompletelyEmpty) Red else Yellow
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = if (isCompletelyEmpty)
                    "PROMPT: CLICK [+] TO INITIALIZE"
                else
                    "PROMPT: ADJUST FILTERS TO RETRY",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = Overlay2
            )
        }
    }
}
