package com.github.truefmartin.MainActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.github.truefmartin.photomapper.Model.Task
import com.github.truefmartin.photomapper.Model.TaskRepository

class TaskListViewModel(private val repository: TaskRepository) : ViewModel() {

    // Using LiveData and caching what allWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allTasks: LiveData<List<Task>> = repository.allTasks.asLiveData()
    suspend fun update(task: Task) {
        repository.update(task)
    }
    fun getTaskByID(id: Int): Task {
        return repository.getTaskNotLive(id)
    }

}

class TaskListViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskListViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
