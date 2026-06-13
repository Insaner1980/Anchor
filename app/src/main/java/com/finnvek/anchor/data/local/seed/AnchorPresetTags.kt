package com.finnvek.anchor.data.local.seed

import com.finnvek.anchor.data.local.entity.TagEntity
import com.finnvek.anchor.domain.model.TagCategory

data class PresetTagSpec(
    val presetKey: String,
    val category: TagCategory,
    val sortOrder: Int,
) {
    fun toEntity(): TagEntity =
        TagEntity(
            presetKey = presetKey,
            customLabel = null,
            emoji = null,
            category = category,
            isPreset = true,
            sortOrder = sortOrder,
        )
}

object AnchorPresetTags {
    val Need =
        listOf(
            PresetTagSpec("calm_down", TagCategory.NEED, 0),
            PresetTagSpec("release_anger", TagCategory.NEED, 1),
            PresetTagSpec("feel_safe", TagCategory.NEED, 2),
            PresetTagSpec("get_energy", TagCategory.NEED, 3),
            PresetTagSpec("ground_myself", TagCategory.NEED, 4),
            PresetTagSpec("feel_less_alone", TagCategory.NEED, 5),
            PresetTagSpec("fall_asleep", TagCategory.NEED, 6),
            PresetTagSpec("ride_out_a_wave", TagCategory.NEED, 7),
        )

    val Feeling =
        listOf(
            PresetTagSpec("calm", TagCategory.FEELING, 0),
            PresetTagSpec("safe", TagCategory.FEELING, 1),
            PresetTagSpec("joyful", TagCategory.FEELING, 2),
            PresetTagSpec("energized", TagCategory.FEELING, 3),
            PresetTagSpec("melancholy", TagCategory.FEELING, 4),
            PresetTagSpec("anxious", TagCategory.FEELING, 5),
            PresetTagSpec("angry", TagCategory.FEELING, 6),
            PresetTagSpec("restless", TagCategory.FEELING, 7),
            PresetTagSpec("numb", TagCategory.FEELING, 8),
            PresetTagSpec("sad", TagCategory.FEELING, 9),
        )

    val All = Need + Feeling
}
