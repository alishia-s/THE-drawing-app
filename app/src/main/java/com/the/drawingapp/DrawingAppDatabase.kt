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

@Database(entities= [DrawingAppData::class], version = 3, exportSchema = false)
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
     * Retrieves only the user's drawings from DB. Query recommended by Copilot.
     */
    @Query("SELECT * from drawings WHERE userId = :userId OR ',' || sharedWith || ',' LIKE '%,' || :userId || ',%'")
    fun getUserDrawings(userId: String) : Flow<List<DrawingAppData>>

    @Query("UPDATE drawings SET imageUrl = :imageUrl, isSynced = :isSynced, sharedWith = :sharedWith WHERE id = :id")
    suspend fun updateDrawingCloudStatus(id: Int, imageUrl: String?, isSynced: Boolean, sharedWith: String)

    @Query("SELECT EXISTS(SELECT 1 FROM drawings WHERE imageUrl = :imageUrl)")
    suspend fun exists(imageUrl: String?): Boolean

    @Query("SELECT * from drawings WHERE id = :id")
    suspend fun getDrawingById(id: Int): DrawingAppData

    @Query("DELETE from drawings where id = :id")
    fun deleteDrawingById(id: Int)

    @Query("Delete from drawings")
    suspend fun NUKEITALL()

}