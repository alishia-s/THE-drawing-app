package com.the.drawingapp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.the.drawingapp.databinding.FragmentDrawableBinding


class DrawableFragment: Fragment() {
    private val drawingViewModel : DrawingViewModel by activityViewModels{DrawingViewModel.DrawingViewModelFactory((getActivity()?.application as DrawingApplication).drawingAppRepository)}
    private lateinit var binding: FragmentDrawableBinding
    private lateinit var drawingCanvas: Canvas
    private lateinit var bitmap: Bitmap
    private lateinit var tool: Tool
    private var isCircle = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDrawableBinding.inflate(inflater, container, false)
        tool = drawingViewModel.tool
        initBackButton(binding)
        initPenSizeSlider(binding)
        initSaveButton(binding)
        initShareButton(binding, inflater)
        initToolbarButtons()
        initDeleteButton()
        return binding.root
    }

    private fun initDeleteButton() {
        binding.deleteButton.setOnClickListener {
            drawingViewModel.deleteCurrentDrawing()
            findNavController().navigate(R.id.action_DrawableFragmentToMainScreen)
        }
    }

    private fun initShareButton(binding: FragmentDrawableBinding, inflater : LayoutInflater) {
        binding.shareButton.setOnClickListener {
            val popupView = inflater.inflate(R.layout.share_popup, null)
            val sendButton = popupView.findViewById<AppCompatButton>(R.id.send_button)
            val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
            popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
            sendButton.setOnClickListener{
                val emailEntry = popupView.findViewById<EditText>(R.id.popup_entry)
                val email = emailEntry.text.toString()
                drawingViewModel.shareDrawing(email)
                popupWindow.dismiss()
            }
        }
    }

    private fun initSaveButton(binding: FragmentDrawableBinding) {
        binding.saveButton.setOnClickListener {
            drawingViewModel.sendDrawing()
        }
    }

    private fun initBackButton(binding: FragmentDrawableBinding) {
        binding.backButton.setOnClickListener {
            binding.penSizeBar.progress = 12
            bitmap.eraseColor(Color.WHITE)
            findNavController().navigate(R.id.action_DrawableFragmentToMainScreen)
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
        drawingViewModel.initBitmap()
        val drawingView = view.findViewById<DrawingView>(R.id.canvas)
        initDrawingCanvas(drawingView)
        initObservers(drawingView)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initDrawingCanvas(drawingView: DrawingView) {
        val tempPath = Path()
        drawingView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    tempPath.moveTo(event.x, event.y)
                    drawingCanvas.drawPoint(event.x, event.y, tool.paint)

                }
                MotionEvent.ACTION_MOVE -> {
                    tempPath.lineTo(event.x, event.y)
                    drawingCanvas.drawPath(tempPath, tool.paint)

                }
                MotionEvent.ACTION_UP -> {
                    tempPath.reset()
                }
            }
            drawingViewModel.updateBitmap(bitmap)
            drawingView.invalidate()
            true
        }
    }

    private fun initToolbarButtons() {

        binding.shapeButton.setOnClickListener{
            if (isCircle){
                binding.shapeButton.setBackgroundResource(R.drawable.shape_button_icon_rect)
            }
            else {
                binding.shapeButton.setBackgroundResource(R.drawable.shape_button_icon_circle)
            }
            tool.toggleShape(isCircle)
            isCircle = !isCircle

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
        drawingViewModel.canvasBitmap.observe(viewLifecycleOwner, Observer { bitmap ->
            this.bitmap = bitmap
            drawingView.setBitmap(bitmap)
            drawingCanvas = Canvas(bitmap)
            drawingView.invalidate()
        })
    }
}