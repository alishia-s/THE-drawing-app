package com.the.drawingapp

import android.graphics.Color
import android.graphics.Paint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class Tool {

    private val _currentColor = MutableLiveData<Int>().apply { value = 0xFF000000.toInt() }
    val currentColor: LiveData<Int> = _currentColor

    private val _isPenActive = MutableLiveData<Boolean>().apply { value = false }
    val isPenActive: LiveData<Boolean> = _isPenActive

    private val _isShapeActive = MutableLiveData<Boolean>().apply { value = false }
    val isShapeActive: LiveData<Boolean> = _isShapeActive

    private val _isEraserActive = MutableLiveData<Boolean>().apply { value = false }
    val isEraserActive: LiveData<Boolean> = _isEraserActive

    private val _strokeWidth = MutableLiveData<Float>().apply { value = 12f }
    val strokeWidth: LiveData<Float> = _strokeWidth

    val paint = Paint().apply {
        color = _currentColor.value ?: Color.BLACK
        strokeWidth = 12f // Default stroke width
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    fun updateColor(color: Int) {
        _currentColor.value = color
        if (!isEraserActive.value!!) {
            paint.color = color
        }
    }

    fun activatePen() {
        _isPenActive.value = true
        _isShapeActive.value = false
        _isEraserActive.value = false
        paint.color = _currentColor.value ?: Color.BLACK
        paint.strokeWidth = _strokeWidth.value ?: 12f
    }

    fun updateStrokeWidth(width: Float) {
        _strokeWidth.value = width
        paint.strokeWidth = width
    }

    fun activateShape() {
        _isPenActive.value = false
        _isShapeActive.value = true
        _isEraserActive.value = false
        paint.apply {
            strokeJoin = Paint.Join.BEVEL
            strokeCap = Paint.Cap.SQUARE  }
    }

    fun activateEraser() {
        _isEraserActive.value = true
        _isPenActive.value = false
        _isShapeActive.value = false
        paint.color = Color.WHITE
    }
}