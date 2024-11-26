package com.absut.tasksapp

import android.app.Application
import androidx.work.Configuration
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TaskApplication : Application(){

	override fun onCreate() {
		super.onCreate()
		DynamicColors.applyToActivitiesIfAvailable(this)
	}

}



/**
 * Future TODO
 *
 * FEATURES
 * ----------->
 * Add notification action "Mark complete" and update task completion status via broadcast receiver and work manager to perform db operation
 * Change notification icon color to red if it is of yesterday's task and remind yesterdays due date task on next day if not completed
 * Long press to rearrange task position (add one more column for indexing custom priority or position)
 * Splash screen api implementation (resolve issue with dynamic color theming)
 * good morning notification with message to note down today's task notification everyday at 9:00 am with different notification channel and unique periodic request
 * Add repetitive task while rub periodical on given time (weekly, daily, monthly)
 *
 * ISSUES
 * ----------->
 * Stop scheduling or checking to schedule notification if task details are not changed
 * confirmation on back press when changes has been made on task detail screen
 * worker not working when app is not opened (SCHEDULE_EXACT_ALARM) migrate to AlarmManager API
 *
 * */