package com.the.drawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var canvasBitmap: Bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(canvasBitmap)

    init {
        canvasBitmap.eraseColor(Color.WHITE)
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(canvasBitmap, 0f, 0f, null)
    }

    fun setBitmap(bitmap: Bitmap){
        canvasBitmap = bitmap
    }

    fun getBitmap(): Bitmap = canvasBitmap

}