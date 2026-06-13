package com.finnvek.anchor.ui.onboarding

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finnvek.anchor.R
import com.finnvek.anchor.repository.SettingsRepository
import com.finnvek.anchor.ui.theme.LocalAnchorSpacing
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    fun updateOnboardingDone() {
        viewModelScope.launch {
            settingsRepository.updateOnboardingDone(true)
        }
    }
}

@Composable
fun OnboardingRoute(
    onDone: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    OnboardingScreen(
        onDone = {
            viewModel.updateOnboardingDone()
            onDone()
        },
    )
}

@Composable
private fun OnboardingScreen(
    onDone: () -> Unit,
) {
    var page by remember { mutableIntStateOf(0) }
    val step = OnboardingSteps[page]
    val spacing = LocalAnchorSpacing.current

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .semantics { isTraversalGroup = true }
                .padding(spacing.large),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
            Text(
                text = stringResource(step.titleRes),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(step.bodyRes),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = onDone) {
                Text(stringResource(R.string.onboarding_skip))
            }
            Button(
                onClick = {
                    if (page == OnboardingSteps.lastIndex) {
                        onDone()
                    } else {
                        page += 1
                    }
                },
            ) {
                Text(
                    stringResource(
                        if (page == OnboardingSteps.lastIndex) {
                            R.string.onboarding_start
                        } else {
                            R.string.onboarding_next
                        },
                    ),
                )
            }
        }
    }
}

private data class OnboardingStep(
    @param:StringRes val titleRes: Int,
    @param:StringRes val bodyRes: Int,
)

private val OnboardingSteps =
    listOf(
        OnboardingStep(R.string.onboarding_step_one_title, R.string.onboarding_step_one_body),
        OnboardingStep(R.string.onboarding_step_two_title, R.string.onboarding_step_two_body),
    )
