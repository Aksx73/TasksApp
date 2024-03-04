package com.absut.tasksapp.ui.tasks

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.absut.tasksapp.R
import com.absut.tasksapp.data.Task
import com.absut.tasksapp.databinding.FragmentTodoTaskBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class TodoTaskFragment : Fragment(),TaskAdapter.OnItemClickListener {

    private var _binding: FragmentTodoTaskBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<TaskViewModel>()

    private val taskAdapter: TaskAdapter by lazy {
        TaskAdapter(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTodoTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply{
            recyclerView.apply {
                adapter = taskAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }

        }

        setFragmentResultListener("add_edit_request") { _, bundle ->
            val result = bundle.getInt("add_edit_result")

            viewModel.onAddEditResult(result)
            Log.d("TAG", "onViewCreated: $result")
        }

        viewModel.tasks.observe(viewLifecycleOwner) {
            taskAdapter.submitList(it)
            binding.emptyView.isVisible = it.isEmpty()
        }

        observeEvents()
    }

    private fun observeEvents(){
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.collectLatest { event ->
                when (event) {
                    is TaskViewModel.TasksEvent.ShowTaskSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                        Log.d("TAG", "observeEvents: ${event.msg}")
                    }
                    is TaskViewModel.TasksEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(requireView(), "Task deleted", Snackbar.LENGTH_LONG)
                            .setAction("Undo") {
                                viewModel.onUndoDeleteClick(event.task)
                            }.show()
                    }
                }
            }
        }
    }

    override fun onItemClick(task: Task) {
        val action = TasksFragmentDirections.actionTaskFragmentToAddEditTaskFragment(task)
        findNavController().navigate(action)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(task, isChecked)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

    }
}