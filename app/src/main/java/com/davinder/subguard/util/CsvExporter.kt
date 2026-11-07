package com.davinder.subguard.util

import com.davinder.subguard.data.model.Subscription

private fun csvEscape(value: String): String =
    if (value.any { it == ',' || it == '"' || it == '\n' }) "\"" + value.replace("\"", "\"\"") + "\"" else value

fun subscriptionsToCsv(subscriptions: List<Subscription>): String {
    val header = "name,amount,currency,category,billing_cycle,start_date,next_renewal,status,is_trial"
    val rows = subscriptions.map { s ->
        listOf(
            csvEscape(s.name), s.amount.toString(), s.currency, csvEscape(s.category),
            s.billingCycle.name, s.startDate, s.nextRenewalDate, s.status.name, s.isTrial.toString()
        ).joinToString(",")
    }
    return (listOf(header) + rows).joinToString("\n")
}