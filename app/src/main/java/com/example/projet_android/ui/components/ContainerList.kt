package com.example.projet_android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projet_android.model.Container

@Composable
fun ContainerList(
    containers: List<Container>,
    totalCount: Int,
    onEdit: (Container) -> Unit,
    onDelete: (Container) -> Unit,
    onToggleEnabled: (Container) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            Text(
                "Containers (${containers.size}/$totalCount)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(containers, key = { it.id }) { container ->
            ContainerCard(
                container = container,
                onEdit = { onEdit(container) },
                onDelete = { onDelete(container) },
                onToggleEnabled = { onToggleEnabled(container) }
            )
        }
    }
}

