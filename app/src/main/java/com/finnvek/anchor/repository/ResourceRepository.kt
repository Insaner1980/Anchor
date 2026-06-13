package com.finnvek.anchor.repository

import com.finnvek.anchor.domain.model.LibraryContentType
import com.finnvek.anchor.domain.model.ResourceCapture
import com.finnvek.anchor.domain.model.ResourceContentUpdate
import com.finnvek.anchor.domain.model.ResourceDetail
import com.finnvek.anchor.domain.model.ResourceSummary
import com.finnvek.anchor.domain.model.ResourceTagSelection
import com.finnvek.anchor.domain.model.TimelineDay
import kotlinx.coroutines.flow.Flow

interface ResourceRepository {
    fun observeHomeResources(): Flow<List<ResourceSummary>>

    fun observeRecentResources(): Flow<List<ResourceSummary>>

    fun observeLibraryResources(
        contentType: LibraryContentType,
        query: String,
        favoriteOnly: Boolean,
        tagIds: Set<Long>,
    ): Flow<List<ResourceSummary>>

    fun observeResourceDetail(resourceId: Long): Flow<ResourceDetail?>

    fun observeHelpResults(tagId: Long): Flow<List<ResourceSummary>>

    fun observeTimeline(): Flow<List<TimelineDay>>

    suspend fun saveCapture(capture: ResourceCapture): Long

    suspend fun updateContent(
        resourceId: Long,
        update: ResourceContentUpdate,
    )

    suspend fun replaceTags(
        resourceId: Long,
        tags: List<ResourceTagSelection>,
    )

    suspend fun setFavorite(
        resourceId: Long,
        favorite: Boolean,
    )

    suspend fun incrementUsage(
        resourceId: Long,
        usedAtMs: Long,
    )

    suspend fun delete(resourceId: Long)
}
