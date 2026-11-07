package com.davinder.subguard.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.davinder.subguard.data.remote.CurrencyApi
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

private val Context.currencyCacheDataStore by preferencesDataStore(name = "currency_cache")

data class CachedRates(
    val base: String = "",
    val date: String = "",
    val rates: Map<String, Double> = emptyMap(),
    val fetchedAtMillis: Long = 0L
)

data class ConversionResult(
    val converted: Double,
    val rate: Double,
    val rateDate: String,
    val isStale: Boolean
)

/**
 * Exchange rates with a 12-hour local cache. Rates are only refetched when the
 * cache is older than that, and if the network fails the last cached rates are
 * used — so conversions keep working offline (just possibly a day or two old).
 */
class CurrencyRepository(context: Context) {

    private val dataStore = context.applicationContext.currencyCacheDataStore
    private val gson = Gson()
    private val api = CurrencyApi.service

    private fun keyFor(base: String) = stringPreferencesKey("rates_$base")

    suspend fun getRates(base: String): CachedRates? {
        val cached = readCache(base)
        val now = System.currentTimeMillis()
        if (cached != null && now - cached.fetchedAtMillis < CACHE_MAX_AGE_MS) return cached

        return try {
            val response = api.getLatest(base)
            val fresh = CachedRates(
                base = base,
                date = response.date,
                rates = response.rates + (base to 1.0),
                fetchedAtMillis = now
            )
            writeCache(fresh)
            fresh
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            cached // offline or API error -> fall back to whatever we last saved (may be null)
        }
    }

    /** Converts `amount` from one currency to another, or null if no rate is available at all. */
    suspend fun convert(amount: Double, from: String, to: String): ConversionResult? {
        if (from == to) return ConversionResult(amount, 1.0, "", false)
        val rates = getRates(from) ?: return null
        val rate = rates.rates[to] ?: return null
        val stale = System.currentTimeMillis() - rates.fetchedAtMillis >= CACHE_MAX_AGE_MS
        return ConversionResult(amount * rate, rate, rates.date, stale)
    }

    private suspend fun readCache(base: String): CachedRates? {
        val json = dataStore.data.first()[keyFor(base)] ?: return null
        return try { gson.fromJson(json, CachedRates::class.java) } catch (e: Exception) { null }
    }

    private suspend fun writeCache(rates: CachedRates) {
        dataStore.edit { it[keyFor(rates.base)] = gson.toJson(rates) }
    }

    private companion object {
        const val CACHE_MAX_AGE_MS = 12 * 60 * 60 * 1000L
    }
}