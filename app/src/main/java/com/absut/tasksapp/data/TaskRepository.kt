package com.absut.tasksapp.data

import kotlinx.coroutines.flow.Flow

/**
 * Interface to the data layer.
 */
interface TaskRepository {

    fun getTasks(sortOrder: SortOrder, completed: Boolean): Flow<List<Task>>

    suspend fun updateTask(task: Task)

    suspend fun deleteTask(task: Task)

    suspend fun deleteTaskById(id: Long)

    suspend fun deleteCompletedTasks()

    suspend fun insertTask(task: Task) : Long

    suspend fun getTaskById(id: Long): Task

    suspend fun updateTaskAsCompleted(id: Long)
}