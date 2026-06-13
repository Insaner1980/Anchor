package com.finnvek.anchor.ui.capture

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finnvek.anchor.R
import com.finnvek.anchor.domain.model.AnchorTag
import com.finnvek.anchor.domain.model.ResourceType
import com.finnvek.anchor.ui.theme.LocalAnchorSpacing

@Composable
fun CaptureRoute(
    initialDraft: CaptureInitialDraft,
    onFinished: (saved: Boolean) -> Unit,
    viewModel: CaptureViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnFinished by rememberUpdatedState(onFinished)
    val photoPicker =
        rememberLauncherForActivityResult(PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                viewModel.selectPhotoUri(uri)
            }
        }

    LaunchedEffect(initialDraft) {
        viewModel.loadInitialDraft(initialDraft)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                CaptureEvent.Saved -> currentOnFinished(true)
            }
        }
    }

    CaptureScreen(
        uiState = uiState,
        onTitleChange = viewModel::updateTitle,
        onSubtitleChange = viewModel::updateSubtitle,
        onNoteChange = viewModel::updateNote,
        onBodyFeelChange = viewModel::updateBodyFeel,
        onTagToggle = viewModel::toggleTag,
        onFeelingToggle = viewModel::toggleFeelingTags,
        onChoosePhoto = {
            photoPicker.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        },
        onSave = viewModel::save,
        onCancel = { onFinished(false) },
    )
}

@Composable
private fun CaptureScreen(
    uiState: CaptureUiState,
    onTitleChange: (String) -> Unit,
    onSubtitleChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onBodyFeelChange: (String) -> Unit,
    onTagToggle: (AnchorTag) -> Unit,
    onFeelingToggle: () -> Unit,
    onChoosePhoto: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAnchorSpacing.current

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.large),
    ) {
        CaptureTopBar(onCancel = onCancel)

        if (uiState.type == ResourceType.PHOTO) {
            PhotoPickerSection(
                photoSelected = uiState.photoDraft != null,
                onChoosePhoto = onChoosePhoto,
            )
        }

        OutlinedTextField(
            value = uiState.title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(R.string.capture_title_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = uiState.subtitle,
            onValueChange = onSubtitleChange,
            label = { Text(stringResource(R.string.capture_subtitle_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        TagSection(
            titleRes = R.string.capture_need_tags,
            tags = uiState.needTags,
            uiState = uiState,
            onTagToggle = onTagToggle,
        )

        TextButton(onClick = onFeelingToggle) {
            Text(stringResource(R.string.capture_add_how_it_felt))
        }
        if (uiState.showFeelingTags) {
            TagSection(
                titleRes = R.string.capture_body_feel_label,
                tags = uiState.feelingTags,
                uiState = uiState,
                onTagToggle = onTagToggle,
            )
        }

        OutlinedTextField(
            value = uiState.note,
            onValueChange = onNoteChange,
            label = { Text(stringResource(R.string.capture_note_label)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = NOTE_MIN_LINES,
        )
        OutlinedTextField(
            value = uiState.bodyFeel,
            onValueChange = onBodyFeelChange,
            label = { Text(stringResource(R.string.capture_body_feel_label)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = BODY_FEEL_MIN_LINES,
        )

        if (uiState.errorMessage != null) {
            Text(
                text = stringResource(R.string.capture_error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Button(
            onClick = onSave,
            enabled = uiState.canSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                imageVector = Icons.Outlined.Save,
                contentDescription = null,
            )
            Text(
                text =
                    if (uiState.saving) {
                        stringResource(R.string.capture_saving)
                    } else {
                        stringResource(R.string.capture_save)
                    },
            )
        }
    }
}

@Composable
private fun CaptureTopBar(onCancel: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.capture_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        IconButton(onClick = onCancel) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = stringResource(R.string.capture_cancel),
            )
        }
    }
}

@Composable
private fun PhotoPickerSection(
    photoSelected: Boolean,
    onChoosePhoto: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(LocalAnchorSpacing.current.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(onClick = onChoosePhoto) {
            Icon(
                imageVector = Icons.Outlined.PhotoLibrary,
                contentDescription = null,
            )
            Text(stringResource(R.string.capture_choose_photo))
        }
        if (photoSelected) {
            AssistChip(
                onClick = onChoosePhoto,
                label = { Text(stringResource(R.string.capture_photo_selected)) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagSection(
    @StringRes titleRes: Int,
    tags: List<AnchorTag>,
    uiState: CaptureUiState,
    onTagToggle: (AnchorTag) -> Unit,
) {
    val spacing = LocalAnchorSpacing.current

    Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleMedium,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            tags.forEach { tag ->
                FilterChip(
                    selected = uiState.selectedTags.any { selected -> selected.tagId == tag.id },
                    onClick = { onTagToggle(tag) },
                    label = { Text(tag.label()) },
                )
            }
        }
    }
}

@Composable
private fun AnchorTag.label(): String {
    val presetLabel = presetKey?.let(::presetLabelRes)
    return when {
        presetLabel != null -> stringResource(presetLabel)
        customLabel != null -> customLabel
        else -> checkNotNull(presetKey)
    }
}

@StringRes
private fun presetLabelRes(presetKey: String): Int? = PresetLabelResources[presetKey]

private val PresetLabelResources =
    mapOf(
        "calm" to R.string.tag_feeling_calm,
        "safe" to R.string.tag_feeling_safe,
        "joyful" to R.string.tag_feeling_joyful,
        "energized" to R.string.tag_feeling_energized,
        "melancholy" to R.string.tag_feeling_melancholy,
        "anxious" to R.string.tag_feeling_anxious,
        "angry" to R.string.tag_feeling_angry,
        "restless" to R.string.tag_feeling_restless,
        "numb" to R.string.tag_feeling_numb,
        "sad" to R.string.tag_feeling_sad,
        "calm_down" to R.string.tag_need_calm_down,
        "release_anger" to R.string.tag_need_release_anger,
        "feel_safe" to R.string.tag_need_feel_safe,
        "get_energy" to R.string.tag_need_get_energy,
        "ground_myself" to R.string.tag_need_ground_myself,
        "feel_less_alone" to R.string.tag_need_feel_less_alone,
        "fall_asleep" to R.string.tag_need_fall_asleep,
        "ride_out_a_wave" to R.string.tag_need_ride_out_a_wave,
    )

private const val NOTE_MIN_LINES = 2
private const val BODY_FEEL_MIN_LINES = 2
