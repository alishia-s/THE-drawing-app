package com.the.drawingapp

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
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
    var undoStatus = MutableLiveData<Boolean>()
    var redoStatus = MutableLiveData<Boolean>()

    private val undoStack : MutableList<Bitmap> = mutableListOf()
    private val redoStack : MutableList<Bitmap> = mutableListOf()

    //for reverting

    private val _savedCanvases = MutableStateFlow<List<Drawing>>(emptyList())
    val savedCanvases: Flow<List<Drawing>> = _savedCanvases

    private var _currentDrawing = MutableStateFlow<Drawing?>(null)
    var currentDrawing: StateFlow<Drawing?> = _currentDrawing.asStateFlow()

    val tool = Tool()
    private val userViewModel = UserViewModel()

    private val greyscale = Greyscale()

    private fun push(bmp : Bitmap, stack: MutableList<Bitmap>) = stack.add(bmp)
    private fun pop(stack: MutableList<Bitmap>) : Bitmap?
    {
        val bmp = stack.lastOrNull()

        if(!stack.isEmpty()){
            stack.removeAt(stack.size-1)
        }
        return bmp
    }

    fun greyscale(){
        val bmp = _canvasBitmap.value!!.copy(Bitmap.Config.ARGB_8888, true)
        greyscale.greyscale(bmp)
        _canvasBitmap.value = bmp
    }

    fun undo(){
        val bmp = pop(undoStack)
        if(bmp != null)
        {
            push(_canvasBitmap.value!!, redoStack)
            _canvasBitmap.value = bmp!!
            Log.e("UNDO", "UNDO with redo stack ${redoStack.size}")

        }
        else{
            undoStatus.value = false
            Log.e("UNDO", "DIDN'T UNDO")
        }
    }

    fun redo()
    {
       val bmp = pop(redoStack)
       if(bmp != null)
       {
           push(_canvasBitmap.value!!, undoStack)
           _canvasBitmap.value = bmp!!
           Log.e("REDO", "REDO")
       }
       else{
           redoStatus.value = false
           Log.e("REDO", "DIDN'T REDO")
       }
    }

    fun initBitmap() {
        if(_canvasBitmap.value == null){
            _canvasBitmap.value = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888).apply {
                eraseColor(Color.WHITE) }
        }
        push(_canvasBitmap.value!!, undoStack)
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
        Log.e("size of undo stack", "${undoStack.size}")
        _canvasBitmap.value = bitmap
    }

    fun addToUndoStack(bitmap : Bitmap)
    {
        push(bitmap, undoStack)
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