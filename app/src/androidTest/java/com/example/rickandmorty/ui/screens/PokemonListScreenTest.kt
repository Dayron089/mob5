package com.example.rickandmorty.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rickandmorty.data.local.dao.FavoritesDao
import com.example.rickandmorty.data.local.dao.HistoryDao
import com.example.rickandmorty.data.local.entity.FavoritePokemonEntity
import com.example.rickandmorty.data.local.entity.HistoryEntity
import com.example.rickandmorty.data.model.PokemonResponse
import com.example.rickandmorty.data.model.PokemonResult
import com.example.rickandmorty.data.repository.PokemonRepository
import com.example.rickandmorty.testing.FakePokeApi
import com.example.rickandmorty.ui.viewmodel.MainViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PokemonListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun stubFavoritesDao() = object : FavoritesDao {
        private val flow = MutableStateFlow<List<FavoritePokemonEntity>>(emptyList())
        override suspend fun add(entity: FavoritePokemonEntity) {}
        override suspend fun remove(id: Int) {}
        override fun observeAll(): Flow<List<FavoritePokemonEntity>> = flow
    }

    private fun stubHistoryDao() = object : HistoryDao {
        private val flow = MutableStateFlow<List<HistoryEntity>>(emptyList())
        override suspend fun upsert(entity: HistoryEntity) {}
        override fun observeAll(): Flow<List<HistoryEntity>> = flow
        override suspend fun clear() {}
    }

    private fun viewModelWith(api: FakePokeApi): MainViewModel =
        MainViewModel(PokemonRepository(api, stubFavoritesDao(), stubHistoryDao()))

    @Test
    fun list_displaysItemsAfterLoading() {
        val api = FakePokeApi().apply {
            listResponse = PokemonResponse(
                listOf(
                    PokemonResult("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"),
                    PokemonResult("charmander", "https://pokeapi.co/api/v2/pokemon/4/")
                )
            )
        }
        val viewModel = viewModelWith(api)

        composeTestRule.setContent {
            PokemonListScreen(
                viewModel = viewModel,
                onNavigateToDetail = {},
                onNavigateToFavorites = {},
                onNavigateToHistory = {}
            )
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithText("Bulbasaur")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Bulbasaur").assertIsDisplayed()
        composeTestRule.onNodeWithText("Charmander").assertIsDisplayed()
    }

    @Test
    fun list_clickOnItem_invokesNavigationWithCorrectName() {
        val api = FakePokeApi().apply {
            listResponse = PokemonResponse(
                listOf(
                    PokemonResult("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"),
                    PokemonResult("charmander", "https://pokeapi.co/api/v2/pokemon/4/")
                )
            )
        }
        val viewModel = viewModelWith(api)
        var navigatedTo: String? = null

        composeTestRule.setContent {
            PokemonListScreen(
                viewModel = viewModel,
                onNavigateToDetail = { navigatedTo = it },
                onNavigateToFavorites = {},
                onNavigateToHistory = {}
            )
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithText("Charmander")
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Charmander").performClick()

        assertEquals("charmander", navigatedTo)
    }
}
