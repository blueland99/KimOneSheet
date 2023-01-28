package com.blueland.kimonesheet.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.blueland.kimonesheet.db.dao.FolderDao
import com.blueland.kimonesheet.db.dao.MappingDao
import com.blueland.kimonesheet.db.dao.MemoDao
import com.blueland.kimonesheet.db.entity.FolderEntity
import com.blueland.kimonesheet.db.entity.MappingEntity
import com.blueland.kimonesheet.db.entity.MemoEntity

@Database(entities = [MemoEntity::class, MappingEntity::class, FolderEntity::class], version = 2, exportSchema = false)
abstract class RoomHelper : RoomDatabase() {
    abstract fun memoDao(): MemoDao
    abstract fun folderDao(): FolderDao
    abstract fun mappingDao(): MappingDao

    companion object {
        private var instance: RoomHelper? = null
        private val sLock = Any()

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE 'Memo' ADD COLUMN bookmark INTEGER NOT NULL DEFAULT 0")
                database.execSQL(
                    "CREATE TABLE 'Folder' (" +
                            "'id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "'name' TEXT NOT NULL, " +
                            "'reg_date' INTEGER NOT NULL, " +
                            "'mod_date' INTEGER NOT NULL, " +
                            "'bookmark' INTEGER NOT NULL DEFAULT 0" +
                            ")"
                )
                database.execSQL(
                    "CREATE TABLE 'Mapping' (" +
                            "'id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                            "'parent_id' INTEGER NOT NULL, " +
                            "'child_id' INTEGER NOT NULL, " +
                            "'type' INTEGER NOT NULL DEFAULT 0" +
                            ")"
                )
                database.execSQL(
                    "INSERT INTO Mapping (parent_id, child_id, type) SELECT -1 AS parent_id, id AS child_id, 1 AS type FROM Memo"
                )
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