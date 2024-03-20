package com.the.drawingapp

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }
}

//when save is pressed, save and send it as a DrawingAppData
@Entity(tableName = "drawing")
data class DrawingAppData(var timestamp: Date,
                          var name : String) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
