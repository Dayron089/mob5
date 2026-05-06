package com.example.rickandmorty.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rickandmorty.data.local.entity.FavoritePokemonEntity
import com.example.rickandmorty.data.local.entity.HistoryEntity
import com.example.rickandmorty.data.model.PokemonDetail
import com.example.rickandmorty.data.model.PokemonResult
import com.example.rickandmorty.data.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// UI States
sealed class ListUiState {
    object Loading : ListUiState()
    data class Success(val pokemon: List<PokemonResult>) : ListUiState()
    object Empty : ListUiState()
    data class Error(val message: String) : ListUiState()
}

sealed class DetailUiState {
    object Loading : DetailUiState()
    data class Success(val pokemon: PokemonDetail) : DetailUiState()
    data class Error(val message: String) : DetailUiState()
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    // List State
    private val _listUiState = MutableStateFlow<ListUiState>(ListUiState.Loading)
    val listUiState: StateFlow<ListUiState> = _listUiState.asStateFlow()

    // Detail State
    private val _detailUiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val detailUiState: StateFlow<DetailUiState> = _detailUiState.asStateFlow()

    // Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Favorites — backed by Room
    val favorites: StateFlow<List<FavoritePokemonEntity>> =
        repository.favorites.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val favoriteIds: StateFlow<Set<Int>> =
        repository.favorites
            .map { list -> list.mapTo(mutableSetOf(), FavoritePokemonEntity::id) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptySet()
            )

    // History — backed by Room
    val history: StateFlow<List<HistoryEntity>> =
        repository.history.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private var searchJob: Job? = null

    init {
        loadPokemonList()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // Debounce
            loadPokemonList(query)
        }
    }

    fun loadPokemonList(query: String = "") {
        viewModelScope.launch {
            if (_listUiState.value !is ListUiState.Success) {
                _listUiState.value = ListUiState.Loading
            }

            val result = repository.getPokemonList(if (query.isBlank()) null else query)

            result.onSuccess { list ->
                if (list.isEmpty()) {
                    _listUiState.value = ListUiState.Empty
                } else {
                    _listUiState.value = ListUiState.Success(list)
                }
            }.onFailure { e ->
                _listUiState.value = ListUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadPokemonDetails(name: String) {
        viewModelScope.launch {
            _detailUiState.value = DetailUiState.Loading
            val result = repository.getPokemonDetail(name)

            result.onSuccess { detail ->
                _detailUiState.value = DetailUiState.Success(detail)
                // Record view in history (Room)
                repository.recordHistory(detail)
            }.onFailure { e ->
                _detailUiState.value = DetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun toggleFavorite(detail: PokemonDetail) {
        viewModelScope.launch {
            repository.toggleFavorite(detail, favoriteIds.value.contains(detail.id))
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
