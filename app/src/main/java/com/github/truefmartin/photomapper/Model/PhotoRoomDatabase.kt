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

@Database(entities = [PhotoPath::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PhotoRoomDatabase : RoomDatabase() {

    abstract fun photoDao(): PhotoDao

    private class PhotoDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            INSTANCE?.let { database ->
                scope.launch {
                    val photoDao = database.photoDao()
                    // Delete all content here.
                    photoDao.deleteAll()
                }
            }
            Log.d("Database","DB created")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: PhotoRoomDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): PhotoRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PhotoRoomDatabase::class.java,
                    "task_database"
                )
                    .addCallback(PhotoDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}

