package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.ToolBoxDatabase
import com.example.core.database.entity.HistoryEntity
import com.example.core.model.ToolCategory
import com.example.core.model.ToolDefinition
import com.example.core.registry.ToolRegistry
import com.example.core.repository.PreferencesManager
import com.example.core.repository.ThemeMode
import com.example.core.repository.UserDataRepository
import com.example.core.util.FileUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ToolBoxDatabase.getDatabase(application)
    private val repository = UserDataRepository(database.favoriteDao(), database.historyDao())
    private val preferencesManager = PreferencesManager(application)

    // User preferences (Theme & Auto-save to Mobile Memory)
    val themeMode: StateFlow<ThemeMode> = preferencesManager.themeMode
    val autoSaveMedia: StateFlow<Boolean> = preferencesManager.autoSaveMedia

    // Search and filter state
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow(ToolCategory.ALL)

    // Dynamic filtered tool list
    val filteredTools: StateFlow<List<ToolDefinition>> = combine(searchQuery, selectedCategory) { query, cat ->
        ToolRegistry.searchTools(query, cat)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ToolRegistry.allTools)

    // Favorites
    val favoriteToolIds: StateFlow<Set<String>> = repository.favorites
        .map { list -> list.map { it.toolId }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val favoriteTools: StateFlow<List<ToolDefinition>> = favoriteToolIds.map { idSet ->
        ToolRegistry.allTools.filter { it.id in idSet }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // History
    val historyItems: StateFlow<List<HistoryEntity>> = repository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cache size
    val cacheSizeBytes = MutableStateFlow(0L)

    init {
        refreshCacheSize()
    }

    fun setThemeMode(mode: ThemeMode) {
        preferencesManager.setThemeMode(mode)
    }

    fun setAutoSaveMedia(enabled: Boolean) {
        preferencesManager.setAutoSaveMedia(enabled)
    }

    fun onSearchQueryChanged(newQuery: String) {
        searchQuery.value = newQuery
    }

    fun onCategorySelected(category: ToolCategory) {
        selectedCategory.value = category
    }

    fun toggleFavorite(toolId: String) {
        viewModelScope.launch {
            val isFav = favoriteToolIds.value.contains(toolId)
            repository.toggleFavorite(toolId, isFav)
        }
    }

    fun recordHistory(toolId: String, toolName: String, summary: String) {
        viewModelScope.launch {
            repository.addHistory(toolId, toolName, summary)
            refreshCacheSize()
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.removeHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun refreshCacheSize() {
        cacheSizeBytes.value = FileUtils.getCacheSize(getApplication())
    }

    fun clearTemporaryCache(): Long {
        val freed = FileUtils.clearCache(getApplication())
        refreshCacheSize()
        return freed
    }
}
