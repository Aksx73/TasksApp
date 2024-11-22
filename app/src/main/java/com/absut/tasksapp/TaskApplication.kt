package com.absut.tasksapp

import android.app.Application
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
 * Change notification icon color to red if it is of yesterday's task and remind yesterdays duedate task on next day if not completed
 * Long press to rearrange task position (add one more column for indexing custom priority or position)
 * Splash screen api implementation (resolve issue with dynamic color theming)
 *
 * ISSUES
 * ----------->
 * Stop scheduling or checking to schedule notification if task details are not changed
 * confirmation on back press when changes has been made on task detail screen
 *
 *
 * */