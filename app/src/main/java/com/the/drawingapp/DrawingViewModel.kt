package com.the.drawingapp

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DrawingViewModel : ViewModel() {
    private val _currentColor = MutableLiveData<Int>()
    val currentColor: LiveData<Int>
        get() = _currentColor

    private val _currentStrokeWidth = MutableLiveData<Float>()
    val currentStrokeWidth: LiveData<Float>
        get() = _currentStrokeWidth

    private val _canvasBitmap = MutableLiveData<Bitmap>()
    val canvasBitmap: LiveData<Bitmap>
        get() = _canvasBitmap

    fun setColor(color: Int){
        _currentColor.value = color
    }

    fun setStrokeWidth(strokeWidth: Float){
        _currentStrokeWidth.value = strokeWidth
    }

    fun updateBitmap(bitmap: Bitmap){
        _canvasBitmap.value = bitmap
    }
    fun setDrawingBitmap(bitmap: Bitmap){
        _canvasBitmap.value = bitmap
    }
}