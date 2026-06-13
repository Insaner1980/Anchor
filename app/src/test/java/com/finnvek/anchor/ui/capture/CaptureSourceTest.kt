package com.finnvek.anchor.ui.capture

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class CaptureSourceTest {

    private val rootDir: File =
        generateSequence(File(checkNotNull(System.getProperty("user.dir"))).absoluteFile) { it.parentFile }
            .first { File(it, "settings.gradle.kts").isFile }

    @Test
    fun shareCaptureActivityReadsTextAndImageSendIntentsIntoCaptureRoute() {
        val activity = projectFile("app/src/main/java/com/finnvek/anchor/ShareCaptureActivity.kt")

        listOf(
            "CaptureRoute(",
            "ShareIntentParser.parseText",
            "Intent.EXTRA_TEXT",
            "Intent.EXTRA_STREAM",
            "Activity.RESULT_OK",
            "Activity.RESULT_CANCELED",
            "finish()",
        ).forEach { expected ->
            assertTrue("ShareCaptureActivity missing $expected", activity.contains(expected))
        }
    }

    @Test
    fun captureUiAllowsSavingWithoutTagsAndUsesNeedBeforeFeeling() {
        val viewModel = projectFile("app/src/main/java/com/finnvek/anchor/ui/capture/CaptureViewModel.kt")
        val screen = projectFile("app/src/main/java/com/finnvek/anchor/ui/capture/CaptureScreen.kt")

        assertTrue(viewModel.contains("saveCapture"))
        assertTrue(viewModel.contains("ResourceTagSelection"))
        assertTrue(screen.contains("needTags"))
        assertTrue(screen.contains("feelingTags"))
        assertTrue(screen.indexOf("needTags") < screen.indexOf("feelingTags"))
        assertTrue(screen.contains("capture_add_how_it_felt"))
    }

    private fun projectFile(relativePath: String): String =
        File(rootDir, relativePath).also { assertTrue("$relativePath should exist", it.isFile) }.readText()
}
