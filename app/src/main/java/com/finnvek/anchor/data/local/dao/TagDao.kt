package com.finnvek.anchor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.finnvek.anchor.data.local.entity.TagEntity
import com.finnvek.anchor.domain.model.TagCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT * FROM tags WHERE is_hidden = 0 ORDER BY category DESC, sort_order ASC, id ASC")
    fun observeVisibleTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE is_hidden = 0 AND category = :category ORDER BY sort_order ASC, id ASC")
    fun observeVisibleTagsByCategory(category: TagCategory): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTags(tags: List<TagEntity>)

    @Insert
    suspend fun createCustomTag(tag: TagEntity): Long

    @Update
    suspend fun updateTag(tag: TagEntity)

    @Query("SELECT preset_key FROM tags WHERE is_preset = 1 AND preset_key IS NOT NULL")
    suspend fun getPresetKeys(): List<String>

    @Query("UPDATE tags SET is_hidden = 1 WHERE is_preset = 1 AND preset_key = :presetKey")
    suspend fun hidePreset(presetKey: String)

    @Query("DELETE FROM tags WHERE is_preset = 0 AND id = :tagId")
    suspend fun deleteCustom(tagId: Long)
}
