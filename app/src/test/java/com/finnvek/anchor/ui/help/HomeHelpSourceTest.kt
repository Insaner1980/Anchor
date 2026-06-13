package com.finnvek.anchor.ui.help

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeHelpSourceTest {

    private val rootDir: File =
        generateSequence(File(checkNotNull(System.getProperty("user.dir"))).absoluteFile) { it.parentFile }
            .first { File(it, "settings.gradle.kts").isFile }

    @Test
    fun homeRouteShowsHelpHeroAndRecentResources() {
        val home = projectFile("app/src/main/java/com/finnvek/anchor/ui/home/HomeScreen.kt")
        val homeViewModel = projectFile("app/src/main/java/com/finnvek/anchor/ui/home/HomeViewModel.kt")
        val navigation = projectFile("app/src/main/java/com/finnvek/anchor/ui/navigation/AnchorAppRoot.kt")

        assertTrue(home.contains("home_help_now"))
        assertTrue(homeViewModel.contains("observeRecentResources"))
        assertTrue(navigation.contains("HomeRoute("))
        assertTrue(navigation.contains("AnchorRoute.HelpNeedPicker.route"))
    }

    @Test
    fun helpNowUsesNeedPickerResultsAndUsageCounting() {
        val help = projectFile("app/src/main/java/com/finnvek/anchor/ui/help/HelpNowScreen.kt")
        val opener = projectFile("app/src/main/java/com/finnvek/anchor/data/intent/ExternalSongOpener.kt")
        val tagRepository = projectFile("app/src/main/java/com/finnvek/anchor/data/repository/DataTagRepository.kt")

        assertTrue(help.contains("observeHelpResults"))
        assertTrue(help.contains("incrementUsage"))
        assertTrue(tagRepository.contains("TagCategory.NEED"))
        assertTrue(opener.contains("Intent.ACTION_VIEW"))
        assertTrue(opener.contains("ActivityNotFoundException"))
    }

    private fun projectFile(relativePath: String): String =
        File(rootDir, relativePath).also { assertTrue("$relativePath should exist", it.isFile) }.readText()
}
