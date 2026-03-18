package com.example.projet_android.ui.components

import androidx.compose.runtime.Composable
import com.example.projet_android.dao.ContainerDao
import com.example.projet_android.model.Container

@Composable
fun ContainerDialogs(
    showAddDialog: Boolean,
    editingContainer: Container?,
    deletingContainer: Container?,
    onDismissAdd: () -> Unit,
    onDismissEdit: () -> Unit,
    onDismissDelete: () -> Unit,
    containerDao: ContainerDao
) {

}
