package com.absut.tasksapp

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.absut.tasksapp.databinding.ActivityMainBinding
import com.absut.tasksapp.util.Constants.PACKAGE_NAME
import com.absut.tasksapp.util.Util
import com.absut.tasksapp.util.worker.NotificationWorker
import com.absut.tasksapp.util.worker.NotificationWorker.Companion.TASK_ID
import com.absut.tasksapp.util.worker.NotificationWorker.Companion.TASK_TITLE
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    val workManager: WorkManager by lazy {
        WorkManager.getInstance(this.applicationContext)
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                //has notification permission
            } else {
                if (!shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                    Util.manualPermissionNeededDialog(this, this)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        //installSplashScreen()
        //enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        //DynamicColors.applyToActivityIfAvailable(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        if (notificationPermissionNeeded()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Util.checkForNotificationPermission(this, this, requestNotificationPermissionLauncher) {
                    //when has permission
                }
            }
        }

        val workManager = WorkManager.getInstance(application.applicationContext)

        val data = Data.Builder()
        data.putInt(TASK_ID, -1)
        data.putString(TASK_TITLE, "this is note title")

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val work = PeriodicWorkRequestBuilder<NotificationWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setInputData(data.build())
            .build()

        //workManager.enqueue(work)
    }

    override fun onResume() {
        super.onResume()
        handleIntent()
    }

    private fun handleIntent() {
        intent?.let {
            val taskId = intent.getLongExtra(TASK_ID, -1)
            Log.d("TAG", "handleIntent: Task ID : $taskId")
            if (taskId > 0) {
                val uri = Uri.parse("android-app://${PACKAGE_NAME}/taskDetail/$taskId")
                navController.navigate(uri)
            }
        }
    }

    private fun notificationPermissionNeeded(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        } else false
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}