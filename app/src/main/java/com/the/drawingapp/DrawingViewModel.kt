package com.the.drawingapp

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DrawingViewModel(private val repo : DrawingAppRepository) : ViewModel() {
    init{
        System.loadLibrary("filter")
    }
    private val _canvasBitmap = MutableLiveData<Bitmap>()
    val canvasBitmap: LiveData<Bitmap> = _canvasBitmap

    private val _savedCanvases = MutableStateFlow<List<Drawing>>(emptyList())
    val savedCanvases: Flow<List<Drawing>> = _savedCanvases

    private var _currentDrawing = MutableStateFlow<Drawing?>(null)
    var currentDrawing: StateFlow<Drawing?> = _currentDrawing.asStateFlow()

    val tool = Tool()
    private val userViewModel = UserViewModel()

    private val greyscale = Greyscale()

    fun greyscale(){
        val bmp = _canvasBitmap.value!!.copy(Bitmap.Config.ARGB_8888, true)
        greyscale.greyscale(bmp)
        _canvasBitmap.value = bmp
    }

    fun initBitmap() {
        if(_canvasBitmap.value == null){
            _canvasBitmap.value = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }
        }
    }

    fun updateCurrentDrawing(drawing: Drawing) {
        _currentDrawing.value = drawing
    }

    fun deleteCurrentDrawing() {
        viewModelScope.launch {
            _currentDrawing.value?.let {
                it.id?.let { currID -> repo.deleteDrawing(currID) }
            }
        }
    }

    //send drawing to repo via bitmap to png
    fun sendDrawing()
    {
        //send directly to repo
        val drawing = Drawing(null, _canvasBitmap.value)
        viewModelScope.launch {
            drawing.id = repo.saveDrawing(drawing)
        }

    }

    fun syncWithCloud() {
        viewModelScope.launch {
            try {
                val images = repo.retrieveUserImagesFromCloud(userViewModel.getUserID() ?: return@launch)
                Log.d("DrawingViewModel", "Synced ${userViewModel.getUserID() ?: ""} with cloud, retrieved ${images.size} images")
            } catch (e: Exception) {
                Log.e("DrawingViewModel", e.localizedMessage ?: "Error syncing with cloud")
            }
        }
    }


//    fun restoreDrawing(pos : Int)
//    {
//        viewModelScope.launch{
//            repo.retrieveDrawing.collect{
//                if (pos < it.size){
//                    _canvasBitmap.value = it[pos];
//                }
//            }
//        }
//    }

    fun getAllUserDrawings(){
        viewModelScope.launch {
            repo.getAllUserDrawings().collect {
                _savedCanvases.value = it
            }
        }
    }

    fun shareDrawing(email: String) {
        _currentDrawing.value?.id?.let { userViewModel.getUserIDByEmail(email,
            { uID -> repo.shareDrawingWithUser(it, uID!!)},
            { e -> Log.e("DrawingViewModel", e.localizedMessage ?: "Error sharing drawing") }) }
    }

    fun NUKE(){
        repo.NUKE()
    }


    fun updateBitmap(bitmap: Bitmap)
    {
        _canvasBitmap.value = bitmap
    }

    class DrawingViewModelFactory(private val repo : DrawingAppRepository) : ViewModelProvider.Factory{
        override fun <T: ViewModel> create(modelClass: Class<T>) : T {
            if(modelClass.isAssignableFrom(DrawingViewModel::class.java)){
                @Suppress("UNCHECKED_CAST")
                return DrawingViewModel(repo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}