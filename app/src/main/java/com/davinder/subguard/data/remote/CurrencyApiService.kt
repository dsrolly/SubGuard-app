package com.davinder.subguard.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/** Shape of https://api.frankfurter.dev/v1/latest?base=USD */
data class RatesResponse(
    val base: String = "",
    val date: String = "",
    val rates: Map<String, Double> = emptyMap()
)

interface CurrencyApiService {
    /** `rates[X]` in the response = how many X you get for 1 unit of `base`. */
    @GET("latest")
    suspend fun getLatest(@Query("base") base: String): RatesResponse
}

/** Single shared Retrofit instance. Free API, no key needed. */
object CurrencyApi {
    private const val BASE_URL = "https://api.frankfurter.dev/v1/"

    val service: CurrencyApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CurrencyApiService::class.java)
    }
}