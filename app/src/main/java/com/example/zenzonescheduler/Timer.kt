package com.example.zenzonescheduler
import androidx.compose.runtime.collectAsState

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zenzonescheduler.ui.theme.ZenZoneSchedulerTheme
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit


class Timer : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZenZoneSchedulerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val title = intent.getStringExtra("TASK_TITLE")
                    val hour = intent.getLongExtra("HOURS",0)
                    val min = intent.getLongExtra("MINUTES",0)
                    val sec = intent.getLongExtra("SECONDS",0)

                    val hourGoal = intent.getLongExtra("HOURSGOAL",0)
                    val minGoal = intent.getLongExtra("MINUTESGOAL",0)
                    val secGoal = intent.getLongExtra("SECONDSGOAL",0)


                    Timer(hour,min,sec, "${title}",hourGoal, minGoal, secGoal)
                }
            }
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Timer(
    hour:Long,
    min:Long,
    sec:Long,
    taskName:String,
    hourGoal:Long,
    minGoal:Long,
    secGoal:Long) {

    val taskViewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(context = LocalContext.current)
    )

    var time by remember { mutableStateOf(TimeComponents(hour,min,sec)) }
    var isRun by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf(0L) }
    var isShowAlert:Boolean by remember { mutableStateOf(false) }
    var isFinish by remember { mutableStateOf(false) }
    var isReset by remember { mutableStateOf(false) }
    var isBack by remember { mutableStateOf(false) }
    var isGoal by remember { mutableStateOf(false) }
    var isGoalContinue by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    // Collecting completed tasks total time state
    val completedTasksTotalTime by taskViewModel.completedTasksTotalTime.collectAsState()


    Row {
        ExtendedFloatingActionButton(
            onClick = {
                isShowAlert = true
                isBack = true
                taskViewModel.updateTaskTime("${taskName}", TimeComponents(time.hours,time.minutes,time.seconds))
            },

            icon = { Icon(Icons.Filled.ArrowBack, "Task Name") },
            text = { Text(text = "${taskName}") },
            modifier = Modifier.weight(1f)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Row {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription ="task",
                modifier = Modifier.size(12.dp) )
            Text(
                text = " ${taskName}",
                fontSize = 14.sp,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(9.dp, top = 0.dp, end = 0.dp, bottom = 0.dp)        )
        }
        Spacer(modifier =Modifier.height(18.dp))

        Row {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Goal",
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Goal ${String.format("%02d:%02d", hourGoal, minGoal)}",
                fontSize = 18.sp,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(9.dp, top = 0.dp, end = 0.dp, bottom = 0.dp)
            )
        }
        Spacer(modifier =Modifier.height(18.dp))
        Text(
            text = String.format("%02d:%02d:%02d", time.hours, time.minutes, time.seconds),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(9.dp)        )


        Spacer(modifier =Modifier.height(18.dp))

        Row {
            Button(onClick = {
                if(isRun)
                    isRun = false
                else{
                    startTime = System.currentTimeMillis() - time.toMilliseconds()

                    isRun = true
                    keyboardController?.hide()
                }
            }, modifier = Modifier.weight(1f)) {
                Text(text = if(isRun)"Pause" else "Start", color = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    isShowAlert = true
                    isFinish = true},
                modifier = Modifier.weight(1f)) {
                Text(text = "Finish", color = Color.White)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = {
                    isShowAlert = true
                    isReset = true},
                modifier = Modifier.weight(1f)) {
                Text(text = "Reset", color = Color.White)
            }

        }
    }
    if (isShowAlert){
        if (isReset){
            isRun = false
            alert(
                onDismissRequest={
                    isShowAlert = false
                    isReset = false
                    startTime = System.currentTimeMillis() - time.toMilliseconds()
                    isRun = true},
                onConfirmationRequest = {
                    time = TimeComponents(0,0,0)
                    isRun = false
                    isShowAlert = false
                    isReset = false
                },
                "Reset",
                "Are you sure you want to reset the timer?",
                "The timer is reset"
            )
        }
        if (isFinish){
            isRun = false
            alert(
                onDismissRequest={
                    isShowAlert = false
                    isFinish = false
                    startTime = System.currentTimeMillis() - time.toMilliseconds()
                    isRun = true},
                onConfirmationRequest = {
                    isRun = false
                    isShowAlert = false
                    isFinish = false
                    // **Calculate and pass `timeTaken` to `removeTask`**
                    val elapsedMillis = System.currentTimeMillis() - startTime

                    val elapsedHours = TimeUnit.MILLISECONDS.toHours(elapsedMillis)
                    val elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis) % 60
                    val elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60
                    var timeTaken = TimeComponents(elapsedHours, elapsedMinutes, elapsedSeconds)
                    if(startTime == 0L){
                        timeTaken = TimeComponents(0,0,0)
                    }

                    taskViewModel.removeTask(taskName, timeTaken)
                    //taskViewModel.removeTask("${taskName}")


                    val navigate = Intent(context,MainActivity::class.java)
                    context.startActivity(navigate)
                },
                "Finish",
                "Are you sure you want to finish the task?",
                "The task is completed"
            )
        }
        if (isBack){
            isRun = false
            alert(
                onDismissRequest={
                    isShowAlert = false
                    isBack = false
                    startTime = System.currentTimeMillis() - time.toMilliseconds()
                    isRun = true},
                onConfirmationRequest = {
                    isRun = false
                    isShowAlert = false
                    isBack = false

                    val navigate = Intent(context,MainActivity::class.java)
                    context.startActivity(navigate)
                },
                "Back",
                "If you go back the timer will stop, are you sure you want to go back?",
                "The task is paused"
            )
        }
        if (isGoal){
            isRun = false
            GoalAlert(
                onDismissRequest={
                    isGoal = false
                    isShowAlert = false
                    startTime = System.currentTimeMillis() - time.toMilliseconds()
                    isGoalContinue = true
                    isRun = true},
                onConfirmationRequest = {
                    isRun = false
                    isGoal = true
                    isShowAlert = false
                    isGoalContinue = false

                    val elapsedMillis = System.currentTimeMillis() - startTime
                    val elapsedHours = TimeUnit.MILLISECONDS.toHours(elapsedMillis)
                    val elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis) % 60
                    val elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60
                    val timeTaken = TimeComponents(elapsedHours, elapsedMinutes, elapsedSeconds)

                    taskViewModel.removeTask(taskName, timeTaken)
                    val navigate = Intent(context,MainActivity::class.java)
                    context.startActivity(navigate)
                },
            )
        }
    }

    LaunchedEffect(isRun) {
        while (isRun) {
            delay(1000)
            val currentTime = System.currentTimeMillis()
            val elapsedMillis = currentTime - startTime
            val elapsedHours = TimeUnit.MILLISECONDS.toHours(elapsedMillis)
            val elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis) % 60
            val elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60
            time = TimeComponents(elapsedHours, elapsedMinutes, elapsedSeconds)

            val currentTotalSeconds = time.hours * 3600 + time.minutes * 60 + time.seconds
            val goalTotalSeconds = hourGoal * 3600 + minGoal * 60 + secGoal

            if (currentTotalSeconds == goalTotalSeconds) {
                isGoal = true
                isShowAlert = true
                isRun = false
                if (isGoalContinue == true) {
                    //taskViewModel.removeTask("${taskName}")
                    val elapsedMillis = System.currentTimeMillis() - startTime
                    val elapsedHours = TimeUnit.MILLISECONDS.toHours(elapsedMillis)
                    val elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis) % 60
                    val elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60
                    val timeTaken = TimeComponents(elapsedHours, elapsedMinutes, elapsedSeconds)

                    taskViewModel.removeTask(taskName, timeTaken)
                }
            }
        }
    }
}

