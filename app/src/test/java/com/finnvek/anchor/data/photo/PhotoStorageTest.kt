package com.finnvek.anchor.data.photo

import java.io.File
import java.io.IOException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoStorageTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun savePhotoCopiesImageToPrivateFilesAndCreatesThumbnail() =
        runTest {
            val thumbnailer = FakePhotoThumbnailer()
            val storage = createStorage(thumbnailer = thumbnailer)

            val storedPhoto =
                storage.savePhoto(
                    PhotoStorageInput(
                        mimeType = "image/png",
                        displayName = "shared.png",
                        openInputStream = { "source image".byteInputStream() },
                    ),
                )

            assertEquals("photos/fixed-id.png", storedPhoto.photoPath)
            assertEquals("anchor-thumbnails/fixed-id.jpg", storedPhoto.thumbnailPath)
            assertEquals("source image", File(filesDir, storedPhoto.photoPath).readText())
            assertEquals("thumbnail:fixed-id.png", File(cacheDir, storedPhoto.thumbnailPath).readText())
            assertEquals(File(filesDir, storedPhoto.photoPath), thumbnailer.lastSourceFile)
        }

    @Test
    fun deletePhotoRemovesOriginalAndThumbnailFiles() =
        runTest {
            val storage = createStorage()
            val storedPhoto =
                StoredPhoto(
                    photoPath = "photos/fixed-id.jpg",
                    thumbnailPath = "anchor-thumbnails/fixed-id.jpg",
                )
            File(filesDir, storedPhoto.photoPath).also {
                it.parentFile?.mkdirs()
                it.writeText("image")
            }
            File(cacheDir, storedPhoto.thumbnailPath).also {
                it.parentFile?.mkdirs()
                it.writeText("thumbnail")
            }

            storage.deletePhoto(storedPhoto)

            assertFalse(File(filesDir, storedPhoto.photoPath).exists())
            assertFalse(File(cacheDir, storedPhoto.thumbnailPath).exists())
        }

    @Test
    fun savePhotoDeletesPartialFilesWhenThumbnailCreationFails() =
        runTest {
            val storage = createStorage(thumbnailer = FakePhotoThumbnailer(fail = true))

            try {
                storage.savePhoto(
                    PhotoStorageInput(
                        mimeType = "image/jpeg",
                        displayName = "broken.jpg",
                        openInputStream = { "source image".byteInputStream() },
                    ),
                )
                fail("Expected thumbnail failure")
            } catch (expected: IOException) {
                assertTrue(expected.message.orEmpty().contains("thumbnail"))
            }

            assertFalse(File(filesDir, "photos/fixed-id.jpg").exists())
            assertFalse(File(cacheDir, "anchor-thumbnails/fixed-id.jpg").exists())
        }

    private val filesDir: File
        get() = File(temporaryFolder.root, "files")

    private val cacheDir: File
        get() = File(temporaryFolder.root, "cache")

    private fun createStorage(
        thumbnailer: PhotoThumbnailer = FakePhotoThumbnailer(),
    ): FilePhotoStorage =
        FilePhotoStorage(
            roots = PhotoStorageRoots(filesDir = filesDir, cacheDir = cacheDir),
            thumbnailer = thumbnailer,
            idGenerator = { "fixed-id" },
            dispatcher = UnconfinedTestDispatcher(),
        )

    private class FakePhotoThumbnailer(
        private val fail: Boolean = false,
    ) : PhotoThumbnailer {
        var lastSourceFile: File? = null

        override fun createThumbnail(sourceFile: File, destinationFile: File) {
            lastSourceFile = sourceFile
            if (fail) throw IOException("thumbnail failed")
            destinationFile.parentFile?.mkdirs()
            destinationFile.writeText("thumbnail:${sourceFile.name}")
        }
    }
}
