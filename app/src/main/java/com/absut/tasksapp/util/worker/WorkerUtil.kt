package com.absut.tasksapp.util.worker

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
        //todo calculate task deadline by combining date/time

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

        workManager.enqueueUniqueWork(
            task.id.toString(), // Unique work name
            ExistingWorkPolicy.REPLACE, // Replace existing work if any
            workRequest
        )
    }

    fun cancelTaskNotification(workManager: WorkManager, taskId: Long) {
        workManager.cancelUniqueWork(taskId.toString())
    }

    fun updateTaskNotification(workManager: WorkManager, task: Task) {
        cancelTaskNotification(workManager, task.id)
        scheduleTaskNotification(workManager, task)
    }

}