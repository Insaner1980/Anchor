package com.finnvek.anchor.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.finnvek.anchor.domain.model.TagRole

@Entity(
    tableName = "resource_tag_refs",
    primaryKeys = ["resource_id", "tag_id", "role"],
    foreignKeys = [
        ForeignKey(
            entity = ResourceEntity::class,
            parentColumns = ["id"],
            childColumns = ["resource_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["resource_id"]),
        Index(value = ["tag_id"]),
    ],
)
data class ResourceTagRefEntity(
    @ColumnInfo(name = "resource_id")
    val resourceId: Long,
    @ColumnInfo(name = "tag_id")
    val tagId: Long,
    @ColumnInfo(name = "role")
    val role: TagRole,
)
