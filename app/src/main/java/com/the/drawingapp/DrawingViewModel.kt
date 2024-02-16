package com.the.drawingapp

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DrawingViewModel : ViewModel() {
    private val _canvasBitmap = MutableLiveData<Bitmap>()
    val canvasBitmap: LiveData<Bitmap> = _canvasBitmap

    val tool = Tool()

    fun initBitmap() {
        if(_canvasBitmap.value == null){
            _canvasBitmap.value = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }
        }
    }

    fun updateBitmap(bitmap: Bitmap) {
        _canvasBitmap.value = bitmap
    }

}