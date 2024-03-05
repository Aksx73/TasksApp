@file:OptIn(ExperimentalCoroutinesApi::class)

package com.absut.tasksapp.ui.tasks

import androidx.lifecycle.*
import com.absut.tasksapp.data.PreferenceManager
import com.absut.tasksapp.data.SortOrder
import com.absut.tasksapp.data.Task
import com.absut.tasksapp.data.TaskRepository
import com.absut.tasksapp.util.Constants.ADD_TASK_RESULT_OK
import com.absut.tasksapp.util.Constants.EDIT_TASK_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    val sortOrderFlow = preferenceManager.sortOrderFlow

    private val _tasksEvent = MutableSharedFlow<TasksEvent>()
    val tasksEvent: SharedFlow<TasksEvent> = _tasksEvent


    private val taskFlow = sortOrderFlow.flatMapLatest { sortOrder ->
        taskRepository.getTasks(sortOrder, false)
    }

    private val completedTaskFlow = sortOrderFlow.flatMapLatest { sortOrder ->
        taskRepository.getTasks(sortOrder, true)
    }

    val tasks = taskFlow.asLiveData()
    val completedTasks = completedTaskFlow.asLiveData()

    /*fun getTasks(sortOrder: SortOrder, completedTask: Boolean = false) =
        taskRepository.getTasks(sortOrder, completedTask).asLiveData()*/

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferenceManager.updateSortOrder(sortOrder)
    }

    fun onTaskCheckedChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        taskRepository.updateTask(task.copy(completed = isChecked))
    }

    fun onTaskDeleteClick(task: Task) = viewModelScope.launch {
        taskRepository.deleteTask(task)
        _tasksEvent.emit(TasksEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        taskRepository.insertTask(task)
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task added")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task updated")
        }
    }

    private fun showTaskSavedConfirmationMessage(text: String) = viewModelScope.launch {
        _tasksEvent.emit(TasksEvent.ShowTaskSavedConfirmationMessage(text))
    }

    fun deleteAllCompletedTask() = viewModelScope.launch {
        taskRepository.deleteCompletedTasks()
    }


    sealed class TasksEvent {
        data class ShowUndoDeleteTaskMessage(val task: Task) : TasksEvent()
        data class ShowTaskSavedConfirmationMessage(val msg: String) : TasksEvent()
    }

}