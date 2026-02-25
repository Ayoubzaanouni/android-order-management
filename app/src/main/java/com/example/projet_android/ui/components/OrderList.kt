package com.example.projet_android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projet_android.model.Order

@Composable
fun OrderList(
    orders    : List<Order>,
    isLoading : Boolean,
    modifier  : Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {

        LazyColumn(
            contentPadding      = PaddingValues(
                start  = 16.dp,
                end    = 16.dp,
                top    = 8.dp,
                bottom = 88.dp          // clear the FAB
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier            = Modifier.fillMaxSize()
        ) {
            items(
                items = orders,
                key   = { it.id }
            ) { order ->
                OrderCard(
                    order    = order,
                    modifier = Modifier.animateItem()
                )
            }
        }

        // subtle top shimmer while re-seeding over existing data
        AnimatedVisibility(
            visible = isLoading,
            enter   = fadeIn(),
            exit    = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

