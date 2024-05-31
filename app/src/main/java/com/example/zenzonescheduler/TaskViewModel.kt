package com.example.zenzonescheduler

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Calendar
data class TaskType(
    val title: String,
    val description: String,
    var timerTime: TimeComponents,
    var goalTime:TimeComponents,
    var timeTaken: TimeComponents = TimeComponents(0, 0, 0),
    var completionDate: Long? = null,
    var isCompleted: Boolean = false,
)

class TaskViewModel(private val context: Context) : ViewModel() {

    // Task List StateFlow
    private val _taskList = MutableStateFlow<List<TaskType>>(emptyList())
    val taskList: StateFlow<List<TaskType>> = _taskList.asStateFlow()

    // Completed Task Counter StateFlow
    private val _completedTasks = MutableStateFlow(0) // Task completion counter
    val completedTasks: StateFlow<Int> = _completedTasks.asStateFlow()

    // Completed Tasks Total Time Taken
    private val _completedTasksTotalTime = MutableStateFlow(TimeComponents(0, 0, 0))
    val completedTasksTotalTime: StateFlow<TimeComponents> = _completedTasksTotalTime.asStateFlow()

    private val _completedTasksOfTheDay = MutableStateFlow<List<TaskType>>(emptyList())
    val completedTasksOfTheDay: StateFlow<List<TaskType>> = _completedTasksOfTheDay.asStateFlow()

