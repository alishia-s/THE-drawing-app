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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.Date

class DrawingAppRepository(
    private val scope: CoroutineScope,
    private val dao: DrawingAppDao, private val context: Context
) {

    private val userViewModel = UserViewModel()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    //var allDrawings = mutableListOf<Bitmap>()
    fun getAllUserDrawings(): Flow<List<Drawing>> {
        val userId = userViewModel.getUserID() ?: ""
        return dao.getUserDrawings(userId).map { drawings ->
            drawings.map { drawing -> retrieveBitmapFromFile(drawing.name, drawing.id) }
        }
    }

    private fun retrieveBitmapFromFile(fileName: String, drawingId: Int): Drawing {
        val dir = context.filesDir.path
        val file = File(dir, fileName)
        var drawing = Drawing(drawingId, null)
        if (!file.exists()) {
            Log.e("Repository", "File $dir + $fileName does not exist")
            return drawing
        }

        val opts = BitmapFactory.Options().apply {
            inMutable = true
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        return try {
            val bitmap = decodeFile(file.absolutePath, opts)
            drawing = Drawing(drawingId, bitmap)
            drawing
        } catch (e: Exception) {
            Log.e("Repository", "Failed to decode file")
            drawing
        }
    }

    suspend fun retrieveUserImagesFromCloud(userId: String): List<Bitmap> =
        withContext(Dispatchers.IO) {
            try {
                val docs = firestore.collection("drawings")
                    .whereEqualTo("owner", userId)
                    .get()
                    .await()

                val urls = docs.documents.mapNotNull { it.getString("url") }
                if (urls.isNotEmpty()) downloadImages(urls)
                else emptyList()
            } catch (e: Exception) {
                throw e
            }
        }


    private suspend fun downloadImages(urls: List<String>): List<Bitmap> = coroutineScope {
        val imageTasks = urls.map { url ->
            async(Dispatchers.IO) {
                try {
                    val storageRef = storage.getReferenceFromUrl(url)
                    val oneMegabyte: Long = 1024 * 1024
                    val bytes = storageRef.getBytes(oneMegabyte).await()
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bitmap != null) {
                        saveImageLocally(bitmap, url)
                        Log.d("Repository", "Downloaded image $bitmap")
                    } else Log.e("Repository", "Failed to download image")
                    bitmap
                } catch (e: Exception) {
                    Log.e("Repository", "Failed to download image from $url", e)
                    null // Return null if an error occurs, allowing other downloads to continue
                }
            }
        }
        imageTasks.awaitAll().filterNotNull() // Collect the results, filtering out any nulls
    }

    private suspend fun saveImageLocally(bitmap: Bitmap, url: String) {
        val fileName = url.substringAfterLast("/") // Get filename
        val file = File(context.filesDir, fileName)
        withContext(Dispatchers.IO) {
            FileOutputStream(file).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
        val drawingData = DrawingAppData(
            userId = userViewModel.getUserID() ?: "",
            name = fileName,
            timestamp = Date(),
            imageUrl = url,
            isSynced = true
        )
        dao.saveDrawing(drawingData)
    }

    fun NUKE() {
        scope.launch {
            dao.NUKEITALL()
        }
    }

    suspend fun saveDrawing(drawing: Drawing): Int {

        val fileName = "${System.currentTimeMillis()}.png"
        val file = File(context.filesDir, fileName)
        withContext(Dispatchers.IO) {
            FileOutputStream(file).use {
                drawing.bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
        val userId = userViewModel.getUserID()
        val drawingData = DrawingAppData(userId ?: "", fileName, Date())
        dao.saveDrawing(drawingData)
        uploadToCloud(drawing, drawingData)
        return drawingData.id
    }

    private fun uploadToCloud(drawing: Drawing, drawingData: DrawingAppData) {
        val userID = userViewModel.getUserID() ?: return
        val storageRef = storage.reference.child("drawings/$userID/${drawingData.name}")
        val baos = ByteArrayOutputStream()
        drawing.bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        storageRef.putBytes(data).addOnCompleteListener {
            storageRef.downloadUrl.addOnCompleteListener() { uri ->
                val drawingUrl = uri.result.toString()
                scope.launch(Dispatchers.IO) {
                    dao.updateDrawingCloudStatus(
                        drawingData.id,
                        drawingUrl,
                        true,
                        drawingData.sharedWith
                    )
                }
                Log.d("upload", "DrawingURL: $drawingUrl")
                updateDrawingInfoToFirestore(userID, drawingUrl, drawingData)

            }
                .addOnFailureListener {
                    Log.d("upload", "Failed to upload drawing")
                    scope.launch(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Failed to upload drawing",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun updateDrawingInfoToFirestore(
        userId: String,
        drawingUrl: String,
        drawingData: DrawingAppData
    ) {
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
                    Toast.makeText(
                        context,
                        "Drawing uploaded to Firebase Cloud",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener {
                Log.d("upload", "Failed to upload drawing info to Firestore")
                scope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to upload drawing", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun shareDrawingWithUser(drawingId: Int, userId: String) {
        scope.launch(Dispatchers.IO) {
            val drawing = dao.getDrawingById(drawingId)
            val updatedSharedWith = drawing.sharedWith.split(",").toMutableList().apply {
                if (!contains(userId)) add(userId)
            }.joinToString(",")
            dao.updateDrawingCloudStatus(
                drawingId,
                drawing.imageUrl,
                drawing.isSynced,
                updatedSharedWith
            )
            updateFirestoreSharedWith(drawing.name, updatedSharedWith)
        }
    }

    private fun updateFirestoreSharedWith(drawingName: String, sharedWith: String) {
        val drawingRef = firestore.collection("drawings").document(drawingName)
        drawingRef.update("sharedWith", sharedWith)
            .addOnSuccessListener {
                Log.d(
                    "Repository",
                    "Updated sharing permissions in Firestore"
                )
            }
            .addOnFailureListener { e ->
                Log.e(
                    "Repository",
                    "Error updating sharing permissions",
                    e
                )
            }
    }


    fun retrieveSharedImages(
        userId: String,
        onSuccess: (List<String>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        firestore.collection("drawings")
            .whereArrayContains("sharedWith", userId)
            .get()
            .addOnSuccessListener { result ->
                val imageUrls = result.documents.mapNotNull { it.getString("url") }
                onSuccess(imageUrls)
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }
}