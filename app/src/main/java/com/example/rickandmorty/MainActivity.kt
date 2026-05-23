package com.example.rickandmorty

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.rickandmorty.ui.screens.FavoritesScreen
import com.example.rickandmorty.ui.screens.HistoryScreen
import com.example.rickandmorty.ui.screens.PokemonDetailScreen
import com.example.rickandmorty.ui.screens.PokemonListScreen
import com.example.rickandmorty.ui.theme.RickAndMortyAppTheme
import com.example.rickandmorty.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RickAndMortyAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PokemonNavGraph()
                }
            }
        }
    }
}

private const val GRAPH_ROUTE = "pokemon_graph"

@Composable
fun PokemonNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = GRAPH_ROUTE) {
        navigation(startDestination = "list", route = GRAPH_ROUTE) {
            composable("list") { entry ->
                val parent = remember(entry) { navController.getBackStackEntry(GRAPH_ROUTE) }
                val viewModel: MainViewModel = hiltViewModel(parent)
                PokemonListScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { name -> navController.navigate("detail/$name") },
                    onNavigateToFavorites = { navController.navigate("favorites") },
                    onNavigateToHistory = { navController.navigate("history") }
                )
            }
            composable("favorites") { entry ->
                val parent = remember(entry) { navController.getBackStackEntry(GRAPH_ROUTE) }
                val viewModel: MainViewModel = hiltViewModel(parent)
                FavoritesScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { name -> navController.navigate("detail/$name") },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("history") { entry ->
                val parent = remember(entry) { navController.getBackStackEntry(GRAPH_ROUTE) }
                val viewModel: MainViewModel = hiltViewModel(parent)
                HistoryScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { name -> navController.navigate("detail/$name") },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "detail/{name}",
                arguments = listOf(navArgument("name") { type = NavType.StringType })
            ) { entry ->
                val parent = remember(entry) { navController.getBackStackEntry(GRAPH_ROUTE) }
                val viewModel: MainViewModel = hiltViewModel(parent)
                val name = entry.arguments?.getString("name") ?: ""
                PokemonDetailScreen(
                    viewModel = viewModel,
                    pokemonName = name,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
