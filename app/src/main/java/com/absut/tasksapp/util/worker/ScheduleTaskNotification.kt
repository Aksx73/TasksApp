package com.absut.tasksapp.util.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.absut.tasksapp.data.Task
import com.absut.tasksapp.util.worker.NotificationWorker.Companion.TASK_ID
import com.absut.tasksapp.util.worker.NotificationWorker.Companion.TASK_TITLE
import java.util.concurrent.TimeUnit

class ScheduleTaskNotification(context: Context, task: Task) {

    val workManager = WorkManager.getInstance(context.applicationContext)

    val taskDeadline = task.dueTime
    val currentTime = System.currentTimeMillis()
    val initialDelay = taskDeadline - currentTime

    val data = workDataOf(
        TASK_ID to task.id,
        TASK_TITLE to task.name
    )

    /*val constraints = Constraints.Builder()
        .setRequiresBatteryNotLow(false)
        .build()*/

    val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
        //.setConstraints(constraints)
        .setInputData(data)
        .build()

    workManager.enqueueUniqueWork(
        taskId.toString(), // Unique work name
        ExistingWorkPolicy.REPLACE, // Replace existing work if any
        workRequest
    )

}