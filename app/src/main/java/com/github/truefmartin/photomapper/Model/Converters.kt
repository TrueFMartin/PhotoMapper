package com.github.truefmartin.photomapper.Model

import androidx.room.TypeConverter
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class Converters {
    private val timeFormatter: DateTimeFormatter? = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.US)
    @TypeConverter
    fun fromDatabaseTimestamp(time: String?): LocalDateTime? {
        return time?.let { LocalDateTime.parse(time, timeFormatter) }
    }

    @TypeConverter
    fun toDatabaseTimeStamp(time: LocalDateTime?): String? {
        return time?.format(timeFormatter)
    }

    @TypeConverter
    fun fromDatabaseGeo(geo: String?): GeoPoint {
        return GeoPoint.fromDoubleString(geo, ',');
    }

    @TypeConverter
    fun toDatabaseGeo(geo: GeoPoint?): String {
        return geo.toString()
    }



}