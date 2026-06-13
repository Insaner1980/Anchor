package com.finnvek.anchor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.finnvek.anchor.data.local.dao.ResourceDao
import com.finnvek.anchor.data.local.dao.TagDao
import com.finnvek.anchor.data.local.entity.ResourceEntity
import com.finnvek.anchor.data.local.entity.ResourceTagRefEntity
import com.finnvek.anchor.data.local.entity.TagEntity

const val ANCHOR_DATABASE_NAME = "anchor.db"

@Database(
    entities = [
        ResourceEntity::class,
        TagEntity::class,
        ResourceTagRefEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(AnchorTypeConverters::class)
abstract class AnchorDatabase : RoomDatabase() {
    abstract fun resourceDao(): ResourceDao

    abstract fun tagDao(): TagDao
}
