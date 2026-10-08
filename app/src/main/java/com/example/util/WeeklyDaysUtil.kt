package com.example.util

import com.example.data.local.InstituteWeeklyDayEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object WeeklyDaysUtil {
    /**
     * Map Calendar.DAY_OF_WEEK (Sunday=1, Monday=2..) to dayIndex (1: Monday .. 7: Sunday)
     */
    fun getDayIndexForCalendarDay(calDayOfWeek: Int): Int {
        return when (calDayOfWeek) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    fun getDayIndexForDateString(dateStr: String): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance().apply { time = sdf.parse(dateStr) ?: Date() }
            getDayIndexForCalendarDay(cal.get(Calendar.DAY_OF_WEEK))
        } catch (e: Exception) {
            1
        }
    }

    fun getDayEntityForDate(dateStr: String, weeklyDays: List<InstituteWeeklyDayEntity>): InstituteWeeklyDayEntity? {
        val idx = getDayIndexForDateString(dateStr)
        return weeklyDays.find { it.dayIndex == idx }
    }

    fun isDateOff(dateStr: String, weeklyDays: List<InstituteWeeklyDayEntity>): Boolean {
        val day = getDayEntityForDate(dateStr, weeklyDays)
        return day != null && !day.isOn
    }

    fun getTodayEntity(weeklyDays: List<InstituteWeeklyDayEntity>): InstituteWeeklyDayEntity? {
        val cal = Calendar.getInstance()
        val idx = getDayIndexForCalendarDay(cal.get(Calendar.DAY_OF_WEEK))
        return weeklyDays.find { it.dayIndex == idx }
    }

    fun isTodayOff(weeklyDays: List<InstituteWeeklyDayEntity>): Boolean {
        val today = getTodayEntity(weeklyDays)
        return today != null && !today.isOn
    }

    fun getDayNameFromDate(dateStr: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val out = SimpleDateFormat("EEEE", Locale.getDefault())
            val d = sdf.parse(dateStr) ?: Date()
            out.format(d)
        } catch (e: Exception) {
            ""
        }
    }
}
