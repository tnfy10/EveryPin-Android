package everypin.app.feature.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

fun NavController.navigateHome(navOptions: NavOptions) {
    navigate(HomeRoute.route, navOptions)
}

fun NavGraphBuilder.homeNavGraph(
    padding: PaddingValues
) {
    composable(route = HomeRoute.route) {
        HomeRoute(
            padding = padding,
        )
    }
}

object HomeRoute {
    const val route = "home"
}