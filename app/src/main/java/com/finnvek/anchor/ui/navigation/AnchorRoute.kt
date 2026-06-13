package com.finnvek.anchor.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.finnvek.anchor.R

sealed class AnchorRoute(
    val route: String,
    @param:StringRes val titleRes: Int,
) {
    data object Onboarding : AnchorRoute("onboarding", R.string.screen_onboarding)
    data object Home : AnchorRoute("home", R.string.screen_home)
    data object Library : AnchorRoute("library", R.string.screen_library)
    data object Timeline : AnchorRoute("timeline", R.string.screen_timeline)
    data object ResourceDetail : AnchorRoute("resource/{resourceId}", R.string.screen_resource_detail)
    data object HelpNeedPicker : AnchorRoute("help/need", R.string.screen_help_need)
    data object HelpResults : AnchorRoute("help/results/{tagId}", R.string.screen_help_results)
    data object PhotoPager : AnchorRoute("photos/{resourceId}", R.string.screen_photo_pager)
    data object Settings : AnchorRoute("settings", R.string.screen_settings)
    data object TagManager : AnchorRoute("tags", R.string.screen_tag_manager)
    data object InternalPhotoCapture : AnchorRoute("capture/photo", R.string.screen_photo_capture)

    companion object {
        val All =
            listOf(
                Onboarding,
                Home,
                Library,
                Timeline,
                ResourceDetail,
                HelpNeedPicker,
                HelpResults,
                PhotoPager,
                Settings,
                TagManager,
                InternalPhotoCapture,
            )
    }
}

object AnchorRouteArgument {
    const val ResourceId = "resourceId"
    const val TagId = "tagId"
}

enum class AnchorBottomDestination(
    val route: AnchorRoute,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Home(AnchorRoute.Home, R.string.nav_home, Icons.Outlined.Home),
    Library(AnchorRoute.Library, R.string.nav_library, Icons.Outlined.LibraryMusic),
    You(AnchorRoute.Settings, R.string.nav_you, Icons.Outlined.Person),
}
