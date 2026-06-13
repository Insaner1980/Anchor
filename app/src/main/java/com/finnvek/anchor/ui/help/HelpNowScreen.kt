package com.finnvek.anchor.ui.help

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.finnvek.anchor.R
import com.finnvek.anchor.data.intent.ExternalSongOpener
import com.finnvek.anchor.domain.model.AnchorTag
import com.finnvek.anchor.domain.model.ResourceSummary
import com.finnvek.anchor.domain.model.ResourceType
import com.finnvek.anchor.repository.ResourceRepository
import com.finnvek.anchor.repository.TagRepository
import com.finnvek.anchor.ui.home.ResourceSummaryRow
import com.finnvek.anchor.ui.navigation.AnchorRouteArgument
import com.finnvek.anchor.ui.tags.localizedLabel
import com.finnvek.anchor.ui.theme.LocalAnchorSpacing
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HelpNeedPickerUiState(
    val needTags: List<AnchorTag> = emptyList(),
)

@HiltViewModel
class HelpNeedPickerViewModel @Inject constructor(
    tagRepository: TagRepository,
) : ViewModel() {

    val uiState: StateFlow<HelpNeedPickerUiState> =
        tagRepository
            .observeHelpNeedTags()
            .map { tags -> HelpNeedPickerUiState(needTags = tags) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MS),
                initialValue = HelpNeedPickerUiState(),
            )

    private companion object {
        private const val STATE_STOP_TIMEOUT_MS = 5_000L
    }
}

@Composable
fun HelpNeedPickerRoute(
    onNeedSelected: (Long) -> Unit,
    onClose: () -> Unit,
    viewModel: HelpNeedPickerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HelpNeedPickerScreen(
        uiState = uiState,
        onNeedSelected = onNeedSelected,
        onClose = onClose,
    )
}

@Composable
private fun HelpNeedPickerScreen(
    uiState: HelpNeedPickerUiState,
    onNeedSelected: (Long) -> Unit,
    onClose: () -> Unit,
) {
    val spacing = LocalAnchorSpacing.current

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.screen_help_need),
                style = MaterialTheme.typography.headlineSmall,
            )
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.help_close),
                )
            }
        }

        uiState.needTags.forEach { tag ->
            NeedCard(
                tag = tag,
                onClick = { onNeedSelected(tag.id) },
            )
        }
    }
}

@Composable
private fun NeedCard(
    tag: AnchorTag,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = LocalAnchorSpacing.current.helpNeedCardMinHeight)
                .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(
            text = tag.localizedLabel(),
            modifier = Modifier.padding(LocalAnchorSpacing.current.large),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

data class HelpResultsUiState(
    val resources: List<ResourceSummary> = emptyList(),
    val openFailed: Boolean = false,
)

sealed interface HelpResultsEvent {
    data class OpenPhoto(val resourceId: Long) : HelpResultsEvent
}

@HiltViewModel
class HelpResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resourceRepository: ResourceRepository,
    private val externalSongOpener: ExternalSongOpener,
) : ViewModel() {

    private val openFailed = MutableStateFlow(false)
    private val eventFlow = MutableSharedFlow<HelpResultsEvent>()
    private val tagId: Long =
        checkNotNull(savedStateHandle.get<String>(AnchorRouteArgument.TagId)).toLong()

    val events: SharedFlow<HelpResultsEvent> = eventFlow

    val uiState: StateFlow<HelpResultsUiState> =
        combine(
            resourceRepository.observeHelpResults(tagId),
            openFailed,
        ) { resources, failed ->
            HelpResultsUiState(
                resources = resources,
                openFailed = failed,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MS),
            initialValue = HelpResultsUiState(),
        )

    fun openResource(resource: ResourceSummary) {
        viewModelScope.launch {
            openFailed.update { false }
            resourceRepository.incrementUsage(
                resourceId = resource.id,
                usedAtMs = System.currentTimeMillis(),
            )

            when (resource.type) {
                ResourceType.PHOTO -> eventFlow.emit(HelpResultsEvent.OpenPhoto(resource.id))
                ResourceType.SONG -> openSong(resource)
            }
        }
    }

    private fun openSong(resource: ResourceSummary) {
        val opened = resource.uri?.let(externalSongOpener::open) == true
        if (!opened) {
            openFailed.update { true }
        }
    }

    private companion object {
        private const val STATE_STOP_TIMEOUT_MS = 5_000L
    }
}

@Composable
fun HelpResultsRoute(
    onOpenPhoto: (Long) -> Unit,
    viewModel: HelpResultsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnOpenPhoto by rememberUpdatedState(onOpenPhoto)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is HelpResultsEvent.OpenPhoto -> currentOnOpenPhoto(event.resourceId)
            }
        }
    }

    HelpResultsScreen(
        uiState = uiState,
        onResourceClick = viewModel::openResource,
    )
}

@Composable
private fun HelpResultsScreen(
    uiState: HelpResultsUiState,
    onResourceClick: (ResourceSummary) -> Unit,
) {
    val spacing = LocalAnchorSpacing.current

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        Text(
            text = stringResource(R.string.screen_help_results),
            style = MaterialTheme.typography.headlineSmall,
        )

        if (uiState.openFailed) {
            Text(
                text = stringResource(R.string.help_could_not_open),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        if (uiState.resources.isEmpty()) {
            Text(
                text = stringResource(R.string.help_results_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            uiState.resources.forEach { resource ->
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { onResourceClick(resource) },
                    shape = MaterialTheme.shapes.small,
                ) {
                    ResourceSummaryRow(
                        resource = resource,
                        modifier = Modifier.padding(spacing.medium),
                    )
                }
            }
        }
    }
}
