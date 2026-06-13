package com.finnvek.anchor.data.local.seed

import com.finnvek.anchor.data.local.dao.TagDao
import javax.inject.Inject

class TagSeeder @Inject constructor(
    private val tagDao: TagDao,
) {
    suspend fun seedIfNeeded() {
        val existingPresetKeys = tagDao.getPresetKeys().toSet()
        val missingPresets =
            AnchorPresetTags.All
                .filterNot { it.presetKey in existingPresetKeys }
                .map(PresetTagSpec::toEntity)

        if (missingPresets.isNotEmpty()) {
            tagDao.insertTags(missingPresets)
        }
    }
}
