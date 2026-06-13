package com.finnvek.anchor.data.photo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import java.io.File
import java.io.IOException
import javax.inject.Inject

class AndroidPhotoThumbnailer @Inject constructor() : PhotoThumbnailer {

    override fun createThumbnail(sourceFile: File, destinationFile: File) {
        destinationFile.parentFile?.mkdirs()

        val bounds =
            BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
        BitmapFactory.decodeFile(sourceFile.path, bounds)

        val bitmap =
            BitmapFactory.decodeFile(
                sourceFile.path,
                BitmapFactory.Options().apply {
                    inSampleSize = bounds.sampleSizeForThumbnail()
                },
            ) ?: throw IOException("Could not decode image for thumbnail: ${sourceFile.name}")

        val thumbnail = ThumbnailUtils.extractThumbnail(bitmap, THUMBNAIL_SIZE_PX, THUMBNAIL_SIZE_PX)
        try {
            destinationFile.outputStream().use { output ->
                if (!thumbnail.compress(Bitmap.CompressFormat.JPEG, THUMBNAIL_QUALITY, output)) {
                    throw IOException("Could not write thumbnail: ${destinationFile.name}")
                }
            }
        } finally {
            if (thumbnail !== bitmap) {
                thumbnail.recycle()
            }
            bitmap.recycle()
        }
    }

    private fun BitmapFactory.Options.sampleSizeForThumbnail(): Int {
        var sampleSize = 1
        var halfWidth = outWidth / SAMPLE_HALF_DIVISOR
        var halfHeight = outHeight / SAMPLE_HALF_DIVISOR

        while (halfWidth / sampleSize >= THUMBNAIL_SIZE_PX && halfHeight / sampleSize >= THUMBNAIL_SIZE_PX) {
            sampleSize *= SAMPLE_HALF_DIVISOR
        }

        return sampleSize
    }

    private companion object {
        private const val THUMBNAIL_SIZE_PX = 512
        private const val THUMBNAIL_QUALITY = 85
        private const val SAMPLE_HALF_DIVISOR = 2
    }
}
