package com.finnvek.anchor.di

import com.finnvek.anchor.data.repository.DataResourceRepository
import com.finnvek.anchor.data.repository.DataTagRepository
import com.finnvek.anchor.repository.ResourceRepository
import com.finnvek.anchor.repository.TagRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindResourceRepository(repository: DataResourceRepository): ResourceRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(repository: DataTagRepository): TagRepository
}
