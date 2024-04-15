package com.the.drawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.decodeFile
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Date

class DrawingAppRepository(private val scope: CoroutineScope,
                           private val dao: DrawingAppDao, private val context: Context) {

    private val userViewModel = UserViewModel()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    //var allDrawings = mutableListOf<Bitmap>()
    val retrieveDrawing: Flow<List<Bitmap?>> = dao.getAllDrawings()
        .map { list ->
            list.map { drawing ->
                retrieveBitmapFromFile(drawing.name)
            }
        }

    private fun retrieveBitmapFromFile(fileName: String): Bitmap? {
        val dir = context.filesDir.path
        val file = File(dir, fileName)
        if (!file.exists()) {
            Log.e("Repository", "File $dir + $fileName does not exist")
            return null
        }

        val opts = BitmapFactory.Options().apply {
            inMutable = true
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        return try {
            decodeFile(file.absolutePath, opts)
        } catch (e: Exception) {
            Log.e("Repository", "Failed to decode file")
            null
        }
    }

    fun NUKE(){
        scope.launch {
            dao.NUKEITALL()
        }
    }
    
    fun saveDrawing(drawing : Bitmap) {
        scope.launch(Dispatchers.IO) {
            val fileName = "${System.currentTimeMillis()}.png"
            val file = File(context.filesDir, fileName)
            withContext(Dispatchers.IO) {
                FileOutputStream(file).use {
                    drawing.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
            }
            val drawingData = DrawingAppData(Date(), fileName, file.path)
            dao.saveDrawing(drawingData)
            uploadToCloud(drawing, drawingData)
        }
    }

    private fun uploadToCloud(drawing: Bitmap, drawingData: DrawingAppData) {
        val userID = userViewModel.getUserID() ?: return
        val storageRef = storage.reference.child("drawings/$userID/${drawingData.name}")
        val baos = ByteArrayOutputStream()
        drawing.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()
        
        storageRef.putBytes(data).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                storageRef.downloadUrl.addOnCompleteListener() { uri ->
                    val drawingUrl = uri.toString()
                    scope.launch(Dispatchers.IO) { dao.updateDrawingCloudStatus(drawingData.id.toString(), drawingUrl, true) }
                    updateDrawingInfoToFirestore(userID, drawingUrl, drawingData)
                }.addOnFailureListener {
                    Log.d("upload", "Failed to upload drawing")
                    scope.launch(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to upload drawing", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateDrawingInfoToFirestore(userId:String, drawingUrl: String, drawingData: DrawingAppData) {
        val drawingInfo = hashMapOf(
            "owner" to userId,
            "url" to drawingUrl,
            "name" to drawingData.name,
            "timestamp" to drawingData.timestamp.time
        )
        firestore.collection("drawings").document(drawingData.name).set(drawingInfo)
            .addOnSuccessListener {
            Log.d("upload", "Drawing information deployed to Firestore")
            scope.launch(Dispatchers.Main) {
                Toast.makeText(context, "Drawing uploaded to Firebase Cloud", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Log.d("upload", "Failed to upload drawing info to Firestore")
            scope.launch(Dispatchers.Main) {
                Toast.makeText(context, "Failed to upload drawing", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun shareDrawingWithUser(drawingId: String, userId: String) {
        val drawingRef = firestore.collection("drawings").document(drawingId)
        firestore.runTransaction() { transaction->
            val snapshot = transaction.get(drawingRef)
            val sharedWith = snapshot.get("sharedWith") as? List<String> ?: emptyList()
            if (userId !in sharedWith) {
                transaction.update(drawingRef, "sharedWith", sharedWith + userId)
            }
        }.addOnSuccessListener {
            Log.d("share", "Drawing shared with user")
            scope.launch(Dispatchers.Main) {
                Toast.makeText(context, "Drawing shared with user", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Log.d("share", "Failed to share drawing with user")
            scope.launch(Dispatchers.Main) {
                Toast.makeText(context, "Failed to share drawing with user", Toast.LENGTH_SHORT).show()
            }
        }
    }
}