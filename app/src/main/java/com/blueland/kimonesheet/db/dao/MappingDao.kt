package com.blueland.kimonesheet.db.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Query

@Dao
interface MappingDao {
    @Query(
        "SELECT Mapping.id, Memo.title, Memo.content, Folder.name, Memo.reg_date, Memo.mod_date, Memo.bookmark, parent_id, child_id, type " +
                "FROM Mapping " +
                "LEFT JOIN Folder ON Mapping.child_id = Folder.id " +
                "LEFT JOIN Memo ON Mapping.child_id = Memo.id " +
                "WHERE Mapping.parent_id = :parentId " +
                "ORDER BY type ASC, Memo.bookmark DESC, Memo.mod_date DESC"
    )
    fun select(parentId: Long): List<MappingDto>

    @Query(
        "SELECT Mapping.id, Memo.title, Memo.content, Memo.reg_date, Memo.mod_date, Memo.bookmark, parent_id, child_id, type " +
                "FROM Mapping " +
                "LEFT JOIN Memo ON Mapping.child_id = Memo.id " +
                "WHERE title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%' AND type = '1' " +
                "ORDER BY Memo.bookmark DESC, Memo.mod_date DESC"
    )
    fun select(keyword: String): List<MappingDto>

    @Query("INSERT INTO Mapping (parent_id, child_id, type) VALUES (:parentId, :childId, 1)")
    fun insertMemo(parentId: Long, childId: Long)

    @Query("INSERT INTO Mapping (parent_id, child_id, type) VALUES (:parentId, :childId, 0)")
    fun insertFolder(parentId: Long, childId: Long)

    @Query("DELETE FROM Mapping WHERE id = :id")
    fun delete(id: Long)

    @Query("SELECT * " +
            "FROM Mapping " +
            "LEFT JOIN Folder ON Mapping.child_id = Folder.id " +
            "LEFT JOIN Memo ON Mapping.child_id = Memo.id " +
            "WHERE Mapping.parent_id = :parentId " +
            "ORDER BY type ASC, Memo.bookmark DESC, Memo.mod_date DESC")
    fun updateMapping(parentId: Long): List<MappingDto>

    @Query("UPDATE Mapping SET parent_id = :newParentId WHERE parent_id = :parentId")
    fun updateMapping(parentId: Long, newParentId: Long)
}

data class MappingDto(
    @ColumnInfo(name = "id")
    val mappingId: Long,
    @ColumnInfo(name = "name")
    val folder: String? = null,
    val title: String? = null,
    val content: String? = null,
    @ColumnInfo(name = "reg_date") val regDate: Long?,
    @ColumnInfo(name = "mod_date") val modDate: Long?,
    var bookmark: Boolean,
    @ColumnInfo(name = "parent_id") val parentId: Long,
    @ColumnInfo(name = "child_id") val childId: Long,
    val type: Int
)