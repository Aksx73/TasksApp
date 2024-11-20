package com.absut.tasksapp.util

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
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
                val formattedMinutes = String.format(Locale.getDefault(),"%02d", minutes)
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
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = this

            val localTimeZone = TimeZone.getDefault()
            calendar.timeZone = localTimeZone

            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            return calendar.timeInMillis
        }
    }

    /**
     * Return timestamp if today's date at 00:00 (12:00 am / midnight / start of the day)
     * */
    fun getTodayMidnightTimestamp(): Long {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val today = LocalDate.now() // Get today's date
            val localZoneId = ZoneId.systemDefault() // Get the system's default time zone
            val midnight = today.atStartOfDay(localZoneId) // Get midnight in local time zone
            return midnight.toInstant().toEpochMilli() // Convert to timestamp
        } else {
            val calendar = Calendar.getInstance()
            calendar.timeZone = TimeZone.getDefault() // Set to local time zone
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return calendar.timeInMillis
        }
    }

    /**
     * Convert current local timezone timestamp to UTC zone timestamp
     * Required in MaterialDatePicker for default initial date selection
     * as it only take timestamp in UTC timezone
     * */
    fun Long.convertLocalToUtc(): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this

        val localTimeZone = TimeZone.getDefault()
        val utcTimeZone = TimeZone.getTimeZone("UTC")

        val localOffset = localTimeZone.getOffset(this)
        val utcOffset = utcTimeZone.getOffset(this)

        return this - (localOffset - utcOffset)
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
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                hasPermission()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }

            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }
    }


    fun manualPermissionNeededDialog(
        context: Context,
        activity: Activity?,
        cancelable: Boolean = false ) {
        val dialogBuilder = MaterialAlertDialogBuilder(context, R.style.MaterialAlert_CustomActionButton_Primary)
        dialogBuilder.setTitle("Notification permission required")
        dialogBuilder.setMessage("Since you have denied the notification permission earlier, now you have enable it manually from app setting. Click on open setting button to go to app setting.")
        dialogBuilder.setPositiveButton("Open setting") { _, _ ->
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", activity?.packageName, null)
            intent.setData(uri)
            context.startActivity(intent)
        }
        dialogBuilder.setNegativeButton("Cancel", null)
        dialogBuilder.setCancelable(cancelable)
        dialogBuilder.show()
    }

}