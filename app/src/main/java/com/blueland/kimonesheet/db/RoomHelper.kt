package com.blueland.kimonesheet.db

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.blueland.kimonesheet.db.dao.MemoDao
import com.blueland.kimonesheet.db.entity.MemoEntity

@Database(entities = [MemoEntity::class], version = 2, exportSchema = false)
abstract class RoomHelper : RoomDatabase() {
    abstract fun memoDao(): MemoDao

    companion object {
        private var instance: RoomHelper? = null
        private val sLock = Any()

        @VisibleForTesting
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE Memo ADD COLUMN bookmark INTEGER NOT NULL DEFAULT 0;")
            }
        }

        fun getInstance(context: Context): RoomHelper {
            synchronized(sLock) {
                if (instance == null) {
                    instance =
                        Room.databaseBuilder(context.applicationContext, RoomHelper::class.java, "memo_db")
                            .addMigrations(MIGRATION_1_2)
                            .build()
                }
                return instance!!
            }
        }
    }
}