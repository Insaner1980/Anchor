package com.finnvek.anchor.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finnvek.anchor.R
import com.finnvek.anchor.domain.model.ResourceSummary
import com.finnvek.anchor.ui.theme.LocalAnchorSpacing

@Composable
fun HomeRoute(
    onHelpNow: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onHelpNow = onHelpNow,
    )
}

@Composable
private fun HomeScreen(
    uiState: HomeUiState,
    onHelpNow: () -> Unit,
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
        HelpHero(onHelpNow = onHelpNow)
        Text(
            text = stringResource(R.string.home_recent),
            style = MaterialTheme.typography.titleMedium,
        )
        if (uiState.recentResources.isEmpty()) {
            Text(
                text = stringResource(R.string.home_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            uiState.recentResources.forEach { resource ->
                ResourceSummaryRow(resource = resource)
            }
        }
    }
}

@Composable
private fun HelpHero(onHelpNow: () -> Unit) {
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = LocalAnchorSpacing.current.helpHeroMinHeight)
                .clickable(onClick = onHelpNow),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Text(
            text = stringResource(R.string.home_help_now),
            modifier = Modifier.padding(LocalAnchorSpacing.current.large),
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}

@Composable
fun ResourceSummaryRow(
    resource: ResourceSummary,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAnchorSpacing.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.tiny),
    ) {
        Text(
            text = resource.title,
            style = MaterialTheme.typography.titleMedium,
        )
        resource.subtitle?.let { subtitle ->
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
