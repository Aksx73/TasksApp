package com.absut.tasksapp.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class UpdateTaskWorker @AssistedInject constructor(
    private val taskRepository: TaskRepository,
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getInt("taskId", -1)
        if (taskId > 0) {
            //todo set completed to true of the given task id
            withContext(Dispatchers.IO) {
                taskRepository.updateTaskAsCompleted(taskId)
            }
        }
        return Result.success()
    }

}