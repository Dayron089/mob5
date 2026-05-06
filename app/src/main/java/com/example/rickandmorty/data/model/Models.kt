package com.example.rickandmorty.data.model

import com.google.gson.annotations.SerializedName

data class PokemonResponse(
    val results: List<PokemonResult>
)

data class PokemonResult(
    val name: String,
    val url: String
) {
    // Helper to extract ID from URL like "https://pokeapi.co/api/v2/pokemon/1/"
    val id: Int
        get() = url.trimEnd('/').split("/").last().toIntOrNull() ?: 0
        
    val imageUrl: String
        get() = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/$id.png"
}

data class PokemonDetail(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val sprites: Sprites,
    val types: List<TypeSlot>
)

data class Sprites(
    @SerializedName("front_default") val frontDefault: String?,
    @SerializedName("front_shiny") val frontShiny: String?
)

data class TypeSlot(
    val type: TypeInfo
)

data class TypeInfo(
    val name: String
)
