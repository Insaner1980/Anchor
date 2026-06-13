package com.finnvek.anchor.ui.capture

import com.finnvek.anchor.data.capture.SharedSongDraft
import com.finnvek.anchor.data.photo.PhotoPickerDraft

sealed interface CaptureInitialDraft {
    data class Song(val draft: SharedSongDraft) : CaptureInitialDraft

    data class Photo(val draft: PhotoPickerDraft) : CaptureInitialDraft

    data object EmptyPhoto : CaptureInitialDraft

    data object Empty : CaptureInitialDraft
}
