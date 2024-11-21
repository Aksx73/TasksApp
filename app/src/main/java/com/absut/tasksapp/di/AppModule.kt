package com.absut.tasksapp.di

import android.app.Application
import androidx.room.Room
import com.absut.tasksapp.data.MIGRATION_1_2
import com.absut.tasksapp.data.MIGRATION_2_3
import com.absut.tasksapp.data.MIGRATION_3_4
import com.absut.tasksapp.data.TaskDao
import com.absut.tasksapp.data.TaskDatabase
import com.absut.tasksapp.data.TaskRepository
import com.absut.tasksapp.data.TaskRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object AppModule {

	@Provides
	@Singleton
	fun providesDatabase(app: Application): TaskDatabase {
		return Room.databaseBuilder(app, TaskDatabase::class.java, "task_db")
			.addMigrations(MIGRATION_1_2)
			.addMigrations(MIGRATION_2_3)
			.addMigrations(MIGRATION_3_4)
			.build()
	}

	@Provides
	@Singleton
	fun providesTaskDao(db: TaskDatabase): TaskDao {
		return db.taskDao()
	}

	@Provides
	@Singleton
	fun provideTaskRepository(taskDao: TaskDao): TaskRepository {
		return TaskRepositoryImpl(taskDao)
	}

}

