package com.github.truefmartin.photomapper.NewEditTaskActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.github.truefmartin.photomapper.Model.Task
import com.github.truefmartin.photomapper.Model.TaskRepository
import kotlinx.coroutines.coroutineScope

class NewTaskViewModel(private val repository: TaskRepository, private val id:Int) : ViewModel() {

    var curTask: LiveData<Task> = repository.getTask(id).asLiveData()

    fun updateId(id:Int){
        curTask = repository.getTask(id).asLiveData()
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    suspend fun insert(task: Task){
        coroutineScope {
            repository.insert(task)
        }
    }

    /**
     * Launching a new coroutine to Update the data in a non-blocking way
     */
    suspend fun update(task: Task) {
        coroutineScope {
            repository.update(task)
        }
    }

    /**
     * Launching a new coroutine to Update the data in a non-blocking way
     */
    suspend fun deleteTask(task: Task) {
        coroutineScope {
            repository.deleteTask(task)
        }
    }
}

class NewTaskViewModelFactory(private val repository: TaskRepository, private val id:Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewTaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewTaskViewModel(repository,id) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
