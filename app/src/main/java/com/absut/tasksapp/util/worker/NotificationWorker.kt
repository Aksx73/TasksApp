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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.absut.tasksapp.MainActivity
import com.absut.tasksapp.R
import kotlin.properties.Delegates


class NotificationWorker(private val appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        //todo

        if (ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            with(NotificationManagerCompat.from(applicationContext)) {
                notify(0, createNotification())
            }
        }

        return Result.success()
    }

    /**
     * If you use notifications in your worker class,
     * you need to also override the getForegroundInfo() suspend function.
     * Your app crashes without this override.
     * */
    override fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(0, createNotification())
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

    private fun createNotification(): Notification {
        createNotificationChannel()

        val mainActivityIntent = Intent(applicationContext, MainActivity::class.java)
        val mainActivityPendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            mainActivityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val taskListPendingIntent = NavDeepLinkBuilder(applicationContext)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.TaskFragment)
                .createPendingIntent()

        val taskDetailPendingIntent = NavDeepLinkBuilder(applicationContext)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.AddEditTaskFragment)
            .createPendingIntent()



        //todo handle detail screen intent
        //todo create action click intent (mark completed)

        return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText("Work Request Done!") //tasks text
            .setContentIntent(mainActivityPendingIntent)
            //.addAction(R.drawable.action1_icon, "Mark completed", actionPendingIntent)
            .setAutoCancel(true)
            .build()
    }

    companion object {
        const val CHANNEL_NAME = "Timed tasks"
        const val CHANNEL_DESCRIPTION = "channel for all timed tasks"
        const val CHANNEL_ID = "channel_timed_tasks"
    }
}