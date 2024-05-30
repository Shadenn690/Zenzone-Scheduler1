package com.example.zenzonescheduler

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ProductivityScreen() {
    var selectedToggle by remember { mutableStateOf("Day") }

    val taskViewModel: TaskViewModel =
        viewModel(factory = TaskViewModelFactory(context = LocalContext.current))
    taskViewModel.printTasks()

    val completedTasks =
        taskViewModel.completedTasks.collectAsState().value // Get completed tasks count

    val completedTasksTotalTime by taskViewModel.completedTasksTotalTime.collectAsState()
    val completedTasksOfTheDay by taskViewModel.completedTasksOfTheDay.collectAsState()
    val completedTasksOfTheWeek by taskViewModel.weeklyCompletedTasks.collectAsState()
    // Format the total time taken into a readable string
    val formattedTotalTimeTaken = String.format(
        "%02dh %02dm %02ds",
        completedTasksTotalTime.hours,
        completedTasksTotalTime.minutes,
        completedTasksTotalTime.seconds
    )
    val weekdays = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")



    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    )
    {
        Text(
            text = "My Productivity",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),//for spaces between task and time duration
            modifier = Modifier.fillMaxWidth()
        ) {
            // Task Completed Box
            ProductivityItem(
                title = "Tasks Completed",
                count = completedTasks,
                icon = Icons.Filled.CheckCircle,
                iconColor = Color(0xFF34C759), // Green color
                modifier = Modifier.weight(1f)
            )

            // Time Duration Box
            ProductivityItem(
                title = "Time Duration",
                time = formattedTotalTimeTaken,
                icon = Icons.Filled.Timer,
                iconColor = Color(0xFF5856D6), // Blue color
                //timeFontSize = 20,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Centered Toggle Buttons (Day/Week)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            ProductivityToggle(
                selectedToggle = selectedToggle,
                onToggleChange = { selectedToggle = it },
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Show the tasks based on selected toggle
        if (selectedToggle == "Day") {
            DailyTasksList(completedTasksOfTheDay)
        } else {
            // You can implement WeeklyTasksList similarly if needed
            WeeklyTasksList(completedTasksOfTheWeek)
        }



        Spacer(modifier = Modifier.height(24.dp))

        // Reset Button
        Button(
            onClick = {
                taskViewModel.resetProductivityData()
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Reset")
        }


    }
}


@Composable
fun WeeklyTasksList(tasks: Map<String, List<TaskType>>) {
    Column {
        tasks.map{
            (dayName, tasks) ->
            
                Text(dayName, fontSize = 20.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            if(tasks.isEmpty())
                Text(text = "Empty", color = Color.LightGray)
            Column(modifier = Modifier.padding(12.dp)) {
                tasks.forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = task.title)
                        Text(
                            text = String.format(
                                "%02dh %02dm %02ds",
                                task.timeTaken.hours,
                                task.timeTaken.minutes,
                                task.timeTaken.seconds
                            )
                        )
                    }
                    Divider()
                }
            }
        }
    }
}
@Composable
fun DailyTasksList(tasks: List<TaskType>) {
    Column(modifier = Modifier.padding(12.dp)){
        tasks.forEach { task ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = task.title)
                Text(
                    text = String.format(
                        "%02dh %02dm %02ds",
                        task.timeTaken.hours,
                        task.timeTaken.minutes,
                        task.timeTaken.seconds
                    )
                )
            }
            Divider()

        }
    }
}

@Composable
fun ProductivityToggle(
    selectedToggle: String,
    onToggleChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isDaySelected = selectedToggle == "Day"
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .background(Color(0xffe9eaff), shape = RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier

                .weight(1f)
                .shadow(
                    elevation = if (isDaySelected) 2.dp else 0.dp,
                    shape = RoundedCornerShape(8.dp)
                )
                .background(
                    if (isDaySelected) Color.White else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { onToggleChange("Day") }
                .padding(horizontal = 4.dp, vertical = 10.dp)
        ) {
            Text(

                text = "Day",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (selectedToggle == "Day") Color.Black else Color.LightGray,
                modifier = Modifier
                   

            )
        }
        Spacer(modifier = Modifier.width(2.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .shadow(
                    elevation = if (!isDaySelected) 2.dp else 0.dp,
                    shape = RoundedCornerShape(8.dp)
                )
                .background(
                    if (!isDaySelected) Color.White else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { onToggleChange("Week") }
                .padding(horizontal = 4.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Week",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (selectedToggle == "Week") Color.Black else Color.LightGray,
                modifier = Modifier
            )
        }

    }
}


@Composable
fun ProductivityItem(
    title: String,
    count: Int? = null,
    time: String? = null,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier

            .background(
                color = Color(0xFFF5F5F5), // Background color
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = count?.toString() ?: time.orEmpty(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ProductivityScreenPreview() {
    ProductivityScreen()
}
