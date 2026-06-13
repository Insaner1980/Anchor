package com.finnvek.anchor.repository

import com.finnvek.anchor.domain.model.AnchorTag
import com.finnvek.anchor.domain.model.TagCategory
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun observeVisibleTags(): Flow<List<AnchorTag>>

    fun observeVisibleTags(category: TagCategory): Flow<List<AnchorTag>>

    fun observeHelpNeedTags(): Flow<List<AnchorTag>>

    suspend fun createCustomTag(
        label: String,
        emoji: String?,
        category: TagCategory,
    ): Long

    suspend fun updateCustomTag(
        tagId: Long,
        label: String,
        emoji: String?,
        sortOrder: Int,
    )

    suspend fun hidePreset(presetKey: String)

    suspend fun deleteCustom(tagId: Long)
}
