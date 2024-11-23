@file:OptIn(ExperimentalCoroutinesApi::class)

package com.absut.tasksapp.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.absut.tasksapp.data.PreferenceManager
import com.absut.tasksapp.data.SortOrder
import com.absut.tasksapp.data.Task
import com.absut.tasksapp.data.TaskRepository
import com.absut.tasksapp.ui.addedittask.AddEditViewModel.AddEditTaskEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val sortOrderFlow = preferenceManager.sortOrderFlow

    private val _taskCheckedChangeState = MutableSharedFlow<Int>()
    val taskCheckedChangeState: SharedFlow<Int> = _taskCheckedChangeState

    private val taskFlow = sortOrderFlow.flatMapLatest { sortOrder ->
        taskRepository.getTasks(sortOrder, false)
    }
    val tasks = taskFlow
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val completedTaskFlow = taskRepository.getTasks(SortOrder.BY_COMPLETED_DATE, true)
    val completedTasks = completedTaskFlow
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferenceManager.updateSortOrder(sortOrder)
    }

    fun onTaskCheckedChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        try {
            _taskCheckedChangeState.emit(
                taskRepository.updateTask(
                    task.copy(
                        completed = isChecked,
                        completedDate = if (isChecked) System.currentTimeMillis() else 0
                    )
                )
            )
        } catch (e: Exception) {
            _taskCheckedChangeState.emit(-1)
        }

    }

    fun deleteAllCompletedTask() = viewModelScope.launch {
        taskRepository.deleteCompletedTasks()
    }

}