package com.finnvek.anchor.ui.capture

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finnvek.anchor.data.capture.SharedSongDraft
import com.finnvek.anchor.data.photo.ContentUriPhotoInputFactory
import com.finnvek.anchor.data.photo.PhotoPickerDraft
import com.finnvek.anchor.data.photo.PhotoStorage
import com.finnvek.anchor.domain.model.AnchorTag
import com.finnvek.anchor.domain.model.ResourceCapture
import com.finnvek.anchor.domain.model.ResourceTagSelection
import com.finnvek.anchor.domain.model.ResourceType
import com.finnvek.anchor.domain.model.TagCategory
import com.finnvek.anchor.domain.model.TagRole
import com.finnvek.anchor.repository.ResourceRepository
import com.finnvek.anchor.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CaptureUiState(
    val type: ResourceType = ResourceType.SONG,
    val title: String = "",
    val subtitle: String = "",
    val uri: String? = null,
    val sourceApp: String? = null,
    val note: String = "",
    val bodyFeel: String = "",
    val photoDraft: PhotoPickerDraft? = null,
    val needTags: List<AnchorTag> = emptyList(),
    val feelingTags: List<AnchorTag> = emptyList(),
    val selectedTags: Set<ResourceTagSelection> = emptySet(),
    val showFeelingTags: Boolean = false,
    val saving: Boolean = false,
    val errorMessage: String? = null,
) {
    val canSave: Boolean =
        !saving &&
            title.isNotBlank() &&
            (type == ResourceType.SONG || photoDraft != null)
}

