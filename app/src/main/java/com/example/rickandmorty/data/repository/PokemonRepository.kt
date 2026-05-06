package com.example.rickandmorty.data.repository

import com.example.rickandmorty.api.PokeApi
import com.example.rickandmorty.data.local.dao.FavoritesDao
import com.example.rickandmorty.data.local.dao.HistoryDao
import com.example.rickandmorty.data.local.entity.FavoritePokemonEntity
import com.example.rickandmorty.data.local.entity.HistoryEntity
import com.example.rickandmorty.data.model.PokemonDetail
import com.example.rickandmorty.data.model.PokemonResult
import kotlinx.coroutines.flow.Flow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonRepository @Inject constructor(
    private val api: PokeApi,
    private val favoritesDao: FavoritesDao,
    private val historyDao: HistoryDao
) {

    private var cachedList: List<PokemonResult> = emptyList()

    val favorites: Flow<List<FavoritePokemonEntity>> = favoritesDao.observeAll()

    val history: Flow<List<HistoryEntity>> = historyDao.observeAll()

    suspend fun getPokemonList(query: String? = null): Result<List<PokemonResult>> {
        return try {
            if (cachedList.isEmpty()) {
                // Fetch 151 (Gen 1) pokemon as a base set
                cachedList = api.getPokemonList(limit = 151).results
            }

            val result = if (query.isNullOrBlank()) {
                cachedList
            } else {
                cachedList.filter {
                    it.name.contains(query.lowercase(Locale.getDefault()))
                }
            }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPokemonDetail(nameOrId: String): Result<PokemonDetail> {
        return try {
            val response = api.getPokemonDetail(nameOrId.lowercase(Locale.getDefault()))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(detail: PokemonDetail, isCurrentlyFavorite: Boolean) {
        if (isCurrentlyFavorite) {
            favoritesDao.remove(detail.id)
        } else {
            favoritesDao.add(
                FavoritePokemonEntity(
                    id = detail.id,
                    name = detail.name,
                    imageUrl = detail.sprites.frontDefault ?: detail.sprites.frontShiny.orEmpty(),
                    types = detail.types.joinToString(",") { it.type.name },
                    addedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun recordHistory(detail: PokemonDetail) {
        historyDao.upsert(
            HistoryEntity(
                id = detail.id,
                name = detail.name,
                imageUrl = detail.sprites.frontDefault ?: detail.sprites.frontShiny.orEmpty(),
                viewedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearHistory() {
        historyDao.clear()
    }
}
