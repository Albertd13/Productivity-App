package com.example.productivitygame

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.productivitygame.navigation.InventoryNavHost
import com.example.productivitygame.ui.components.TaskBottomAppBar

@Composable
fun ProductivityApp(navController: NavHostController = rememberNavController()) {
    Scaffold(
        bottomBar = {
            TaskBottomAppBar(navController = navController)
        }
    ) { innerPadding ->
        InventoryNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}