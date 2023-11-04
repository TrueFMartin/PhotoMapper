package com.github.truefmartin.photomapper.Model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    //Get all photos sorted chronologically
    @Query("SELECT * FROM photo_table ORDER BY date ASC")
    fun getTimeSortedPhotos(): Flow<List<PhotoPath>>

    //Get a single photo with a given id
    @Query("SELECT * FROM photo_table WHERE id=:id")
    fun getPhotos(id:Int): Flow<PhotoPath>

    //Get a single photo with a given id
    @Query("SELECT * FROM photo_table WHERE id=:id")
    fun getPhotosNotLive(id:Int): PhotoPath

    //Insert a single photo
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(photoPath: PhotoPath): Long

    //Insert a single photo
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertBlocking(photoPath: PhotoPath): Long

    //Delete all photos
    @Query("DELETE FROM photo_table")
    suspend fun deleteAll()

    //Update a single photo
    @Update
    suspend fun update(photoPath: PhotoPath):Int

    // Delete a single photo

    @Delete
    suspend fun deletePhoto(photoPath: PhotoPath)

}
