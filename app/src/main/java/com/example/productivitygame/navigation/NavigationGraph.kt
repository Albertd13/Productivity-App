package com.example.productivitygame.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.productivitygame.ui.screens.AddTaskDestination
import com.example.productivitygame.ui.screens.AddTaskScreen
import com.example.productivitygame.ui.screens.EditTaskDestination
import com.example.productivitygame.ui.screens.EditTaskScreen
import com.example.productivitygame.ui.screens.ScheduleDestination
import com.example.productivitygame.ui.screens.ViewScheduleScreen

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
                navigateToNewTask = { navController.navigate(AddTaskDestination.route) },
                navigateToEditTask = {
                    navController.navigate("")
                }
            )
        }
        composable(route = AddTaskDestination.route) {
            AddTaskScreen(navigateBack = { navController.popBackStack() })
        }
        composable(route = EditTaskDestination.routeWithArgs,
            arguments = listOf(navArgument(EditTaskDestination.taskIdArg) {
                type = NavType.IntType
            })
        ) {
            EditTaskScreen(navigateBack = { navController.popBackStack() })
        }
    }
}