package com.finnvek.anchor.data.photo

import java.io.File
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class FilePhotoStorage(
    private val roots: PhotoStorageRoots,
    private val thumbnailer: PhotoThumbnailer,
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
    private val dispatcher: CoroutineDispatcher,
) : PhotoStorage {

    override suspend fun savePhoto(input: PhotoStorageInput): StoredPhoto =
        withContext(dispatcher) {
            val id = idGenerator()
            val photoPath = "$PHOTO_DIRECTORY/$id.${input.fileExtension()}"
            val thumbnailPath = "$THUMBNAIL_DIRECTORY/$id.$THUMBNAIL_EXTENSION"
            val photoFile = roots.filesDir.childInside(photoPath)
            val thumbnailFile = roots.cacheDir.childInside(thumbnailPath)

            var saved = false
            try {
                photoFile.parentFile?.mkdirs()
                input.openInputStream().use { source ->
                    photoFile.outputStream().use { destination ->
                        source.copyTo(destination)
                    }
                }

                thumbnailer.createThumbnail(photoFile, thumbnailFile)
                saved = true

                StoredPhoto(
                    photoPath = photoPath,
                    thumbnailPath = thumbnailPath,
                )
            } finally {
                if (!saved) {
                    photoFile.delete()
                    thumbnailFile.delete()
                }
            }
        }

    override suspend fun deletePhoto(storedPhoto: StoredPhoto) {
        withContext(dispatcher) {
            roots.filesDir.childInside(storedPhoto.photoPath).delete()
            roots.cacheDir.childInside(storedPhoto.thumbnailPath).delete()
        }
    }

    private fun PhotoStorageInput.fileExtension(): String =
        mimeType?.lowercase(Locale.ROOT)?.let(::extensionForMimeType)
            ?: displayName?.substringAfterLast('.', missingDelimiterValue = "")
                ?.takeIf { extension -> extension.isSafeExtension() }
                ?.lowercase(Locale.ROOT)
            ?: DEFAULT_PHOTO_EXTENSION

    private fun extensionForMimeType(mimeType: String): String? =
        when (mimeType) {
            "image/jpeg",
            "image/jpg",
            -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/heic",
            "image/heif",
            -> "heic"
            else -> null
        }

    private fun String.isSafeExtension(): Boolean =
        isNotBlank() &&
            length <= MAX_EXTENSION_LENGTH &&
            all { character -> character.isLetterOrDigit() }

    private fun File.childInside(relativePath: String): File {
        val relativeFile = File(relativePath)
        require(!relativeFile.isAbsolute) { "Path must be relative: $relativePath" }

        val root = canonicalFile
        val child = File(root, relativePath).canonicalFile
        require(child.path == root.path || child.path.startsWith(root.path + File.separator)) {
            "Path escapes storage root: $relativePath"
        }
        return child
    }

    private companion object {
        private const val PHOTO_DIRECTORY = "photos"
        private const val THUMBNAIL_DIRECTORY = "anchor-thumbnails"
        private const val DEFAULT_PHOTO_EXTENSION = "jpg"
        private const val THUMBNAIL_EXTENSION = "jpg"
        private const val MAX_EXTENSION_LENGTH = 8
    }
}
