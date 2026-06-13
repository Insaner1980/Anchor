package com.finnvek.anchor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.finnvek.anchor.data.local.entity.ResourceEntity
import com.finnvek.anchor.data.local.entity.ResourceTagRefEntity
import com.finnvek.anchor.domain.model.ResourceType
import com.finnvek.anchor.domain.model.TagRole
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ResourceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertResource(resource: ResourceEntity): Long

    @Update
    abstract suspend fun updateResource(resource: ResourceEntity)

    @Query("SELECT * FROM resources WHERE id = :resourceId")
    abstract fun observeResource(resourceId: Long): Flow<ResourceEntity?>

    @Query("SELECT * FROM resources WHERE id = :resourceId")
    abstract suspend fun getResource(resourceId: Long): ResourceEntity?

    @Query("SELECT * FROM resources ORDER BY created_at_ms DESC LIMIT :limit")
    abstract fun observeHomeResources(limit: Int): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources ORDER BY created_at_ms DESC LIMIT :limit")
    abstract fun observeRecentResources(limit: Int): Flow<List<ResourceEntity>>

    @Query(
        """
        SELECT * FROM resources
        WHERE (:type IS NULL OR type = :type)
        AND (:query = '' OR title LIKE '%' || :query || '%' OR subtitle LIKE '%' || :query || '%'
            OR note LIKE '%' || :query || '%' OR body_feel LIKE '%' || :query || '%')
        ORDER BY created_at_ms DESC
        """,
    )
    abstract fun observeLibraryResources(
        type: ResourceType?,
        query: String,
    ): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources ORDER BY created_at_ms DESC")
    abstract fun observeTimelineResources(): Flow<List<ResourceEntity>>

    @Query(
        """
        SELECT resources.* FROM resources
        INNER JOIN resource_tag_refs refs ON refs.resource_id = resources.id
        WHERE refs.tag_id = :tagId AND refs.role = :role
        ORDER BY favorite DESC, times_used DESC, COALESCE(last_used_at_ms, 0) DESC, created_at_ms DESC
        """,
    )
    abstract fun observeHelpResults(
        tagId: Long,
        role: TagRole,
    ): Flow<List<ResourceEntity>>

    @Query("UPDATE resources SET favorite = :favorite WHERE id = :resourceId")
    abstract suspend fun setFavorite(
        resourceId: Long,
        favorite: Boolean,
    )

    @Query("UPDATE resources SET times_used = times_used + 1, last_used_at_ms = :usedAtMs WHERE id = :resourceId")
    abstract suspend fun incrementUsage(
        resourceId: Long,
        usedAtMs: Long,
    )

    @Query("DELETE FROM resources WHERE id = :resourceId")
    abstract suspend fun deleteResource(resourceId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertTagRefs(refs: List<ResourceTagRefEntity>)

    @Query("DELETE FROM resource_tag_refs WHERE resource_id = :resourceId")
    protected abstract suspend fun deleteTagRefs(resourceId: Long)

    @Transaction
    open suspend fun replaceTagRefs(
        resourceId: Long,
        refs: List<ResourceTagRefEntity>,
    ) {
        deleteTagRefs(resourceId)
        if (refs.isNotEmpty()) {
            insertTagRefs(refs)
        }
    }
}
