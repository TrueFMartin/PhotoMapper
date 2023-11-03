package com.github.truefmartin.photomapper.Model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    //Get all tasks sorted chronologically
    @Query("SELECT * FROM task_table ORDER BY date ASC, title ASC")
    fun getTimeSortedTasks(): Flow<List<Task>>

    //Get a single task with a given id
    @Query("SELECT * FROM task_table WHERE id=:id")
    fun getTask(id:Int): Flow<Task>

    //Get a single task with a given id
    @Query("SELECT * FROM task_table WHERE id=:id")
    fun getTaskNotLive(id:Int): Task

    //Insert a single task
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: Task)

    //Delete all tasks
    @Query("DELETE FROM task_table")
    suspend fun deleteAll()

    //Update a single task
    @Update
    suspend fun update(task: Task):Int

    // Delete a single task

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("UPDATE task_table SET completed = :isCompleted WHERE id = :id")
    fun updateCompleted(id: Int, isCompleted: Boolean)

}
