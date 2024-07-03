package com.example.productivitygame.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.productivitygame.R
import com.example.productivitygame.ui.screens.ScheduleDestination
import com.example.productivitygame.ui.screens.TimerDestination

sealed class Screen(val route: String, @StringRes val labelResId: Int, @DrawableRes val iconResId: Int) {
    data object Home:
        Screen("", R.string.home_nav_label, R.drawable.baseline_home_24)
    data object Schedule:
        Screen(ScheduleDestination.route, R.string.schedule_nav_label, R.drawable.calendar_icon)
    data object Timer:
        Screen(TimerDestination.route, R.string.timer_nav_label, R.drawable.clock_icon)
}

val items = listOf(
    //Screen.Home,
    Screen.Timer,
    Screen.Schedule
)
@Composable
fun TaskBottomAppBar(navController: NavController, modifier: Modifier = Modifier) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    //TODO: If user is currently editing task, should confirm if they want to save first
    NavigationBar(modifier = modifier) {
        items.forEach {screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = screen.iconResId),
                        contentDescription = stringResource(id = screen.labelResId)
                    )
                },
                label = { Text(stringResource(id = screen.labelResId)) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when reselect same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}