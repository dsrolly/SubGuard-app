package com.davinder.subguard.util

import com.davinder.subguard.data.model.BillingCycle
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn

/**
 * The date of the Nth billing after (and including) `start`.
 * Always computed from the ORIGINAL start date (start + N cycles), never by
 * repeatedly adding one cycle — so a subscription started on Jan 31 renews on
 * Feb 28, then Mar 31 (not Mar 28) and month-end dates never "drift".
 */
fun occurrenceDate(start: LocalDate, cycle: BillingCycle, index: Int): LocalDate = when (cycle) {
    BillingCycle.WEEKLY -> start.plus(index * 7, DateTimeUnit.DAY)
    BillingCycle.MONTHLY -> start.plus(index, DateTimeUnit.MONTH)
    BillingCycle.QUARTERLY -> start.plus(index * 3, DateTimeUnit.MONTH)
    BillingCycle.YEARLY -> start.plus(index, DateTimeUnit.YEAR)
}

/** First billing date that is on or after `today`. */
fun calculateNextRenewalDate(
    startDate: LocalDate,
    billingCycle: BillingCycle,
    today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
): LocalDate {
    var index = 0
    var next = startDate
    while (next < today && index < 10_000) {
        index++
        next = occurrenceDate(startDate, billingCycle, index)
    }
    return next
}

/** Every billing date that falls between `from` and `to` (inclusive). Used by the Calendar. */
fun occurrencesBetween(
    start: LocalDate,
    cycle: BillingCycle,
    from: LocalDate,
    to: LocalDate
): List<LocalDate> {
    val result = mutableListOf<LocalDate>()
    var index = 0
    while (index < 10_000) {
        val date = occurrenceDate(start, cycle, index)
        if (date > to) break
        if (date >= from) result.add(date)
        index++
    }
    return result
}

fun daysUntil(
    date: LocalDate,
    today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
): Int = today.daysUntil(date)

/** Normalizes any billing cycle to a monthly figure, for the burn-rate total. */
fun monthlyEquivalentAmount(amount: Double, billingCycle: BillingCycle): Double = when (billingCycle) {
    BillingCycle.WEEKLY -> amount * 4.345
    BillingCycle.MONTHLY -> amount
    BillingCycle.QUARTERLY -> amount / 3.0
    BillingCycle.YEARLY -> amount / 12.0
}

/** Rough cycle length in days — used only for the Detail screen's timeline visual. */
fun cycleLengthDays(cycle: BillingCycle): Int = when (cycle) {
    BillingCycle.WEEKLY -> 7
    BillingCycle.MONTHLY -> 30
    BillingCycle.QUARTERLY -> 91
    BillingCycle.YEARLY -> 365
}

fun String.toLocalDateOrNull(): LocalDate? =
    try { LocalDate.parse(this) } catch (e: Exception) { null }

/** Material3 DatePicker works in UTC-midnight millis — these two convert to/from LocalDate. */
fun LocalDate.toPickerMillis(): Long = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

fun pickerMillisToLocalDate(millis: Long): LocalDate =
    Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date