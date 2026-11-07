package com.davinder.subguard.ui.components

import com.davinder.subguard.ui.components.PremiumChip
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.davinder.subguard.ui.theme.AzurePrimary
import com.davinder.subguard.util.GENDER_OPTIONS
import com.davinder.subguard.util.ProfileFormState
import com.davinder.subguard.util.formatDobForDisplay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileFormFields(
    form: ProfileFormState,
    onFormChange: (ProfileFormState) -> Unit,
    showName: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var showPicker by remember { mutableStateOf(false) }

    val dobInteraction = remember { MutableInteractionSource() }
    LaunchedEffect(dobInteraction) {
        dobInteraction.interactions.collect {
            if (it is PressInteraction.Release && enabled) showPicker = true
        }
    }

    if (showPicker) {
        ModernCalendarDialog(
            // Timezone offset shift prevent karne ke liye UTC use karein
            initialDate = form.dobMillis?.let {
                Instant.ofEpochMilli(it).atOffset(ZoneOffset.UTC).toLocalDate()
            },
            maxDate = LocalDate.now(),
            onDismissRequest = { showPicker = false },
            onDateSelected = { selectedDate ->
                // Selected date ko UTC epoch millis me convert karein
                val millis = selectedDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
                onFormChange(form.copy(dobMillis = millis))
            }
        )
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedBorderColor = AzurePrimary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
        focusedLabelColor = AzurePrimary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
    )

    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (showName) {
            OutlinedTextField(
                value = form.name,
                onValueChange = { onFormChange(form.copy(name = it.take(50))) },
                label = { Text("Full name") },
                singleLine = true,
                enabled = enabled,
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = form.countryCode,
                onValueChange = { v ->
                    onFormChange(form.copy(countryCode = "+" + v.filter { it.isDigit() }.take(4)))
                },
                label = { Text("Code") },
                singleLine = true,
                enabled = enabled,
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors,
                modifier = Modifier.width(96.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            OutlinedTextField(
                value = form.phone,
                onValueChange = { v -> onFormChange(form.copy(phone = v.filter { it.isDigit() }.take(14))) },
                label = { Text("Mobile number") },
                singleLine = true,
                enabled = enabled,
                shape = RoundedCornerShape(16.dp),
                colors = textFieldColors,
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
        }

        OutlinedTextField(
            value = form.dobMillis?.let(::formatDobForDisplay).orEmpty(),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            colors = textFieldColors,
            label = { Text("Date of birth") },
            trailingIcon = { Icon(Icons.Filled.DateRange, contentDescription = "Pick date", tint = AzurePrimary) },
            interactionSource = dobInteraction,
            modifier = Modifier.fillMaxWidth()
        )

        Column {
            Text(
                text = "Gender",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GENDER_OPTIONS.forEach { option ->
                    val isSelected = form.gender == option
                    PremiumChip(
                        label = option,
                        selected = isSelected,
                        enabled = enabled,
                        onClick = { onFormChange(form.copy(gender = option)) }
                    )
                }
            }
        }
    }
}