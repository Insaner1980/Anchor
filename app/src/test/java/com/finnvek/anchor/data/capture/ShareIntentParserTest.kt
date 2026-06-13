package com.finnvek.anchor.data.capture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShareIntentParserTest {

    @Test
    fun spotifyShareTextUsesTitleArtistAndUrl() {
        val draft =
            ShareIntentParser.parseText(
                """
                Space Song
                Beach House
                https://open.spotify.com/track/1ZgMsA55GIY7ICkQh5MILA?si=abc
                """.trimIndent(),
            )

        assertEquals("Space Song", draft.title)
        assertEquals("Beach House · Spotify", draft.subtitle)
        assertEquals("https://open.spotify.com/track/1ZgMsA55GIY7ICkQh5MILA?si=abc", draft.uri)
        assertEquals("Spotify", draft.sourceApp)
    }

    @Test
    fun youtubeMusicShareTextUsesTitleArtistAndUrl() {
        val draft =
            ShareIntentParser.parseText(
                """
                Holocene
                Bon Iver
                https://music.youtube.com/watch?v=TWcyIpul8OE&si=abc
                """.trimIndent(),
            )

        assertEquals("Holocene", draft.title)
        assertEquals("Bon Iver · YouTube Music", draft.subtitle)
        assertEquals("https://music.youtube.com/watch?v=TWcyIpul8OE&si=abc", draft.uri)
        assertEquals("YouTube Music", draft.sourceApp)
    }

    @Test
    fun genericUrlShareTextUsesLeadingTextAndHost() {
        val draft = ShareIntentParser.parseText("Read later https://example.com/article?id=10")

        assertEquals("Read later", draft.title)
        assertEquals("example.com", draft.subtitle)
        assertEquals("https://example.com/article?id=10", draft.uri)
        assertEquals("example.com", draft.sourceApp)
    }

    @Test
    fun rawTextWithoutUrlStaysEditableAsTitle() {
        val draft = ShareIntentParser.parseText("A song I want to remember")

        assertEquals("A song I want to remember", draft.title)
        assertNull(draft.subtitle)
        assertNull(draft.uri)
        assertNull(draft.sourceApp)
    }
}
