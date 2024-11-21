package com.absut.tasksapp.util.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.absut.tasksapp.data.Task
import com.absut.tasksapp.util.Util
import com.absut.tasksapp.util.worker.NotificationWorker.Companion.TASK_ID
import com.absut.tasksapp.util.worker.NotificationWorker.Companion.TASK_TITLE
import kotlin.to

object WorkerUtil {

    fun scheduleTaskNotification(workManager: WorkManager, task: Task) {
        //todo calculate task deadline by combining date/time

        val taskDeadline = Util.getMillisecondsFromDateTime(
            Task.dueDate,
            Pair.first,
            Pair.second
        )
        val currentTime = System.currentTimeMillis()
        val initialDelay = taskDeadline - currentTime

        val data = workDataOf(
            com.absut.tasksapp.util.worker.NotificationWorker.Companion.TASK_ID to Task.id,
            com.absut.tasksapp.util.worker.NotificationWorker.Companion.TASK_TITLE to Task.name
        )

        /*val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()*/

        val workRequest = WorkRequest.Builder.build()

        WorkManager.enqueueUniqueWork(
            toString(), // Unique work name
            ExistingWorkPolicy.REPLACE, // Replace existing work if any
            workRequest
        )
    }

    fun cancelTaskNotification(workManager: WorkManager, taskId: Long) {
        WorkManager.cancelUniqueWork(toString())
    }

    fun updateTaskNotification(workManager: WorkManager, task: Task) {
        cancelTaskNotification(workManager, Task.id)
        scheduleTaskNotification(workManager, task)
    }

}