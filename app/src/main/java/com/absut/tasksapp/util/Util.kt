package com.absut.tasksapp.util

import android.content.Context
import android.view.View
import com.google.android.material.snackbar.Snackbar

object Util {

    /*fun Long.formattedDate(): String {
        return
    }*/

    fun View.showSnackbarWithAnchor(message: String) {
        val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
        snackbar.anchorView = this
        snackbar.show()
    }

}