package com.finnvek.anchor

import android.app.Application
import com.finnvek.anchor.data.settings.AppLocaleController
import com.finnvek.anchor.data.local.seed.TagSeeder
import com.finnvek.anchor.di.ApplicationScope
import com.finnvek.anchor.di.MainDispatcher
import com.finnvek.anchor.repository.SettingsRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var tagSeeder: TagSeeder

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var appLocaleController: AppLocaleController

    @Inject
    @MainDispatcher
    lateinit var mainDispatcher: CoroutineDispatcher

    override fun onCreate() {
        super.onCreate()

        observeSelectedLocale()
        seedPresetTags()
    }

    private fun seedPresetTags() {
        applicationScope.launch {
            tagSeeder.seedIfNeeded()
        }
    }

    private fun observeSelectedLocale() {
        applicationScope.launch {
            settingsRepository.selectedAppLocale
                .distinctUntilChanged()
                .collect { localeTag ->
                    withContext(mainDispatcher) {
                        appLocaleController.applyLocale(localeTag)
                    }
                }
        }
    }
}
