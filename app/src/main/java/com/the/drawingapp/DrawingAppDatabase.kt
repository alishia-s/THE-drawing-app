package com.the.drawingapp

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow

@Database(entities= [DrawingAppData::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class DrawingAppDatabase : RoomDatabase() {
    abstract fun drawingAppDao() : DrawingAppDao

    companion object {
        @Volatile
        private var INSTANCE : DrawingAppDatabase? = null

        fun getDatabase(context: Context) : DrawingAppDatabase{
            return INSTANCE ?: synchronized(this){
                if(INSTANCE != null) return INSTANCE!!
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DrawingAppDatabase::class.java,
                    "drawingapp_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

@Dao
interface DrawingAppDao
{
    @Insert
    suspend fun saveDrawing(data: DrawingAppData)

    @Query("SELECT * from drawings")
    fun getAllDrawings() : Flow<List<DrawingAppData>>

    @Query("Delete from drawings")
    fun NUKEITALL()
}