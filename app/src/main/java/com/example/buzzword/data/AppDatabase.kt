package com.example.buzzword.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The local database, saved as a file on the phone ("buzzword.db").
 * Because it is a file, data survives closing the app: this is the Persistence point.
 * Each new table added later means version + 1.
 */
@Database(entities = [SavedMessage::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun savedMessageDao(): SavedMessageDao

    companion object {
        // Only one database object for the whole app.
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "buzzword.db",
                ).build().also { instance = it }
            }
    }
}