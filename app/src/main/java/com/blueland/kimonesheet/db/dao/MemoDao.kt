package com.blueland.kimonesheet.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.blueland.kimonesheet.base.BaseDao
import com.blueland.kimonesheet.db.entity.MemoEntity

@Dao
interface MemoDao : BaseDao<MemoEntity> {
    @Query("SELECT * FROM Memo WHERE id = :id")
    fun select(id: Int): List<MemoEntity>

    @Query("SELECT * FROM Memo WHERE title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%' ORDER BY mod_date DESC")
    fun select(keyword: String): List<MemoEntity>

    @Query("SELECT id FROM Memo ORDER BY id DESC LIMIT 1")
    fun getLastId(): List<Int>

    @Query("UPDATE Memo SET bookmark = :bookmarked WHERE id = :id")
    fun updateBookmark(id: Int, bookmarked: Boolean)

    @Query("DELETE FROM Memo WHERE id = :id")
    fun delete(id: Int)
}