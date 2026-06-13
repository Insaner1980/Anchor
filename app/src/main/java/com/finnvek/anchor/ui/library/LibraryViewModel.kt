package com.finnvek.anchor.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finnvek.anchor.domain.model.LibraryContentType
import com.finnvek.anchor.domain.model.LibraryViewMode
import com.finnvek.anchor.domain.model.ResourceSummary
import com.finnvek.anchor.domain.model.TimelineDay
import com.finnvek.anchor.repository.ResourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class LibraryUiState(
    val contentType: LibraryContentType = LibraryContentType.ALL,
    val viewMode: LibraryViewMode = LibraryViewMode.LIBRARY,
    val query: String = "",
    val favoriteOnly: Boolean = false,
    val resources: List<ResourceSummary> = emptyList(),
    val timelineDays: List<TimelineDay> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val resourceRepository: ResourceRepository,
) : ViewModel() {

    private val filters = MutableStateFlow(LibraryFilters())

    val uiState: StateFlow<LibraryUiState> =
        combine(
            filters.flatMapLatest { current ->
                resourceRepository.observeLibraryResources(
                    contentType = current.contentType,
                    query = current.query,
                    favoriteOnly = current.favoriteOnly,
                    tagIds = emptySet(),
                )
            },
            resourceRepository.observeTimeline(),
            filters,
        ) { resources, timelineDays, current ->
            LibraryUiState(
                contentType = current.contentType,
                viewMode = current.viewMode,
                query = current.query,
                favoriteOnly = current.favoriteOnly,
                resources = resources,
                timelineDays = timelineDays,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MS),
            initialValue = LibraryUiState(),
        )

    fun selectContentType(contentType: LibraryContentType) {
        filters.update { current -> current.copy(contentType = contentType) }
    }

    fun selectViewMode(viewMode: LibraryViewMode) {
        filters.update { current -> current.copy(viewMode = viewMode) }
    }

    fun updateQuery(query: String) {
        filters.update { current -> current.copy(query = query) }
    }

    fun toggleFavoriteOnly() {
        filters.update { current -> current.copy(favoriteOnly = !current.favoriteOnly) }
    }

    private data class LibraryFilters(
        val contentType: LibraryContentType = LibraryContentType.ALL,
        val viewMode: LibraryViewMode = LibraryViewMode.LIBRARY,
        val query: String = "",
        val favoriteOnly: Boolean = false,
    )

    private companion object {
        private const val STATE_STOP_TIMEOUT_MS = 5_000L
    }
}