@Composable
fun alert(
    onDismissRequest:()->Unit,
    onConfirmationRequest: ()->Unit,
    title: String,
    description: String,
    toast: String){

    val context = LocalContext.current

    AlertDialog(
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        onDismissRequest = { onDismissRequest() },
        title = { Text(text = "${title}")},
        text = { Text(text = "${description}")},
        confirmButton = {
            TextButton(onClick = {onConfirmationRequest()
                Toast.makeText(context,"${toast}", Toast.LENGTH_LONG).show()}) {
                Text(text = "Confirm",color = Color.Green)}
        },
        dismissButton = {
            TextButton(onClick = {onDismissRequest()
            }) {
                Text(text = "Cancel",color = Color.Black)}
        },
        containerColor = Color.White)

}
@Composable
fun GoalAlert(
    onDismissRequest:()->Unit,
    onConfirmationRequest: ()->Unit){

    val context = LocalContext.current

    AlertDialog(
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        onDismissRequest = { onDismissRequest() },
        title = { Text(text = "Goal Reached")},
        text = { Text(text = "You arrived the goal do you want to continue?")},
        confirmButton = {
            TextButton(onClick = {onConfirmationRequest()
                Toast.makeText(context,"Congratulations you arrived the goal! ", Toast.LENGTH_LONG).show()}) {
                Text(text = "No",color = Color.Green)}
        },
        dismissButton = {
            TextButton(onClick = {onDismissRequest()


            }) {
                Text(text = "Continue",color = Color.Black)}
        },
        containerColor = Color.White)

}