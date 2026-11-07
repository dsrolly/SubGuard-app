package com.davinder.subguard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davinder.subguard.data.local.PreferencesManager
import com.davinder.subguard.data.repository.CurrencyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class CurrencyConverterUiState(
    val amount: String = "1",
    val from: String = "USD",
    val to: String = "INR",
    val result: Double? = null,
    val rate: Double? = null,
    val rateDate: String = "",
    val isStale: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class CurrencyConverterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CurrencyRepository(application)
    private val preferences = PreferencesManager(application)

    private val _uiState = MutableStateFlow(CurrencyConverterUiState())
    val uiState: StateFlow<CurrencyConverterUiState> = _uiState.asStateFlow()

    private var convertJob: Job? = null

    init {
        // Start with "to" = the user's main currency (and "from" = USD unless that IS main).
        viewModelScope.launch {
            val main = preferences.mainCurrency.first()
            _uiState.value = _uiState.value.copy(
                to = main,
                from = if (main == "USD") "EUR" else "USD"
            )
            recompute()
        }
    }

    fun onAmountChange(value: String) {
        _uiState.value = _uiState.value.copy(amount = value)
        recompute()
    }

    fun onFromChange(code: String) {
        _uiState.value = _uiState.value.copy(from = code)
        recompute()
    }

    fun onToChange(code: String) {
        _uiState.value = _uiState.value.copy(to = code)
        recompute()
    }

    fun onSwap() {
        val s = _uiState.value
        _uiState.value = s.copy(from = s.to, to = s.from)
        recompute()
    }

    private fun recompute() {
        convertJob?.cancel()
        convertJob = viewModelScope.launch {
            val state = _uiState.value
            val amountValue = state.amount.toDoubleOrNull()
            if (amountValue == null || amountValue < 0) {
                _uiState.value = state.copy(result = null, error = null, isLoading = false)
                return@launch
            }

            _uiState.value = state.copy(isLoading = true, error = null)
            val result = repository.convert(amountValue, state.from, state.to)

            _uiState.value = if (result == null) {
                _uiState.value.copy(
                    isLoading = false,
                    result = null,
                    error = "Couldn't get exchange rates. Check your internet connection."
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    result = result.converted,
                    rate = result.rate,
                    rateDate = result.rateDate,
                    isStale = result.isStale,
                    error = null
                )
            }
        }
    }
}