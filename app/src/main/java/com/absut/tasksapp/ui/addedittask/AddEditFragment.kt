package com.absut.tasksapp.ui.addedittask

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.absut.tasksapp.R
import com.absut.tasksapp.databinding.FragmentAddEditBinding
import com.absut.tasksapp.databinding.FragmentTodoTaskBinding
import com.absut.tasksapp.ui.tasks.TaskViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat

@AndroidEntryPoint
class AddEditFragment : Fragment(), MenuProvider {

    private var _binding: FragmentAddEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<AddEditViewModel>()
    private val args: AddEditFragmentArgs by navArgs()

    private var selectedDueDate: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.task = args.task
        selectedDueDate = args.task?.dueDate ?: 0

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        viewModel.task?.let { task ->
            binding.apply {
                etTask.setText(task.name)
                cbCompleted.isChecked = task.completed == true
                txtAddDate.isVisible = task.dueDate.toInt() == 0
                chipDate.isVisible = task.dueDate.toInt() != 0
                chipDate.text = task.formattedDate(task.dueDate)
            }
        }

        binding.lytAddDate.setOnClickListener {
            showDatePicker()
        }

        binding.chipDate.setOnClickListener {
            showDatePicker(selectedDueDate)
        }

        binding.chipDate.setOnCloseIconClickListener {
            selectedDueDate = 0
            binding.txtAddDate.isVisible = true
            binding.chipDate.isVisible = false
        }

        binding.fabSave.setOnClickListener {
            if (binding.etTask.text.isNullOrBlank()) {
                val snackbar =
                    Snackbar.make(binding.fabSave, "Task cannot be empty", Snackbar.LENGTH_SHORT)
                snackbar.anchorView = it
                snackbar.show()
            } else {
                viewModel.onSaveClick(
                    title = binding.etTask.text.toString(),
                    completed = binding.cbCompleted.isChecked,
                    dueDate = selectedDueDate
                )
            }
        }

        observeEvents()
    }

    private fun observeEvents() {
        lifecycleScope.launchWhenStarted {
            viewModel.addEditTaskEvent.collect { event ->
                when (event) {
                    is AddEditViewModel.AddEditTaskEvent.NavigateBackWithResult -> {
                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result)
                        )
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_delete, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_delete -> {
                Toast.makeText(context, "delete", Toast.LENGTH_SHORT).show()
                true
            }
            else -> false
        }
    }

    private fun showDatePicker(selectedDate: Long = 0) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select due date")
            .setSelection(if (selectedDate.toInt() == 0) MaterialDatePicker.todayInUtcMilliseconds() else selectedDate)
            .build()

        datePicker.addOnPositiveButtonClickListener {
            // Respond to positive button click.\
            binding.txtAddDate.isVisible = false
            binding.chipDate.isVisible = true
            binding.chipDate.text = DateFormat.getDateInstance().format(it)
        }

        datePicker.show(childFragmentManager, "datePicker")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

    }

}