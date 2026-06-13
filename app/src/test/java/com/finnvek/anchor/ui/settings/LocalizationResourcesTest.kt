package com.finnvek.anchor.ui.settings

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

class LocalizationResourcesTest {

    private val rootDir: File =
        generateSequence(File(checkNotNull(System.getProperty("user.dir"))).absoluteFile) { it.parentFile }
            .first { File(it, "settings.gradle.kts").isFile }

    @Test
    fun englishAndFinnishResourcesCoverSettingsStrings() {
        val english = stringResources("app/src/main/res/values/strings.xml")
        val finnish = stringResources("app/src/main/res/values-fi/strings.xml")

        val requiredSettingsStrings =
            setOf(
                "settings_theme_title",
                "settings_theme_system",
                "settings_theme_light",
                "settings_theme_dark",
                "settings_theme_calm",
                "settings_language_title",
                "settings_language_system",
                "settings_language_finnish",
                "settings_language_english",
                "settings_privacy_title",
                "settings_privacy_body",
            )

        requiredSettingsStrings.forEach { key ->
            assertTrue("English resources missing $key", english.containsKey(key))
            assertTrue("Finnish resources missing $key", finnish.containsKey(key))
        }

        assertEquals(english.keys, finnish.keys)
    }

    @Test
    fun appLocaleImplementationUsesAppCompatLocalesAndGeneratedLocaleConfig() {
        val localeController = projectFile("app/src/main/java/com/finnvek/anchor/data/settings/AppLocaleController.kt")
        val appBuild = projectFile("app/build.gradle.kts")

        assertTrue(localeController.contains("AppCompatDelegate.setApplicationLocales"))
        assertTrue(localeController.contains("LocaleListCompat.forLanguageTags"))
        assertTrue(appBuild.contains("generateLocaleConfig = true"))
    }

    private fun stringResources(relativePath: String): Map<String, String> {
        val file = File(rootDir, relativePath).also { assertTrue("$relativePath should exist", it.isFile) }
        val document =
            DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(file)

        return document
            .getElementsByTagName("string")
            .let { nodes ->
                buildMap {
                    repeat(nodes.length) { index ->
                        val element = nodes.item(index) as Element
                        put(element.getAttribute("name"), element.textContent)
                    }
                }
            }
    }

    private fun projectFile(relativePath: String): String =
        File(rootDir, relativePath).also { assertTrue("$relativePath should exist", it.isFile) }.readText()
}
