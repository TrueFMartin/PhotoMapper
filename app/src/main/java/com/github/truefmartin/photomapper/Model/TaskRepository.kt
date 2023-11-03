package com.github.truefmartin.photomapper.Model

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class TaskRepository(private val taskDao: TaskDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allTasks: Flow<List<Task>> = taskDao.getTimeSortedTasks()

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    fun getTask(id:Int):Flow<Task>{
        return taskDao.getTask(id)
    }

    fun getTaskNotLive(id:Int):Task{
        return taskDao.getTaskNotLive(id)
    }

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(task: Task) {
        //Note that I am pretending this is a network call by adding
        //a 5 second sleep call here
        //If you don't run this in a scope that is still active
        //Then the call won't complete
        taskDao.insert(task)
    }

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(task: Task) {
        taskDao.update(task)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
}
