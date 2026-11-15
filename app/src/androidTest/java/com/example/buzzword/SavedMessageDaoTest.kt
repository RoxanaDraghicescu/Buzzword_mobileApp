package com.example.buzzword

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import com.example.buzzword.data.AppDatabase
import com.example.buzzword.data.SavedMessage

@RunWith(AndroidJUnit4::class)
class SavedMessageDaoTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    /** Create + Read: two added messages come back, newest first. */
    @Test
    fun addTwo_getAllReturnsBothNewestFirst() = runBlocking<Unit> {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val dao = db.savedMessageDao()

        dao.add(SavedMessage(title = "Wi-Fi", text = "WIFI: table42"))
        dao.add(SavedMessage(title = "Door", text = "Code 1234"))

        val all = dao.getAll().first()
        assertEquals(2, all.size)
        assertEquals("Door", all[0].title)
        db.close()
    }

    /** Persistence: data is still there after closing and reopening the database. */
    @Test
    fun dataSurvivesClosingAndReopening() = runBlocking<Unit> {
        val name = "persistence-test.db"
        context.deleteDatabase(name)

        val first = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        first.savedMessageDao().add(SavedMessage(title = "Wi-Fi", text = "WIFI: table42"))
        first.close()

        val reopened = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        val all = reopened.savedMessageDao().getAll().first()
        assertEquals(1, all.size)
        assertEquals("WIFI: table42", all[0].text)
        reopened.close()
        context.deleteDatabase(name)
    }

    /** Update: the same row gets the new text, nothing new is added. */
    @Test
    fun update_changesTheSameRow() = runBlocking<Unit> {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val dao = db.savedMessageDao()
        dao.add(SavedMessage(title = "Wi-Fi", text = "WIFI: table42"))
        val saved = dao.getAll().first()[0]

        dao.update(saved.copy(text = "WIFI: table43"))

        val all = dao.getAll().first()
        assertEquals(1, all.size)
        assertEquals("WIFI: table43", all[0].text)
        db.close()
    }

    /** Delete: only the chosen message disappears. */
    @Test
    fun delete_removesOnlyThatMessage() = runBlocking<Unit> {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val dao = db.savedMessageDao()
        dao.add(SavedMessage(title = "Wi-Fi", text = "WIFI: table42"))
        dao.add(SavedMessage(title = "Door", text = "Code 1234"))
        val door = dao.getAll().first().first { it.title == "Door" }

        dao.delete(door)

        val all = dao.getAll().first()
        assertEquals(1, all.size)
        assertEquals("Wi-Fi", all[0].title)
        db.close()
    }
}