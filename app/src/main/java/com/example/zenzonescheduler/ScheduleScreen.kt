package com.example.zenzonescheduler

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.*

@Composable
fun ScheduleScreen(modifier: Modifier = Modifier, padding: PaddingValues = PaddingValues(all = 16.dp),) {

    val taskViewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(context = LocalContext.current)
    )

    val tasks by taskViewModel.taskList.collectAsState()

    val today = LocalDate.now().dayOfMonth
    val index = (today - 1) % quoteList.size
    val dailyQuote = quoteList[index].quote
    val currentDate = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)

    // Calculate remaining times and filter out the ones with no positive remaining time
    val validTasks = tasks.mapNotNull { task ->
        val remainingMillis = TimeUtils.remainingTime(task.timerTime, task.goalTime).toMilliseconds()
        if (remainingMillis >= 0) task to remainingMillis else null
    }.sortedBy { it.second } // Sort by remaining milliseconds

    // Select the most recent task if available
    val recentTask = validTasks.firstOrNull()?.first


    Column(modifier = modifier.padding(vertical = 4.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally) {

        quotes(dailyQuote)

        Spacer(modifier = Modifier.height(15.dp))

    //        recentTask?.let {
    //            RecentTask(it.title, it.timerTime)
    //        }

        Spacer(modifier = Modifier.height(15.dp))

        Text(text = " ${currentDate.format(dateFormatter)}",fontSize = 16.sp,)

        Tasks(modifier = Modifier.padding(padding), taskList = tasks.filter{ !it.isCompleted })

    }
}

@Composable
fun Task(title: String, description: String, timerTime:TimeComponents, goalTime:TimeComponents, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Surface(
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = "Icon",
            )
            Spacer(modifier = Modifier.width(18.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$title",
                    fontSize = 16.sp,
                )
                Text(
                    text = "$description",
                    fontSize = 12.sp,
                )
                Text(
                    text = "Goal: ${String.format("%02d:%02d", goalTime.hours, goalTime.minutes)}",
                    fontSize = 12.sp,
                )
            }
            Spacer(modifier = Modifier.width(18.dp))

            OutlinedButton(
                onClick = {
                    val navigate = Intent(context,Timer::class.java)
                    navigate.putExtra("TASK_TITLE", title)

                    navigate.putExtra("HOURSGOAL", goalTime.hours)
                    navigate.putExtra("MINUTESGOAL", goalTime.minutes)
                    navigate.putExtra("HOURS", timerTime.hours);
                    navigate.putExtra("MINUTES", timerTime.minutes);
                    navigate.putExtra("SECONDS", timerTime.seconds);
                    context.startActivity(navigate)
                },
                modifier = Modifier
            ) {
                Text(text = String.format("%02d:%02d:%02d", timerTime.hours, timerTime.minutes, timerTime.seconds), color = Color.White)
                Spacer(modifier = Modifier.width(18.dp))
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "goTask",
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
            }


        }
    }
}

@Composable
fun RecentTask(title: String, timerTime:TimeComponents, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Surface(
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(30.dp)) {

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = String.format("%02d:%02d:%02d", timerTime.hours, timerTime.minutes, timerTime.seconds),
                    fontSize = 20.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(15.dp))

                Row {

                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "icon",
                        tint = Color.White

                    )
                    Spacer(modifier = Modifier.width(18.dp))
                    Text(
                        text = "${title}",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.width(18.dp))

            OutlinedButton(
                onClick = {
                    val navigate = Intent(context, Timer::class.java)
                    navigate.putExtra("TASK_TITLE", title)
                    navigate.putExtra("HOURS", timerTime.hours)
                    navigate.putExtra("MINUTES", timerTime.minutes)
                    navigate.putExtra("SECONDS", timerTime.seconds)
                    context.startActivity(navigate)
                          },
                modifier = Modifier
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "goTask",
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
            }


        }
    }
}

@Composable
fun quotes(quote: String, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .width(350.dp)
            .height(80.dp),

    ) {
        Row(modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${quote}",
                fontSize = 12.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center

            )
        }
    }
}

@Composable
fun Tasks(modifier: Modifier = Modifier, taskList: List<TaskType>) {
    Column(modifier = modifier.padding(vertical = 4.dp)) {

        for (subTask in taskList) {
            Task(title = subTask.title, description = subTask.description, timerTime = subTask.timerTime,goalTime = subTask.goalTime,)
            Spacer(modifier = Modifier.height(15.dp))
        }
    }
}
