package com.davinder.subguard.ui.screens.addeditsubscription

import com.davinder.subguard.ui.theme.AccentGradient
import com.davinder.subguard.ui.components.PremiumButton
import com.davinder.subguard.ui.components.PremiumSurface
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.davinder.subguard.data.model.BillingCycle
import com.davinder.subguard.data.model.Categories
import com.davinder.subguard.data.model.SubscriptionCategory
import com.davinder.subguard.data.model.SupportedCurrencies
import com.davinder.subguard.ui.components.CategoryChip
import com.davinder.subguard.ui.components.ModernCalendarDialog
import com.davinder.subguard.ui.components.rememberHaptic
import com.davinder.subguard.ui.theme.AzureButtonContainer
import com.davinder.subguard.ui.theme.AzureHeroGradient
import com.davinder.subguard.ui.theme.AzurePrimary
import com.davinder.subguard.viewmodel.AddEditSubscriptionViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubscriptionScreen(
    subscriptionId: String? = null,
    onSaved: () -> Unit,
    viewModel: AddEditSubscriptionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }
    val haptic = rememberHaptic()
    val customTags by viewModel.customTags.collectAsState()

    // Built-in categories + the user's own tags. If an older subscription uses a
    // category that is in neither list, it is added so its chip still shows as selected.
    val categoryOptions = remember(customTags, uiState.category) {
        val base = Categories.all + customTags.map { SubscriptionCategory(it, Icons.Outlined.Label) }
        if (uiState.category.isNotBlank() &&
            base.none { it.label.equals(uiState.category, ignoreCase = true) }
        ) {
            base + SubscriptionCategory(uiState.category, Icons.Outlined.Label)
        } else {
            base
        }
    }

    LaunchedEffect(subscriptionId) {
        if (!subscriptionId.isNullOrBlank()) {
            viewModel.loadExisting(subscriptionId)
        }
    }

    LaunchedEffect(uiState.saveComplete) {
        if (uiState.saveComplete) onSaved()
    }

    val activeAccent = AzurePrimary
    val activeBtnBg = AzureButtonContainer

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedBorderColor = activeAccent,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
        focusedLabelColor = activeAccent,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
    )

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
            Column {
                Text(
                    text = if (subscriptionId.isNullOrBlank()) "Add Subscription" else "Edit Subscription",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Configure details, cycle, and renewal alerts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Service or Product Name") },
                placeholder = { Text("Netflix, Spotify, Gym, Cloud...") },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = null,
                        tint = activeAccent
                    )
                },
                isError = uiState.nameError != null,
                supportingText = { uiState.nameError?.let { Text(it) } },
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Amount
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = viewModel::onAmountChange,
                label = { Text("Recurring Amount") },
                placeholder = { Text("0.00") },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Paid,
                        contentDescription = null,
                        tint = activeAccent
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = uiState.amountError != null,
                supportingText = { uiState.amountError?.let { Text(it) } },
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Billing Currency
            Column {
                Text(
                    text = "Billing Currency",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(SupportedCurrencies.all) { code ->
                        GradientSelectablePill(
                            label = code,
                            isSelected = uiState.currency == code,
                            onClick = { viewModel.onCurrencyChange(code) }
                        )
                    }
                }
            }

            // Start Date Picker Card
            Column {
                Text(
                    text = "First Billing / Start Date",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                PremiumSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showDatePicker = true },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.startDate.ifBlank { "Select First Billing Date" },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = "Pick date",
                            tint = activeAccent
                        )
                    }
                }
            }

            // Category Selection
            Column {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categoryOptions) { cat ->
                        CategoryChip(
                            category = cat,
                            selected = uiState.category == cat.label,
                            onClick = { viewModel.onCategoryChange(cat.label) }
                        )
                    }
                }
            }

            // Billing Frequency
            Column {
                Text(
                    text = "Billing Frequency",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(BillingCycle.entries) { cycle ->
                        val cycleLabel = cycle.name.lowercase().replaceFirstChar { it.uppercase() }
                        GradientSelectablePill(
                            label = cycleLabel,
                            isSelected = uiState.billingCycle == cycle,
                            onClick = { viewModel.onBillingCycleChange(cycle) }
                        )
                    }
                }
            }

            // Free Trial Period Card
            PremiumSurface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = "Free Trial Period",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Mark to receive alerts before trial converts to paid",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = uiState.isTrial,
                        onCheckedChange = viewModel::onTrialToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = activeBtnBg,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            PremiumButton(
                onClick = { haptic(); viewModel.save() },
                enabled = !uiState.isSaving,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = activeBtnBg,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = if (subscriptionId.isNullOrBlank()) "Save Subscription" else "Update Subscription",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Modern Custom Calendar Dialog
        if (showDatePicker) {
            val initialLocalDate = remember(uiState.startDate) {
                uiState.startDate.takeIf { it.isNotBlank() }?.let {
                    runCatching { LocalDate.parse(it) }.getOrNull()
                }
            }

            ModernCalendarDialog(
                initialDate = initialLocalDate,
                onDismissRequest = { showDatePicker = false },
                onDateSelected = { selectedDate ->
                    viewModel.onStartDateChange(selectedDate.toString())
                }
            )
        }
    }
}

@Composable
fun GradientSelectablePill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val activeBtnBg = AzureButtonContainer

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isSelected) Modifier.background(AccentGradient)
                else Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
    }
}