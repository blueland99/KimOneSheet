package com.blueland.kimonesheet.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Mapping")
data class MappingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "parent_id") val parentId: Int,
    @ColumnInfo(name = "child_id") val child_id: Int,
    @ColumnInfo(defaultValue = "0") var type: Int
)