package com.the.drawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private lateinit var bitmap: Bitmap
    private lateinit var canvas: Canvas
    private var paint: Paint = Paint()
    private var drawPath: Path = Path()

    // Lazy rect init
    private val rect: Rect by lazy {
        Rect(0, 0, 800, 800)
    }

    init{
        // Set the default paint color, and stroke width
        paint.color = Color.BLACK
        paint.strokeWidth = 12f
        paint.style = Paint.Style.STROKE

        // Initialize the bitmap
        bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.WHITE) // White Background
        canvas = Canvas(bitmap)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bitmap, null, rect, paint)
        canvas.drawPath(drawPath, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> drawPath.moveTo(touchX, touchY)
            MotionEvent.ACTION_MOVE -> drawPath.lineTo(touchX, touchY)
            MotionEvent.ACTION_UP -> {
                canvas.drawPath(drawPath, paint)
                drawPath.reset()
            }
            else -> return false
        }
        invalidate()
        return true
    }

    fun setPaintColor(color: Int) {
        paint.color = color
    }

    fun setBitmap(bitmap: Bitmap) {
        this.bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        canvas = Canvas(this.bitmap)
        invalidate()
    }

    fun setBrushSize(size: Float) {
        paint.strokeWidth = size
    }
}