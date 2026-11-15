package com.example.buzzword.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A message the user sends often, like "WIFI: table42". One object = one row in the table. */
@Entity(tableName = "saved_messages")
data class SavedMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // 0 = "let Room pick the next id"
    val title: String,
    val text: String,
)