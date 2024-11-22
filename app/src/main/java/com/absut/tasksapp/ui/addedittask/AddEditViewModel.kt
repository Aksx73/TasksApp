package com.absut.tasksapp.ui.addedittask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.absut.tasksapp.data.Task
import com.absut.tasksapp.data.TaskRepository
import com.absut.tasksapp.util.Constants.ADD_TASK_RESULT_OK
import com.absut.tasksapp.util.Constants.DELETE_TASK_RESULT_OK
import com.absut.tasksapp.util.Constants.EDIT_TASK_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val _addEditTaskEvent = MutableSharedFlow<AddEditTaskEvent>()
    val addEditTaskEvent: SharedFlow<AddEditTaskEvent> = _addEditTaskEvent

    var task: Task? = null
    var taskId: Long = -1

    fun onSaveClick(
        title: String,
        isCompleted: Boolean,
        dueDate: Long = 0,
        dueTime: Pair<Int, Int> = Pair(-1, -1),
        desc: String? = null
    ) {
        if (task != null) {
            task = task!!.copy(
                name = title,
                completed = isCompleted,
                dueDate = dueDate,
                desc = desc,
                dueTime = dueTime,
                completedDate = if (isCompleted) System.currentTimeMillis() else 0
            )
            updateTask(task!!)
        } else {
            task = Task(
                name = title,
                completed = isCompleted,
                dueDate = dueDate,
                desc = desc,
                dueTime = dueTime,
                completedDate = if (isCompleted) System.currentTimeMillis() else 0
            )
            createTask(task!!)
        }
    }

    private fun createTask(task: Task) = viewModelScope.launch { //get id
        val newRecordId = taskRepository.insertTask(task)
        _addEditTaskEvent.emit(
            AddEditTaskEvent.NavigateBackWithResult(
                ADD_TASK_RESULT_OK,
                newRecordId
            )
        )
    }

    private fun updateTask(task: Task) = viewModelScope.launch {
        taskRepository.updateTask(task)
        _addEditTaskEvent.emit(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK, task.id))
    }

    fun deleteTask() = viewModelScope.launch {
        task?.let {
            taskRepository.deleteTask(it)
            _addEditTaskEvent.emit(AddEditTaskEvent.NavigateBackWithResult(DELETE_TASK_RESULT_OK, -1))
        }
    }

    suspend fun getTaskById(id: Long): Task {
        val deferred: Deferred<Task> = viewModelScope.async {
            taskRepository.getTaskById(id)
        }
        return deferred.await()
    }


    sealed class AddEditTaskEvent {
        data class NavigateBackWithResult(val result: Int, val recordId: Long) : AddEditTaskEvent()
    }

}