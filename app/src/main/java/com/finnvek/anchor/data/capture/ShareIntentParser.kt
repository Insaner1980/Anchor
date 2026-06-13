package com.finnvek.anchor.data.capture

import java.net.URI
import java.util.Locale

data class SharedSongDraft(
    val title: String,
    val subtitle: String?,
    val uri: String?,
    val sourceApp: String?,
)

object ShareIntentParser {

    fun parseText(sharedText: String): SharedSongDraft {
        val trimmedText = sharedText.trim()
        val urlMatch = UrlRegex.find(trimmedText)
            ?: return SharedSongDraft(
                title = trimmedText,
                subtitle = null,
                uri = null,
                sourceApp = null,
            )

        val url = urlMatch.value.trimSharedUrlSuffix()
        val host = url.hostOrNull()
        val service = host?.serviceFromHost()
        val leadingLines =
            trimmedText
                .substring(startIndex = 0, endIndex = urlMatch.range.first)
                .lines()
                .map { line -> line.trim() }
                .filter { line -> line.isNotBlank() }

        return SharedSongDraft(
            title = leadingLines.firstOrNull() ?: service?.fallbackTitle ?: host ?: trimmedText,
            subtitle = subtitleFor(leadingLines, service, host),
            uri = url,
            sourceApp = service?.label ?: host,
        )
    }

    private fun subtitleFor(
        leadingLines: List<String>,
        service: ShareService?,
        host: String?,
    ): String? =
        when {
            service != null && leadingLines.size >= MIN_LINES_WITH_ARTIST ->
                "${leadingLines[ARTIST_LINE_INDEX]} · ${service.label}"
            service != null -> service.label
            else -> host
        }

    private fun String.trimSharedUrlSuffix(): String =
        trimEnd('.', ',', ')', ']', '}')

    private fun String.hostOrNull(): String? =
        runCatching { URI(this).host }
            .getOrNull()
            ?.removePrefix("www.")

    private fun String.serviceFromHost(): ShareService? {
        val normalizedHost = lowercase(Locale.ROOT)
        return when {
            normalizedHost == SPOTIFY_HOST -> ShareService.Spotify
            normalizedHost == YOUTUBE_MUSIC_HOST -> ShareService.YouTubeMusic
            else -> null
        }
    }

    private enum class ShareService(
        val label: String,
        val fallbackTitle: String,
    ) {
        Spotify("Spotify", "Spotify link"),
        YouTubeMusic("YouTube Music", "YouTube Music link"),
    }

    private val UrlRegex = Regex("""https?://\S+""")

    private const val SPOTIFY_HOST = "open.spotify.com"
    private const val YOUTUBE_MUSIC_HOST = "music.youtube.com"
    private const val MIN_LINES_WITH_ARTIST = 2
    private const val ARTIST_LINE_INDEX = 1
}
