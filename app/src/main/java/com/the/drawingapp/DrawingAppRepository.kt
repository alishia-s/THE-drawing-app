package com.the.drawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.decodeFile
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    
    fun saveDrawing(drawing : Bitmap)
    {
        scope.launch(Dispatchers.IO) {
            val localPath = saveDrawingLocal(drawing)
            if (userViewModel.getUserID() != null) {
                uploadToFirebase(drawing, System.currentTimeMillis().toString(), localPath)
            } else {
                Log.e("upload", "User not logged in")
            }
        }
    }

    private suspend fun saveDrawingLocal(drawing: Bitmap): String {
        val fileName = "${System.currentTimeMillis()}.png"
        val file = File(context.filesDir, fileName)
        withContext(Dispatchers.IO) {
            FileOutputStream(file).use {
                drawing.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
        val drawingData = DrawingAppData(Date(), fileName)
        dao.saveDrawing(drawingData)

        return file.absolutePath
    }

    private fun uploadToFirebase(drawing: Bitmap, drawingName: String, localPath: String) {
        val userID = userViewModel.getUserID() ?: return
        val storageRef = storage.reference.child("drawings/$userID/${drawingName}.png")
        val baos = ByteArrayOutputStream()
        drawing.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()
        
        storageRef.putBytes(data).addOnCompleteListener {
            storageRef.downloadUrl.addOnCompleteListener() { uri -> 
                val drawingUrl = uri.toString()
                saveDrawingUrlToFirestore(userID, drawingUrl, drawingName, localPath)
            }.addOnFailureListener {
                Log.d("upload", "Failed to upload drawing")
                scope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to upload drawing", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveDrawingUrlToFirestore(userId:String, drawingUrl: String, drawingName: String, localPath: String) {
        val drawingData = hashMapOf(
            "owner" to userId,
            "url" to drawingUrl,
            "name" to drawingName,
            "localPath" to localPath,
            "timestamp" to System.currentTimeMillis()
        )
        firestore.collection("drawings").add(drawingData).addOnSuccessListener {
            Log.d("upload", "Drawing uploaded to Firestore")
            scope.launch(Dispatchers.Main) {
                Toast.makeText(context, "Drawing uploaded to Firebase Cloud", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Log.d("upload", "Failed to upload drawing to Firestore")
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