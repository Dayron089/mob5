package com.example.rickandmorty.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.rickandmorty.data.model.PokemonDetail
import com.example.rickandmorty.ui.viewmodel.DetailUiState
import com.example.rickandmorty.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(
    viewModel: MainViewModel,
    pokemonName: String,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(pokemonName) {
        viewModel.loadPokemonDetails(pokemonName)
    }

    val uiState by viewModel.detailUiState.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteIds.collectAsStateWithLifecycle()

    val title = when (val state = uiState) {
        is DetailUiState.Success -> state.pokemon.name.replaceFirstChar { it.uppercaseChar() }
        else -> pokemonName.replaceFirstChar { it.uppercaseChar() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState is DetailUiState.Success) {
                val p = (uiState as DetailUiState.Success).pokemon
                val isFav = p.id in favoriteIds
                FloatingActionButton(
                    onClick = { viewModel.toggleFavorite(p) }
                ) {
                    Icon(
                        imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFav) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is DetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DetailUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadPokemonDetails(pokemonName) }) {
                            Text("Retry")
                        }
                    }
                }
                is DetailUiState.Success -> {
                    PokemonDetailContent(state.pokemon)
                }
            }
        }
    }
}

@Composable
fun PokemonDetailContent(pokemon: PokemonDetail) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = pokemon.sprites.frontDefault ?: pokemon.sprites.frontShiny,
            contentDescription = pokemon.name,
            modifier = Modifier
                .size(250.dp)
                .padding(bottom = 16.dp)
        )
        Text(
            text = "#${pokemon.id} ${pokemon.name.replaceFirstChar { it.uppercaseChar() }}",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow(label = "Height", value = "${pokemon.height * 10} cm")
                DetailRow(label = "Weight", value = "${pokemon.weight / 10.0} kg")

                Spacer(modifier = Modifier.height(8.dp))
                Text("Types:", style = MaterialTheme.typography.titleMedium)
                Row {
                    pokemon.types.forEach { typeSlot ->
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(typeSlot.type.name.replaceFirstChar { it.uppercaseChar() })
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = "$label:", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(2f))
    }
}
