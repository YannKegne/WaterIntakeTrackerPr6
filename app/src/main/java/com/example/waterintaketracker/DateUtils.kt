package com.example.waterintaketracker

import java.util.Calendar

object DateUtils {

    /** Midnight (local time) of the day containing [millis]. */
    fun startOfDay(millis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun startOfToday(): Long = startOfDay(System.currentTimeMillis())

    /** Midnight of the day [daysAgo] days before today (0 = today). */
    fun startOfDayAgo(daysAgo: Int): Long = Calendar.getInstance().apply {
        timeInMillis = startOfToday()
        add(Calendar.DAY_OF_YEAR, -daysAgo)
    }.timeInMillis
}
