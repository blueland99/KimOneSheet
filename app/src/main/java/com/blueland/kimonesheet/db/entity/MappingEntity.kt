package com.blueland.kimonesheet.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Mapping")
data class MappingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo(name = "parent_id") val parentId: Long,
    @ColumnInfo(name = "child_id") val child_id: Long,
    @ColumnInfo(defaultValue = "0") var type: Long,
    @ColumnInfo(defaultValue = "0") var depth: Long
)