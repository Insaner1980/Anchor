package com.finnvek.anchor.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finnvek.anchor.domain.model.ThemeMode
import com.finnvek.anchor.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AnchorAppViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> =
        settingsRepository.themeMode.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MS),
            initialValue = ThemeMode.SYSTEM,
        )

    val onboardingDone: StateFlow<Boolean> =
        settingsRepository.onboardingDone.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MS),
            initialValue = false,
        )

    private companion object {
        private const val STATE_STOP_TIMEOUT_MS = 5_000L
    }
}
