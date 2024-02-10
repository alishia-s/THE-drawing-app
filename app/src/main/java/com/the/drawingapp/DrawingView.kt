package com.the.drawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val bitmap: Bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
    private val canvas: Canvas = Canvas(bitmap)
    private val paint: Paint = Paint();
    // Lazy rect init
    private val rect: Rect by lazy {
        Rect(0, 0, 800, 800)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bitmap, null, rect, paint)
    }

    fun drawSomething() {
        paint.color = 0xFF0000FF.toInt()
        canvas.drawCircle(400f, 400f, 200f, paint)
        invalidate()
    }
}