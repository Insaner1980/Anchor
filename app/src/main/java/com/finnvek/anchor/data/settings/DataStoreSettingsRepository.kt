package com.finnvek.anchor.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.finnvek.anchor.domain.model.ThemeMode
import com.finnvek.anchor.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

const val ANCHOR_SETTINGS_DATASTORE_NAME = "anchor_settings"

class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override val themeMode: Flow<ThemeMode> =
        dataStore.data
            .map { preferences ->
                preferences[SettingsPreferenceKeys.ThemeMode]
                    ?.let { storedValue -> ThemeMode.entries.firstOrNull { it.name == storedValue } }
                    ?: ThemeMode.SYSTEM
            }
            .distinctUntilChanged()

    override val onboardingDone: Flow<Boolean> =
        dataStore.data
            .map { preferences -> preferences[SettingsPreferenceKeys.OnboardingDone] ?: false }
            .distinctUntilChanged()

    override val selectedAppLocale: Flow<String?> =
        dataStore.data
            .map { preferences -> preferences[SettingsPreferenceKeys.SelectedAppLocale] }
            .distinctUntilChanged()

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[SettingsPreferenceKeys.ThemeMode] = themeMode.name
        }
    }

    override suspend fun updateOnboardingDone(done: Boolean) {
        dataStore.edit { preferences ->
            preferences[SettingsPreferenceKeys.OnboardingDone] = done
        }
    }

    override suspend fun updateSelectedAppLocale(localeTag: String?) {
        dataStore.edit { preferences ->
            if (localeTag.isNullOrBlank()) {
                preferences.remove(SettingsPreferenceKeys.SelectedAppLocale)
            } else {
                preferences[SettingsPreferenceKeys.SelectedAppLocale] = localeTag
            }
        }
    }
}

private object SettingsPreferenceKeys {
    val ThemeMode = stringPreferencesKey("theme_mode")
    val OnboardingDone = booleanPreferencesKey("onboarding_done")
    val SelectedAppLocale = stringPreferencesKey("selected_app_locale")
}
