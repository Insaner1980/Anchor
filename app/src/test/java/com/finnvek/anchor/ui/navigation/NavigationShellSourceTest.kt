package com.finnvek.anchor.ui.navigation

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationShellSourceTest {

    private val rootDir: File =
        generateSequence(File(checkNotNull(System.getProperty("user.dir"))).absoluteFile) { it.parentFile }
            .first { File(it, "settings.gradle.kts").isFile }

    @Test
    fun mainActivityHostsAnchorAppRootInsideAnchorTheme() {
        val mainActivity = projectFile("app/src/main/java/com/finnvek/anchor/MainActivity.kt")

        assertTrue(mainActivity.contains("AnchorTheme"))
        assertTrue(mainActivity.contains("AnchorAppRoot("))
    }

    @Test
    fun navigationShellDeclaresMvpRoutesAndBottomDestinations() {
        val routes = projectFile("app/src/main/java/com/finnvek/anchor/ui/navigation/AnchorRoute.kt")
        val shell = projectFile("app/src/main/java/com/finnvek/anchor/ui/navigation/AnchorAppRoot.kt")

        listOf(
            "onboarding",
            "home",
            "library",
            "timeline",
            "resource/{resourceId}",
            "help/need",
            "help/results/{tagId}",
            "photos/{resourceId}",
            "settings",
            "tags",
            "capture/photo",
        ).forEach { route ->
            assertTrue("Missing route $route", routes.contains(route))
        }

        assertTrue(shell.contains("AnchorBottomDestination.Home"))
        assertTrue(shell.contains("AnchorBottomDestination.Library"))
        assertTrue(shell.contains("AnchorBottomDestination.You"))
    }

    @Test
    fun themeHasCentralizedAnchorTokensAndCrisisMotion() {
        val theme = projectFile("app/src/main/java/com/finnvek/anchor/ui/theme/Theme.kt")
        val spacing = projectFile("app/src/main/java/com/finnvek/anchor/ui/theme/Spacing.kt")
        val shapes = projectFile("app/src/main/java/com/finnvek/anchor/ui/theme/Shapes.kt")
        val motion = projectFile("app/src/main/java/com/finnvek/anchor/ui/theme/Motion.kt")

        assertTrue(theme.contains("fun AnchorTheme"))
        assertTrue(theme.contains("dynamicDarkColorScheme"))
        assertTrue(theme.contains("AnchorCalmLightColorScheme"))
        assertTrue(spacing.contains("data class AnchorSpacing"))
        assertTrue(shapes.contains("object AnchorShapes"))
        assertTrue(motion.contains("object AnchorMotion"))
        assertTrue(motion.contains("Crisis"))
    }

    private fun projectFile(relativePath: String): String =
        File(rootDir, relativePath).also { assertTrue("$relativePath should exist", it.isFile) }.readText()
}
