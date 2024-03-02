package com.absut.tasksapp.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.DateFormat
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "task_table")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val completed: Boolean = false,
    val dueDate: Long = 0,
    val createdDate: Long = System.currentTimeMillis(),
    val completedDate: Long = 0
) : Parcelable {
    val formattedDate: String
        get() = DateFormat.getDateInstance().format(createdDate)

    fun formattedDate(date: String) = DateFormat.getDateInstance().format(date)
}