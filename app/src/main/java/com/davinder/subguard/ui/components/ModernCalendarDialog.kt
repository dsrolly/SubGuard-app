package com.davinder.subguard.ui.components

import com.davinder.subguard.ui.theme.AccentGradient
import com.davinder.subguard.ui.components.PremiumButton
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.davinder.subguard.ui.theme.AzurePrimary
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ModernCalendarDialog(
    initialDate: LocalDate? = null,
    onDismissRequest: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    maxDate: LocalDate? = null,
    minDate: LocalDate? = null
) {
    var currentMonth by remember {
        mutableStateOf(
            initialDate?.let { YearMonth.of(it.year, it.month) } ?: YearMonth.now()
        )
    }
    var selectedDate by remember {
        mutableStateOf(initialDate ?: LocalDate.now())
    }

    val dialogBg = MaterialTheme.colorScheme.surfaceContainerHigh
    val cardBorder = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val buttonNavBg = MaterialTheme.colorScheme.surfaceVariant

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(26.dp),
            color = dialogBg,
            border = BorderStroke(1.dp, cardBorder),
            tonalElevation = 10.dp
        ) {
            Column(
                modifier = Modifier.padding(22.dp)
            ) {
                // Header: Month, Year & Arrows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${currentMonth.year}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = AzurePrimary
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { currentMonth = currentMonth.minusMonths(1) },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(buttonNavBg)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Previous Month",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { currentMonth = currentMonth.plusMonths(1) },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(buttonNavBg)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Next Month",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Days of week row
                val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    daysOfWeek.forEach { day ->
                        Box(modifier = Modifier.size(38.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = day,
                                color = AzurePrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Dates Grid
                val firstDayOfMonth = currentMonth.atDay(1)
                val daysInMonth = currentMonth.lengthOfMonth()
                val startDayOffset = firstDayOfMonth.dayOfWeek.value - 1

                val totalCells = startDayOffset + daysInMonth
                val rows = (totalCells + 6) / 7

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (row in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (col in 0..6) {
                                val dayIndex = row * 7 + col - startDayOffset + 1
                                if (dayIndex in 1..daysInMonth) {
                                    val date = currentMonth.atDay(dayIndex)
                                    val isSelected = date == selectedDate
                                    val isToday = date == LocalDate.now()
                                    val isEnabled = (maxDate == null || !date.isAfter(maxDate)) &&
                                            (minDate == null || !date.isBefore(minDate))

                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .then(
                                                when {
                                                    isSelected -> Modifier.background(AccentGradient)
                                                    isToday -> Modifier.border(1.5.dp, AzurePrimary, CircleShape)
                                                    else -> Modifier
                                                }
                                            )
                                            .clickable(enabled = isEnabled) {
                                                selectedDate = date
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$dayIndex",
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = when {
                                                isSelected -> Color.White
                                                !isEnabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(38.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text(
                            text = "Cancel",
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    PremiumButton(
                        onClick = {
                            onDateSelected(selectedDate)
                            onDismissRequest()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AzurePrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Select", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}