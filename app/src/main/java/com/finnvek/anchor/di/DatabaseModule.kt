package com.finnvek.anchor.di

import android.content.Context
import androidx.room.Room
import com.finnvek.anchor.data.local.ANCHOR_DATABASE_NAME
import com.finnvek.anchor.data.local.AnchorDatabase
import com.finnvek.anchor.data.local.dao.ResourceDao
import com.finnvek.anchor.data.local.dao.TagDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAnchorDatabase(
        @ApplicationContext context: Context,
    ): AnchorDatabase =
        Room.databaseBuilder(
            context,
            AnchorDatabase::class.java,
            ANCHOR_DATABASE_NAME,
        ).build()

    @Provides
    fun provideResourceDao(database: AnchorDatabase): ResourceDao = database.resourceDao()

    @Provides
    fun provideTagDao(database: AnchorDatabase): TagDao = database.tagDao()
}
