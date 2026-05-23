package com.example.rickandmorty.testing

import com.example.rickandmorty.data.local.dao.FavoritesDao
import com.example.rickandmorty.data.local.entity.FavoritePokemonEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeFavoritesDao : FavoritesDao {

    private val items = mutableListOf<FavoritePokemonEntity>()
    private val state = MutableStateFlow<List<FavoritePokemonEntity>>(emptyList())

    override suspend fun add(entity: FavoritePokemonEntity) {
        items.removeAll { it.id == entity.id }
        items.add(entity)
        emitSnapshot()
    }

    override suspend fun remove(id: Int) {
        items.removeAll { it.id == id }
        emitSnapshot()
    }

    override fun observeAll(): Flow<List<FavoritePokemonEntity>> = state

    private fun emitSnapshot() {
        state.value = items.sortedByDescending { it.addedAt }.toList()
    }
}
