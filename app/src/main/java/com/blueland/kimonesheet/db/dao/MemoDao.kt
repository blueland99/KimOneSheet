package com.blueland.kimonesheet.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.blueland.kimonesheet.base.BaseDao
import com.blueland.kimonesheet.db.entity.MemoEntity

@Dao
interface MemoDao : BaseDao<MemoEntity> {
    @Query("SELECT * FROM Memo ORDER BY mod_date DESC")
    fun selectAll(): List<MemoEntity>

    @Query("SELECT * FROM Memo WHERE title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%' ORDER BY mod_date DESC")
    fun selectKeyword(keyword: String): List<MemoEntity>
}