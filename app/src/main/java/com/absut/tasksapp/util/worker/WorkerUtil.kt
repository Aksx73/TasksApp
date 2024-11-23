package com.absut.tasksapp.util.worker

import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.absut.tasksapp.data.Task
import com.absut.tasksapp.util.Util
import com.absut.tasksapp.util.worker.NotificationWorker.Companion.TASK_ID
import com.absut.tasksapp.util.worker.NotificationWorker.Companion.TASK_TITLE
import java.util.concurrent.TimeUnit
import kotlin.to

object WorkerUtil {

    fun scheduleTaskNotification(workManager: WorkManager, task: Task) {
        //if no time is set then by default time will be 9:00 am
        val taskDeadline = Util.getMillisecondsFromDateTime(
            task.dueDate,
            task.dueTime.first,
            task.dueTime.second
        )
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
            .setInputData(data)
            //.setConstraints(constraints)
            .build()

        // check if date/time is of future
        if (initialDelay >= 0) {
            workManager.enqueueUniqueWork(
                task.id.uniqueWorkName(), // Unique work name
                ExistingWorkPolicy.REPLACE, // Replace existing work if any
                workRequest
            )
        } else {
            Log.d("WorkerUtil", "scheduleTaskNotification: Task reminder not scheduled as date/time is of past")
        }
    }

    fun cancelTaskNotification(workManager: WorkManager, taskId: Long) {
        workManager.cancelUniqueWork(taskId.uniqueWorkName())
    }

    fun updateTaskNotification(workManager: WorkManager, task: Task) {
        cancelTaskNotification(workManager, task.id)
        scheduleTaskNotification(workManager, task)
    }

}

fun Long.uniqueWorkName(): String{
    return "work_$this"
}