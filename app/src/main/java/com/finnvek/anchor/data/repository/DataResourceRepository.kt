package com.finnvek.anchor.data.repository

import androidx.room.withTransaction
import com.finnvek.anchor.data.local.AnchorDatabase
import com.finnvek.anchor.data.local.dao.ResourceDao
import com.finnvek.anchor.data.local.entity.ResourceEntity
import com.finnvek.anchor.data.local.entity.ResourceTagRefEntity
import com.finnvek.anchor.data.photo.PhotoStorage
import com.finnvek.anchor.data.photo.StoredPhoto
import com.finnvek.anchor.di.IoDispatcher
import com.finnvek.anchor.domain.model.AnchorTag
import com.finnvek.anchor.domain.model.LibraryContentType
import com.finnvek.anchor.domain.model.ResourceCapture
import com.finnvek.anchor.domain.model.ResourceContentUpdate
import com.finnvek.anchor.domain.model.ResourceDetail
import com.finnvek.anchor.domain.model.ResourceSummary
import com.finnvek.anchor.domain.model.ResourceTagSelection
import com.finnvek.anchor.domain.model.ResourceType
import com.finnvek.anchor.domain.model.TagRole
import com.finnvek.anchor.domain.model.TimelineDay
import com.finnvek.anchor.repository.ResourceRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DataResourceRepository @Inject constructor(
    private val database: AnchorDatabase,
    private val resourceDao: ResourceDao,
    private val photoStorage: PhotoStorage,
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher,
) : ResourceRepository {

    override fun observeHomeResources(): Flow<List<ResourceSummary>> =
        resourceDao.observeHomeResources(HOME_LIMIT).map { resources -> resources.map { it.toSummary() } }

    override fun observeRecentResources(): Flow<List<ResourceSummary>> =
        resourceDao.observeRecentResources(RECENT_LIMIT).map { resources -> resources.map { it.toSummary() } }

    override fun observeLibraryResources(
        contentType: LibraryContentType,
        query: String,
        favoriteOnly: Boolean,
        tagIds: Set<Long>,
    ): Flow<List<ResourceSummary>> =
        resourceDao
            .observeLibraryResources(
                type = contentType.toResourceTypeOrNull(),
                query = query.trim(),
            )
            .map { resources ->
                resources
                    .filter { resource -> !favoriteOnly || resource.favorite }
                    .map { resource -> resource.toSummary() }
            }

    override fun observeResourceDetail(resourceId: Long): Flow<ResourceDetail?> =
        resourceDao.observeResource(resourceId).map { resource -> resource?.toDetail(tags = emptyList()) }

    override fun observeHelpResults(tagId: Long): Flow<List<ResourceSummary>> =
        resourceDao
            .observeHelpResults(tagId = tagId, role = TagRole.HELPS_WITH)
            .map { resources -> resources.map { it.toSummary() } }

    override fun observeTimeline(): Flow<List<TimelineDay>> =
        resourceDao.observeTimelineResources().map { resources ->
            resources
                .map { resource -> resource.toSummary() }
                .groupBy { resource -> resource.createdAtMs.dayStartMs() }
                .map { (dayStartMs, dayResources) ->
                    TimelineDay(dayStartMs = dayStartMs, resources = dayResources)
                }
        }

    override suspend fun saveCapture(capture: ResourceCapture): Long =
        withContext(dispatcher) {
            database.withTransaction {
                val resourceId = resourceDao.upsertResource(capture.toEntity())
                resourceDao.replaceTagRefs(resourceId, capture.tags.toRefEntities(resourceId))
                resourceId
            }
        }

    override suspend fun updateContent(
        resourceId: Long,
        update: ResourceContentUpdate,
    ) {
        withContext(dispatcher) {
            val existing = resourceDao.getResource(resourceId) ?: return@withContext
            resourceDao.updateResource(
                existing.copy(
                    title = update.title,
                    subtitle = update.subtitle,
                    uri = update.uri,
                    sourceApp = update.sourceApp,
                    note = update.note,
                    bodyFeel = update.bodyFeel,
                ),
            )
        }
    }

    override suspend fun replaceTags(
        resourceId: Long,
        tags: List<ResourceTagSelection>,
    ) {
        withContext(dispatcher) {
            resourceDao.replaceTagRefs(resourceId, tags.toRefEntities(resourceId))
        }
    }

    override suspend fun setFavorite(
        resourceId: Long,
        favorite: Boolean,
    ) {
        withContext(dispatcher) {
            resourceDao.setFavorite(resourceId, favorite)
        }
    }

    override suspend fun incrementUsage(
        resourceId: Long,
        usedAtMs: Long,
    ) {
        withContext(dispatcher) {
            resourceDao.incrementUsage(resourceId, usedAtMs)
        }
    }

    override suspend fun delete(resourceId: Long) {
        withContext(dispatcher) {
            val resource = resourceDao.getResource(resourceId)
            if (resource?.type == ResourceType.PHOTO && resource.uri != null && resource.thumbnailPath != null) {
                photoStorage.deletePhoto(
                    StoredPhoto(
                        photoPath = resource.uri,
                        thumbnailPath = resource.thumbnailPath,
                    ),
                )
            }
            resourceDao.deleteResource(resourceId)
        }
    }

    private fun ResourceCapture.toEntity(): ResourceEntity =
        ResourceEntity(
            type = type,
            createdAtMs = createdAtMs,
            title = title,
            subtitle = subtitle,
            uri = uri,
            sourceApp = sourceApp,
            note = note,
            bodyFeel = bodyFeel,
            thumbnailPath = thumbnailPath,
        )

    private fun ResourceEntity.toSummary(): ResourceSummary =
        ResourceSummary(
            id = id,
            type = type,
            createdAtMs = createdAtMs,
            title = title,
            subtitle = subtitle,
            uri = uri,
            note = note,
            favorite = favorite,
            timesUsed = timesUsed,
            lastUsedAtMs = lastUsedAtMs,
            thumbnailPath = thumbnailPath,
        )

    private fun ResourceEntity.toDetail(tags: List<AnchorTag>): ResourceDetail =
        ResourceDetail(
            id = id,
            type = type,
            createdAtMs = createdAtMs,
            title = title,
            subtitle = subtitle,
            uri = uri,
            sourceApp = sourceApp,
            note = note,
            bodyFeel = bodyFeel,
            favorite = favorite,
            timesUsed = timesUsed,
            lastUsedAtMs = lastUsedAtMs,
            thumbnailPath = thumbnailPath,
            tags = tags,
        )

    private fun List<ResourceTagSelection>.toRefEntities(resourceId: Long): List<ResourceTagRefEntity> =
        map { selection ->
            ResourceTagRefEntity(
                resourceId = resourceId,
                tagId = selection.tagId,
                role = selection.role,
            )
        }

    private fun LibraryContentType.toResourceTypeOrNull(): ResourceType? =
        when (this) {
            LibraryContentType.ALL -> null
            LibraryContentType.SONGS -> ResourceType.SONG
            LibraryContentType.PHOTOS -> ResourceType.PHOTO
        }

    private fun Long.dayStartMs(): Long =
        this - floorMod(DAY_IN_MILLIS)

    private fun Long.floorMod(divisor: Long): Long =
        ((this % divisor) + divisor) % divisor

    private companion object {
        private const val HOME_LIMIT = 8
        private const val RECENT_LIMIT = 20
        private const val DAY_IN_MILLIS = 86_400_000L
    }
}
