package com.the.drawingapp

import android.app.Application
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class DrawingApplication : Application(){

    val scope = CoroutineScope(SupervisorJob())

    val db by lazy{Room.databaseBuilder(
        applicationContext,
        DrawingAppDatabase::class.java,
        "drawingapp_database"
    )
        .fallbackToDestructiveMigration()
        .build()}

    val drawingAppRepository by lazy{DrawingAppRepository(scope, db.drawingAppDao(), applicationContext)}
}