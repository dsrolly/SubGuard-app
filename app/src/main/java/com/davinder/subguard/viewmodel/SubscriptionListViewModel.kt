package com.davinder.subguard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davinder.subguard.data.model.Subscription
import com.davinder.subguard.data.repository.SubscriptionRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.catch

enum class SortOption { ALPHABETICAL, MOST_EXPENSIVE, CHEAPEST }

class SubscriptionListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SubscriptionRepositoryProvider.get(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.ALPHABETICAL)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val allSubscriptions: StateFlow<List<Subscription>> =
        repository.observeSubscriptions()
            .catch { emit(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val visibleSubscriptions: StateFlow<List<Subscription>> =
        combine(allSubscriptions, _searchQuery, _sortOption) { list, query, sort ->
            val filtered = if (query.isBlank()) {
                list
            } else {
                list.filter { it.name.contains(query, ignoreCase = true) }
            }
            when (sort) {
                SortOption.ALPHABETICAL -> filtered.sortedBy { it.name.lowercase() }
                SortOption.MOST_EXPENSIVE -> filtered.sortedByDescending { it.amount }
                SortOption.CHEAPEST -> filtered.sortedBy { it.amount }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    fun onSortChange(option: SortOption) {
        _sortOption.value = option
    }
}