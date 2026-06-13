package com.finnvek.anchor.ui.photo

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import com.finnvek.anchor.R
import com.finnvek.anchor.domain.model.ResourceDetail
import com.finnvek.anchor.repository.ResourceRepository
import com.finnvek.anchor.ui.navigation.AnchorRouteArgument
import com.finnvek.anchor.ui.theme.LocalAnchorSpacing
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class PhotoPagerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    resourceRepository: ResourceRepository,
) : ViewModel() {

    private val resourceId: Long =
        checkNotNull(savedStateHandle.get<String>(AnchorRouteArgument.ResourceId)).toLong()

    val resource: StateFlow<ResourceDetail?> =
        resourceRepository.observeResourceDetail(resourceId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MS),
            initialValue = null,
        )

    private companion object {
        private const val STATE_STOP_TIMEOUT_MS = 5_000L
    }
}

@Composable
fun PhotoPagerRoute(
    viewModel: PhotoPagerViewModel = hiltViewModel(),
) {
    val resource by viewModel.resource.collectAsStateWithLifecycle()
    PhotoPagerScreen(resource = resource)
}

@Composable
private fun PhotoPagerScreen(
    resource: ResourceDetail?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val spacing = LocalAnchorSpacing.current

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(spacing.large),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        if (resource?.uri == null) {
            Text(
                text = stringResource(R.string.photo_pager_empty),
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            Text(
                text = resource.title,
                style = MaterialTheme.typography.headlineSmall,
            )
            AsyncImage(
                model = context.privatePhotoFile(resource.uri),
                contentDescription = resource.title,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

private fun Context.privatePhotoFile(relativePath: String): File =
    File(filesDir, relativePath)
