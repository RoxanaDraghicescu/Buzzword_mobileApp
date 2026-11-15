package com.example.buzzword.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** The operations on the "saved_messages" table. Room generates the real code at build time (KSP). */
@Dao
interface SavedMessageDao {

    /** Create: inserts one message and returns the id Room gave it. */
    @Insert
    suspend fun add(message: SavedMessage): Long

    /** Read: all saved messages, newest first. A Flow updates the screen by itself when the table changes. */
    @Query("SELECT * FROM saved_messages ORDER BY id DESC")
    fun getAll(): Flow<List<SavedMessage>>

    // Person 1 (Day 2): add update and delete here, for example
    //   @Update suspend fun update(message: SavedMessage)
    //   @Delete suspend fun delete(message: SavedMessage)
}