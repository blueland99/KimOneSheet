package com.blueland.kimonesheet.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.blueland.kimonesheet.base.BaseDao
import com.blueland.kimonesheet.db.entity.MemoEntity

@Dao
interface MemoDao : BaseDao<MemoEntity> {
    @Query("SELECT * FROM Memo WHERE id = :id")
    fun select(id: Long): List<MemoEntity>

    @Query("SELECT * FROM Memo ORDER BY mod_date DESC")
    fun selectAll(): List<MemoEntity>

    @Query("SELECT * FROM Memo WHERE title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%' ORDER BY mod_date DESC")
    fun select(keyword: String): List<MemoEntity>

    @Query("UPDATE Memo SET bookmark = :bookmarked WHERE id = :id")
    fun updateBookmark(id: Long, bookmarked: Boolean)

    @Query("SELECT id FROM Memo ORDER BY id DESC LIMIT 1")
    fun getLastId(): List<Long>

    @Query("DELETE FROM Memo WHERE id = :id")
    fun delete(id: Long)
}