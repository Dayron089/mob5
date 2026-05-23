package com.example.rickandmorty.testing

import com.example.rickandmorty.data.model.PokemonDetail
import com.example.rickandmorty.data.model.Sprites
import com.example.rickandmorty.data.model.TypeInfo
import com.example.rickandmorty.data.model.TypeSlot

fun pokemonDetail(
    id: Int = 1,
    name: String = "bulbasaur",
    types: List<String> = listOf("grass")
) = PokemonDetail(
    id = id,
    name = name,
    height = 7,
    weight = 69,
    sprites = Sprites(frontDefault = "https://example.com/$id.png", frontShiny = null),
    types = types.map { TypeSlot(TypeInfo(it)) }
)
