package com.finnvek.anchor.repository

import com.finnvek.anchor.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val themeMode: Flow<ThemeMode>
    val onboardingDone: Flow<Boolean>
    val selectedAppLocale: Flow<String?>

    suspend fun updateThemeMode(themeMode: ThemeMode)

    suspend fun updateOnboardingDone(done: Boolean)

    suspend fun updateSelectedAppLocale(localeTag: String?)
}
