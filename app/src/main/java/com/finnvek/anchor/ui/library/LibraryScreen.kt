package com.finnvek.anchor.ui.library

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finnvek.anchor.R
import com.finnvek.anchor.domain.model.LibraryContentType
import com.finnvek.anchor.domain.model.LibraryViewMode
import com.finnvek.anchor.domain.model.ResourceSummary
import com.finnvek.anchor.domain.model.TimelineDay
import com.finnvek.anchor.ui.home.ResourceSummaryRow
import com.finnvek.anchor.ui.theme.LocalAnchorSpacing

@Composable
fun LibraryRoute(
    onResourceSelected: (Long) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LibraryScreen(
        uiState = uiState,
        onContentTypeSelected = viewModel::selectContentType,
        onViewModeSelected = viewModel::selectViewMode,
        onQueryChange = viewModel::updateQuery,
        onFavoriteToggle = viewModel::toggleFavoriteOnly,
        onResourceSelected = onResourceSelected,
    )
}

@Composable
private fun LibraryScreen(
    uiState: LibraryUiState,
    onContentTypeSelected: (LibraryContentType) -> Unit,
    onViewModeSelected: (LibraryViewMode) -> Unit,
    onQueryChange: (String) -> Unit,
    onFavoriteToggle: () -> Unit,
    onResourceSelected: (Long) -> Unit,
) {
    val spacing = LocalAnchorSpacing.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        item {
            Text(
                text = stringResource(R.string.screen_library),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        item {
            LibrarySegmentedPicker(
                options = ContentTypeOptions,
                selectedValue = uiState.contentType,
                onSelected = onContentTypeSelected,
            )
        }
        item {
            LibrarySegmentedPicker(
                options = ViewModeOptions,
                selectedValue = uiState.viewMode,
                onSelected = onViewModeSelected,
            )
        }
        item {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = onQueryChange,
                label = { Text(stringResource(R.string.library_search)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            FilterChip(
                selected = uiState.favoriteOnly,
                onClick = onFavoriteToggle,
                label = { Text(stringResource(R.string.library_favorites)) },
            )
        }

        if (uiState.viewMode == LibraryViewMode.TIMELINE) {
            timelineItems(
                days = uiState.timelineDays,
                onResourceSelected = onResourceSelected,
            )
        } else {
            resourceItems(
                resources = uiState.resources,
                onResourceSelected = onResourceSelected,
            )
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.resourceItems(
    resources: List<ResourceSummary>,
    onResourceSelected: (Long) -> Unit,
) {
    if (resources.isEmpty()) {
        item {
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.library_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        items(resources, key = { resource -> resource.id }) { resource ->
            LibraryResourceSurface(onClick = { onResourceSelected(resource.id) }) {
                ResourceSummaryRow(resource = resource)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.timelineItems(
    days: List<TimelineDay>,
    onResourceSelected: (Long) -> Unit,
) {
    if (days.isEmpty()) {
        item {
            Text(
                text = androidx.compose.ui.res.stringResource(R.string.library_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        days.forEach { day ->
            item(key = "day-${day.dayStartMs}") {
                Text(
                    text = day.dayStartMs.toString(),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            items(day.resources, key = { resource -> "${day.dayStartMs}-${resource.id}" }) { resource ->
                LibraryResourceSurface(onClick = { onResourceSelected(resource.id) }) {
                    ResourceSummaryRow(resource = resource)
                }
            }
        }
    }
}

@Composable
private fun LibraryResourceSurface(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> LibrarySegmentedPicker(
    options: List<LibraryOption<T>>,
    selectedValue: T,
    onSelected: (T) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option.value == selectedValue,
                onClick = { onSelected(option.value) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                label = { Text(stringResource(option.labelRes), maxLines = 1) },
            )
        }
    }
}

private data class LibraryOption<T>(
    val value: T,
    @param:StringRes val labelRes: Int,
)

private val ContentTypeOptions =
    listOf(
        LibraryOption(LibraryContentType.ALL, R.string.library_all),
        LibraryOption(LibraryContentType.SONGS, R.string.library_songs),
        LibraryOption(LibraryContentType.PHOTOS, R.string.library_photos),
    )

private val ViewModeOptions =
    listOf(
        LibraryOption(LibraryViewMode.LIBRARY, R.string.library_view_library),
        LibraryOption(LibraryViewMode.TIMELINE, R.string.library_view_timeline),
    )
