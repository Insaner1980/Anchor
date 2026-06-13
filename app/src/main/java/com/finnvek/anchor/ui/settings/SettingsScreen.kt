package com.finnvek.anchor.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finnvek.anchor.R
import com.finnvek.anchor.data.settings.ANCHOR_LOCALE_ENGLISH
import com.finnvek.anchor.data.settings.ANCHOR_LOCALE_FINNISH
import com.finnvek.anchor.domain.model.ThemeMode
import com.finnvek.anchor.ui.theme.LocalAnchorSpacing

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        onThemeSelected = viewModel::selectThemeMode,
        onLocaleSelected = viewModel::selectAppLocale,
    )
}

@Composable
private fun SettingsScreen(
    uiState: SettingsUiState,
    onThemeSelected: (ThemeMode) -> Unit,
    onLocaleSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAnchorSpacing.current

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.large),
    ) {
        Text(
            text = stringResource(R.string.screen_settings),
            style = MaterialTheme.typography.headlineMedium,
        )

        SettingsSection(titleRes = R.string.settings_theme_title) {
            SettingsSegmentedPicker(
                options = ThemeOptions,
                selectedValue = uiState.themeMode,
                onSelected = onThemeSelected,
            )
        }

        SettingsSection(titleRes = R.string.settings_language_title) {
            SettingsSegmentedPicker(
                options = LanguageOptions,
                selectedValue = uiState.selectedAppLocale,
                onSelected = onLocaleSelected,
            )
        }

        SettingsSection(titleRes = R.string.settings_privacy_title) {
            Text(
                text = stringResource(R.string.settings_privacy_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsSection(
    @StringRes titleRes: Int,
    content: @Composable () -> Unit,
) {
    val spacing = LocalAnchorSpacing.current

    Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleMedium,
        )
        content()
        HorizontalDivider()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SettingsSegmentedPicker(
    options: List<SettingsOption<T>>,
    selectedValue: T,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option.value == selectedValue,
                onClick = { onSelected(option.value) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                label = {
                    Text(
                        text = stringResource(option.labelRes),
                        maxLines = 1,
                        softWrap = false,
                    )
                },
            )
        }
    }
}

private data class SettingsOption<T>(
    val value: T,
    @param:StringRes val labelRes: Int,
)

private val ThemeOptions =
    listOf(
        SettingsOption(ThemeMode.SYSTEM, R.string.settings_theme_system),
        SettingsOption(ThemeMode.LIGHT, R.string.settings_theme_light),
        SettingsOption(ThemeMode.DARK, R.string.settings_theme_dark),
        SettingsOption(ThemeMode.CALM, R.string.settings_theme_calm),
    )

private val LanguageOptions: List<SettingsOption<String?>> =
    listOf(
        SettingsOption(null, R.string.settings_language_system),
        SettingsOption(ANCHOR_LOCALE_FINNISH, R.string.settings_language_finnish),
        SettingsOption(ANCHOR_LOCALE_ENGLISH, R.string.settings_language_english),
    )
