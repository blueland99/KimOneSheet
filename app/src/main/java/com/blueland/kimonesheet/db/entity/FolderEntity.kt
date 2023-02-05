package com.blueland.kimonesheet.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Folder")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    @ColumnInfo val name: String,
    @ColumnInfo(name = "reg_date") val regDate: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "mod_date") val modDate: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "0") var bookmark: Boolean = false
)