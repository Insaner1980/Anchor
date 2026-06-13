package com.finnvek.anchor.data.repository

import com.finnvek.anchor.data.local.dao.TagDao
import com.finnvek.anchor.data.local.entity.TagEntity
import com.finnvek.anchor.di.IoDispatcher
import com.finnvek.anchor.domain.model.AnchorTag
import com.finnvek.anchor.domain.model.TagCategory
import com.finnvek.anchor.domain.model.TagRole
import com.finnvek.anchor.repository.TagRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DataTagRepository @Inject constructor(
    private val tagDao: TagDao,
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher,
) : TagRepository {

    override fun observeVisibleTags(): Flow<List<AnchorTag>> =
        tagDao.observeVisibleTags().map { tags -> tags.map { it.toDomain() } }

    override fun observeVisibleTags(category: TagCategory): Flow<List<AnchorTag>> =
        tagDao.observeVisibleTagsByCategory(category).map { tags -> tags.map { it.toDomain() } }

    override fun observeHelpNeedTags(): Flow<List<AnchorTag>> =
        tagDao
            .observeHelpNeedTags(
                category = TagCategory.NEED,
                role = TagRole.HELPS_WITH,
            )
            .map { tags -> tags.map { it.toDomain() } }

    override suspend fun createCustomTag(
        label: String,
        emoji: String?,
        category: TagCategory,
    ): Long =
        withContext(dispatcher) {
            tagDao.createCustomTag(
                TagEntity(
                    presetKey = null,
                    customLabel = label,
                    emoji = emoji,
                    category = category,
                    isPreset = false,
                    sortOrder = CUSTOM_TAG_SORT_ORDER,
                ),
            )
        }

    override suspend fun updateCustomTag(
        tagId: Long,
        label: String,
        emoji: String?,
        sortOrder: Int,
    ) {
        withContext(dispatcher) {
            tagDao.updateTag(
                TagEntity(
                    id = tagId,
                    presetKey = null,
                    customLabel = label,
                    emoji = emoji,
                    category = TagCategory.NEED,
                    isPreset = false,
                    sortOrder = sortOrder,
                ),
            )
        }
    }

    override suspend fun hidePreset(presetKey: String) {
        withContext(dispatcher) {
            tagDao.hidePreset(presetKey)
        }
    }

    override suspend fun deleteCustom(tagId: Long) {
        withContext(dispatcher) {
            tagDao.deleteCustom(tagId)
        }
    }

    private fun TagEntity.toDomain(): AnchorTag =
        AnchorTag(
            id = id,
            presetKey = presetKey,
            customLabel = customLabel,
            emoji = emoji,
            category = category,
            isPreset = isPreset,
            isHidden = isHidden,
            sortOrder = sortOrder,
        )

    private companion object {
        private const val CUSTOM_TAG_SORT_ORDER = 1_000
    }
}
