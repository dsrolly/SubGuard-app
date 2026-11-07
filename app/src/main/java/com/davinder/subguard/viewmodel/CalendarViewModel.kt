package com.davinder.subguard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davinder.subguard.data.model.SubscriptionStatus
import com.davinder.subguard.data.repository.SubscriptionRepositoryProvider
import com.davinder.subguard.util.occurrencesBetween
import com.davinder.subguard.util.toLocalDateOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.coroutines.flow.catch

data class CalendarEvent(
    val subscriptionId: String,
    val name: String,
    val amount: Double,
    val currency: String,
    val date: LocalDate,
    val isUpcoming: Boolean
)

data class CalendarUiState(
    val monthStart: LocalDate,
    val today: LocalDate,
    val selectedDate: LocalDate?,
    val eventsByDate: Map<LocalDate, List<CalendarEvent>>
)

/**
 * Every billing date of every active subscription is derived straight from its
 * start date + billing cycle — so the calendar shows past charges, today, and
 * all future renewals of the visible month, without needing stored history.
 */
class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SubscriptionRepositoryProvider.get(application)
    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    private val visibleMonthStart = MutableStateFlow(LocalDate(today.year, today.month, 1))
    private val selectedDate = MutableStateFlow<LocalDate?>(today)

    val uiState: StateFlow<CalendarUiState> = combine(
        repository.observeSubscriptions(),
        visibleMonthStart,
        selectedDate
    ) { subscriptions, monthStart, selected ->
        val monthEnd = monthStart.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)

        val events = subscriptions
            .filter { it.status == SubscriptionStatus.ACTIVE }
            .flatMap { sub ->
                val start = sub.startDate.toLocalDateOrNull() ?: return@flatMap emptyList()
                occurrencesBetween(start, sub.billingCycle, monthStart, monthEnd).map { date ->
                    CalendarEvent(
                        subscriptionId = sub.id,
                        name = sub.name,
                        amount = sub.amount,
                        currency = sub.currency,
                        date = date,
                        isUpcoming = date >= today
                    )
                }
            }
            .groupBy { it.date }

        CalendarUiState(monthStart, today, selected, events)
    }.catch {
        emit(CalendarUiState(LocalDate(today.year, today.month, 1), today, today, emptyMap()))
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CalendarUiState(LocalDate(today.year, today.month, 1), today, today, emptyMap())
    )

    fun onPreviousMonth() {
        visibleMonthStart.value = visibleMonthStart.value.minus(1, DateTimeUnit.MONTH)
        selectedDate.value = null
    }

    fun onNextMonth() {
        visibleMonthStart.value = visibleMonthStart.value.plus(1, DateTimeUnit.MONTH)
        selectedDate.value = null
    }

    fun onSelectDate(date: LocalDate) {
        selectedDate.value = date
    }
}