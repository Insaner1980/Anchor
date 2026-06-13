package com.finnvek.anchor.ui.library

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class LibraryTimelineSourceTest {

    private val rootDir: File =
        generateSequence(File(checkNotNull(System.getProperty("user.dir"))).absoluteFile) { it.parentFile }
            .first { File(it, "settings.gradle.kts").isFile }

    @Test
    fun libraryScreenUsesRepositorySearchFiltersAndTimeline() {
        val viewModel = projectFile("app/src/main/java/com/finnvek/anchor/ui/library/LibraryViewModel.kt")
        val screen = projectFile("app/src/main/java/com/finnvek/anchor/ui/library/LibraryScreen.kt")
        val navigation = projectFile("app/src/main/java/com/finnvek/anchor/ui/navigation/AnchorAppRoot.kt")

        assertTrue(viewModel.contains("observeLibraryResources"))
        assertTrue(viewModel.contains("observeTimeline"))
        assertTrue(screen.contains("LibraryContentType.ALL"))
        assertTrue(screen.contains("LibraryContentType.SONGS"))
        assertTrue(screen.contains("LibraryContentType.PHOTOS"))
        assertTrue(screen.contains("library_search"))
        assertTrue(navigation.contains("LibraryRoute("))
    }

    private fun projectFile(relativePath: String): String =
        File(rootDir, relativePath).also { assertTrue("$relativePath should exist", it.isFile) }.readText()
}
