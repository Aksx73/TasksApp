package com.absut.tasksapp.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.absut.tasksapp.R
import com.absut.tasksapp.data.Task
import com.absut.tasksapp.databinding.TaskListItemBinding
import com.absut.tasksapp.util.Util.getFormattedTime
import com.absut.tasksapp.util.Util.getTodayMidnightTimestamp
import com.absut.tasksapp.util.Util.toFormattedDateString
import com.absut.tasksapp.util.Util.today
import com.google.android.material.color.MaterialColors
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Calendar

class TaskAdapter(
    private val listener: OnItemClickListener
) : ListAdapter<Task, TaskAdapter.TasksViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val binding =
            TaskListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TasksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class TasksViewHolder(private val binding: TaskListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onItemClick(task)
                    }
                }

                /*checkbox.setOnCheckedChangeListener { _, b ->
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onCheckBoxClick(task, b)
                    }
                }*/

                checkbox.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onCheckBoxClick(task, checkbox.isChecked)
                    }
                }

            }
        }

        fun bind(task: Task) {
            binding.apply {
                txtSubtitle.isVisible = !task.desc.isNullOrBlank()
                txtDate.isVisible = task.dueDate.toInt() != 0 && !task.completed

                checkbox.isChecked = task.completed
                txtTitle.text = task.name.trim()
                txtTitle.paint.isStrikeThruText = task.completed
                txtDate.text =
                    if (task.dueTime.first != -1)
                        "${task.dueDate.toFormattedDateString(true)}, ${
                            getFormattedTime(
                                task.dueTime.first,
                                task.dueTime.second,
                                true
                            )
                        }"
                    else task.dueDate.toFormattedDateString(true)
                txtSubtitle.text = task.desc?.trim()

                val colorPrimary: Int = MaterialColors.getColor(
                    binding.root.context,
                    com.google.android.material.R.attr.colorPrimary,
                    binding.root.context.getColor(com.google.android.material.R.color.design_default_color_primary)
                )
                val colorError: Int = MaterialColors.getColor(
                    binding.root.context,
                    com.google.android.material.R.attr.colorError,
                    binding.root.context.getColor(com.google.android.material.R.color.design_default_color_error)
                )
                val textColorSecondary: Int = MaterialColors.getColor(
                    binding.root.context,
                    android.R.attr.textColorSecondary,
                    binding.root.context.getColor(com.google.android.material.R.color.m3_default_color_secondary_text)
                )

                if (task.dueDate.toInt() != 0) {
                    if (task.dueDate.today()) {
                        txtDate.setTextColor(colorPrimary)
                    } else if (task.dueDate < getTodayMidnightTimestamp()) {
                        txtDate.setTextColor(colorError)
                    } else {
                        txtDate.setTextColor(textColorSecondary)
                    }
                }
            }
        }

    }

    interface OnItemClickListener {
        fun onItemClick(task: Task)
        fun onCheckBoxClick(task: Task, isChecked: Boolean)
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Task, newItem: Task) =
            oldItem == newItem
    }
}