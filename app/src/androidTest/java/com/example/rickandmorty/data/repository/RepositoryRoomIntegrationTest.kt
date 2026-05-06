package com.example.rickandmorty.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rickandmorty.data.local.AppDatabase
import com.example.rickandmorty.testing.FakePokeApi
import com.example.rickandmorty.testing.pokemonDetail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test: PokemonRepository + Fake API + real Room.
 * Проверяет, что записи через Repository корректно сохраняются в Room и читаются обратно.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class RepositoryRoomIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: PokemonRepository
    private lateinit var api: FakePokeApi

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        api = FakePokeApi()
        repository = PokemonRepository(api, db.favoritesDao(), db.historyDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun toggleFavorite_writesToRoom_andRepositoryFlowEmitsUpdate() = runTest {
        val detail = pokemonDetail(id = 1, name = "bulbasaur", types = listOf("grass", "poison"))

        repository.toggleFavorite(detail, isCurrentlyFavorite = false)

        val items = repository.favorites.first()
        assertEquals(1, items.size)
        assertEquals(1, items.first().id)
        assertEquals("grass,poison", items.first().types)
    }

    @Test
    fun toggleFavorite_addThenRemove_resultsInEmptyFavorites() = runTest {
        val detail = pokemonDetail(id = 1)

        repository.toggleFavorite(detail, isCurrentlyFavorite = false)
        assertEquals(1, repository.favorites.first().size)

        repository.toggleFavorite(detail, isCurrentlyFavorite = true)

        assertEquals(0, repository.favorites.first().size)
    }

    @Test
    fun recordHistory_writeAndReadBack_returnsEntry() = runTest {
        val detail = pokemonDetail(id = 25, name = "pikachu", types = listOf("electric"))

        repository.recordHistory(detail)

        val history = repository.history.first()
        assertEquals(1, history.size)
        assertEquals(25, history.first().id)
        assertEquals("pikachu", history.first().name)
    }

    @Test
    fun clearHistory_afterRecordingMultiple_emptiesEverything() = runTest {
        repository.recordHistory(pokemonDetail(id = 1))
        repository.recordHistory(pokemonDetail(id = 2))
        repository.recordHistory(pokemonDetail(id = 3))
        assertEquals(3, repository.history.first().size)

        repository.clearHistory()

        assertEquals(0, repository.history.first().size)
    }
}
