package com.blueland.kimonesheet.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.blueland.kimonesheet.base.BaseDao
import com.blueland.kimonesheet.db.entity.FolderEntity

@Dao
interface FolderDao : BaseDao<FolderEntity> {
    @Query("SELECT id FROM Folder ORDER BY id DESC LIMIT 1")
    fun getLastId(): List<Long>

    @Query("DELETE FROM Folder WHERE id = :id")
    fun delete(id: Long)
}