package com.davinder.subguard.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.davinder.subguard.ui.theme.AzurePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubGuardDatePickerDialog(
    state: DatePickerState,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    confirmText: String = "Select"
) {
    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.padding(end = 8.dp, bottom = 4.dp)
            ) {
                Text(
                    text = confirmText,
                    color = AzurePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        shape = RoundedCornerShape(28.dp),
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        DatePicker(
            state = state,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                titleContentColor = AzurePrimary,
                headlineContentColor = MaterialTheme.colorScheme.onSurface,
                weekdayContentColor = AzurePrimary,
                subheadContentColor = MaterialTheme.colorScheme.onSurface,
                yearContentColor = MaterialTheme.colorScheme.onSurface,
                currentYearContentColor = AzurePrimary,
                selectedYearContentColor = Color.White,
                selectedYearContainerColor = AzurePrimary,
                dayContentColor = MaterialTheme.colorScheme.onSurface,
                disabledDayContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                selectedDayContentColor = Color.White,
                selectedDayContainerColor = AzurePrimary,
                todayContentColor = AzurePrimary,
                todayDateBorderColor = AzurePrimary,
                dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                navigationContentColor = AzurePrimary
            )
        )
    }
}