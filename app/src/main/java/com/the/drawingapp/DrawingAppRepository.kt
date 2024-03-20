package com.the.drawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.decodeFile
import android.util.Log
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Date

class DrawingAppRepository(private val scope: CoroutineScope,
                           private val dao: DrawingAppDao, private val context: Context) {

    //var allDrawings = mutableListOf<Bitmap>()
    val retrieveDrawing:Flow<List<Bitmap>> = dao.getAllDrawings()
        .map{list: List<DrawingAppData> ->
            list.map{drawing : DrawingAppData -> decodeFile(drawing.name)
            }
        }
    fun saveDrawing(drawing : Bitmap)
    {
        scope.launch {
            //source: https://stackoverflow.com/questions/65767693/android-save-bitmap-to-image-file
            try {
                //change "drawing" into proper file name
                val data = DrawingAppData(Date(), "drawing")
                val dir = context.filesDir.path
                val file = File(dir, "${data.name}-${data.id}.png")

                if (!file.exists()) {
                    Log.d("saving", "creating new file")
                    file.createNewFile()
                }

                //sanity check
                else{
                    Log.d("saving", "using existing file: ${file.path}")
                }

                val out = FileOutputStream(file)
                drawing.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()

                data.replaceName(file.path)

                dao.saveDrawing(data)
                Log.d("saving", "saved to ${file}")
            } catch (e: Exception) {
                Log.d("saving", e.toString())
            }
        }
    }
}