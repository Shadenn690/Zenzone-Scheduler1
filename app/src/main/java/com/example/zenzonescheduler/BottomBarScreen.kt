package com.example.zenzonescheduler

import android.graphics.drawable.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomBarScreen (
    val route : String,
    val title : String,
    val icon : ImageVector
){
    object ScheduleScreen :BottomBarScreen(
        route = "scheduleScreen",
        title = "Schedule Screen",
        icon = Icons.Default.Home
    )

    object NewTimer :BottomBarScreen(
        route = "newTimer",
        title = "New Timer",
        icon = Icons.Default.AddCircle
    )

    object Productivity :BottomBarScreen(
        route = "productivity",
        title = "Productivity",
        icon = Icons.Default.DateRange
    )
}