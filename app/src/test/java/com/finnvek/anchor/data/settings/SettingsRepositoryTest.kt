package com.finnvek.anchor.data.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.finnvek.anchor.domain.model.ThemeMode
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun settingsStartWithMvpDefaults() =
        runTest {
            val repository = createRepository("defaults.preferences_pb")

            assertEquals(ThemeMode.SYSTEM, repository.themeMode.first())
            assertFalse(repository.onboardingDone.first())
            assertNull(repository.selectedAppLocale.first())
        }

    @Test
    fun settingsPersistThemeOnboardingAndLanguageSelections() =
        runTest {
            val repository = createRepository("updates.preferences_pb")

            repository.updateThemeMode(ThemeMode.CALM)
            repository.updateOnboardingDone(true)
            repository.updateSelectedAppLocale(ANCHOR_LOCALE_FINNISH)

            assertEquals(ThemeMode.CALM, repository.themeMode.first())
            assertEquals(true, repository.onboardingDone.first())
            assertEquals(ANCHOR_LOCALE_FINNISH, repository.selectedAppLocale.first())

            repository.updateSelectedAppLocale(null)

            assertNull(repository.selectedAppLocale.first())
        }

    private fun TestScope.createRepository(fileName: String): DataStoreSettingsRepository {
        val dataStore =
            PreferenceDataStoreFactory.create(
                scope = backgroundScope,
                produceFile = { File(temporaryFolder.root, fileName) },
            )

        return DataStoreSettingsRepository(dataStore)
    }
}
