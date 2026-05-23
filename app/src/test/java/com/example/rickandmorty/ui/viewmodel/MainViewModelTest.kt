package com.example.rickandmorty.ui.viewmodel

import app.cash.turbine.test
import com.example.rickandmorty.data.model.PokemonResponse
import com.example.rickandmorty.data.model.PokemonResult
import com.example.rickandmorty.data.repository.PokemonRepository
import com.example.rickandmorty.testing.FakeFavoritesDao
import com.example.rickandmorty.testing.FakeHistoryDao
import com.example.rickandmorty.testing.FakePokeApi
import com.example.rickandmorty.testing.MainDispatcherRule
import com.example.rickandmorty.testing.pokemonDetail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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
    fun `initial state is Loading before first emission`() {
        val viewModel = MainViewModel(repository)

        assertEquals(ListUiState.Loading, viewModel.listUiState.value)
    }

    @Test
    fun `init emits full sequence Loading then Success on successful load`() = runTest {
        val viewModel = MainViewModel(repository)

        viewModel.listUiState.test {
            assertEquals(ListUiState.Loading, awaitItem())
            advanceUntilIdle()
            val success = awaitItem()
            assertTrue("expected Success, got $success", success is ListUiState.Success)
            assertEquals(3, (success as ListUiState.Success).pokemon.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init emits Loading then Error when api fails`() = runTest {
        api.shouldFail = true

        val viewModel = MainViewModel(repository)

        viewModel.listUiState.test {
            assertEquals(ListUiState.Loading, awaitItem())
            advanceUntilIdle()
            val errorState = awaitItem()
            assertTrue("expected Error, got $errorState", errorState is ListUiState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry after error triggers a new api request and reaches Success`() = runTest {
        api.shouldFail = true
        val viewModel = MainViewModel(repository)
        advanceUntilIdle()
        assertTrue(viewModel.listUiState.value is ListUiState.Error)
        val callsBeforeRetry = api.listCallCount

        api.shouldFail = false
        viewModel.loadPokemonList()
        advanceUntilIdle()

        assertTrue(viewModel.listUiState.value is ListUiState.Success)
        assertEquals(callsBeforeRetry + 1, api.listCallCount)
    }

    @Test
    fun `empty search result emits Empty (not Success with empty list)`() = runTest {
        val viewModel = MainViewModel(repository)
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("xxxnothing")
        advanceUntilIdle()

        assertEquals(ListUiState.Empty, viewModel.listUiState.value)
        assertNotEquals(
            ListUiState.Success(emptyList()),
            viewModel.listUiState.value
        )
    }

    @Test
    fun `successful search returns Success with filtered pokemon`() = runTest {
        val viewModel = MainViewModel(repository)
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("char")
        advanceUntilIdle()

        val state = viewModel.listUiState.value
        assertTrue(state is ListUiState.Success)
        assertEquals(1, (state as ListUiState.Success).pokemon.size)
        assertEquals("charmander", state.pokemon.first().name)
    }

    @Test
    fun `debounce coalesces rapid search updates into a single load`() = runTest {
        val viewModel = MainViewModel(repository)
        advanceUntilIdle()
        val callsBeforeSearch = api.listCallCount

        viewModel.onSearchQueryChanged("a")
        advanceTimeBy(100)
        viewModel.onSearchQueryChanged("ab")
        advanceTimeBy(100)
        viewModel.onSearchQueryChanged("abc")
        advanceTimeBy(100)
        viewModel.onSearchQueryChanged("char")

        advanceTimeBy(600)
        advanceUntilIdle()

        assertEquals("char", viewModel.searchQuery.value)
        // API list endpoint вызывался только раз при init — поиск идёт по кэшу,
        // но если бы каждый набор символов триггерил getPokemonList, был бы каскад
        // запросов. Debounce + кэш = ровно один реальный сетевой вызов.
        assertEquals(callsBeforeSearch, api.listCallCount)
        // Финальный state соответствует именно последнему запросу, а не промежуточному
        val state = viewModel.listUiState.value
        assertTrue(state is ListUiState.Success)
        assertEquals("charmander", (state as ListUiState.Success).pokemon.first().name)
    }

    @Test
    fun `loadPokemonDetails success records the pokemon in history`() = runTest {
        val viewModel = MainViewModel(repository)
        advanceUntilIdle()

        viewModel.loadPokemonDetails("charmander")
        advanceUntilIdle()

        val history = historyDao.observeAll().first()
        assertEquals(1, history.size)
        assertEquals("charmander", history.first().name)
    }

    @Test
    fun `loadPokemonDetails sets DetailUiState to Success with correct id`() = runTest {
        val viewModel = MainViewModel(repository)
        advanceUntilIdle()

        viewModel.loadPokemonDetails("ivysaur")
        advanceUntilIdle()

        val state = viewModel.detailUiState.value
        assertTrue(state is DetailUiState.Success)
        assertEquals("ivysaur", (state as DetailUiState.Success).pokemon.name)
    }

    @Test
    fun `loadPokemonDetails error puts DetailUiState to Error`() = runTest {
        api.shouldFail = true
        val viewModel = MainViewModel(repository)
        advanceUntilIdle()

        viewModel.loadPokemonDetails("bulbasaur")
        advanceUntilIdle()

        assertTrue(viewModel.detailUiState.value is DetailUiState.Error)
    }

    @Test
    fun `clearHistory empties history flow`() = runTest {
        val viewModel = MainViewModel(repository)
        advanceUntilIdle()
        viewModel.loadPokemonDetails("bulbasaur")
        advanceUntilIdle()
        assertEquals(1, historyDao.observeAll().first().size)

        viewModel.clearHistory()
        advanceUntilIdle()

        assertEquals(0, historyDao.observeAll().first().size)
    }

    @Test
    fun `detail state initial value is Loading`() {
        val viewModel = MainViewModel(repository)

        assertEquals(DetailUiState.Loading, viewModel.detailUiState.value)
    }
}
