package com.finnvek.anchor.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.finnvek.anchor.ui.capture.CaptureInitialDraft
import com.finnvek.anchor.ui.capture.CaptureRoute
import com.finnvek.anchor.ui.detail.ResourceDetailRoute
import com.finnvek.anchor.ui.help.HelpNeedPickerRoute
import com.finnvek.anchor.ui.help.HelpResultsRoute
import com.finnvek.anchor.ui.home.HomeRoute
import com.finnvek.anchor.ui.library.LibraryRoute
import com.finnvek.anchor.ui.onboarding.OnboardingRoute
import com.finnvek.anchor.ui.photo.PhotoPagerRoute
import com.finnvek.anchor.ui.settings.SettingsRoute
import com.finnvek.anchor.ui.tags.TagManagerRoute
import com.finnvek.anchor.ui.theme.LocalAnchorSpacing

@Composable
fun AnchorAppRoot(
    onboardingDone: Boolean,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val bottomDestinations = AnchorBottomDestinations
    val showBottomBar = bottomDestinations.any { it.route.route == currentRoute }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                AnchorBottomBar(
                    destinations = bottomDestinations,
                    currentRoute = currentRoute,
                    navController = navController,
                )
            }
        },
    ) { contentPadding ->
        AnchorNavHost(
            navController = navController,
            onboardingDone = onboardingDone,
            modifier = Modifier.padding(contentPadding),
        )
    }
}

private val AnchorBottomDestinations =
    listOf(
        AnchorBottomDestination.Home,
        AnchorBottomDestination.Library,
        AnchorBottomDestination.You,
    )

@Composable
private fun AnchorBottomBar(
    destinations: List<AnchorBottomDestination>,
    currentRoute: String?,
    navController: NavHostController,
) {
    NavigationBar {
        destinations.forEach { destination ->
            val label = stringResource(destination.labelRes)
            NavigationBarItem(
                selected = currentRoute == destination.route.route,
                onClick = { navController.navigateTopLevel(destination.route.route) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = label,
                    )
                },
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun AnchorNavHost(
    navController: NavHostController,
    onboardingDone: Boolean,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = if (onboardingDone) AnchorRoute.Home.route else AnchorRoute.Onboarding.route,
        modifier = modifier.fillMaxSize(),
    ) {
        composable(AnchorRoute.Onboarding.route) {
            OnboardingRoute(
                onDone = {
                    navController.navigate(AnchorRoute.Home.route) {
                        popUpTo(AnchorRoute.Onboarding.route) {
                            inclusive = true
                        }
                    }
                },
            )
        }
        composable(AnchorRoute.Home.route) {
            HomeRoute(
                onHelpNow = { navController.navigate(AnchorRoute.HelpNeedPicker.route) },
            )
        }
        composable(AnchorRoute.Library.route) {
            LibraryRoute(
                onResourceSelected = { resourceId ->
                    navController.navigate(AnchorRoute.ResourceDetail.createRoute(resourceId))
                },
            )
        }
        composable(AnchorRoute.Timeline.route) { AnchorPlaceholder(AnchorRoute.Timeline.titleRes) }
        composable(
            route = AnchorRoute.ResourceDetail.route,
            arguments = listOf(navArgument(AnchorRouteArgument.ResourceId) { type = NavType.StringType }),
        ) {
            ResourceDetailRoute(
                onDeleted = { navController.popBackStack() },
            )
        }
        composable(AnchorRoute.HelpNeedPicker.route) {
            HelpNeedPickerRoute(
                onNeedSelected = { tagId ->
                    navController.navigate(AnchorRoute.HelpResults.createRoute(tagId))
                },
                onClose = { navController.popBackStack() },
            )
        }
        composable(
            route = AnchorRoute.HelpResults.route,
            arguments = listOf(navArgument(AnchorRouteArgument.TagId) { type = NavType.StringType }),
        ) {
            HelpResultsRoute(
                onOpenPhoto = { resourceId ->
                    navController.navigate(AnchorRoute.PhotoPager.createRoute(resourceId))
                },
            )
        }
        composable(
            route = AnchorRoute.PhotoPager.route,
            arguments = listOf(navArgument(AnchorRouteArgument.ResourceId) { type = NavType.StringType }),
        ) {
            PhotoPagerRoute()
        }
        composable(AnchorRoute.Settings.route) { SettingsRoute() }
        composable(AnchorRoute.TagManager.route) { TagManagerRoute() }
        composable(AnchorRoute.InternalPhotoCapture.route) {
            CaptureRoute(
                initialDraft = CaptureInitialDraft.EmptyPhoto,
                onFinished = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun AnchorPlaceholder(
    @StringRes titleRes: Int,
    modifier: Modifier = Modifier,
) {
    val spacing = LocalAnchorSpacing.current

    Box(
        modifier = modifier.fillMaxSize().padding(spacing.large),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
    }
}

private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
