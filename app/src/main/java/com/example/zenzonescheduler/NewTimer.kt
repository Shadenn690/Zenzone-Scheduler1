package com.example.zenzonescheduler
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DigitalClockInput(newTask:String, description:String) {
    var hours by remember { mutableStateOf("00") }
    var minutes by remember { mutableStateOf("00") }
    var showTimePickerDialog by remember { mutableStateOf(false) }

    Row (modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically){
        Text(
            text = "Goal",
            fontSize = 30.sp,
            color = Color.Gray

        )
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "icon",
            modifier = Modifier.size(28.dp),
            tint = Color.Gray

        )

    }
    Box(modifier = Modifier.fillMaxWidth(),contentAlignment = Alignment.Center ) {
        Row(
            modifier = Modifier
                .clickable { showTimePickerDialog = true }
                .padding(32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = hours,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = ":",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = minutes,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        if (showTimePickerDialog) {
            CombinedTimePickerDialog(hours, minutes) { newHours, newMinutes ->
                hours = newHours
                minutes = newMinutes
                showTimePickerDialog = false
            }
        }
    }
    Spacer(modifier =Modifier.height(90.dp))
    SetTimerButton(hours.toLong(),minutes.toLong(),newTask, description)

}

@Composable
fun CombinedTimePickerDialog(hours: String, minutes: String, onTimeSelected: (String, String) -> Unit) {
    var tempHours by remember { mutableStateOf("") }
    var tempMinutes by remember { mutableStateOf("") }

    AlertDialog(
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        onDismissRequest = { },
        title = { Text("Set Time") },
        text = {
            Column {
                TextField(
                    value = tempHours,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } && newValue.toIntOrNull() in 0..23) {
                            tempHours = newValue
                        }
                    },
                    label = { Text("Hours") },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth() ,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    keyboardActions = KeyboardActions(onDone = { onTimeSelected(tempHours, tempMinutes) }),

                    )

                TextField(
                    value = tempMinutes,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() } && newValue.toIntOrNull() in 0..59) {
                            tempMinutes = newValue
                        }
                    },
                    label = { Text("Minutes") },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    keyboardActions = KeyboardActions(onDone = { onTimeSelected(tempHours, tempMinutes) }),

                    )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onTimeSelected(tempHours.padStart(2, '0'), tempMinutes.padStart(2, '0'))
                }
            ) {
                Text("Confirm")
            }
        }
    )
}

@Composable
fun TaskNameCard( ) {

    var taskName by remember { mutableStateOf("New Task") }
    var taskDescription by remember { mutableStateOf(" Description") }
    var nameDialog by remember { mutableStateOf(false) }

    Row(modifier = Modifier.padding(8.dp)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            Text(
                text = taskName,
                color = Color.Black,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,)

            Text(
                text = taskDescription,
                color = Color.DarkGray,
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,)
        }
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit",
            modifier = Modifier
                .clickable { nameDialog = true }
                .padding(8.dp),
        )
        if (nameDialog) {

            nameAlert(
                taskName,
                        taskDescription
                , onDismissRequest ={ nameDialog = false } , onConfirmationRequest = { newName, newDescription ->
                    taskName = newName;
                    taskDescription = newDescription;
                    nameDialog = false;
                }
            )
        }
    }
    Spacer(modifier =Modifier.height(160.dp))
    DigitalClockInput(taskName, taskDescription)
}

@Composable
fun SetTimerButton(hours:Long,minutes:Long,newTask:String, description:String ) {
    val taskViewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(context = LocalContext.current)
    )
    val context = LocalContext.current

    ExtendedFloatingActionButton(
        onClick = {
            if (hours == 0L && minutes == 0L){
                Toast.makeText(context, "Timer cannot be set to 00:00", Toast.LENGTH_LONG).show()
            } else if(taskViewModel.taskNameExists(newTask)) {
                Toast.makeText(context, "Task with this name already exists", Toast.LENGTH_LONG).show()
            }else {
                val goalTime = TimeComponents(hours, minutes, 0).toMilliseconds()
                taskViewModel.addTask(TaskType(newTask, description, TimeUtils.formatTime(0), TimeUtils.formatTime(goalTime)))
                Toast.makeText(context, "New task added", Toast.LENGTH_LONG).show()
                val navigate = Intent(context, MainActivity::class.java)
                context.startActivity(navigate)
            }
        },
        icon = { },
        text = { Text(text = "Set Timer") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun QuitButton() {
    val context = LocalContext.current

    ExtendedFloatingActionButton(

        onClick = { val navigate = Intent(context,MainActivity::class.java)
            context.startActivity(navigate)},
        icon = { },
        text = { Text(text = "Quit") },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun nameAlert(
    taskName:String,
    taskDescription:String,
    onDismissRequest:()->Unit,
    onConfirmationRequest:(name:String,description:String)->Unit,
){
    val context = LocalContext.current
    var name by remember { mutableStateOf(taskName) }
    var description by remember { mutableStateOf(taskDescription) }
    var isNameFocused by remember { mutableStateOf(false) }
    var isDescriptionFocused by remember { mutableStateOf(false) }

    AlertDialog(
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        onDismissRequest = {  },
        title = { Text(text = "New Task")},
        text = {
               Column {
                   TextField(
                       value = name,
                       onValueChange = {name = it},
                       label = { Text("Task name") },
                       singleLine = true,
                       modifier = Modifier
                           .fillMaxWidth()
                           .onFocusChanged { focusState ->
                               if (focusState.isFocused && name == taskName) {
                                   name = ""
                               }
                               isNameFocused = focusState.isFocused
                           },
                   )
                   TextField(
                       value = description,
                       onValueChange = { description = it },
                       label = { Text("Task description") },
                       singleLine = true,
                       modifier = Modifier.fillMaxWidth().onFocusChanged { focusState ->
                           if (focusState.isFocused && description == taskDescription) {
                               description = ""
                           }
                           isDescriptionFocused = focusState.isFocused
                       },
                   )
               }
            },
        confirmButton = {
            TextButton(onClick = {onConfirmationRequest(name,description)

                }) {
                Text(text = "Confirm",color = Color.Green)}
        },
        dismissButton = {
            TextButton(onClick = {onDismissRequest()}) {
                Text(text = "Cancel",color = Color.Black)}
        },
        containerColor = Color.White)
}

@Composable
fun NewTimer(padding: PaddingValues){

    Column {
        Spacer(modifier =Modifier.height(15.dp))
        TaskNameCard()

        Spacer(modifier =Modifier.height(18.dp))
        QuitButton()
    }


}
