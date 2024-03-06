package com.absut.tasksapp.ui.tasks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.absut.tasksapp.data.Task
import com.absut.tasksapp.databinding.TaskListItemBinding
import com.absut.tasksapp.util.Util.formattedDate

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

                checkbox.setOnCheckedChangeListener { _, b ->
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onCheckBoxClick(task, b)
                    }
                }
            }
        }

        fun bind(task: Task) {
            binding.apply {
                checkbox.isChecked = task.completed
                txtTitle.text = task.name
                txtTitle.paint.isStrikeThruText = task.completed

                if (!task.completed) {
                    if (task.dueDate.toInt() != 0) {
                        chipDate.isVisible = true
                        chipDate.text = task.dueDate.formattedDate()
                    } else chipDate.isVisible = false
                } else chipDate.isVisible = false

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