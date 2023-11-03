package com.github.truefmartin.photomapper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.truefmartin.photomapper.Model.Task
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

object NotificationHandler {
    private val applicationContext = PhotoMapper.applicationContext()
    private val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    private fun getFormat(): DateTimeFormatter? {
        return DateTimeFormatter.ofPattern("MM/dd/yy, hh:mm a", Locale.getDefault())
    }
    fun scheduleNotification(task: Task) {
        val time = task.date
        val title = task.title
        val notificationId = task.noteID
        if(time.isAfter(LocalDateTime.now())){
            Log.d("MainActivity","Scheduling Notification")
            val alarmIntent = Intent(this.applicationContext, AlarmReceiver::class.java)
            alarmIntent.putExtra(applicationContext.getString(R.string.EXTRA_ID),notificationId)
            alarmIntent.putExtra(applicationContext.getString(R.string.EXTRA_TITLE_ID), title)
            alarmIntent.putExtra(applicationContext.getString(R.string.EXTRA_TIME_ID), time.format(
                getFormat()
            ))
            val pendingAlarmIntent = PendingIntent.getBroadcast(this.applicationContext,
                notificationId,alarmIntent, PendingIntent.FLAG_IMMUTABLE
            )
            val tz = TimeZone.getDefault().toZoneId().rules.getOffset(time)
            alarmManager?.setWindow(AlarmManager.RTC_WAKEUP,time.toInstant(tz).toEpochMilli(),1000*10,pendingAlarmIntent)
        }
    }

    fun removeNotification(notificationID: Int) {
        val alarmIntent = Intent(this.applicationContext, AlarmReceiver::class.java)
        val pendingAlarmIntent = PendingIntent.getBroadcast(this.applicationContext,
            notificationID,alarmIntent, PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager?.cancel(pendingAlarmIntent)
    }
}