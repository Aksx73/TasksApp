package com.absut.tasksapp.data

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(tableName = "task_table")
@TypeConverters(PairTypeConverter::class)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val desc: String? = null,
    val completed: Boolean = false,
    val dueDate: Long = 0,
    val dueTime: Pair<Int,Int> = Pair(-1,-1), //first -> hour [0:23], second -> minutes [0:60]
    val createdDate: Long = System.currentTimeMillis(),
    val completedDate: Long = 0
) : Parcelable