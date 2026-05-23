package com.example.rickandmorty.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.rickandmorty.data.local.dao.FavoritesDao
import com.example.rickandmorty.data.local.dao.HistoryDao
import com.example.rickandmorty.data.local.entity.FavoritePokemonEntity
import com.example.rickandmorty.data.local.entity.HistoryEntity

@Database(
    entities = [FavoritePokemonEntity::class, HistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
    abstract fun historyDao(): HistoryDao
}
