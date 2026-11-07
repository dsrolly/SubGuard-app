package com.davinder.subguard.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.davinder.subguard.data.local.PreferencesManager
import com.davinder.subguard.data.model.Categories
import com.davinder.subguard.data.model.Subscription
import com.davinder.subguard.data.repository.SubscriptionRepositoryProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TagItem(
    val name: String,
    /** Real number of subscriptions that use this category/tag. */
    val count: Int,
    /** True if the user created it on the Tags screen. */
    val isCustom: Boolean,
    /** Only custom tags with zero subscriptions can be deleted. */
    val deletable: Boolean
)

class TagsManagementViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SubscriptionRepositoryProvider.get(application)
    private val preferences = PreferencesManager(application)

    /**
     * List = every category your real subscriptions use + every custom tag you created
     * (saved in DataStore, so it survives leaving the screen and restarting the app).
     */
    val items: StateFlow<List<TagItem>> = combine(
        repository.observeSubscriptions(),
        preferences.customTags
    ) { subscriptions, customTags -> buildItems(subscriptions, customTags) }
        .catch { emit(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun buildItems(
        subscriptions: List<Subscription>,
        customTags: Set<String>
    ): List<TagItem> {
        val used = subscriptions.filter { it.category.isNotBlank() }
        val counts = used.groupingBy { it.category.trim().lowercase() }.eachCount()
        val custom = customTags.filter { it.isNotBlank() }
        val customKeys = custom.map { it.trim().lowercase() }.toSet()

        // lowercase key -> display name (case-insensitive merge, so "work" and "Work" are one row)
        val names = LinkedHashMap<String, String>()
        used.forEach { names.putIfAbsent(it.category.trim().lowercase(), it.category.trim()) }
        custom.forEach { names.putIfAbsent(it.trim().lowercase(), it.trim()) }

        return names.map { (key, name) ->
            val count = counts[key] ?: 0
            val isCustom = key in customKeys
            TagItem(
                name = name,
                count = count,
                isCustom = isCustom,
                deletable = isCustom && count == 0
            )
        }.sortedWith(compareByDescending<TagItem> { it.count }.thenBy { it.name.lowercase() })
    }

    /** Returns an error message to show in the dialog, or null if the tag was saved. */
    fun addTag(raw: String): String? {
        val name = raw.trim()
        return when {
            name.isEmpty() -> "Enter a tag name"
            name.length > 24 -> "Keep the name under 25 characters"
            Categories.all.any { it.label.equals(name, ignoreCase = true) } ->
                "This is already a built-in category"
            items.value.any { it.name.equals(name, ignoreCase = true) } ->
                "This tag already exists"
            else -> {
                viewModelScope.launch { preferences.addCustomTag(name) }
                null
            }
        }
    }

    fun deleteTag(name: String) {
        viewModelScope.launch { preferences.removeCustomTag(name) }
    }
}