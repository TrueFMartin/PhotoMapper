package com.github.truefmartin.photomapper.Model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.truefmartin.photomapper.NewEditTaskActivity.RecurringState
import java.time.LocalDateTime

@Entity(tableName = "task_table")
class Task(
    //Note that we now allow for ID as the primary key
    //It needs to be nullable when creating a new word in the database
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "title") var title: String,
    @ColumnInfo(name = "body") var body: String,
    @ColumnInfo(name = "date") var date: LocalDateTime,
    @ColumnInfo(name = "completed") var completed: Boolean,
    @ColumnInfo(name = "repeated") var repeated: RecurringState,
    @ColumnInfo(name = "notification_id") var noteID: Int
)

