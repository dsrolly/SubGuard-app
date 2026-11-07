package com.davinder.subguard.ui.screens.currencyconverter

import com.davinder.subguard.ui.theme.AccentGradient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davinder.subguard.data.model.SupportedCurrencies
import com.davinder.subguard.ui.theme.AzureButtonContainer
import com.davinder.subguard.ui.theme.AzureHeroGradient
import com.davinder.subguard.ui.theme.AzurePrimary
import com.davinder.subguard.ui.theme.OnAzureButtonContainer
import com.davinder.subguard.viewmodel.CurrencyConverterViewModel

@Composable
fun CurrencyConverterScreen(viewModel: CurrencyConverterViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header
            Column {
                Text(
                    text = "Currency Converter",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Live exchange calculation for multi-currency subscriptions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // High-Contrast Input Amount Field
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = viewModel::onAmountChange,
                label = { Text("Amount to Convert") },
                placeholder = { Text("1.00") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Paid,
                        contentDescription = null,
                        tint = AzurePrimary
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = AzurePrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                    focusedLabelColor = AzurePrimary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // 'From' Currency Selector
            Column {
                Text(
                    text = "From Currency",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                ModernCurrencyChips(selected = uiState.from, onSelect = viewModel::onFromChange)
            }

            // Enhanced Central Swap Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = CircleShape,
                            spotColor = AzureButtonContainer.copy(alpha = 0.4f)
                        )
                        .clip(CircleShape)
                        .background(AccentGradient)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            CircleShape
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = viewModel::onSwap
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap currencies",
                        tint = OnAzureButtonContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // 'To' Currency Selector
            Column {
                Text(
                    text = "To Currency",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                ModernCurrencyChips(selected = uiState.to, onSelect = viewModel::onToChange)
            }

            // Result Hero Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = Color(0x380284C7))
                    .clip(RoundedCornerShape(24.dp))
                    .background(AzureHeroGradient)
                    .padding(22.dp)
            ) {
                Column {
                    when {
                        uiState.isLoading -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Fetching live rates...",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        uiState.error != null -> {
                            Text(
                                text = uiState.error.orEmpty(),
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        uiState.result != null -> {
                            Text(
                                text = "${uiState.amount.ifBlank { "0" }} ${uiState.from} equals",
                                color = Color.White.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${"%.2f".format(uiState.result)} ${uiState.to}",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp
                                )
                            )
                        }
                        else -> {
                            Text(
                                text = "Enter an amount above to view exchange value",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Exchange Rate Meta Pill
            if (uiState.result != null && uiState.rate != null && uiState.from != uiState.to) {
                val updated = if (uiState.rateDate.isNotBlank()) "rates as of ${uiState.rateDate}" else "recent rates"
                val stale = if (uiState.isStale) " (cached offline)" else ""

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "1 ${uiState.from} = ${"%.4f".format(uiState.rate)} ${uiState.to} • $updated$stale",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Modern High-Contrast Currency Pill Chips
 * Uses AzureButtonContainer and OnAzureButtonContainer to avoid washed-out white buttons in dark themes.
 */
@Composable
private fun ModernCurrencyChips(
    selected: String,
    onSelect: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(SupportedCurrencies.all) { code ->
            val isSelected = selected == code

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .then(
                        if (isSelected) {
                            Modifier
                                .background(AccentGradient)
                                .border(
                                    1.dp,
                                    OnAzureButtonContainer.copy(alpha = 0.2f),
                                    RoundedCornerShape(14.dp)
                                )
                        } else {
                            Modifier
                                .background(MaterialTheme.colorScheme.surface)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                                    RoundedCornerShape(14.dp)
                                )
                        }
                    )
                    .clickable { onSelect(code) }
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = code,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) OnAzureButtonContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}