package com.github.truefmartin.photomapper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.truefmartin.photomapper.Model.PhotoPath
import com.github.truefmartin.photomapper.NewEditTaskActivity.EXTRA_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(EXTRA_ID,-1)
        Log.d("MyReceiver","Broadcast Received $id")

        val repository = (context.applicationContext as PhotoMapper).repository
        CoroutineScope(SupervisorJob()).launch {
            val photoPath: PhotoPath = repository.getPhotoNotLive(id)
            Log.d("MyReceiver", "Task is ${photoPath.fileName} with date ${photoPath.date}")
        }

    }
}