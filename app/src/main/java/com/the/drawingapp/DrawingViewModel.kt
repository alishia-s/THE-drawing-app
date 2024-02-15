package com.the.drawingapp

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DrawingViewModel : ViewModel() {
    private val _canvasBitmap = MutableLiveData<Bitmap>()
    val canvasBitmap: LiveData<Bitmap> = _canvasBitmap

    private val _color = MutableLiveData<Int>()
    val color: LiveData<Int> = _color

    fun updateBitmap(bitmap: Bitmap) {
        _canvasBitmap.value = bitmap
    }
    fun updateColor(color: Int) {
        _color.value = color
    }

}