package com.absut.tasksapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Task::class], version = 4, exportSchema = false)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE task_table ADD COLUMN desc TEXT")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE task_table ADD COLUMN dueTime TEXT NOT NULL")
    }
}

//to change primary key (id) data type to Long from Int
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create a new table with the desired structure
        db.execSQL("CREATE TABLE task_table_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL,desc TEXT,completed INTEGER NOT NULL,dueDate INTEGER NOT NULL,dueTime TEXT NOT NULL,createdDate INTEGER NOT NULL,completedDate INTEGER NOT NULL)")
        // Copy data from the old table
        db.execSQL("INSERT INTO task_table_new (id,name,desc,completed, dueDate,dueTime,createdDate,completedDate) SELECT id, name, desc, completed, dueDate, dueTime, createdDate, completedDate FROM task_table")
        // Drop the old table
        db.execSQL("DROP TABLE task_table")
        // Rename the new table
        db.execSQL("ALTER TABLE task_table_new RENAME TO task_table")

    }
}
