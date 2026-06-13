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
import com.finnvek.anchor.ui.settings.SettingsRoute
import com.finnvek.anchor.ui.theme.LocalAnchorSpacing

@Composable
fun AnchorAppRoot(modifier: Modifier = Modifier) {
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
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = AnchorRoute.Home.route,
        modifier = modifier.fillMaxSize(),
    ) {
        composable(AnchorRoute.Onboarding.route) { AnchorPlaceholder(AnchorRoute.Onboarding.titleRes) }
        composable(AnchorRoute.Home.route) { AnchorPlaceholder(AnchorRoute.Home.titleRes) }
        composable(AnchorRoute.Library.route) { AnchorPlaceholder(AnchorRoute.Library.titleRes) }
        composable(AnchorRoute.Timeline.route) { AnchorPlaceholder(AnchorRoute.Timeline.titleRes) }
        composable(
            route = AnchorRoute.ResourceDetail.route,
            arguments = listOf(navArgument(AnchorRouteArgument.ResourceId) { type = NavType.StringType }),
        ) {
            AnchorPlaceholder(AnchorRoute.ResourceDetail.titleRes)
        }
        composable(AnchorRoute.HelpNeedPicker.route) { AnchorPlaceholder(AnchorRoute.HelpNeedPicker.titleRes) }
        composable(
            route = AnchorRoute.HelpResults.route,
            arguments = listOf(navArgument(AnchorRouteArgument.TagId) { type = NavType.StringType }),
        ) {
            AnchorPlaceholder(AnchorRoute.HelpResults.titleRes)
        }
        composable(
            route = AnchorRoute.PhotoPager.route,
            arguments = listOf(navArgument(AnchorRouteArgument.ResourceId) { type = NavType.StringType }),
        ) {
            AnchorPlaceholder(AnchorRoute.PhotoPager.titleRes)
        }
        composable(AnchorRoute.Settings.route) { SettingsRoute() }
        composable(AnchorRoute.TagManager.route) { AnchorPlaceholder(AnchorRoute.TagManager.titleRes) }
        composable(AnchorRoute.InternalPhotoCapture.route) { AnchorPlaceholder(AnchorRoute.InternalPhotoCapture.titleRes) }
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
