package com.the.drawingapp

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

    fun setColor(color: Int){
        _currentColor.value = color
    }

    fun setStrokeWidth(strokeWidth: Float){
        _currentStrokeWidth.value = strokeWidth
    }
}