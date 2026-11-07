package com.davinder.subguard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davinder.subguard.data.local.PreferencesManager
import com.davinder.subguard.data.model.Subscription
import com.davinder.subguard.data.model.SubscriptionStatus
import com.davinder.subguard.data.repository.CurrencyRepository
import com.davinder.subguard.data.repository.SubscriptionRepositoryProvider
import com.davinder.subguard.util.daysUntil
import com.davinder.subguard.util.monthlyEquivalentAmount
import com.davinder.subguard.util.occurrencesBetween
import com.davinder.subguard.util.toLocalDateOrNull
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

data class UpcomingRenewal(val subscription: Subscription, val daysAway: Int)

enum class SpendingView(val label: String, val unit: String) {
    DAILY("Daily", "day"),
    WEEKLY("Weekly", "week"),
    MONTHLY("Monthly", "month");

    companion object {
        fun fromName(name: String?): SpendingView =
            values().firstOrNull { it.name == name } ?: MONTHLY
    }
}

/** The 3 saved options on the "Dashboard display" settings screen. */
data class DisplaySettings(
    val spendingView: SpendingView = SpendingView.MONTHLY,
    val cashFlowView: Boolean = false,
    val cycleStartDay: Int = 1
)

data class DashboardUiState(
    /** Always the 30-day monthly burn rate (used for the Annual Run Rate). */
    val monthlyBurnRate: Double = 0.0,
    /** Burn rate converted to the chosen view (daily / weekly / monthly). */
    val estimatedAmount: Double = 0.0,
    val currency: String = "INR",
    /** True when at least one subscription's currency couldn't be converted (e.g. offline, no cached rates). */
    val conversionIncomplete: Boolean = false,
    val activeCount: Int = 0,
    val dueSoonCount: Int = 0,
    val upcoming: List<UpcomingRenewal> = emptyList(),

    // Display settings
    val spendingView: SpendingView = SpendingView.MONTHLY,
    val cashFlowView: Boolean = false,
    val cycleStartDay: Int = 1,

    // Cash-flow numbers: REAL bills that fall inside the current period
    val periodLabel: String = "",
    val periodTotal: Double = 0.0,
    val periodSpent: Double = 0.0,
    val periodLeft: Double = 0.0
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SubscriptionRepositoryProvider.get(application)
    private val currencyRepository = CurrencyRepository(application)
    private val preferences = PreferencesManager(application)

    /** Straight from DataStore, so the settings screen never waits for Firestore. */
    val displaySettings: StateFlow<DisplaySettings> = combine(
        preferences.spendingView,
        preferences.cashFlowView,
        preferences.cycleStartDay
    ) { view, cashFlow, startDay ->
        DisplaySettings(SpendingView.fromName(view), cashFlow, startDay)
    }
        .catch { emit(DisplaySettings()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DisplaySettings())

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.observeSubscriptions(),
        preferences.mainCurrency,
        preferences.spendingView,
        preferences.cashFlowView,
        preferences.cycleStartDay
    ) { subscriptions, mainCurrency, viewName, cashFlow, startDay ->
        buildUiState(
            subscriptions = subscriptions,
            mainCurrency = mainCurrency,
            view = SpendingView.fromName(viewName),
            cashFlow = cashFlow,
            cycleStartDay = startDay
        )
    }
        .catch { emit(DashboardUiState()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun onSpendingViewChange(view: SpendingView) {
        viewModelScope.launch { preferences.setSpendingView(view.name) }
    }

    fun onCashFlowChange(enabled: Boolean) {
        viewModelScope.launch { preferences.setCashFlowView(enabled) }
    }

    fun onCycleStartDayChange(day: Int) {
        viewModelScope.launch { preferences.setCycleStartDay(day) }
    }

    private suspend fun buildUiState(
        subscriptions: List<Subscription>,
        mainCurrency: String,
        view: SpendingView,
        cashFlow: Boolean,
        cycleStartDay: Int
    ): DashboardUiState {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val active = subscriptions.filter { it.status == SubscriptionStatus.ACTIVE }

        // One rates lookup (base = main currency) covers every subscription:
        // rates[X] = X per 1 main-currency unit, so amountInMain = amountInX / rates[X].
        val needsConversion = active.any { it.currency != mainCurrency }
        val rates = if (needsConversion) currencyRepository.getRates(mainCurrency)?.rates else null

        var incomplete = false
        fun toMain(amount: Double, currency: String): Double {
            if (currency == mainCurrency) return amount
            val rate = rates?.get(currency)
            return if (rate == null || rate == 0.0) {
                incomplete = true
                amount // no rate available — counted unconverted rather than dropped
            } else {
                amount / rate
            }
        }

        // 1) Estimated spend (average), shown when Cash-flow view is OFF
        val burnRate = active.sumOf { sub ->
            toMain(monthlyEquivalentAmount(sub.amount, sub.billingCycle), sub.currency)
        }
        val estimated = when (view) {
            SpendingView.DAILY -> burnRate / 30.0
            SpendingView.WEEKLY -> burnRate / 4.33
            SpendingView.MONTHLY -> burnRate
        }

        // 2) Cash flow: real bills inside the current period
        val (windowStart, windowEnd, periodLabel) = periodWindow(today, view, cycleStartDay)
        var total = 0.0
        var spent = 0.0
        active.forEach { sub ->
            val start = sub.startDate.toLocalDateOrNull() ?: return@forEach
            occurrencesBetween(start, sub.billingCycle, windowStart, windowEnd).forEach { date ->
                val amount = toMain(sub.amount, sub.currency)
                total += amount
                if (date <= today) spent += amount
            }
        }

        // 3) Upcoming renewals list (unchanged)
        val upcoming = active.mapNotNull { sub ->
            val renewalDate = sub.nextRenewalDate.toLocalDateOrNull() ?: return@mapNotNull null
            val days = daysUntil(renewalDate)
            if (days in 0..30) UpcomingRenewal(sub, days) else null
        }.sortedBy { it.daysAway }

        return DashboardUiState(
            monthlyBurnRate = burnRate,
            estimatedAmount = estimated,
            currency = mainCurrency,
            conversionIncomplete = incomplete,
            activeCount = active.size,
            dueSoonCount = upcoming.count { it.daysAway <= 7 },
            upcoming = upcoming.take(5),
            spendingView = view,
            cashFlowView = cashFlow,
            cycleStartDay = cycleStartDay,
            periodLabel = periodLabel,
            periodTotal = total,
            periodSpent = spent,
            periodLeft = (total - spent).coerceAtLeast(0.0)
        )
    }

    /**
     * Daily   -> today only
     * Weekly  -> this week (Monday to Sunday)
     * Monthly -> the cycle that starts on [startDay] of the month
     */
    private fun periodWindow(
        today: LocalDate,
        view: SpendingView,
        startDay: Int
    ): Triple<LocalDate, LocalDate, String> = when (view) {
        SpendingView.DAILY -> Triple(today, today, "Today")

        SpendingView.WEEKLY -> {
            val start = today.minus(today.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)
            val end = start.plus(6, DateTimeUnit.DAY)
            Triple(start, end, "This week · ${shortDate(start)} – ${shortDate(end)}")
        }

        SpendingView.MONTHLY -> {
            val day = startDay.coerceIn(1, 28)
            val thisMonthStart = LocalDate(today.year, today.month, day)
            val start = if (today >= thisMonthStart) {
                thisMonthStart
            } else {
                thisMonthStart.minus(1, DateTimeUnit.MONTH)
            }
            val end = start.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
            Triple(start, end, "This cycle · ${shortDate(start)} – ${shortDate(end)}")
        }
    }

    private fun shortDate(date: LocalDate): String {
        val month = date.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        return "${date.dayOfMonth} $month"
    }
}