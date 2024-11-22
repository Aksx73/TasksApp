package com.absut.tasksapp.ui.addedittask

import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.work.WorkManager
import com.absut.tasksapp.R
import com.absut.tasksapp.data.Task
import com.absut.tasksapp.databinding.FragmentAddEditBinding
import com.absut.tasksapp.util.Util.convertLocalToUtc
import com.absut.tasksapp.util.Util.convertUtcToLocalMidnight
import com.absut.tasksapp.util.Util.getFormattedTime
import com.absut.tasksapp.util.Util.getTodayMidnightTimestamp
import com.absut.tasksapp.util.Util.showSnackbarWithAnchor
import com.absut.tasksapp.util.Util.toFormattedDateString
import com.absut.tasksapp.util.worker.WorkerUtil
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class AddEditFragment : Fragment(), MenuProvider {

    private var _binding: FragmentAddEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<AddEditViewModel>()
    private val args: AddEditFragmentArgs by navArgs()

    private var selectedDueDate: Long = 0L
    private var selectedDueMinute: Int = -1
    private var selectedDueHour: Int = -1

    private lateinit var workManager : WorkManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditBinding.inflate(inflater, container, false)

        viewModel.task = args.task
        viewModel.taskId = args.taskId
        selectedDueDate = viewModel.task?.dueDate ?: 0L

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        if (viewModel.task != null) {
            setTaskData(viewModel.task)
        } else if (viewModel.taskId > 0) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.task = viewModel.getTaskById(viewModel.taskId)
                withContext(Dispatchers.Main) {
                    setTaskData(viewModel.task)
                }
            }
        }

        workManager = WorkManager.getInstance(requireContext().applicationContext)

        clickListener()
        observeEvents()
    }

    private fun setTaskData(taskData: Task?) {
        taskData?.let { task ->
            binding.apply {
                etTask.setText(task.name.trim())
                cbCompleted.isChecked = task.completed == true
                etDesc.setText(task.desc?.trim())
                etTask.paint.isStrikeThruText = cbCompleted.isChecked

                txtAddDate.isVisible = task.dueDate == 0L
                chipDate.isVisible = task.dueDate != 0L
                chipDate.text = task.dueDate.toFormattedDateString(false)

                selectedDueHour = task.dueTime.first
                selectedDueMinute = task.dueTime.second

                txtAddTime.isVisible = selectedDueHour == -1
                chipTime.isVisible = selectedDueHour != -1
                chipTime.text = getFormattedTime(selectedDueHour, selectedDueMinute)
            }
        }
        if (taskData==null){
            // task not found for given task id
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Task not found")
                .setMessage("Task your are searching might have been deleted earlier")
                .setPositiveButton("Okay, Go Back"){ dialog,_ ->
                    dialog.dismiss()
                    findNavController().navigateUp()
                }
                .show()
        }
    }

    private fun clickListener() {
        binding.apply {
            lytAddDate.setOnClickListener {
                showDatePicker()
            }

            lytAddTime.setOnClickListener {
                showTimePicker()
            }

            chipDate.setOnClickListener {
                showDatePicker(selectedDueDate)
            }

            chipTime.setOnClickListener {
                showTimePicker(selectedDueHour, selectedDueMinute)
            }

            chipDate.setOnCloseIconClickListener {
                selectedDueDate = 0
                binding.txtAddDate.isVisible = true
                binding.chipDate.isVisible = false
            }

            chipTime.setOnCloseIconClickListener {
                selectedDueHour = -1
                selectedDueMinute = -1
                binding.txtAddTime.isVisible = true
                binding.chipTime.isVisible = false
            }

            cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) etTask.paintFlags = etTask.paintFlags or STRIKE_THRU_TEXT_FLAG
                else etTask.paintFlags = etTask.paintFlags and STRIKE_THRU_TEXT_FLAG.inv()
            }

            fabSave.setOnClickListener {
                if (binding.etTask.text.isNullOrBlank()) {
                    it.showSnackbarWithAnchor("Task cannot be empty")
                } else {
                    viewModel.onSaveClick(
                        title = binding.etTask.text.toString().trim(),
                        isCompleted = binding.cbCompleted.isChecked,
                        dueDate = selectedDueDate,
                        dueTime = Pair(selectedDueHour, selectedDueMinute),
                        desc = binding.etDesc.text.toString().trim()
                    )
                }
            }
        }
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addEditTaskEvent.collect { event ->
                    when (event) {
                        is AddEditViewModel.AddEditTaskEvent.NavigateBackWithResult -> {

                            //todo schedule notification worker if due date set
                            //set id to task object as for new insert record id will be generated after insertion
                            viewModel.task = viewModel.task?.copy(id = event.recordId)
                            WorkerUtil.scheduleTaskNotification(workManager, viewModel.task!! )

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
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_delete, menu)
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        menu.findItem(R.id.action_delete).isVisible = viewModel.task != null
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_delete -> {
                deleteTaskDialog()
                true
            }
            else -> false
        }
    }

    private fun deleteTaskDialog() {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle("Delete this task?")
            .setMessage("This action cannot be undone")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteTask()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePicker(selectedDate: Long = 0) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select due date")
            .setSelection(if (selectedDate.toInt() == 0) System.currentTimeMillis() else selectedDate.convertLocalToUtc())
            .build()

        datePicker.addOnPositiveButtonClickListener {
            selectedDueDate = it.convertUtcToLocalMidnight()
            binding.txtAddDate.isVisible = false
            binding.chipDate.isVisible = true
            binding.chipDate.text = selectedDueDate.toFormattedDateString(false)
        }

        datePicker.show(childFragmentManager, "datePicker")
    }

    private fun showTimePicker(hour: Int = 0, minutes: Int = 0) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(if (hour > 0) hour else 0)
            .setMinute(if (minutes > 0) minutes else 0)
            .setTitleText("Select due time")
            .build()

        picker.addOnPositiveButtonClickListener {
            selectedDueHour = picker.hour // [0, 23]
            selectedDueMinute = picker.minute // [0, 60]

            if (selectedDueDate == 0L) {
                selectedDueDate = getTodayMidnightTimestamp()
                binding.txtAddDate.isVisible = false
                binding.chipDate.isVisible = true
                binding.chipDate.text = selectedDueDate.toFormattedDateString(false)
            }

            binding.txtAddTime.isVisible = false
            binding.chipTime.isVisible = true
            binding.chipTime.text = getFormattedTime(selectedDueHour, selectedDueMinute)
        }


        picker.show(childFragmentManager, "timePicker");
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}