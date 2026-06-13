package com.finnvek.anchor.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.finnvek.anchor.domain.model.TagCategory

@Entity(
    tableName = "tags",
    indices = [Index(value = ["preset_key"], unique = true)],
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "preset_key")
    val presetKey: String?,
    @ColumnInfo(name = "custom_label")
    val customLabel: String?,
    @ColumnInfo(name = "emoji")
    val emoji: String?,
    @ColumnInfo(name = "category")
    val category: TagCategory,
    @ColumnInfo(name = "is_preset")
    val isPreset: Boolean,
    @ColumnInfo(name = "is_hidden")
    val isHidden: Boolean = false,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
)
