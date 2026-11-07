package com.davinder.subguard.util

import com.davinder.subguard.data.model.BillingCycle
import com.davinder.subguard.data.model.Subscription
import com.davinder.subguard.data.model.SubscriptionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Place under app/src/test/java/com/davinder/subguard/util/ alongside DateUtilsTest. */
class CsvExporterTest {

    @Test
    fun `header row lists every column`() {
        val csv = subscriptionsToCsv(emptyList())
        assertEquals(
            "name,amount,currency,category,billing_cycle,start_date,next_renewal,status,is_trial",
            csv.trim()
        )
    }

    @Test
    fun `one subscription produces exactly one data row`() {
        val sub = Subscription(
            id = "1", name = "Netflix", amount = 499.0, currency = "INR",
            category = "Entertainment", billingCycle = BillingCycle.MONTHLY,
            isTrial = false, status = SubscriptionStatus.ACTIVE,
            startDate = "2026-09-25", nextRenewalDate = "2026-10-25"
        )
        val lines = subscriptionsToCsv(listOf(sub)).lines()
        assertEquals(2, lines.size)
        assertEquals(
            "Netflix,499.0,INR,Entertainment,MONTHLY,2026-09-25,2026-10-25,ACTIVE,false",
            lines[1]
        )
    }

    @Test
    fun `a comma in the name is quoted so the CSV stays valid`() {
        val sub = Subscription(name = "Coffee, Inc.", amount = 5.0, category = "Other")
        val dataRow = subscriptionsToCsv(listOf(sub)).lines()[1]
        assertTrue(dataRow.startsWith("\"Coffee, Inc.\","))
    }
}