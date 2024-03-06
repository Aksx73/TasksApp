package com.absut.tasksapp.util

import android.content.Context
import android.view.View
import com.google.android.material.snackbar.Snackbar
import java.text.DateFormat

object Util {
    fun View.showSnackbarWithAnchor(message: String) {
        val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
        snackbar.anchorView = this
        snackbar.show()
    }

    fun Long.formattedDate() = DateFormat.getDateInstance().format(this)

}