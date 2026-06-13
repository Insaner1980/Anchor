package com.finnvek.anchor.data.local

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainAndRoomSourceTest {

    private val rootDir: File =
        generateSequence(File(checkNotNull(System.getProperty("user.dir"))).absoluteFile) { it.parentFile }
            .first { File(it, "settings.gradle.kts").isFile }

    @Test
    fun domainTypesMatchMvpPlan() {
        val domainTypes = projectFile("app/src/main/java/com/finnvek/anchor/domain/model/AnchorDomainTypes.kt")

        listOf(
            "enum class ResourceType",
            "SONG",
            "PHOTO",
            "enum class TagCategory",
            "FEELING",
            "NEED",
            "enum class TagRole",
            "FEELING_THEN",
            "HELPS_WITH",
            "enum class ThemeMode",
            "SYSTEM",
            "LIGHT",
            "DARK",
            "CALM",
            "enum class LibraryContentType",
            "ALL",
            "SONGS",
            "PHOTOS",
            "enum class LibraryViewMode",
            "LIBRARY",
            "TIMELINE",
        ).forEach { expected ->
            assertTrue("Missing domain type token $expected", domainTypes.contains(expected))
        }
    }

    @Test
    fun roomSchemaDeclaresResourcesTagsAndCrossRefs() {
        val resource = projectFile("app/src/main/java/com/finnvek/anchor/data/local/entity/ResourceEntity.kt")
        val tag = projectFile("app/src/main/java/com/finnvek/anchor/data/local/entity/TagEntity.kt")
        val ref = projectFile("app/src/main/java/com/finnvek/anchor/data/local/entity/ResourceTagRefEntity.kt")
        val database = projectFile("app/src/main/java/com/finnvek/anchor/data/local/AnchorDatabase.kt")
        val converters = projectFile("app/src/main/java/com/finnvek/anchor/data/local/AnchorTypeConverters.kt")

        listOf(
            "tableName = \"resources\"",
            "created_at_ms",
            "source_app",
            "body_feel",
            "times_used",
            "last_used_at_ms",
            "thumbnail_path",
        ).forEach { expected -> assertTrue("Missing resources token $expected", resource.contains(expected)) }

        listOf(
            "tableName = \"tags\"",
            "preset_key",
            "custom_label",
            "is_preset",
            "is_hidden",
            "sort_order",
        ).forEach { expected -> assertTrue("Missing tags token $expected", tag.contains(expected)) }

        listOf(
            "tableName = \"resource_tag_refs\"",
            "primaryKeys = [\"resource_id\", \"tag_id\", \"role\"]",
            "ForeignKey.CASCADE",
        ).forEach { expected -> assertTrue("Missing cross-ref token $expected", ref.contains(expected)) }

        assertTrue(database.contains("version = 1"))
        assertTrue(database.contains("ResourceEntity::class"))
        assertTrue(database.contains("TagEntity::class"))
        assertTrue(database.contains("ResourceTagRefEntity::class"))
        assertTrue(converters.contains("ResourceType"))
        assertTrue(converters.contains("TagCategory"))
        assertTrue(converters.contains("TagRole"))
    }

    @Test
    fun daosSeederAndRepositoriesExposeMvpContracts() {
        val resourceDao = projectFile("app/src/main/java/com/finnvek/anchor/data/local/dao/ResourceDao.kt")
        val tagDao = projectFile("app/src/main/java/com/finnvek/anchor/data/local/dao/TagDao.kt")
        val seeder = projectFile("app/src/main/java/com/finnvek/anchor/data/local/seed/TagSeeder.kt")
        val resourceRepository = projectFile("app/src/main/java/com/finnvek/anchor/repository/ResourceRepository.kt")
        val tagRepository = projectFile("app/src/main/java/com/finnvek/anchor/repository/TagRepository.kt")
        val settingsRepository = projectFile("app/src/main/java/com/finnvek/anchor/repository/SettingsRepository.kt")
        val module = projectFile("app/src/main/java/com/finnvek/anchor/di/DatabaseModule.kt")

        listOf(
            "observeRecentResources",
            "observeLibraryResources",
            "observeHelpResults",
            "incrementUsage",
            "deleteResource",
        ).forEach { expected -> assertTrue("Missing ResourceDao token $expected", resourceDao.contains(expected)) }

        listOf(
            "observeVisibleTags",
            "getPresetKeys",
            "hidePreset",
            "deleteCustom",
        ).forEach { expected -> assertTrue("Missing TagDao token $expected", tagDao.contains(expected)) }

        assertTrue(seeder.contains("seedIfNeeded"))
        assertTrue(seeder.contains("AnchorPresetTags"))
        assertTrue(resourceRepository.contains("interface ResourceRepository"))
        assertTrue(tagRepository.contains("interface TagRepository"))
        assertTrue(settingsRepository.contains("interface SettingsRepository"))
        assertTrue(module.contains("Room.databaseBuilder"))
        assertTrue(module.contains("AnchorDatabase"))
    }

    @Test
    fun presetTagsUseStableKeysInNeedFirstCaptureOrder() {
        val presets = projectFile("app/src/main/java/com/finnvek/anchor/data/local/seed/AnchorPresetTags.kt")

        listOf(
            "calm",
            "safe",
            "joyful",
            "energized",
            "melancholy",
            "anxious",
            "angry",
            "restless",
            "numb",
            "sad",
            "calm_down",
            "release_anger",
            "feel_safe",
            "get_energy",
            "ground_myself",
            "feel_less_alone",
            "fall_asleep",
            "ride_out_a_wave",
        ).forEach { expected -> assertTrue("Missing preset key $expected", presets.contains(expected)) }

        assertTrue(presets.indexOf("Need") < presets.indexOf("Feeling"))
    }

    private fun projectFile(relativePath: String): String =
        File(rootDir, relativePath).also { assertTrue("$relativePath should exist", it.isFile) }.readText()
}
