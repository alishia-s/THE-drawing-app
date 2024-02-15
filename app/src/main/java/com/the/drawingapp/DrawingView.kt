package com.the.drawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var canvasBitmap: Bitmap? = null
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvasBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
    }

    fun setBitmap(bitmap: Bitmap){
        canvasBitmap = bitmap
        invalidate()
    }

}