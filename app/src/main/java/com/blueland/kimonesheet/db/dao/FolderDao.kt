package com.blueland.kimonesheet.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.blueland.kimonesheet.base.BaseDao
import com.blueland.kimonesheet.db.entity.FolderEntity

@Dao
interface FolderDao : BaseDao<FolderEntity> {
    @Insert
    fun insert(folder: FolderEntity): Long

//    @Query("SELECT id FROM Folder ORDER BY id DESC LIMIT 1")
//    fun getLastId(): List<Int>

    @Query("DELETE FROM Folder WHERE id = :id")
    fun delete(id: Long)

    @Query("UPDATE Folder SET name = :name WHERE id = :id")
    fun updateFolder(name: String, id: Long)
}