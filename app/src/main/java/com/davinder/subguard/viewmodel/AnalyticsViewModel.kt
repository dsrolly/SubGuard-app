package com.davinder.subguard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davinder.subguard.data.model.BillingHistoryEntry
import com.davinder.subguard.data.repository.BillingHistoryRepositoryProvider
import com.davinder.subguard.data.repository.SubscriptionRepositoryProvider
import com.davinder.subguard.util.toLocalDateOrNull
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MonthPoint(val monthLabel: String, val total: Double)
data class CategorySpend(val category: String, val total: Double)

data class AnalyticsUiState(
    val trend: List<MonthPoint> = emptyList(),
    val categoryBreakdown: List<CategorySpend> = emptyList(),
    val history: List<BillingHistoryEntry> = emptyList()
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val billingRepository = BillingHistoryRepositoryProvider.get(application)
    private val subscriptionRepository = SubscriptionRepositoryProvider.get(application)

    // Analytics ab sirf unhi billing records ko dikhata hai jinki subscription
    // abhi user ki subscriptions list (Dashboard wali list) mein maujood hai.
    // Isse purane / orphan / guest records kabhi Analytics mein nahi aayenge,
    // aur 0 subscriptions par screen khali (empty state) dikhegi.
    val uiState: StateFlow<AnalyticsUiState> = combine(
        billingRepository.observeEntries().catch { emit(emptyList()) },
        subscriptionRepository.observeSubscriptions().catch { emit(emptyList()) }
    ) { entries, subscriptions ->
        val activeIds = subscriptions.map { it.id }.toSet()
        buildUiState(entries.filter { it.subscriptionId in activeIds })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsUiState())

    private fun buildUiState(entries: List<BillingHistoryEntry>): AnalyticsUiState {
        val withDates = entries.mapNotNull { entry ->
            entry.billedOn.toLocalDateOrNull()?.let { date -> entry to date }
        }

        // Group by year+month (sortable key), not just the label, so the
        // trend stays in true chronological order across year boundaries.
        val trend = withDates
            .groupBy { (_, date) -> date.year * 12 + date.monthNumber }
            .toSortedMap()
            .map { (_, list) ->
                val date = list.first().second
                val label = "${date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }} ${date.year}"
                MonthPoint(label, list.sumOf { it.first.amount })
            }

        val categoryBreakdown = entries
            .groupBy { it.category }
            .map { (category, list) -> CategorySpend(category, list.sumOf { it.amount }) }
            .sortedByDescending { it.total }

        val history = entries.sortedByDescending { it.billedOn }

        return AnalyticsUiState(trend = trend, categoryBreakdown = categoryBreakdown, history = history)
    }
}