sealed interface CaptureEvent {
    data object Saved : CaptureEvent
}

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val resourceRepository: ResourceRepository,
    private val tagRepository: TagRepository,
    private val photoStorage: PhotoStorage,
    private val photoInputFactory: ContentUriPhotoInputFactory,
) : ViewModel() {

    private val formState = MutableStateFlow(CaptureFormState())
    private val eventFlow = MutableSharedFlow<CaptureEvent>()
    private var draftLoaded = false

    val events: SharedFlow<CaptureEvent> = eventFlow

    val uiState: StateFlow<CaptureUiState> =
        combine(
            formState,
            tagRepository.observeVisibleTags(TagCategory.NEED),
            tagRepository.observeVisibleTags(TagCategory.FEELING),
        ) { form, needTags, feelingTags ->
            form.toUiState(
                needTags = needTags,
                feelingTags = feelingTags,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MS),
            initialValue = CaptureUiState(),
        )

    fun loadInitialDraft(initialDraft: CaptureInitialDraft) {
        if (draftLoaded) return
        draftLoaded = true

        formState.update { current ->
            when (initialDraft) {
                CaptureInitialDraft.Empty -> current
                CaptureInitialDraft.EmptyPhoto ->
                    current.copy(
                        type = ResourceType.PHOTO,
                        title = current.title.ifBlank { DEFAULT_PHOTO_TITLE },
                    )
                is CaptureInitialDraft.Photo ->
                    current.copy(
                        type = ResourceType.PHOTO,
                        title = current.title.ifBlank { DEFAULT_PHOTO_TITLE },
                        photoDraft = initialDraft.draft,
                    )
                is CaptureInitialDraft.Song -> current.fromSongDraft(initialDraft.draft)
            }
        }
    }

    fun selectPhotoUri(uri: Uri) {
        val draft = photoInputFactory.createDraft(uri)
        formState.update { current ->
            current.copy(
                type = ResourceType.PHOTO,
                title = current.title.ifBlank { DEFAULT_PHOTO_TITLE },
                photoDraft = draft,
                uri = null,
                sourceApp = null,
            )
        }
    }

    fun updateTitle(title: String) {
        formState.update { current -> current.copy(title = title) }
    }

    fun updateSubtitle(subtitle: String) {
        formState.update { current -> current.copy(subtitle = subtitle) }
    }

    fun updateNote(note: String) {
        formState.update { current -> current.copy(note = note) }
    }

    fun updateBodyFeel(bodyFeel: String) {
        formState.update { current -> current.copy(bodyFeel = bodyFeel) }
    }

    fun toggleFeelingTags() {
        formState.update { current -> current.copy(showFeelingTags = !current.showFeelingTags) }
    }

    fun toggleTag(tag: AnchorTag) {
        val role =
            when (tag.category) {
                TagCategory.NEED -> TagRole.HELPS_WITH
                TagCategory.FEELING -> TagRole.FEELING_THEN
            }
        val selection = ResourceTagSelection(tagId = tag.id, role = role)

        formState.update { current ->
            val selectedTags =
                if (selection in current.selectedTags) {
                    current.selectedTags - selection
                } else {
                    current.selectedTags + selection
                }
            current.copy(selectedTags = selectedTags)
        }
    }

    fun save() {
        val current = formState.value
        if (!current.canAttemptSave()) return

        viewModelScope.launch {
            formState.update { state -> state.copy(saving = true, errorMessage = null) }
            runCatching {
                resourceRepository.saveCapture(current.toResourceCapture())
            }.onSuccess {
                eventFlow.emit(CaptureEvent.Saved)
            }.onFailure { failure ->
                formState.update { state ->
                    state.copy(
                        saving = false,
                        errorMessage = failure.message ?: CAPTURE_SAVE_ERROR,
                    )
                }
            }
        }
    }

    private suspend fun CaptureFormState.toResourceCapture(): ResourceCapture {
        val storedPhoto =
            if (type == ResourceType.PHOTO) {
                val draft = checkNotNull(photoDraft) { "Choose a photo before saving." }
                photoStorage.savePhoto(photoInputFactory.createInput(draft))
            } else {
                null
            }

        return ResourceCapture(
            type = type,
            createdAtMs = System.currentTimeMillis(),
            title = title.ifBlank { DEFAULT_PHOTO_TITLE },
            subtitle = subtitle.ifBlank { null },
            uri = storedPhoto?.photoPath ?: uri,
            sourceApp = sourceApp,
            note = note.ifBlank { null },
            bodyFeel = bodyFeel.ifBlank { null },
            thumbnailPath = storedPhoto?.thumbnailPath,
            tags = selectedTags.toList(),
        )
    }

    private fun CaptureFormState.fromSongDraft(draft: SharedSongDraft): CaptureFormState =
        copy(
            type = ResourceType.SONG,
            title = draft.title,
            subtitle = draft.subtitle.orEmpty(),
            uri = draft.uri,
            sourceApp = draft.sourceApp,
            photoDraft = null,
        )

    private fun CaptureFormState.toUiState(
        needTags: List<AnchorTag>,
        feelingTags: List<AnchorTag>,
    ): CaptureUiState =
        CaptureUiState(
            type = type,
            title = title,
            subtitle = subtitle,
            uri = uri,
            sourceApp = sourceApp,
            note = note,
            bodyFeel = bodyFeel,
            photoDraft = photoDraft,
            needTags = needTags,
            feelingTags = feelingTags,
            selectedTags = selectedTags,
            showFeelingTags = showFeelingTags,
            saving = saving,
            errorMessage = errorMessage,
        )

    private fun CaptureFormState.canAttemptSave(): Boolean =
        !saving &&
            title.isNotBlank() &&
            (type == ResourceType.SONG || photoDraft != null)

    private data class CaptureFormState(
        val type: ResourceType = ResourceType.SONG,
        val title: String = "",
        val subtitle: String = "",
        val uri: String? = null,
        val sourceApp: String? = null,
        val note: String = "",
        val bodyFeel: String = "",
        val photoDraft: PhotoPickerDraft? = null,
        val selectedTags: Set<ResourceTagSelection> = emptySet(),
        val showFeelingTags: Boolean = false,
        val saving: Boolean = false,
        val errorMessage: String? = null,
    )

    private companion object {
        private const val STATE_STOP_TIMEOUT_MS = 5_000L
        private const val DEFAULT_PHOTO_TITLE = "Photo"
        private const val CAPTURE_SAVE_ERROR = "Could not save"
    }
}
