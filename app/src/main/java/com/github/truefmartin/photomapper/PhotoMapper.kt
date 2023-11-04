package com.github.truefmartin.photomapper

import android.app.Application
import android.content.Context
import com.github.truefmartin.photomapper.Model.PhotoRepository
import com.github.truefmartin.photomapper.Model.PhotoRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class PhotoMapper : Application() {
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { PhotoRoomDatabase.getDatabase(this,applicationScope) }
    val repository by lazy { PhotoRepository(database.photoDao()) }

    init {
        instance = this
    }

    companion object {
        private var instance: PhotoMapper? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        val context: Context = PhotoMapper.applicationContext()
    }
}
