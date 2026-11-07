package com.davinder.subguard.data.model

/**
 * Currencies offered in the app. All of these are covered by the Frankfurter
 * API (ECB reference rates) — don't add one here without checking it's supported,
 * or conversions for it will silently fail.
 */
object SupportedCurrencies {
    val all = listOf("INR", "USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "SGD", "CNY")
    const val DEFAULT = "INR"
}