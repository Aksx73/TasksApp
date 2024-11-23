package com.absut.tasksapp.ui.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.absut.tasksapp.MainActivity
import com.absut.tasksapp.R
import com.absut.tasksapp.data.Task
import com.absut.tasksapp.databinding.FragmentCompletedTaskBinding
import com.absut.tasksapp.util.Util.showSnackbarWithAnchor
import com.absut.tasksapp.util.worker.WorkerUtil
import com.absut.tasksapp.util.worker.uniqueWorkName
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.collections.isNotEmpty

@AndroidEntryPoint
class CompletedTaskFragment : Fragment(), TaskAdapter.OnItemClickListener, MenuProvider {

    private var _binding: FragmentCompletedTaskBinding? = null
    private val binding get() = _binding!!

    private val viewModel by activityViewModels<TaskViewModel>()

    private val taskAdapter: TaskAdapter by lazy {
        TaskAdapter(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompletedTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.apply {
            recyclerView.apply {
                adapter = taskAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.completedTasks.collect { tasks ->
                    taskAdapter.submitList(tasks)
                    binding.emptyView.isVisible = tasks.isEmpty()
                }
            }
        }

    }

    override fun onItemClick(task: Task) {
        val action = TasksFragmentDirections.actionTaskFragmentToAddEditTaskFragment(task = task)
        findNavController().navigate(action)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(task, isChecked)
        observeTaskCompleteStatusChange(task,isChecked)
    }

    private fun observeTaskCompleteStatusChange(task: Task, isChecked: Boolean){
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.taskCheckedChangeState.collect { result ->
                    when {
                        result > 0 -> { //operation successful
                            //re schedule work if has due date
                            if (task.dueDate != 0L && isChecked == false) {
                                //schedule notification (in update case existing worker will be replaced with new one)
                                WorkerUtil.scheduleTaskNotification((activity as MainActivity).workManager, task)
                            }
                        }
                        result == 0 -> { // operation failed
                            binding.root.showSnackbarWithAnchor("Operation failed!")
                        }
                        else ->{ // operation failed
                            binding.root.showSnackbarWithAnchor("Something went wrong!")
                        }
                    }
                }
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_main, menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        menu.findItem(R.id.action_sort).isVisible = false
        menu.findItem(R.id.action_delete_all).isVisible = true
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_delete_all -> {
                deleteAllCompletedTask()
                true
            }

            else -> false
        }
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

}