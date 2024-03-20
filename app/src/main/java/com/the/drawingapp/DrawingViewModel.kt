package com.the.drawingapp

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.io.FileOutputStream
import java.lang.IllegalArgumentException

class DrawingViewModel(private val repo : DrawingAppRepository) : ViewModel() {
    private val _canvasBitmap = MutableLiveData<Bitmap>()
    val canvasBitmap: LiveData<Bitmap> = _canvasBitmap

    val tool = Tool()

    fun initBitmap() {
        if(_canvasBitmap.value == null){
            _canvasBitmap.value = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }
        }
    }

    //send drawing to repo via bitmap to png
    fun sendDrawing()
    {
        //send directly to repo
        repo.saveDrawing(_canvasBitmap.value!!)
    }


    fun restoreDrawing(){}

    fun updateBitmap(bitmap: Bitmap) {
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