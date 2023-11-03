package com.github.truefmartin.photomapper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.truefmartin.photomapper.MainActivity.MainActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val id = intent.getIntExtra(context.getString(R.string.EXTRA_ID),0)
        val title = intent.getStringExtra(context.getString(R.string.EXTRA_TITLE_ID)) ?: "Task Due"
        val time = intent.getStringExtra(context.getString(R.string.EXTRA_TIME_ID)) ?: "Now"
        Log.d("AlarmReceiver", id.toString())
        val clickIntent = Intent(context, MainActivity::class.java)
        clickIntent.putExtra(context.getString(R.string.EXTRA_ID),id)
        NotificationUtil().createClickableNotification(context,title,time,clickIntent,id)
    }
}
