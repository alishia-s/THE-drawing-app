package com.the.drawingapp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
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
import kotlin.math.log


class DrawableFragment: Fragment() {
    private val viewModel: DrawingViewModel by activityViewModels()
    private lateinit var binding: FragmentDrawableBinding
    private lateinit var drawingCanvas: Canvas
    private lateinit var bitmap: Bitmap
    private lateinit var tool: Tool

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDrawableBinding.inflate(inflater, container, false)
        tool = viewModel.tool
        initBackButton(binding)
        initPenSizeSlider(binding)
        initToolbarButtons(binding)
        return binding.root
    }

    private fun initBackButton(binding: FragmentDrawableBinding) {
        binding.backButton.setOnClickListener {
            binding.penSizeBar.progress = 12
//            parentFragmentManager.beginTransaction().remove(this).commit()
            parentFragmentManager.popBackStack()
            bitmap.eraseColor(Color.WHITE)
        }
    }

    private fun initPenSizeSlider(binding: FragmentDrawableBinding) {
        binding.penSizeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tool.updateStrokeWidth(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initBitmap()
        val drawingView = view.findViewById<DrawingView>(R.id.canvas)
        initDrawingCanvas(drawingView)
        initObservers(drawingView)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initDrawingCanvas(drawingView: DrawingView) {
        val tempPath = Path()
        drawingView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> tempPath.moveTo(event.x, event.y)
                MotionEvent.ACTION_MOVE -> {
                    tempPath.lineTo(event.x, event.y)
                    drawingCanvas.drawPath(tempPath, tool.paint)
                    viewModel.updateBitmap(bitmap)
                }
                MotionEvent.ACTION_UP -> {
                    tempPath.reset()
                }
            }
            drawingView.invalidate()
            true
        }
    }

    private fun initToolbarButtons(binding: FragmentDrawableBinding) {

        binding.rectangleButton?.setOnClickListener{
            tool.activateShape()
        }
        binding.eraserButton.setOnClickListener {
            tool.activateEraser()
        }
        binding.penButton.setOnClickListener {
            tool.activatePen()
        }
        binding.colorButton.setOnClickListener {
            ColorPickerDialog
                .Builder(requireContext())
                .setColorShape(ColorShape.CIRCLE)
                .setDefaultColor(0xFF000000.toInt())
                .setColorListener { color, _ ->
                    tool.updateColor(color)
                }
                .show()
        }
    }
    private fun initObservers(drawingView: DrawingView) {
        Log.e("DrawableFragment", "Observers Initialized")
        viewModel.canvasBitmap.observe(viewLifecycleOwner, Observer { bitmap ->
            this.bitmap = bitmap
            drawingView.setBitmap(bitmap)
            drawingCanvas = Canvas(bitmap)
            drawingView.invalidate()
        })
    }
}