package com.finnvek.anchor.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.finnvek.anchor.domain.model.ResourceType

@Entity(tableName = "resources")
data class ResourceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "type")
    val type: ResourceType,
    @ColumnInfo(name = "created_at_ms")
    val createdAtMs: Long,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "subtitle")
    val subtitle: String?,
    @ColumnInfo(name = "uri")
    val uri: String?,
    @ColumnInfo(name = "source_app")
    val sourceApp: String?,
    @ColumnInfo(name = "note")
    val note: String?,
    @ColumnInfo(name = "body_feel")
    val bodyFeel: String?,
    @ColumnInfo(name = "favorite")
    val favorite: Boolean = false,
    @ColumnInfo(name = "times_used")
    val timesUsed: Int = 0,
    @ColumnInfo(name = "last_used_at_ms")
    val lastUsedAtMs: Long? = null,
    @ColumnInfo(name = "thumbnail_path")
    val thumbnailPath: String?,
)
