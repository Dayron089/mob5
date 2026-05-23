package com.example.rickandmorty.testing

import com.example.rickandmorty.api.PokeApi
import com.example.rickandmorty.data.model.PokemonDetail
import com.example.rickandmorty.data.model.PokemonResponse
import com.example.rickandmorty.data.model.PokemonResult
import com.example.rickandmorty.data.model.Sprites
import com.example.rickandmorty.data.model.TypeInfo
import com.example.rickandmorty.data.model.TypeSlot
import java.io.IOException

class FakePokeApi : PokeApi {

    var listResponse: PokemonResponse = PokemonResponse(
        listOf(
            PokemonResult("bulbasaur", "https://pokeapi.co/api/v2/pokemon/1/"),
            PokemonResult("charmander", "https://pokeapi.co/api/v2/pokemon/4/")
        )
    )

    var detailResponse: PokemonDetail = PokemonDetail(
        id = 1,
        name = "bulbasaur",
        height = 7,
        weight = 69,
        sprites = Sprites(frontDefault = "url", frontShiny = null),
        types = listOf(TypeSlot(TypeInfo("grass")))
    )

    var shouldFail: Boolean = false

    override suspend fun getPokemonList(limit: Int, offset: Int): PokemonResponse {
        if (shouldFail) throw IOException("Simulated failure")
        return listResponse
    }

    override suspend fun getPokemonDetail(name: String): PokemonDetail {
        if (shouldFail) throw IOException("Simulated failure")
        return detailResponse.copy(name = name)
    }
}

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
