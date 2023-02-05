package com.blueland.kimonesheet.db.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Query

@Dao
interface MappingDao {
    @Query(
        "SELECT Mapping.id, Memo.title, Memo.content, Folder.name, Memo.reg_date, Memo.mod_date, Memo.bookmark, parent_id, child_id, type " +
                "FROM Mapping " +
                "LEFT JOIN Folder ON Mapping.child_id = Folder.id AND Mapping.type = '0' " +
                "LEFT JOIN Memo ON Mapping.child_id = Memo.id AND Mapping.type = '1' " +
                "WHERE Mapping.parent_id = :parentId " +
                "ORDER BY type ASC, Memo.bookmark DESC, Memo.mod_date DESC"
    )
    fun select(parentId: Int): List<MappingDto>

    @Query(
        "SELECT Mapping.id, Memo.title, Memo.content, NULL AS name, Memo.reg_date, Memo.mod_date, Memo.bookmark, parent_id, child_id, type " +
                "FROM Mapping " +
                "INNER JOIN Memo ON Mapping.child_id = Memo.id AND Mapping.type = '1' " +
                "WHERE title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%' " +
                "ORDER BY Memo.bookmark DESC, Memo.mod_date DESC"
    )
    fun select(keyword: String): List<MappingDto>

    @Query("INSERT INTO Mapping (parent_id, child_id, type) VALUES (:parentId, :childId, 1)")
    fun insertMemo(parentId: Int, childId: Int)

    @Query("INSERT INTO Mapping (parent_id, child_id, type) VALUES (:parentId, :childId, 0)")
    fun insertFolder(parentId: Int, childId: Int)

    @Query("DELETE FROM Mapping WHERE id = :id")
    fun deleteMapping(id: Int)

    @Query(
        "SELECT * " +
                "FROM Mapping " +
                "LEFT JOIN Folder ON Mapping.child_id = Folder.id " +
                "LEFT JOIN Memo ON Mapping.child_id = Memo.id " +
                "WHERE Mapping.parent_id = :parentId " +
                "ORDER BY type ASC, Memo.bookmark DESC, Memo.mod_date DESC"
    )
    fun updateMapping(parentId: Int): List<MappingDto>

    @Query("UPDATE Mapping SET parent_id = :newParentId WHERE parent_id = :parentId")
    fun updateMapping(parentId: Int, newParentId: Int)
}

data class MappingDto(
    @ColumnInfo(name = "id")
    val mappingId: Int,
    @ColumnInfo(name = "name")
    val folder: String? = null,
    val title: String? = null,
    val content: String? = null,
    @ColumnInfo(name = "reg_date") val regDate: Long?,
    @ColumnInfo(name = "mod_date") val modDate: Long?,
    var bookmark: Boolean,
    @ColumnInfo(name = "parent_id") val parentId: Int,
    @ColumnInfo(name = "child_id") val childId: Int,
    val type: Int
)