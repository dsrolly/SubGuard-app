package com.davinder.subguard.util

import com.davinder.subguard.data.model.BillingCycle
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pure unit tests — no Android framework, no Firebase, nothing mocked.
 * Place under app/src/test/java/com/davinder/subguard/util/ (the "test",
 * not "androidTest", source set) and just click the green run arrow.
 */
class DateUtilsTest {

    @Test
    fun `monthly renewal from Jan 31 lands on Feb 28, not drifted`() {
        val start = LocalDate(2026, 1, 31)
        // occurrence 1 = one month after Jan 31 -> clamps to the last day of Feb.
        assertEquals(LocalDate(2026, 2, 28), occurrenceDate(start, BillingCycle.MONTHLY, 1))
        // occurrence 2 must be Mar 31 — NOT Mar 28, which is what naive "+1 month
        // repeated" logic would produce once the date had drifted to the 28th.
        assertEquals(LocalDate(2026, 3, 31), occurrenceDate(start, BillingCycle.MONTHLY, 2))
    }

    @Test
    fun `calculateNextRenewalDate returns the first occurrence on or after today`() {
        val start = LocalDate(2026, 1, 31)
        val today = LocalDate(2026, 2, 15)
        assertEquals(LocalDate(2026, 2, 28), calculateNextRenewalDate(start, BillingCycle.MONTHLY, today))
    }

    @Test
    fun `calculateNextRenewalDate keeps the start date when it is still in the future`() {
        val start = LocalDate(2026, 12, 1)
        val today = LocalDate(2026, 9, 25)
        assertEquals(start, calculateNextRenewalDate(start, BillingCycle.MONTHLY, today))
    }

    @Test
    fun `occurrencesBetween finds every monthly billing date in a quarter`() {
        val start = LocalDate(2026, 1, 1)
        val result = occurrencesBetween(
            start = start,
            cycle = BillingCycle.MONTHLY,
            from = LocalDate(2026, 1, 1),
            to = LocalDate(2026, 3, 31)
        )
        assertEquals(
            listOf(LocalDate(2026, 1, 1), LocalDate(2026, 2, 1), LocalDate(2026, 3, 1)),
            result
        )
    }

    @Test
    fun `monthlyEquivalentAmount normalizes every billing cycle correctly`() {
        assertEquals(100.0, monthlyEquivalentAmount(1200.0, BillingCycle.YEARLY), 0.001)
        assertEquals(100.0, monthlyEquivalentAmount(300.0, BillingCycle.QUARTERLY), 0.001)
        assertEquals(499.0, monthlyEquivalentAmount(499.0, BillingCycle.MONTHLY), 0.001)
        assertEquals(43.45, monthlyEquivalentAmount(10.0, BillingCycle.WEEKLY), 0.001)
    }

    @Test
    fun `toLocalDateOrNull parses valid ISO dates and rejects garbage`() {
        assertEquals(LocalDate(2026, 9, 25), "2026-09-25".toLocalDateOrNull())
        assertNull("not-a-date".toLocalDateOrNull())
        assertNull("".toLocalDateOrNull())
    }
}