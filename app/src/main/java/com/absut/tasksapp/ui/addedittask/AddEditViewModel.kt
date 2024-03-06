package com.absut.tasksapp.ui.addedittask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.absut.tasksapp.data.Task
import com.absut.tasksapp.data.TaskRepository
import com.absut.tasksapp.util.Constants.ADD_TASK_RESULT_OK
import com.absut.tasksapp.util.Constants.DELETE_TASK_RESULT_OK
import com.absut.tasksapp.util.Constants.EDIT_TASK_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
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

    fun onSaveClick(title: String, isCompleted: Boolean, dueDate: Long = 0) {
        if (task != null) {
            val updatedTask = task!!.copy(
                name = title,
                completed = isCompleted,
                dueDate = dueDate,
                completedDate = if (isCompleted) System.currentTimeMillis() else 0
            )
            updateTask(updatedTask)
        } else {
            val newTask = Task(
                name = title,
                completed = isCompleted,
                dueDate = dueDate,
                completedDate = if (isCompleted) System.currentTimeMillis() else 0
            )
            createTask(newTask)
        }
    }

    private fun createTask(task: Task) = viewModelScope.launch {
        taskRepository.insertTask(task)
        _addEditTaskEvent.emit(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }

    private fun updateTask(task: Task) = viewModelScope.launch {
        taskRepository.updateTask(task)
        _addEditTaskEvent.emit(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }

    fun deleteTask() = viewModelScope.launch {
        task?.let {
            taskRepository.deleteTask(it)
            _addEditTaskEvent.emit(AddEditTaskEvent.NavigateBackWithResult(DELETE_TASK_RESULT_OK))
        }
    }


    sealed class AddEditTaskEvent {
        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()
    }

}