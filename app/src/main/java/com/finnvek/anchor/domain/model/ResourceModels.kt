package com.finnvek.anchor.domain.model

data class AnchorTag(
    val id: Long,
    val presetKey: String?,
    val customLabel: String?,
    val emoji: String?,
    val category: TagCategory,
    val isPreset: Boolean,
    val isHidden: Boolean,
    val sortOrder: Int,
)

data class ResourceTagSelection(
    val tagId: Long,
    val role: TagRole,
)

data class ResourceSummary(
    val id: Long,
    val type: ResourceType,
    val createdAtMs: Long,
    val title: String,
    val subtitle: String?,
    val note: String?,
    val favorite: Boolean,
    val timesUsed: Int,
    val lastUsedAtMs: Long?,
    val thumbnailPath: String?,
)

data class ResourceDetail(
    val id: Long,
    val type: ResourceType,
    val createdAtMs: Long,
    val title: String,
    val subtitle: String?,
    val uri: String?,
    val sourceApp: String?,
    val note: String?,
    val bodyFeel: String?,
    val favorite: Boolean,
    val timesUsed: Int,
    val lastUsedAtMs: Long?,
    val thumbnailPath: String?,
    val tags: List<AnchorTag>,
)

data class ResourceCapture(
    val type: ResourceType,
    val createdAtMs: Long,
    val title: String,
    val subtitle: String?,
    val uri: String?,
    val sourceApp: String?,
    val note: String?,
    val bodyFeel: String?,
    val thumbnailPath: String?,
    val tags: List<ResourceTagSelection>,
)

data class ResourceContentUpdate(
    val title: String,
    val subtitle: String?,
    val uri: String?,
    val sourceApp: String?,
    val note: String?,
    val bodyFeel: String?,
)

data class TimelineDay(
    val dayStartMs: Long,
    val resources: List<ResourceSummary>,
)
