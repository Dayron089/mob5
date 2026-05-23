package com.example.rickandmorty.testing

import com.example.rickandmorty.data.local.dao.HistoryDao
import com.example.rickandmorty.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeHistoryDao : HistoryDao {

    private val items = mutableListOf<HistoryEntity>()
    private val state = MutableStateFlow<List<HistoryEntity>>(emptyList())

    override suspend fun upsert(entity: HistoryEntity) {
        items.removeAll { it.id == entity.id }
        items.add(entity)
        emitSnapshot()
    }

    override fun observeAll(): Flow<List<HistoryEntity>> = state

    override suspend fun clear() {
        items.clear()
        emitSnapshot()
    }

    private fun emitSnapshot() {
        state.value = items.sortedByDescending { it.viewedAt }.toList()
    }
}
