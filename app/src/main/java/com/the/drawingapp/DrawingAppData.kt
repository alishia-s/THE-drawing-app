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

    @TypeConverter
    fun fromCsv(value: String?): List<String>? {
        return value?.split(",") ?: emptyList()
    }

    fun toCsv(list: List<String>?) : String {
        return list?.joinToString { "," } ?: ""
    }
}

//when save is pressed, save and send it as a DrawingAppData
@Entity(tableName = "drawings")
data class DrawingAppData(
    var userId: String,
    var name: String,
    var timestamp: Date,
    var imageUrl: String? = null,
    var isSynced: Boolean = false,
    var sharedWith: String = ""
) {
    //will autoincrement
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    fun replaceName(newName: String) {
        name = newName
    }
}
