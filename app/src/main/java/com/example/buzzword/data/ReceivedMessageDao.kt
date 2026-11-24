package com.example.buzzword.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceivedMessageDao {

    /** Saves a received message. Later called from TableManager.onFrameReceived(). */
    @Insert
    suspend fun add(message: ReceivedMessage): Long

    /** The history, newest first. A Flow, so the screen updates by itself. */
    @Query("SELECT * FROM received_messages ORDER BY receivedAt DESC")
    fun getAll(): Flow<List<ReceivedMessage>>

    @Delete
    suspend fun delete(message: ReceivedMessage)
}