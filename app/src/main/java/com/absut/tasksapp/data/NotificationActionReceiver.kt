package com.absut.tasksapp.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.absut.tasksapp.util.worker.NotificationWorker.Companion.TASK_ID

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        intent?.let {
            val taskId = intent.getLongExtra(TASK_ID, -1)

            if (taskId > 0){
                val deleteTaskRequest = OneTimeWorkRequestBuilder<UpdateTaskWorker>()
                    .setInputData(workDataOf("taskId" to taskId))
                    .build()
                WorkManager.getInstance(context!!).enqueue(deleteTaskRequest)
            }

        }
    }

}