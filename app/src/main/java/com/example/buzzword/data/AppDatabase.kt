package com.example.buzzword.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * The local database, saved as a file on the phone ("buzzword.db").
 * Because it is a file, data survives closing the app: this is the Persistence point.
 * Each new table added later means version + 1.
 * Version 2: added the received_messages table.
 */
@Database(
    entities = [SavedMessage::class, ReceivedMessage::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun savedMessageDao(): SavedMessageDao
    abstract fun receivedMessageDao(): ReceivedMessageDao

    companion object {
        /** From version 1 to 2: create the new table. Existing data stays. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `received_messages` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`sender` TEXT NOT NULL, " +
                            "`text` TEXT NOT NULL, " +
                            "`receivedAt` INTEGER NOT NULL)"
                )
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "buzzword.db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}