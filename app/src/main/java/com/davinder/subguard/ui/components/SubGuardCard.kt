package com.davinder.subguard.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Standard glass card. Corner radius standardized to three sizes. */
@Composable
fun Modifier.subCard(size: CardSize = CardSize.Medium): Modifier {
    val radius = when (size) {
        CardSize.Small -> 14.dp
        CardSize.Medium -> 20.dp
        CardSize.Large -> 26.dp
    }
    return this.glass(RoundedCornerShape(radius))
}

enum class CardSize { Small, Medium, Large }
