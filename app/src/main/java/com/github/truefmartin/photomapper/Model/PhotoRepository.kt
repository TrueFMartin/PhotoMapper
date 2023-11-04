package com.github.truefmartin.photomapper.Model

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class PhotoRepository(private val photoDao: PhotoDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allPhotos: Flow<List<PhotoPath>> = photoDao.getTimeSortedPhotos()

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    fun getPhoto(id:Int):Flow<PhotoPath>{
        return photoDao.getPhotos(id)
    }

    fun getPhotoNotLive(id:Int):PhotoPath{
        return photoDao.getPhotosNotLive(id)
    }

    fun insertBlocking(photoPath: PhotoPath): Long {

        return photoDao.insertBlocking(photoPath)
    }
    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(photoPath: PhotoPath): Long {

        return photoDao.insert(photoPath)
    }

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(photoPath: PhotoPath) {
        photoDao.update(photoPath)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deletePhoto(photoPath: PhotoPath) {
        photoDao.deletePhoto(photoPath)
    }
}
