package com.finnvek.anchor

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectIdentityTest {

    private val rootDir: File =
        generateSequence(File(checkNotNull(System.getProperty("user.dir"))).absoluteFile) { it.parentFile }
            .first { File(it, "settings.gradle.kts").isFile }

    @Test
    fun projectIdentityUsesAnchorPackageAndSdkTargets() {
        val settings = projectFile("settings.gradle.kts")
        val appBuild = projectFile("app/build.gradle.kts")
        val strings = projectFile("app/src/main/res/values/strings.xml")

        assertTrue(settings.contains("rootProject.name = \"Anchor\""))
        assertTrue(appBuild.contains("namespace = \"com.finnvek.anchor\""))
        assertTrue(appBuild.contains("applicationId = \"com.finnvek.anchor\""))
        assertTrue(appBuild.contains("compileSdk = 36"))
        assertTrue(appBuild.contains("minSdk = 26"))
        assertTrue(appBuild.contains("targetSdk = 35"))
        assertTrue(strings.contains("<string name=\"app_name\">Anchor</string>"))
    }

    @Test
    fun dependencyCatalogPinsAnchorMvpRuntimeVersions() {
        val catalog = projectFile("gradle/libs.versions.toml")

        assertTrue(catalog.contains("composeBom = \"2026.05.01\""))
        assertTrue(catalog.contains("composeNavigation = \"2.9.8\""))
        assertTrue(catalog.contains("activityCompose = \"1.13.0\""))
        assertTrue(catalog.contains("datastore = \"1.2.1\""))
        assertTrue(catalog.contains("coil = \"3.5.0\""))
        assertTrue(catalog.contains("appcompat = \"1.7.1\""))
        assertTrue(catalog.contains("material3 = \"1.4.0\""))
    }

    @Test
    fun manifestKeepsPrivacyBoundaryAndDeclaresShareTarget() {
        val manifest = projectFile("app/src/main/AndroidManifest.xml")

        assertTrue(manifest.contains("android:allowBackup=\"false\""))
        assertFalse(manifest.contains("android.permission.INTERNET"))
        assertTrue(manifest.contains("ShareCaptureActivity"))
        assertTrue(manifest.contains("android.intent.action.SEND"))
        assertTrue(manifest.contains("text/plain"))
        assertTrue(manifest.contains("image/*"))
    }

    private fun projectFile(relativePath: String): String =
        File(rootDir, relativePath).readText()
}
