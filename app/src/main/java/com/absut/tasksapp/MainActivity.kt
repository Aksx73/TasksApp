package com.absut.tasksapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
import com.absut.tasksapp.util.worker.NotificationWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    val workManager = WorkManager.getInstance(application.applicationContext)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val data = Data.Builder()
        data.putString("note_id", "123")

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        val work = PeriodicWorkRequestBuilder<NotificationWorker>(15,TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setInputData(data.build())
            .build()

        workManager.enqueue(work)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}