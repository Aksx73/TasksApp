package com.absut.tasksapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    fun getTasks(sortOrder: SortOrder, completed: Boolean): Flow<List<Task>> {
        return when (sortOrder) {
            SortOrder.BY_DUE_DATE -> getTaskSortedByDueDate(completed)
            SortOrder.BY_CREATED_DATE -> getTaskSortedByCreatedDate(completed)
            SortOrder.BY_COMPLETED_DATE -> getTaskSortedByCompletedDate(completed)
        }
    }

    @Query("SELECT * FROM task_table WHERE completed == :completed ORDER BY createdDate DESC")
    fun getTaskSortedByCreatedDate(completed: Boolean): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE completed == :completed ORDER BY CASE WHEN dueDate = 0 THEN createdDate ELSE dueDate END")
    fun getTaskSortedByDueDate(completed: Boolean): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE completed == :completed ORDER BY completedDate DESC")
    fun getTaskSortedByCompletedDate(completed: Boolean): Flow<List<Task>>

    @Query("SELECT * FROM task_table WHERE id == :id")
    suspend fun getTaskById(id: Int): Task

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM task_table WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM task_table WHERE completed = 1")
    suspend fun deleteCompletedTasks()

    @Query("UPDATE task_table SET completed = 1 WHERE id = :id")
    suspend fun updateTaskAsCompleted(id:Int)

}