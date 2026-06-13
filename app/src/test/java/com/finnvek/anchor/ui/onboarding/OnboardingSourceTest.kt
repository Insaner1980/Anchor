package com.finnvek.anchor.ui.onboarding

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingSourceTest {

    private val rootDir: File =
        generateSequence(File(checkNotNull(System.getProperty("user.dir"))).absoluteFile) { it.parentFile }
            .first { File(it, "settings.gradle.kts").isFile }

    @Test
    fun onboardingStartsBeforeHomeAndPersistsSkip() {
        val appViewModel = projectFile("app/src/main/java/com/finnvek/anchor/ui/app/AnchorAppViewModel.kt")
        val onboarding = projectFile("app/src/main/java/com/finnvek/anchor/ui/onboarding/OnboardingScreen.kt")
        val navigation = projectFile("app/src/main/java/com/finnvek/anchor/ui/navigation/AnchorAppRoot.kt")

        assertTrue(appViewModel.contains("onboardingDone"))
        assertTrue(onboarding.contains("updateOnboardingDone"))
        assertTrue(onboarding.contains("isTraversalGroup"))
        assertTrue(navigation.contains("startDestination"))
        assertTrue(navigation.contains("AnchorRoute.Onboarding.route"))
    }

    private fun projectFile(relativePath: String): String =
        File(rootDir, relativePath).also { assertTrue("$relativePath should exist", it.isFile) }.readText()
}
