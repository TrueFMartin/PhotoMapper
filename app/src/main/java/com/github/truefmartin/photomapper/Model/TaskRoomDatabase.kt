package com.github.truefmartin.photomapper.Model

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.truefmartin.photomapper.NewEditTaskActivity.RecurringState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Database(entities = [Task::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TaskRoomDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    private class TaskDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            INSTANCE?.let { database ->
                scope.launch {
                    val taskDao = database.taskDao()
                    // Delete all content here.
                    taskDao.deleteAll()
                    val starterTasks =  arrayListOf<Task>()
                    var noteID = 0
                    var dateAdd = 0L
                    val date = LocalDateTime.parse("2023-10-13T21:26")
                    starterTasks.add( Task(null, "Mop Kitchen", "Buy Pinesol",
                        date.plusDays(dateAdd++), true, RecurringState.NONE, noteID++))
                    starterTasks.add( Task(null, "Clean Fridge", "WEAR GLOVES!",
                        date.plusDays(dateAdd++), false, RecurringState.NONE, noteID++))
                    starterTasks.add( Task(null, "Sweep", "Needs some TLC",
                        date.plusDays(dateAdd++), false, RecurringState.WEEKLY, noteID++))
                    starterTasks.add( Task(null, "Dishes", "Buy hammer and nails",
                        date.plusDays(dateAdd++), false, RecurringState.NONE, noteID++))
                    starterTasks.add( Task(null, "Repair Broken Dishes",
                        "Buy new dishes instead", date.plusDays(dateAdd++),
                        false, RecurringState.DAILY, noteID++))
                    starterTasks.add( Task(null, "Sweep Kitchen Again",
                        "Clean up dish shards", date.plusDays(dateAdd),
                        false, RecurringState.DAILY, noteID))
                    starterTasks.forEach { taskDao.insert(it) }
                }
            }
            Log.d("Database","DB created")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: TaskRoomDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): TaskRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskRoomDatabase::class.java,
                    "task_database"
                )
                    .addCallback(TaskDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}

