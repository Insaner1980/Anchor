package com.finnvek.anchor.data.photo

import java.io.File
import java.io.InputStream

data class StoredPhoto(
    val photoPath: String,
    val thumbnailPath: String,
)

data class PhotoStorageInput(
    val mimeType: String?,
    val displayName: String?,
    val openInputStream: () -> InputStream,
)

data class PhotoStorageRoots(
    val filesDir: File,
    val cacheDir: File,
)

interface PhotoStorage {
    suspend fun savePhoto(input: PhotoStorageInput): StoredPhoto

    suspend fun deletePhoto(storedPhoto: StoredPhoto)
}

fun interface PhotoThumbnailer {
    fun createThumbnail(sourceFile: File, destinationFile: File)
}
