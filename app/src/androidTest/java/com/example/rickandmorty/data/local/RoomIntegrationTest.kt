package com.example.rickandmorty.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rickandmorty.data.local.dao.FavoritesDao
import com.example.rickandmorty.data.local.dao.HistoryDao
import com.example.rickandmorty.data.local.entity.FavoritePokemonEntity
import com.example.rickandmorty.data.local.entity.HistoryEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class RoomIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var favoritesDao: FavoritesDao
    private lateinit var historyDao: HistoryDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        favoritesDao = db.favoritesDao()
        historyDao = db.historyDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun favoritesDao_addAndObserve_returnsItem() = runTest {
        val entity = FavoritePokemonEntity(
            id = 1, name = "bulbasaur", imageUrl = "url", types = "grass", addedAt = 100L
        )

        favoritesDao.add(entity)

        val items = favoritesDao.observeAll().first()
        assertEquals(1, items.size)
        assertEquals("bulbasaur", items.first().name)
    }

    @Test
    fun favoritesDao_addingSameId_replacesAndKeepsOnlyOne() = runTest {
        val first = FavoritePokemonEntity(1, "bulbasaur", "url1", "grass", 100L)
        val second = FavoritePokemonEntity(1, "bulbasaur", "url2", "grass,poison", 200L)

        favoritesDao.add(first)
        favoritesDao.add(second)

        val items = favoritesDao.observeAll().first()
        assertEquals(1, items.size)
        assertEquals(200L, items.first().addedAt)
        assertEquals("grass,poison", items.first().types)
    }

    @Test
    fun favoritesDao_remove_clearsItem() = runTest {
        favoritesDao.add(FavoritePokemonEntity(1, "bulbasaur", "url", "grass", 100L))
        assertEquals(1, favoritesDao.observeAll().first().size)

        favoritesDao.remove(1)

        assertTrue(favoritesDao.observeAll().first().isEmpty())
    }

    @Test
    fun favoritesDao_observeAll_orderedByAddedAtDesc() = runTest {
        favoritesDao.add(FavoritePokemonEntity(1, "bulbasaur", "url", "grass", 100L))
        favoritesDao.add(FavoritePokemonEntity(4, "charmander", "url", "fire", 300L))
        favoritesDao.add(FavoritePokemonEntity(7, "squirtle", "url", "water", 200L))

        val items = favoritesDao.observeAll().first()

        assertEquals(listOf(4, 7, 1), items.map { it.id })
    }

    @Test
    fun historyDao_upsertSameId_keepsOneEntryWithLatestViewedAt() = runTest {
        historyDao.upsert(HistoryEntity(1, "bulbasaur", "url", 100L))
        historyDao.upsert(HistoryEntity(1, "bulbasaur", "url", 500L))

        val items = historyDao.observeAll().first()

        assertEquals(1, items.size)
        assertEquals(500L, items.first().viewedAt)
    }

    @Test
    fun historyDao_clear_removesAllEntries() = runTest {
        historyDao.upsert(HistoryEntity(1, "a", "url", 100L))
        historyDao.upsert(HistoryEntity(2, "b", "url", 200L))
        assertEquals(2, historyDao.observeAll().first().size)

        historyDao.clear()

        assertTrue(historyDao.observeAll().first().isEmpty())
    }
}