    private val _weeklyCompletedTasks = MutableStateFlow<Map<String, List<TaskType>>>(emptyMap())
    val weeklyCompletedTasks: StateFlow<Map<String, List<TaskType>>> = _weeklyCompletedTasks.asStateFlow()


    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("TaskPreferences", Context.MODE_PRIVATE)
    }
    init {
        loadTasks()
        loadCompletedTasks()
        loadCompletedTasksTotalTime()
        updateTasksCompletedToday()
        updateTasksCompletedWeekly()
    }

    fun resetProductivityData() {
        viewModelScope.launch {
            _completedTasks.value = 0
            _completedTasksTotalTime.value = TimeComponents(0, 0, 0)
            _completedTasksOfTheDay.value = emptyList()
            val clearedWeeklyTasks = _weeklyCompletedTasks.value.mapValues { emptyList<TaskType>() }
            _weeklyCompletedTasks.value = clearedWeeklyTasks
            val updatedTasks = _taskList.value.filter { it.completionDate == null }
            _taskList.value = updatedTasks
            saveTasks(updatedTasks)
            saveCompletedTasks(0)
            saveCompletedTasksTotalTime(TimeComponents(0, 0, 0))
            Log.d("TaskViewModel", "Productivity data reset")
        }
    }
    fun addTask(task: TaskType) {
        viewModelScope.launch {
            val updatedTasks = _taskList.value.toMutableList()
            updatedTasks.add(task)
            _taskList.value = updatedTasks
            saveTasks(updatedTasks)
        }
    }

    fun removeTask(taskTitle: String, timeTaken: TimeComponents = TimeComponents(0, 0, 0)) {
        viewModelScope.launch {
            val updatedTasks = _taskList.value.toMutableList()
            val task = updatedTasks.find { it.title == taskTitle }
            task?.apply {
                this.timeTaken = timeTaken
                this.completionDate = System.currentTimeMillis()
                this.isCompleted = true
            }
//            updatedTasks.remove(task)
            _taskList.value = updatedTasks
            incrementCompletedTasks()
            updateCompletedTasksTotalTime(timeTaken)
            saveTasks(updatedTasks)
            updateTasksCompletedToday()
            updateTasksCompletedWeekly()

        }
    }

    fun updateTaskTime(taskTitle: String, newTime: TimeComponents) {
        viewModelScope.launch {
            val updatedTasks = _taskList.value.toMutableList()
            val task = updatedTasks.find { it.title == taskTitle }
            task?.timerTime = newTime
            _taskList.value = updatedTasks
            saveTasks(updatedTasks)
        }
    }

    private fun saveTasks(tasks: List<TaskType>) {
        val jsonArray = JSONArray()
        for (task in tasks) {
            val jsonObject = JSONObject().apply {
                put("title", task.title)
                put("description", task.description)
                put("timerTime", JSONObject().apply {
                    put("hours", task.timerTime.hours)
                    put("minutes", task.timerTime.minutes)
                    put("seconds", task.timerTime.seconds)
                })
                put("goalTime", JSONObject().apply {
                    put("hours", task.goalTime.hours)
                    put("minutes", task.goalTime.minutes)
                    put("seconds", task.goalTime.seconds)
                })
                put("timeTaken", JSONObject().apply {
                    put("hours", task.timeTaken.hours)
                    put("minutes", task.timeTaken.minutes)
                    put("seconds", task.timeTaken.seconds)
                })
                put("completionDate", task.completionDate)
                put("isCompleted", if(task.isCompleted) "true" else "false")
            }
            jsonArray.put(jsonObject)
        }
        sharedPreferences.edit().putString("tasks", jsonArray.toString()).apply()
    }


    fun getTotalTimeTaken(): TimeComponents {
        val totalMilliseconds = _taskList.value
            .filter { it.timeTaken != TimeComponents(0, 0, 0) }
            .map { it.timeTaken.toMilliseconds() }
            .sum()
        return TimeComponents.fromMilliseconds(totalMilliseconds)
    }

    private fun loadTasks() {
        val jsonTasks = sharedPreferences.getString("tasks", null)
        jsonTasks?.let {
            val tasks = mutableListOf<TaskType>()
            try {
                val jsonArray = JSONArray(it)
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val title = jsonObject.getString("title")
                    val description = jsonObject.getString("description")

                    val timerTimeJson = jsonObject.getJSONObject("timerTime")
                    val timerTime = TimeComponents(
                        timerTimeJson.getLong("hours"),
                        timerTimeJson.getLong("minutes"),
                        timerTimeJson.getLong("seconds")
                    )

                    val goalTimeJson = jsonObject.getJSONObject("goalTime")
                    val goalTime = TimeComponents(
                        goalTimeJson.getLong("hours"),
                        goalTimeJson.getLong("minutes"),
                        goalTimeJson.getLong("seconds")
                    )
                    val isCompleted = jsonObject.getString("isCompleted")

                    val timeTakenJson = jsonObject.getJSONObject("timeTaken")
                    val timeTaken = TimeComponents(
                        timeTakenJson.getLong("hours"),
                        timeTakenJson.getLong("minutes"),
                        timeTakenJson.getLong("seconds")
                    )

                    val completionDate = jsonObject.optLong("completionDate")

                    tasks.add(TaskType(title, description, timerTime, goalTime, timeTaken, completionDate, isCompleted = isCompleted == "true"))
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            _taskList.value = tasks
            updateTasksCompletedToday() // Ensure to update tasks completed today when loading
            updateTasksCompletedWeekly()
        }
    }
    fun taskNameExists(taskTitle: String): Boolean {
        return _taskList.value.any { it.title == taskTitle }
    }
    private fun incrementCompletedTasks() {
        viewModelScope.launch {
            _completedTasks.value = _completedTasks.value + 1
            saveCompletedTasks(_completedTasks.value)
        }
    }
    private fun saveCompletedTasks(count: Int) {
        sharedPreferences.edit().putInt("completedTasks", count).apply()
    }

    // Load the completed tasks count from SharedPreferences
    private fun loadCompletedTasks() {
        val count = sharedPreferences.getInt("completedTasks", 0)
        _completedTasks.value = count
    }

    // Update the total time taken for completed tasks
    private fun updateCompletedTasksTotalTime(newTaskTime: TimeComponents) {
        viewModelScope.launch {
            val totalMilliseconds =
                _completedTasksTotalTime.value.toMilliseconds() + newTaskTime.toMilliseconds()
            _completedTasksTotalTime.value = TimeComponents.fromMilliseconds(totalMilliseconds)
            saveCompletedTasksTotalTime(_completedTasksTotalTime.value)
        }
    }
    private fun saveCompletedTasksTotalTime(totalTime: TimeComponents) {
        val jsonObject = JSONObject().apply {
            put("hours", totalTime.hours)
            put("minutes", totalTime.minutes)
            put("seconds", totalTime.seconds)
        }
        sharedPreferences.edit().putString("completedTasksTotalTime", jsonObject.toString()).apply()
    }

    // Load the total time taken by completed tasks from SharedPreferences
    private fun loadCompletedTasksTotalTime() {
        val jsonTotalTime = sharedPreferences.getString("completedTasksTotalTime", null)
        jsonTotalTime?.let {
            try {
                val jsonObject = JSONObject(it)
                val hours = jsonObject.getLong("hours")
                val minutes = jsonObject.getLong("minutes")
                val seconds = jsonObject.getLong("seconds")
                _completedTasksTotalTime.value = TimeComponents(hours, minutes, seconds)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }
    private fun updateTasksCompletedToday() {
        val calendar = Calendar.getInstance()
        val todayStart = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val tomorrowStart = calendar.apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis
        val tasksCompletedToday = _taskList.value.filter {
            it.completionDate != null && it.completionDate!! in todayStart until tomorrowStart
        }
        _completedTasksOfTheDay.value = tasksCompletedToday
    }

    private fun updateTasksCompletedWeekly() {
        val calendar = Calendar.getInstance()

        // Set the calendar to the start of the week (Monday)


        // Create a map to store tasks by each day of the week
        val tasksCompletedEachDay = mutableMapOf<String, List<TaskType>>()

        // Iterate through each day of the week (Monday to Sunday)
        for (i in 0 until 7) {


            val todayStart = calendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            // Move to the next day
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val nextDayStart = calendar.timeInMillis

            // Filter tasks completed on this day
            val tasksCompletedToday = _taskList.value
                .filter {
                it.completionDate != null && it.completionDate!! in todayStart until nextDayStart
            }

            // Store the tasks in the map with the day name as the key
            val dayName = when (i) {
                0-> "Friday"
                 1-> "Saturday"
                 2-> "Sunday"
                 3-> "Monday"
                 4-> "Tuesday"
                 5-> "Wednesday"
                else -> "Thursday"
            }
            tasksCompletedEachDay[dayName] = tasksCompletedToday
        }

        // Update your state with the tasksCompletedEachDay map
        _weeklyCompletedTasks.value = tasksCompletedEachDay
    }
    fun printTasks() {
        _taskList.value.forEach {
            Log.d("TaskViewModel", "Task: ${it.title}, Completion Date: ${it.completionDate}, ${it.isCompleted}")
        }
    }




}
