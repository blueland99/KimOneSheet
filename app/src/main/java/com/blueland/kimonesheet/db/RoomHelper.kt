package com.blueland.kimonesheet.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.blueland.kimonesheet.db.dao.MemoDao
import com.blueland.kimonesheet.db.entity.MemoEntity

@Database(entities = [MemoEntity::class], version = 1, exportSchema = false)
abstract class RoomHelper: RoomDatabase() {
    abstract fun memoDao(): MemoDao
}