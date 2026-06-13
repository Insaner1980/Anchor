package com.finnvek.anchor.data.photo

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.FileNotFoundException
import javax.inject.Inject

class ContentUriPhotoInputFactory @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    fun createDraft(uri: Uri): PhotoPickerDraft =
        PhotoPickerDraft(
            uriString = uri.toString(),
            mimeType = context.contentResolver.getType(uri),
        )

    fun createInput(draft: PhotoPickerDraft): PhotoStorageInput {
        val uri = Uri.parse(draft.uriString)
        return PhotoStorageInput(
            mimeType = draft.mimeType ?: context.contentResolver.getType(uri),
            displayName = uri.lastPathSegment,
            openInputStream = {
                context.contentResolver.openInputStream(uri)
                    ?: throw FileNotFoundException("Could not open selected photo: $uri")
            },
        )
    }
}
