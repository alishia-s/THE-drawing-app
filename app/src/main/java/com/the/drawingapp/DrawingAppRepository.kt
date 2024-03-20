package com.the.drawingapp

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.io.FileOutputStream
import java.util.Date

class DrawingAppRepository(private val scope: CoroutineScope,
                           private val dao: DrawingAppDao, private val context: Context) {

    //val currentDrawing = dao.getCurrentDrawing().asLiveData()
    //val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferenceFilename")

    fun saveDrawing(drawing : Bitmap)
    {
        //source: https://stackoverflow.com/questions/65767693/android-save-bitmap-to-image-file
        try{
            //change "drawing" into proper file name
            val data = DrawingAppData(Date(), "drawing")
            val dir = context.filesDir.path
            val file = File(dir, data.name+".png")

            if(!file.exists()){
                file.createNewFile()
            }
            val out = FileOutputStream(file)
            drawing.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            Log.d("saving", "saved to ${file}")
        }
        catch(e : Exception){
            Log.d("saving", e.toString())
        }
    }

//    fun retrieveDrawing() : Bitmap
//    {
//        return Bitm
//    }
}