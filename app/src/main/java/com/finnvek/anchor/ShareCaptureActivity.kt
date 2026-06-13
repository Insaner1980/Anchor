package com.finnvek.anchor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.finnvek.anchor.data.capture.ShareIntentParser
import com.finnvek.anchor.data.capture.SharedSongDraft
import com.finnvek.anchor.data.photo.ContentUriPhotoInputFactory
import com.finnvek.anchor.ui.capture.CaptureInitialDraft
import com.finnvek.anchor.ui.capture.CaptureRoute
import com.finnvek.anchor.ui.theme.AnchorTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShareCaptureActivity : AppCompatActivity() {

    @Inject
    lateinit var photoInputFactory: ContentUriPhotoInputFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialDraft = intent.toCaptureInitialDraft()
        setResult(Activity.RESULT_CANCELED)
        setContent {
            AnchorTheme {
                CaptureRoute(
                    initialDraft = initialDraft,
                    onFinished = ::finishWithResult,
                )
            }
        }
    }

    private fun finishWithResult(saved: Boolean) {
        setResult(if (saved) Activity.RESULT_OK else Activity.RESULT_CANCELED)
        finish()
    }

    private fun Intent.toCaptureInitialDraft(): CaptureInitialDraft {
        val stream = streamUri()
        if (type.orEmpty().startsWith(IMAGE_MIME_PREFIX) && stream != null) {
            return CaptureInitialDraft.Photo(photoInputFactory.createDraft(stream))
        }

        val sharedText = getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        if (sharedText.isNotBlank()) {
            return CaptureInitialDraft.Song(ShareIntentParser.parseText(sharedText))
        }

        return CaptureInitialDraft.Song(
            SharedSongDraft(
                title = "",
                subtitle = null,
                uri = null,
                sourceApp = null,
            ),
        )
    }

    private fun Intent.streamUri(): Uri? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(Intent.EXTRA_STREAM)
        }

    private companion object {
        private const val IMAGE_MIME_PREFIX = "image/"
    }
}
