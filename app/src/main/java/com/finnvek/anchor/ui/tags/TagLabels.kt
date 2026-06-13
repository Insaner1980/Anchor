package com.finnvek.anchor.ui.tags

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.finnvek.anchor.R
import com.finnvek.anchor.domain.model.AnchorTag

@Composable
fun AnchorTag.localizedLabel(): String {
    val presetLabel = presetKey?.let(PresetLabelResources::get)
    return when {
        presetLabel != null -> stringResource(presetLabel)
        customLabel != null -> customLabel
        else -> checkNotNull(presetKey)
    }
}

private val PresetLabelResources =
    mapOf(
        "calm" to R.string.tag_feeling_calm,
        "safe" to R.string.tag_feeling_safe,
        "joyful" to R.string.tag_feeling_joyful,
        "energized" to R.string.tag_feeling_energized,
        "melancholy" to R.string.tag_feeling_melancholy,
        "anxious" to R.string.tag_feeling_anxious,
        "angry" to R.string.tag_feeling_angry,
        "restless" to R.string.tag_feeling_restless,
        "numb" to R.string.tag_feeling_numb,
        "sad" to R.string.tag_feeling_sad,
        "calm_down" to R.string.tag_need_calm_down,
        "release_anger" to R.string.tag_need_release_anger,
        "feel_safe" to R.string.tag_need_feel_safe,
        "get_energy" to R.string.tag_need_get_energy,
        "ground_myself" to R.string.tag_need_ground_myself,
        "feel_less_alone" to R.string.tag_need_feel_less_alone,
        "fall_asleep" to R.string.tag_need_fall_asleep,
        "ride_out_a_wave" to R.string.tag_need_ride_out_a_wave,
    )
