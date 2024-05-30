package com.example.zenzonescheduler

import java.util.concurrent.TimeUnit

data class TimeComponents(
    val hours: Long,
    val minutes: Long,
    val seconds: Long)
{   companion object {
    fun fromMilliseconds(milliseconds: Long): TimeComponents {
        val hours = milliseconds / 3600000
        val minutes = (milliseconds / 60000) % 60
        val seconds = (milliseconds / 1000) % 60
        return TimeComponents(hours, minutes, seconds)
    }
}
    fun toMilliseconds(): Long {
        val hoursInMilliseconds = TimeUnit.HOURS.toMillis(hours)
        val minutesInMilliseconds = TimeUnit.MINUTES.toMillis(minutes)
        val secondsInMilliseconds = TimeUnit.SECONDS.toMillis(seconds)
        return hoursInMilliseconds + minutesInMilliseconds + secondsInMilliseconds
    }
}

object TimeUtils {
    fun formatTime(time: Long): TimeComponents {
        val hours = TimeUnit.MILLISECONDS.toHours(time)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60

        return TimeComponents(hours, minutes, seconds)
    }

    fun remainingTime(timerTime: TimeComponents, goalTime: TimeComponents): TimeComponents {
        val timerMillis = timerTime.toMilliseconds()
        val goalMillis = goalTime.toMilliseconds()
        val remainingMillis = goalMillis - timerMillis
        return if (remainingMillis > 0) formatTime(remainingMillis) else TimeComponents(0, 0, 0)
    }
}



