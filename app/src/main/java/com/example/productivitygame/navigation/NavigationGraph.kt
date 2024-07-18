package com.example.productivitygame.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.productivitygame.ui.screens.AddTaskDestination
import com.example.productivitygame.ui.screens.AddTaskScreen
import com.example.productivitygame.ui.screens.EditTaskDestination
import com.example.productivitygame.ui.screens.EditTaskScreen
import com.example.productivitygame.ui.screens.FocusPlanDestination
import com.example.productivitygame.ui.screens.FocusPlanSelectionScreen
import com.example.productivitygame.ui.screens.FocusTimerScreen
import com.example.productivitygame.ui.screens.ScheduleDestination
import com.example.productivitygame.ui.screens.TimerDestination
import com.example.productivitygame.ui.screens.ViewScheduleScreen
import com.example.productivitygame.ui.utils.POMODORO
import com.example.productivitygame.ui.utils.toEpochMillis
import kotlinx.datetime.TimeZone

@Composable
fun InventoryNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = ScheduleDestination.route,
        modifier = modifier
    ) {
        composable(route = ScheduleDestination.route) {
            ViewScheduleScreen(
                navigateToNewTask = { navController.navigate(
                    AddTaskDestination.route +
                            "?${AddTaskDestination.selectedDateInUTCMillisArg}=" +
                            "${it.toEpochMillis(TimeZone.UTC)}"
                ) },
                navigateToEditTask = {
                    navController.navigate("${EditTaskDestination.route}/$it")
                }
            )
        }
        composable(
            route = AddTaskDestination.routeWithArgs,
            arguments = listOf(
                navArgument(AddTaskDestination.selectedDateInUTCMillisArg) {
                    type = NavType.LongType
                }
            )
        ) {
            AddTaskScreen(
                navigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = EditTaskDestination.routeWithArgs,
            arguments = listOf(
                navArgument(EditTaskDestination.taskIdArg) {
                    type = NavType.IntType
                })
        ) {
            EditTaskScreen(navigateBack = { navController.popBackStack() })
        }
        composable(
            route = TimerDestination.routeWithArgs,
            deepLinks = listOf(navDeepLink { uriPattern = "myapp://${TimerDestination.routeWithArgs}" }),
            arguments = listOf(
                navArgument(TimerDestination.focusPlanNameArg) {
                    type = NavType.StringType
                    defaultValue = POMODORO.name
                }
            )
        ) {
            FocusTimerScreen(
                navigateToFocusPlanSelection = { navController.navigate(FocusPlanDestination.route) }
            )
        }
        composable(route = FocusPlanDestination.route) {
            FocusPlanSelectionScreen(
                onSelectFocusPlan = {
                    navController.navigate("${TimerDestination.route}?${TimerDestination.focusPlanNameArg}=$it")
                },
                navigateBack = { navController.popBackStack() }
            )
        }

    }
}