package com.finnvek.anchor.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finnvek.anchor.domain.model.ResourceSummary
import com.finnvek.anchor.repository.ResourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val recentResources: List<ResourceSummary> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    resourceRepository: ResourceRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        resourceRepository
            .observeRecentResources()
            .map { resources -> HomeUiState(recentResources = resources) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MS),
                initialValue = HomeUiState(),
            )

    private companion object {
        private const val STATE_STOP_TIMEOUT_MS = 5_000L
    }
}
