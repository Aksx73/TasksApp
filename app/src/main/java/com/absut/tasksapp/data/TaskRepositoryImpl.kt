package com.absut.tasksapp.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 *  Implementation of [TaskRepository]. Single entry point for managing tasks data.
 */
class TaskRepositoryImpl @Inject constructor(private val taskDao: TaskDao) : TaskRepository {

    override fun getTasks(
        sortOrder: SortOrder,
        completed: Boolean
    ): Flow<List<Task>> = taskDao.getTasks(sortOrder, completed)

    override suspend fun updateTask(task: Task) = taskDao.update(task)

    override suspend fun deleteTask(task: Task) = taskDao.delete(task)

    override suspend fun deleteTaskById(id: Long) = taskDao.deleteById(id)

    override suspend fun deleteCompletedTasks() = taskDao.deleteCompletedTasks()

    override suspend fun insertTask(task: Task) : Long = taskDao.insert(task)

    override suspend fun getTaskById(id: Long) : Task = taskDao.getTaskById(id)

    override suspend fun updateTaskAsCompleted(id: Long) = taskDao.updateTaskAsCompleted(id)
}