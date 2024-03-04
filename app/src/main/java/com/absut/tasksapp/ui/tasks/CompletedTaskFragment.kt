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
import com.absut.tasksapp.databinding.FragmentCompletedTaskBinding
import com.absut.tasksapp.databinding.FragmentTodoTaskBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class CompletedTaskFragment : Fragment(),TaskAdapter.OnItemClickListener {

    private var _binding: FragmentCompletedTaskBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<TaskViewModel>()

    private val taskAdapter: TaskAdapter by lazy {
        TaskAdapter(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCompletedTaskBinding.inflate(inflater, container, false)
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

        viewModel.completedTasks.observe(viewLifecycleOwner) {
            taskAdapter.submitList(it)
            binding.emptyView.isVisible = it.isEmpty()
        }
    }

    override fun onItemClick(task: Task) {
        val action = TasksFragmentDirections.actionTaskFragmentToAddEditTaskFragment(task)
        findNavController().navigate(action)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(task, isChecked)
    }

    private fun deleteAllCompletedTask() {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle("Delete all completed task?")
            .setMessage("This action cannot be undone")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteAllCompletedTask()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    companion object {

    }
}