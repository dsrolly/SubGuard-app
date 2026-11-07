package com.davinder.subguard.ui.screens.currencies

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.davinder.subguard.data.local.PreferencesManager
import com.davinder.subguard.ui.theme.AzurePrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
data class CurrencyItem(
    val code: String,
    val name: String,
    val symbol: String,
    val flag: String
)

val ALL_CURRENCIES = listOf(
    CurrencyItem("INR", "Indian Rupee", "₹", "🇮🇳"),
    CurrencyItem("USD", "United States Dollar", "$", "🇺🇸"),
    CurrencyItem("EUR", "Euro", "€", "🇪🇺"),
    CurrencyItem("GBP", "British Pound", "£", "🇬🇧"),
    CurrencyItem("AED", "United Arab Emirates Dirham", "د.إ", "🇦🇪"),
    CurrencyItem("AUD", "Australian Dollar", "A$", "🇦🇺"),
    CurrencyItem("CAD", "Canadian Dollar", "C$", "🇨🇦"),
    CurrencyItem("CHF", "Swiss Franc", "CHF", "🇨🇭"),
    CurrencyItem("CNY", "Chinese Yuan", "¥", "🇨🇳"),
    CurrencyItem("JPY", "Japanese Yen", "¥", "🇯🇵"),
    CurrencyItem("SGD", "Singapore Dollar", "S$", "🇸🇬"),
    CurrencyItem("NZD", "New Zealand Dollar", "NZ$", "🇳🇿"),
    CurrencyItem("BRL", "Brazilian Real", "R$", "🇧🇷"),
    CurrencyItem("ZAR", "South African Rand", "R", "🇿🇦")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrenciesScreen(
    currentCurrency: String? = null,
    onSelectCurrency: ((String) -> Unit)? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val prefsManager = remember { PreferencesManager(context) }
    val scope = rememberCoroutineScope()

    // PreferencesManager se saved currency observe karein (Fallback to "INR")
    val savedCurrency by prefsManager.mainCurrency.collectAsState(initial = "INR")
    val effectiveCurrency = currentCurrency ?: savedCurrency

    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) ALL_CURRENCIES
        else ALL_CURRENCIES.filter {
            it.code.contains(searchQuery, ignoreCase = true) ||
                    it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    val activeAccent = AzurePrimary
    val isLightAccent = activeAccent.luminance() > 0.8f
    val highlightColor = if (isLightAccent) MaterialTheme.colorScheme.onBackground else activeAccent

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Currencies", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search name, code or country") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = highlightColor) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = highlightColor
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.code }) { item ->
                    val isSelected = item.code == effectiveCurrency
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) highlightColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isSelected) highlightColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // 1. PreferencesManager ke andar DataStore mein save karein
                                scope.launch {
                                    prefsManager.setMainCurrency(item.code)
                                }
                                // 2. Optional callback agar parent screen ko update bhejna ho
                                onSelectCurrency?.invoke(item.code)
                                // 3. Back jayein
                                onBack()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = item.flag, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = item.code,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.symbol,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) highlightColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        tint = highlightColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}