package com.finnvek.anchor.di

import android.content.Context
import com.finnvek.anchor.data.photo.AndroidPhotoThumbnailer
import com.finnvek.anchor.data.photo.FilePhotoStorage
import com.finnvek.anchor.data.photo.PhotoStorage
import com.finnvek.anchor.data.photo.PhotoStorageRoots
import com.finnvek.anchor.data.photo.PhotoThumbnailer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher

@Module
@InstallIn(SingletonComponent::class)
object PhotoStorageModule {

    @Provides
    @Singleton
    fun providePhotoThumbnailer(thumbnailer: AndroidPhotoThumbnailer): PhotoThumbnailer = thumbnailer

    @Provides
    @Singleton
    fun providePhotoStorage(
        @ApplicationContext context: Context,
        thumbnailer: PhotoThumbnailer,
        @IoDispatcher dispatcher: CoroutineDispatcher,
    ): PhotoStorage =
        FilePhotoStorage(
            roots = PhotoStorageRoots(
                filesDir = context.filesDir,
                cacheDir = context.cacheDir,
            ),
            thumbnailer = thumbnailer,
            dispatcher = dispatcher,
        )
}
