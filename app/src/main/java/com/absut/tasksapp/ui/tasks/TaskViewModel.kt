@file:OptIn(ExperimentalCoroutinesApi::class)

package com.absut.tasksapp.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.absut.tasksapp.data.PreferenceManager
import com.absut.tasksapp.data.SortOrder
import com.absut.tasksapp.data.Task
import com.absut.tasksapp.data.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private val sortOrderFlow = preferenceManager.sortOrderFlow

    @OptIn(ExperimentalCoroutinesApi::class)
    private val taskFlow = sortOrderFlow.flatMapLatest { sortOrder ->
        taskRepository.getTasks(sortOrder, false)
    }
    val tasks = taskFlow.asLiveData()

    private val completedTaskFlow = taskRepository.getTasks(SortOrder.BY_COMPLETED_DATE, true)
    val completedTasks = completedTaskFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferenceManager.updateSortOrder(sortOrder)
    }

    fun onTaskCheckedChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        taskRepository.updateTask(task.copy(completed = isChecked))
    }

    fun deleteAllCompletedTask() = viewModelScope.launch {
        taskRepository.deleteCompletedTasks()
    }

}