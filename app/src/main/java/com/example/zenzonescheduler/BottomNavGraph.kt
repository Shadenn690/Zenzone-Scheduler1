package com.example.zenzonescheduler

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun BottomNavGraph(navController: NavHostController, padding: PaddingValues){

    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.ScheduleScreen.route
        ){
        composable(route = BottomBarScreen.ScheduleScreen.route){
            ScheduleScreen(padding = padding)
        }
        composable(route = BottomBarScreen.NewTimer.route){
            NewTimer(padding = padding)
        }
        composable(route = BottomBarScreen.Productivity.route){
            ProductivityScreen()
        }
    }
}
