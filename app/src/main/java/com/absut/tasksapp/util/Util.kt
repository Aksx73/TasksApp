package com.absut.tasksapp.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object Util {

    fun View.showSnackbarWithAnchor(message: String) {
        val snackbar = Snackbar.make(this, message, Snackbar.LENGTH_SHORT)
        snackbar.anchorView = this
        snackbar.show()
    }

    fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * input -> date timestamp
     * output -> Date string in format (Sun, 17 Nov) , (Mon, 1 Dec, 2025)
     * input -> if @enableTenseBasedDayFormat is true then for today, tomorrow, yesterday is returned
     * and regular date format for other case
     * */
    fun Long.toFormattedDateString(enableTenseBasedDayFormat: Boolean): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this

        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

        if (enableTenseBasedDayFormat) {
            return when {
                calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> {
                    "Today"
                }

                calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                        calendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> {
                    "Yesterday"
                }

                calendar.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) &&
                        calendar.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR) -> {
                    "Tomorrow"
                }

                else -> {
                    val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
                    val formattedDate = dateFormat.format(this)
                    if (calendar.get(Calendar.YEAR) != today.get(Calendar.YEAR)) {
                        "$formattedDate, ${calendar.get(Calendar.YEAR)}"
                    } else {
                        formattedDate
                    }
                }
            }
        } else {
            val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())
            val formattedDate = dateFormat.format(this)
            return if (calendar.get(Calendar.YEAR) != today.get(Calendar.YEAR)) {
                "$formattedDate, ${calendar.get(Calendar.YEAR)}"
            } else {
                formattedDate
            }
        }
    }

    /**
     * To check if given timestamp is of today
     * */
    fun Long.today(): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this
        val today = Calendar.getInstance()
        return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * input hours -> [0, 23] , minutes -> [0:60]
     * get time in format of 12:00 am, 1:15 pm, 4:04 pm
     * if @trimZeroMinutes is true the when minutes are 0 then it is trimmed (for 12:00 pm -> 12 pm is returned)
     * */
    fun getFormattedTime(
        hours: Int = 0,
        minutes: Int = 0,
        trimZeroMinutes: Boolean = false
    ): String {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val time = LocalTime.of(hours, minutes)
                val formatter = if (trimZeroMinutes && time.minute == 0) {
                    DateTimeFormatter.ofPattern("h a")
                } else {
                    DateTimeFormatter.ofPattern("h:mm a")
                }
                return time.format(formatter)
            } else {
                val formattedMinutes = String.format(Locale.getDefault(), "%02d", minutes)
                val formattedHours = (hours % 12).takeIf { it != 0 } ?: 12
                val amPm = if (hours >= 12) "pm" else "am"
                return if (trimZeroMinutes && formattedMinutes == "00") {
                    "$formattedHours $amPm"
                } else {
                    "$formattedHours:$formattedMinutes $amPm"
                }
            }
        } catch (e: DateTimeException) {
            return ""
        }
    }

    /**
     * Convert given UTC zone timestamp to current local timezone timestamp
     * Required in MaterialDatePicker as returned selected date follows UTC timezone
     * */
    fun Long.convertUtcToLocalMidnight(): Long {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val instant = Instant.ofEpochMilli(this)
            val localZoneId = ZoneId.systemDefault() // Get the system's default time zone
            val localDateTime = instant.atZone(localZoneId) // Convert to local time zone
            val localMidnight = localDateTime.toLocalDate()
                .atStartOfDay(localZoneId) // Get midnight in local time zone
            return localMidnight.toInstant().toEpochMilli() // Convert back to timestamp
        } else {
            val localTimeZone = TimeZone.getDefault()
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = this@convertUtcToLocalMidnight
                timeZone = localTimeZone
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            return calendar.timeInMillis
        }
    }

    /**
     * Return timestamp of today's date at 00:00 (12:00 am / midnight / start of the day)
     * */
    fun getTodayMidnightTimestamp(): Long {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val today = LocalDate.now() // Get today's date
            val localZoneId = ZoneId.systemDefault() // Get the system's default time zone
            val midnight = today.atStartOfDay(localZoneId) // Get midnight in local time zone
            return midnight.toInstant().toEpochMilli() // Convert to timestamp
        } else {
            val calendar = Calendar.getInstance().apply {
                timeZone = TimeZone.getDefault() // Set to local time zone
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return calendar.timeInMillis
        }
    }

    /**
     * Convert current local timezone timestamp to UTC zone timestamp
     * Required in MaterialDatePicker for default initial date selection
     * as it only take timestamp in UTC timezone
     * @return UTC timestamp in milliseconds since epoch
     * */
    fun Long.convertLocalToUtc(): Long {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val local = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(this),
                ZoneId.systemDefault()
            )
            return local.atZone(ZoneId.systemDefault())
                .withZoneSameLocal(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        } else {
            val localTimeZone = TimeZone.getDefault()
           // No need to get UTC timezone offset as it's always 0
            val localOffset = localTimeZone.getOffset(this)

            return this - localOffset
        }
    }

    /**
     * with given midnight date timestamp, hours and minutes of the day
     * returns time in milliseconds
     * if hours and minutes are not set then set time as 9:00
     **/
    fun getMillisecondsFromDateTime(dateTimestamp: Long, hours: Int, minutes: Int): Long {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.timeInMillis = dateTimestamp
        if (hours != -1 && minutes != -1) {
            // Set the hours and minutes
            calendar.set(Calendar.HOUR_OF_DAY, hours)
            calendar.set(Calendar.MINUTE, minutes)
        } else {
            //set time to 9:00 am
            calendar.set(Calendar.HOUR_OF_DAY, 9)
            calendar.set(Calendar.MINUTE, 0)
        }
        return calendar.timeInMillis
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkForNotificationPermission(
        context: Context,
        activity: Activity,
        requestPermissionLauncher: ActivityResultLauncher<String>,
        hasPermission: () -> Unit
    ) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                hasPermission()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) -> {
                requestPermissionLauncher.launch(
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
            }

            else -> {
                requestPermissionLauncher.launch(
                    android.Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }


    fun manualPermissionNeededDialog(
        context: Context,
        activity: Activity?,
        cancelable: Boolean = false
    ) {
        val dialogBuilder = MaterialAlertDialogBuilder(context)
        dialogBuilder.setTitle("Notification permission required")
        dialogBuilder.setMessage("Since you have denied the notification permission earlier, now you have enable it manually from app setting. Click on open setting button to go to app setting.")
        dialogBuilder.setPositiveButton("Open setting") { _, _ ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", activity?.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }
        dialogBuilder.setNegativeButton("Cancel", null)
        dialogBuilder.setCancelable(cancelable)
        dialogBuilder.show()
    }

}