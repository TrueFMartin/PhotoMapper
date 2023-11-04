package com.github.truefmartin.photomapper.PhotoViewerActivity

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.github.truefmartin.photomapper.Model.PhotoPath
import com.github.truefmartin.photomapper.Model.PhotoRepository
import kotlinx.coroutines.coroutineScope

class PhotoViewerViewModel(private val repository: PhotoRepository, private val id:Int) : ViewModel() {

    var curPhotoPath: LiveData<PhotoPath> = repository.getPhoto(id).asLiveData()

    fun updateId(id:Int){
        curPhotoPath = repository.getPhoto(id).asLiveData()
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    suspend fun insertNoReturn(photoPath: PhotoPath){
        coroutineScope {
            repository.insert(photoPath)
        }
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insertGetID(photoPath: PhotoPath): Long {
            return repository.insertBlocking(photoPath)
    }
    /**
     * Launching a new coroutine to Update the data in a non-blocking way
     */
    suspend fun update(photoPath: PhotoPath) {
        coroutineScope {
            repository.update(photoPath)
        }
    }

    /**
     * Launching a new coroutine to Update the data in a non-blocking way
     */
    suspend fun deleteTask(photoPath: PhotoPath) {
        coroutineScope {
            repository.deletePhoto(photoPath)
        }
    }
}

class PhotoViewerModelFactory(private val repository: PhotoRepository, private val id:Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PhotoViewerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PhotoViewerViewModel(repository,id) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
