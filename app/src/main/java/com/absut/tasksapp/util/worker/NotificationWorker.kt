package com.absut.tasksapp.util.worker

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.absut.tasksapp.R
import com.absut.tasksapp.data.NotificationActionReceiver


class NotificationWorker(private val appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

        //todo notification conditions
        // schedule notification when date+time is assign to task in new & edit case do same -> DONE
        // cancel scheduled worker if date+time is removed from task -> DONE
        // when task is completed then remove schedule notification task if set (detail screen -> DONE , home screen -> PENDING)
        // when task is deleted then remove schedule notification task if set -> DONE
        // handle "mark completed" notification action click to perform db task to update entry -> PENDING
        // don't schedule task for due date of past -> DONE

    private var taskId: Long = -1
    private var taskTitle: String? = null

    override fun doWork(): Result {

        taskId = inputData.getLong(TASK_ID, -1)
        taskTitle = inputData.getString(TASK_TITLE)

        if (taskId > 0) {
            if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
                with(NotificationManagerCompat.from(applicationContext)) {
                    notify(0, createNotification(taskId, taskTitle))
                }
            }
            return Result.success()
        } else return Result.failure()

    }

    /**
     * If you use notifications in your worker class,
     * you need to also override the getForegroundInfo() suspend function.
     * Your app crashes without this override.
     * */
    override fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(0, createNotification(taskId, taskTitle))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager: NotificationManager? =
                getSystemService(applicationContext, NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(taskId: Long = -1, taskTitle: String?): Notification {
        createNotificationChannel()

        //to handle detail screen intent
        val args = Bundle()
        val pendingIntent = if (taskId > 0) {
            args.putLong(TASK_ID, taskId)
            NavDeepLinkBuilder(applicationContext)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.AddEditTaskFragment)
                .setArguments(args)
                .createPendingIntent()
        } else {
            NavDeepLinkBuilder(applicationContext)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.TaskFragment)
                .createPendingIntent()
        }

        //todo create action click intent (mark complete)
        val actionIntent = Intent(appContext, NotificationActionReceiver::class.java)
        actionIntent.putExtra(TASK_ID, taskId)
        val actionPendingIntent =
            PendingIntent.getBroadcast(appContext, 0, actionIntent, PendingIntent.FLAG_IMMUTABLE)


        return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(taskTitle) //tasks text
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            //.addAction(R.drawable.ic_check_24, "Mark completed", actionPendingIntent)
            .setAutoCancel(true)
            .build()
    }

    companion object {
        const val CHANNEL_NAME = "Timed tasks"
        const val CHANNEL_NAME_TODAY = "Tasks for today"
        const val CHANNEL_NAME_YESTERDAY = "Tasks from yesterday"
        const val CHANNEL_DESCRIPTION = "channel for all timed tasks"
        const val CHANNEL_ID = "channel_timed_tasks"

        const val TASK_ID = "taskId"
        const val TASK_TITLE = "taskTitle"
    }
}