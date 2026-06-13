package com.finnvek.anchor.ui.tags

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.finnvek.anchor.R
import com.finnvek.anchor.domain.model.AnchorTag
import com.finnvek.anchor.domain.model.TagCategory
import com.finnvek.anchor.repository.TagRepository
import com.finnvek.anchor.ui.theme.LocalAnchorSpacing
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TagManagerUiState(
    val tags: List<AnchorTag> = emptyList(),
    val newLabel: String = "",
)

@HiltViewModel
class TagManagerViewModel @Inject constructor(
    private val tagRepository: TagRepository,
) : ViewModel() {

    private val newLabel = MutableStateFlow("")

    val uiState: StateFlow<TagManagerUiState> =
        combine(
            tagRepository.observeVisibleTags(),
            newLabel,
        ) { tags, label ->
            TagManagerUiState(tags = tags, newLabel = label)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MS),
            initialValue = TagManagerUiState(),
        )

    fun updateNewLabel(label: String) {
        newLabel.update { label }
    }

    fun createCustomTag() {
        val label = newLabel.value.trim()
        if (label.isBlank()) return

        viewModelScope.launch {
            tagRepository.createCustomTag(
                label = label,
                emoji = null,
                category = TagCategory.NEED,
            )
            newLabel.update { "" }
        }
    }

    fun hidePreset(tag: AnchorTag) {
        val presetKey = tag.presetKey ?: return
        viewModelScope.launch {
            tagRepository.hidePreset(presetKey)
        }
    }

    fun deleteCustom(tag: AnchorTag) {
        if (tag.isPreset) return
        viewModelScope.launch {
            tagRepository.deleteCustom(tag.id)
        }
    }

    private companion object {
        private const val STATE_STOP_TIMEOUT_MS = 5_000L
    }
}

@Composable
fun TagManagerRoute(
    viewModel: TagManagerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TagManagerScreen(
        uiState = uiState,
        onNewLabelChange = viewModel::updateNewLabel,
        onCreate = viewModel::createCustomTag,
        onHidePreset = viewModel::hidePreset,
        onDeleteCustom = viewModel::deleteCustom,
    )
}

@Composable
private fun TagManagerScreen(
    uiState: TagManagerUiState,
    onNewLabelChange: (String) -> Unit,
    onCreate: () -> Unit,
    onHidePreset: (AnchorTag) -> Unit,
    onDeleteCustom: (AnchorTag) -> Unit,
) {
    val spacing = LocalAnchorSpacing.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        item {
            Text(
                text = stringResource(R.string.screen_tag_manager),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.small),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = uiState.newLabel,
                    onValueChange = onNewLabelChange,
                    label = { Text(stringResource(R.string.tag_manager_new_label)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                Button(
                    onClick = onCreate,
                    enabled = uiState.newLabel.isNotBlank(),
                ) {
                    Text(stringResource(R.string.tag_manager_create))
                }
            }
        }
        items(uiState.tags, key = { tag -> tag.id }) { tag ->
            TagRow(
                tag = tag,
                onHidePreset = onHidePreset,
                onDeleteCustom = onDeleteCustom,
            )
        }
    }
}

@Composable
private fun TagRow(
    tag: AnchorTag,
    onHidePreset: (AnchorTag) -> Unit,
    onDeleteCustom: (AnchorTag) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = tag.localizedLabel(),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = tag.category.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (tag.isPreset) {
            TextButton(onClick = { onHidePreset(tag) }) {
                Text(stringResource(R.string.tag_manager_hide))
            }
        } else {
            TextButton(onClick = { onDeleteCustom(tag) }) {
                Text(stringResource(R.string.tag_manager_delete))
            }
        }
    }
}
