package com.finnvek.anchor.data.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PhotoStorageInstrumentedTest {

    @Test
    fun savePhotoCreatesRealThumbnailAndDeleteCleansBothFiles() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val storage =
                FilePhotoStorage(
                    roots = PhotoStorageRoots(
                        filesDir = context.filesDir,
                        cacheDir = context.cacheDir,
                    ),
                    thumbnailer = AndroidPhotoThumbnailer(),
                    idGenerator = { "instrumented-photo" },
                    dispatcher = Dispatchers.IO,
                )

            val storedPhoto =
                storage.savePhoto(
                    PhotoStorageInput(
                        mimeType = "image/png",
                        displayName = "instrumented.png",
                        openInputStream = { samplePngBytes().inputStream() },
                    ),
                )

            val photoFile = File(context.filesDir, storedPhoto.photoPath)
            val thumbnailFile = File(context.cacheDir, storedPhoto.thumbnailPath)

            assertTrue(photoFile.isFile)
            assertTrue(thumbnailFile.isFile)

            storage.deletePhoto(storedPhoto)

            assertFalse(photoFile.exists())
            assertFalse(thumbnailFile.exists())
        }

    private fun samplePngBytes(): ByteArray {
        val bitmap = Bitmap.createBitmap(PIXEL_SIZE, PIXEL_SIZE, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.RED)

        return ByteArrayOutputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, output)
            output.toByteArray()
        }
    }

    private companion object {
        private const val PIXEL_SIZE = 32
        private const val PNG_QUALITY = 100
    }
}
