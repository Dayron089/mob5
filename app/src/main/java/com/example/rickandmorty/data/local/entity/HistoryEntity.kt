package com.example.rickandmorty.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val imageUrl: String,
    val viewedAt: Long
)
