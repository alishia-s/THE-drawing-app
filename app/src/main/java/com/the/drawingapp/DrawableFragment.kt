package com.the.drawingapp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.the.drawingapp.databinding.FragmentDrawableBinding


class DrawableFragment: Fragment() {
    private val viewModel: DrawingViewModel by activityViewModels()
    private lateinit var binding: FragmentDrawableBinding
    private lateinit var paint: Paint
    private lateinit var drawingCanvas: Canvas
    private lateinit var bitmap: Bitmap
    private var pen = false
    private var shape = false
    private var currentColor: Int = 0xFF000000.toInt()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDrawableBinding.inflate(inflater, container, false)
        initBackButton(binding)
        initPenSizeSlider(binding)
        initToolbarButtons(binding)
        return binding.root
    }

    private fun initBackButton(binding: FragmentDrawableBinding) {
        binding.backButton.setOnClickListener {
            paint.apply { this.color = 0xFF000000.toInt() }
            binding.penSizeBar.progress = 12
            parentFragmentManager.beginTransaction().remove(this).commit()
        }
    }

    private fun initPenSizeSlider(binding: FragmentDrawableBinding) {
        binding.penSizeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                paint.apply { this.strokeWidth = progress.toFloat() }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val drawingView = view.findViewById<DrawingView>(R.id.canvas)
        initObservers(drawingView)
        initDrawingCanvas(drawingView)
        initBitmapAndCanvas()
    }

    private fun initBitmapAndCanvas() {
        if(viewModel.canvasBitmap.value == null) {
            bitmap =
                Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.WHITE) }
            drawingCanvas = Canvas(bitmap)
            viewModel.updateBitmap(bitmap)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initDrawingCanvas(drawingView: DrawingView) {
        //Initialize the Pen tool
        currentColor = 0xFF000000.toInt()
        paint = Paint().apply {
            color = currentColor
            strokeWidth = 12f
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        val tempPath = Path()
        var xi = 0
        var yi = 0
        drawingView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    tempPath.moveTo(event.x, event.y)
                    xi = event.x.toInt()
                    yi = event.y.toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    if(pen){
                        tempPath.lineTo(event.x, event.y)
                        drawingCanvas.drawPath(tempPath, paint)
                        viewModel.updateBitmap(bitmap)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if(shape){
                        val rect = Rect(xi, yi, event.x.toInt(), event.y.toInt())
                        drawingCanvas.drawRect(rect, paint)
                    }
                    tempPath.reset()
                    xi = 0
                    yi = 0
                }
            }
            drawingView.invalidate()
            true
        }
    }

    private fun initToolbarButtons(binding: FragmentDrawableBinding) {

        binding.rectangleButton?.setOnClickListener{
            paint.apply { this.color = currentColor }
            pen = false;
            shape = true;
        }
        binding.eraserButton.setOnClickListener {
            paint.apply { this.color = Color.WHITE }
        }
        binding.penButton.setOnClickListener {
            paint.apply { this.color = currentColor }
                pen = true;
                shape = false;
        }
        binding.colorButton.setOnClickListener {
            ColorPickerDialog
                .Builder(requireContext())
                .setColorShape(ColorShape.CIRCLE)
                .setDefaultColor(0xFF000000.toInt())
                .setColorListener { color, _ ->
                    currentColor = color
                    viewModel.updateColor(currentColor)
                    paint.apply { this.color = currentColor }
                }
                .show()
        }
    }
    private fun initObservers(drawingView: DrawingView) {
        viewModel.canvasBitmap.observe(viewLifecycleOwner, Observer { bitmap ->
            this.bitmap = bitmap
            drawingCanvas = Canvas(bitmap)
            drawingView.setBitmap(bitmap)
            drawingView.invalidate()
        })

        viewModel.color.observe(viewLifecycleOwner, Observer { color ->
            currentColor = color
            paint.apply { this.color = currentColor }
        })
    }

}