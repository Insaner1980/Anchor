package com.finnvek.anchor.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.finnvek.anchor.R
import com.finnvek.anchor.domain.model.ResourceContentUpdate
import com.finnvek.anchor.domain.model.ResourceDetail
import com.finnvek.anchor.repository.ResourceRepository
import com.finnvek.anchor.ui.navigation.AnchorRouteArgument
import com.finnvek.anchor.ui.theme.LocalAnchorSpacing
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ResourceDetailEvent {
    data object Deleted : ResourceDetailEvent
}

@HiltViewModel
class ResourceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resourceRepository: ResourceRepository,
) : ViewModel() {

    private val resourceId: Long =
        checkNotNull(savedStateHandle.get<String>(AnchorRouteArgument.ResourceId)).toLong()
    private val eventFlow = MutableSharedFlow<ResourceDetailEvent>()

    val events: SharedFlow<ResourceDetailEvent> = eventFlow

    val resource: StateFlow<ResourceDetail?> =
        resourceRepository.observeResourceDetail(resourceId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MS),
            initialValue = null,
        )

    fun updateContent(update: ResourceContentUpdate) {
        viewModelScope.launch {
            resourceRepository.updateContent(resourceId, update)
        }
    }

    fun setFavorite(favorite: Boolean) {
        viewModelScope.launch {
            resourceRepository.setFavorite(resourceId, favorite)
        }
    }

    fun delete() {
        viewModelScope.launch {
            resourceRepository.delete(resourceId)
            eventFlow.emit(ResourceDetailEvent.Deleted)
        }
    }

    private companion object {
        private const val STATE_STOP_TIMEOUT_MS = 5_000L
    }
}

@Composable
fun ResourceDetailRoute(
    onDeleted: () -> Unit,
    viewModel: ResourceDetailViewModel = hiltViewModel(),
) {
    val resource by viewModel.resource.collectAsStateWithLifecycle()
    val currentOnDeleted by rememberUpdatedState(onDeleted)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ResourceDetailEvent.Deleted -> currentOnDeleted()
            }
        }
    }

    ResourceDetailScreen(
        resource = resource,
        onSave = viewModel::updateContent,
        onFavoriteChange = viewModel::setFavorite,
        onDelete = viewModel::delete,
    )
}

@Composable
private fun ResourceDetailScreen(
    resource: ResourceDetail?,
    onSave: (ResourceContentUpdate) -> Unit,
    onFavoriteChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    val spacing = LocalAnchorSpacing.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var title by remember(resource?.id) { mutableStateOf(resource?.title.orEmpty()) }
    var subtitle by remember(resource?.id) { mutableStateOf(resource?.subtitle.orEmpty()) }
    var note by remember(resource?.id) { mutableStateOf(resource?.note.orEmpty()) }
    var bodyFeel by remember(resource?.id) { mutableStateOf(resource?.bodyFeel.orEmpty()) }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        Text(
            text = resource?.title ?: stringResource(R.string.screen_resource_detail),
            style = MaterialTheme.typography.headlineSmall,
        )
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.capture_title_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = subtitle,
            onValueChange = { subtitle = it },
            label = { Text(stringResource(R.string.capture_subtitle_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text(stringResource(R.string.capture_note_label)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = DETAIL_TEXT_MIN_LINES,
        )
        OutlinedTextField(
            value = bodyFeel,
            onValueChange = { bodyFeel = it },
            label = { Text(stringResource(R.string.capture_body_feel_label)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = DETAIL_TEXT_MIN_LINES,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = resource?.favorite == true,
                onCheckedChange = onFavoriteChange,
            )
            Text(stringResource(R.string.detail_favorite))
        }
        Button(
            onClick = {
                resource?.let {
                    onSave(
                        ResourceContentUpdate(
                            title = title,
                            subtitle = subtitle.ifBlank { null },
                            uri = it.uri,
                            sourceApp = it.sourceApp,
                            note = note.ifBlank { null },
                            bodyFeel = bodyFeel.ifBlank { null },
                        ),
                    )
                }
            },
            enabled = resource != null && title.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.detail_save_changes))
        }
        TextButton(
            onClick = { showDeleteDialog = true },
            enabled = resource != null,
        ) {
            Text(stringResource(R.string.detail_delete))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.detail_delete_title)) },
            text = { Text(stringResource(R.string.detail_delete_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                ) {
                    Text(stringResource(R.string.detail_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.detail_cancel))
                }
            },
        )
    }
}

private const val DETAIL_TEXT_MIN_LINES = 2
