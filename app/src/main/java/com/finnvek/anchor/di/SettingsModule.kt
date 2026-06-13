package com.finnvek.anchor.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.finnvek.anchor.data.settings.ANCHOR_SETTINGS_DATASTORE_NAME
import com.finnvek.anchor.data.settings.AppCompatAppLocaleController
import com.finnvek.anchor.data.settings.AppLocaleController
import com.finnvek.anchor.data.settings.DataStoreSettingsRepository
import com.finnvek.anchor.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

@Qualifier
annotation class SettingsDataStore

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {

    @Provides
    @Singleton
    @SettingsDataStore
    fun provideSettingsDataStore(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { context.preferencesDataStoreFile(ANCHOR_SETTINGS_DATASTORE_NAME) },
        )

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @SettingsDataStore dataStore: DataStore<Preferences>,
    ): SettingsRepository = DataStoreSettingsRepository(dataStore)

    @Provides
    @Singleton
    fun provideAppLocaleController(controller: AppCompatAppLocaleController): AppLocaleController = controller
}
