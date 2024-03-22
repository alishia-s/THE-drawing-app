package com.the.drawingapp

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class DrawingViewModel(private val repo : DrawingAppRepository) : ViewModel() {
    private val _canvasBitmap = MutableLiveData<Bitmap>()
    val canvasBitmap: LiveData<Bitmap> = _canvasBitmap
    private val _savedCanvases = MutableStateFlow<List<Bitmap>>(emptyList())
    val savedCanvases: Flow<List<Bitmap>> = _savedCanvases
    val tool = Tool()

    fun initBitmap() {
        if(_canvasBitmap.value == null){
            _canvasBitmap.value = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }
        }
    }

    //send drawing to repo via bitmap to png
    fun sendDrawing()
    {
        //send directly to repo
        repo.saveDrawing(_canvasBitmap.value!!)
    }

    //should be set to onclicklistener for the
    fun restoreDrawing(pos : Int)
    {
        viewModelScope.launch{
            repo.retrieveDrawing.collect{
                if (pos < it.size){
                    _canvasBitmap.value = it[pos];
                }
            }
        }
    }

    fun getAllDrawings(){
        viewModelScope.launch {
            repo.retrieveDrawing.collect{
                drawings -> _savedCanvases.value = drawings
            }
        }
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