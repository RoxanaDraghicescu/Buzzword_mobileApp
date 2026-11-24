package com.example.buzzword.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A message that arrived through the table. One object = one row in "received_messages". */
@Entity(tableName = "received_messages")
data class ReceivedMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val text: String,
    val receivedAt: Long = System.currentTimeMillis(), // when it arrived, in milliseconds
)