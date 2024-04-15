package com.the.drawingapp

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Database(entities= [DrawingAppData::class], version = 2, exportSchema = false)
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
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

@Dao
interface DrawingAppDao
{
    /**
     * Inserts a new drawing or replaces an existing one in the database
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDrawing(data: DrawingAppData)

    /**
     * Retrieves all drawings from the database
     */
    @Query("SELECT * from drawings")
    fun getAllDrawings() : Flow<List<DrawingAppData>>

    @Query("UPDATE drawings SET imageUrl = :imageUrl, isSynced = :isSynced WHERE id = :id")
    suspend fun updateDrawingCloudStatus(id: String, imageUrl: String?, isSynced: Boolean)

    @Query("SELECT EXISTS(SELECT 1 FROM drawings WHERE imageUrl = :imageUrl)")
    suspend fun exists(imageUrl: String?): Boolean

    @Query("Delete from drawings")
    fun NUKEITALL()
}