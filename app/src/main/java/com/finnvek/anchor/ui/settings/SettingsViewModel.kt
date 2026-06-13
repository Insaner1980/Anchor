package com.finnvek.anchor.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finnvek.anchor.domain.model.ThemeMode
import com.finnvek.anchor.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val selectedAppLocale: String? = null,
    val onboardingDone: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> =
        combine(
            settingsRepository.themeMode,
            settingsRepository.selectedAppLocale,
            settingsRepository.onboardingDone,
            ::SettingsUiState,
        ).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MS),
            initialValue = SettingsUiState(),
        )

    fun selectThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(themeMode)
        }
    }

    fun selectAppLocale(localeTag: String?) {
        viewModelScope.launch {
            settingsRepository.updateSelectedAppLocale(localeTag)
        }
    }

    private companion object {
        private const val STATE_STOP_TIMEOUT_MS = 5_000L
    }
}
