package com.example.core.repository

import com.example.core.database.dao.FavoriteDao
import com.example.core.database.dao.HistoryDao
import com.example.core.database.entity.FavoriteEntity
import com.example.core.database.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

class UserDataRepository(
    private val favoriteDao: FavoriteDao,
    private val historyDao: HistoryDao
) {
    val favorites: Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()
    val history: Flow<List<HistoryEntity>> = historyDao.getRecentHistory()

    fun isFavorite(toolId: String): Flow<Boolean> = favoriteDao.isFavorite(toolId)

    suspend fun toggleFavorite(toolId: String, isCurrentlyFavorite: Boolean) {
        if (isCurrentlyFavorite) {
            favoriteDao.removeFavorite(toolId)
        } else {
            favoriteDao.addFavorite(FavoriteEntity(toolId))
        }
    }

    suspend fun addHistory(toolId: String, toolName: String, summary: String) {
        historyDao.insertHistory(
            HistoryEntity(
                toolId = toolId,
                toolName = toolName,
                summary = summary
            )
        )
    }

    suspend fun removeHistory(id: Long) {
        historyDao.deleteHistoryById(id)
    }

    suspend fun clearHistory() {
        historyDao.clearAll()
    }
}
