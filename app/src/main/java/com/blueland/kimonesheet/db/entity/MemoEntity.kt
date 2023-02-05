package com.blueland.kimonesheet.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "Memo")
data class MemoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    @ColumnInfo val title: String,
    @ColumnInfo val content: String,
    @ColumnInfo(name = "reg_date") val regDate: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "mod_date") val modDate: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "0") var bookmark: Boolean = false
) : Serializable