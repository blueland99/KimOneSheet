package com.blueland.kimonesheet.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "Memo")
data class MemoEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo val id: Long? = null,
    @ColumnInfo val title: String,
    @ColumnInfo val content: String,
    @ColumnInfo(name = "reg_date") val regDate: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "mod_date") val modDate: Long = System.currentTimeMillis()
) : Serializable