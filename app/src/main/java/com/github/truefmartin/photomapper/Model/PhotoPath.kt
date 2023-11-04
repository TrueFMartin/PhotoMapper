package com.github.truefmartin.photomapper.Model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.time.LocalDateTime

@Entity(tableName = "photo_table")
class PhotoPath(
    //Note that we now allow for ID as the primary key
    //It needs to be nullable when creating a new word in the database
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "filename") var fileName: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "date") var date: LocalDateTime,
    @ColumnInfo(name = "geo") var geo: GeoPoint,

)

