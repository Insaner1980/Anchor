package com.finnvek.anchor.ui.detail

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class DetailTagManagerSourceTest {

    private val rootDir: File =
        generateSequence(File(checkNotNull(System.getProperty("user.dir"))).absoluteFile) { it.parentFile }
            .first { File(it, "settings.gradle.kts").isFile }

    @Test
    fun detailRouteSupportsEditFavoriteAndDelete() {
        val detail = projectFile("app/src/main/java/com/finnvek/anchor/ui/detail/ResourceDetailScreen.kt")
        val navigation = projectFile("app/src/main/java/com/finnvek/anchor/ui/navigation/AnchorAppRoot.kt")

        assertTrue(detail.contains("updateContent"))
        assertTrue(detail.contains("setFavorite"))
        assertTrue(detail.contains("delete("))
        assertTrue(detail.contains("AlertDialog"))
        assertTrue(navigation.contains("ResourceDetailRoute("))
    }

    @Test
    fun tagManagerSupportsCreateHideAndDelete() {
        val tags = projectFile("app/src/main/java/com/finnvek/anchor/ui/tags/TagManagerScreen.kt")

        assertTrue(tags.contains("createCustomTag"))
        assertTrue(tags.contains("hidePreset"))
        assertTrue(tags.contains("deleteCustom"))
        assertTrue(tags.contains("TagCategory.NEED"))
    }

    private fun projectFile(relativePath: String): String =
        File(rootDir, relativePath).also { assertTrue("$relativePath should exist", it.isFile) }.readText()
}
