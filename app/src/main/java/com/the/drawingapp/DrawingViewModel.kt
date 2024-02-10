package com.the.drawingapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DrawingViewModel : ViewModel() {
    private val _currentColor = MutableLiveData<Int>()
    private val _currentStrokeWidth = MutableLiveData<Float>()
    init{
        // Set the default color and stroke width here.
        _currentColor.value = 0
        _currentStrokeWidth.value = 12f
    }

    fun setColor(color: Int){
        _currentColor.value = color
    }

    fun setStrokeWidth(strokeWidth: Float){
        _currentStrokeWidth.value = strokeWidth
    }
}