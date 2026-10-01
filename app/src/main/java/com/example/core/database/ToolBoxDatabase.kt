package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.core.database.dao.FavoriteDao
import com.example.core.database.dao.HistoryDao
import com.example.core.database.entity.FavoriteEntity
import com.example.core.database.entity.HistoryEntity

@Database(entities = [FavoriteEntity::class, HistoryEntity::class], version = 1, exportSchema = false)
abstract class ToolBoxDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: ToolBoxDatabase? = null

        fun getDatabase(context: Context): ToolBoxDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ToolBoxDatabase::class.java,
                    "toolbox_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
