package com.example.rickandmorty.data.repository

import com.example.rickandmorty.data.model.PokemonResponse
import com.example.rickandmorty.data.model.PokemonResult
import com.example.rickandmorty.testing.FakeFavoritesDao
import com.example.rickandmorty.testing.FakeHistoryDao
import com.example.rickandmorty.testing.FakePokeApi
import com.example.rickandmorty.testing.pokemonDetail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PokemonRepositoryTest {

    private lateinit var api: FakePokeApi
    private lateinit var favoritesDao: FakeFavoritesDao
    private lateinit var historyDao: FakeHistoryDao
    private lateinit var repository: PokemonRepository

    @Before
    fun setUp() {
        api = FakePokeApi()
        favoritesDao = FakeFavoritesDao()
        historyDao = FakeHistoryDao()
        repository = PokemonRepository(api, favoritesDao, historyDao)
    }

    @Test
    fun `getPokemonList success returns mapped list`() = runTest {
        val result = repository.getPokemonList()

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrThrow().size)
        assertEquals("bulbasaur", result.getOrThrow().first().name)
    }

    @Test
    fun `getPokemonList api failure returns Result failure with exception`() = runTest {
        api.shouldFail = true

        val result = repository.getPokemonList()

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `getPokemonList second call uses cache and does not call api again`() = runTest {
        repository.getPokemonList()
        repository.getPokemonList()
        repository.getPokemonList("char")

        assertEquals(1, api.listCallCount)
    }

    @Test
    fun `getPokemonList with query filters cached results case insensitive`() = runTest {
        repository.getPokemonList()

        val result = repository.getPokemonList("CHAR")

        assertEquals(1, result.getOrThrow().size)
        assertEquals("charmander", result.getOrThrow().first().name)
    }

    @Test
    fun `getPokemonList with query that matches nothing returns empty list`() = runTest {
        repository.getPokemonList()

        val result = repository.getPokemonList("xxxnothing")

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrThrow().size)
    }

    @Test
    fun `getPokemonDetail success returns detail with requested name`() = runTest {
        val result = repository.getPokemonDetail("charmander")

        assertTrue(result.isSuccess)
        assertEquals("charmander", result.getOrThrow().name)
    }

    @Test
    fun `getPokemonDetail api failure returns Result failure`() = runTest {
        api.shouldFail = true

        val result = repository.getPokemonDetail("bulbasaur")

        assertTrue(result.isFailure)
    }

    @Test
    fun `toggleFavorite adds to favorites when not currently favorite`() = runTest {
        val detail = pokemonDetail(id = 1, name = "bulbasaur")

        repository.toggleFavorite(detail, isCurrentlyFavorite = false)

        val items = favoritesDao.observeAll().first()
        assertEquals(1, items.size)
        assertEquals(1, items.first().id)
    }

    @Test
    fun `toggleFavorite removes from favorites when currently favorite`() = runTest {
        val detail = pokemonDetail(id = 1)
        repository.toggleFavorite(detail, isCurrentlyFavorite = false)

        repository.toggleFavorite(detail, isCurrentlyFavorite = true)

        val items = favoritesDao.observeAll().first()
        assertEquals(0, items.size)
    }

    @Test
    fun `recordHistory called twice for same pokemon stores only one entry (REPLACE)`() = runTest {
        val detail = pokemonDetail(id = 5, name = "charmeleon")

        repository.recordHistory(detail)
        repository.recordHistory(detail)
        repository.recordHistory(detail)

        val items = historyDao.observeAll().first()
        assertEquals(1, items.size)
        assertEquals(5, items.first().id)
    }

    @Test
    fun `clearHistory removes all entries`() = runTest {
        repository.recordHistory(pokemonDetail(id = 1))
        repository.recordHistory(pokemonDetail(id = 2))
        assertEquals(2, historyDao.observeAll().first().size)

        repository.clearHistory()

        assertEquals(0, historyDao.observeAll().first().size)
    }

    @Test
    fun `toggleFavorite stores types as comma-separated csv`() = runTest {
        val detail = pokemonDetail(id = 7, name = "squirtle", types = listOf("water", "ice"))

        repository.toggleFavorite(detail, isCurrentlyFavorite = false)

        val items = favoritesDao.observeAll().first()
        assertEquals("water,ice", items.first().types)
    }
}
