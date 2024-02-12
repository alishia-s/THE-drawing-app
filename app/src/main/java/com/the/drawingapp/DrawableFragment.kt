package com.the.drawingapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.the.drawingapp.databinding.FragmentDrawableBinding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer


class DrawableFragment: Fragment() {
    private val viewModel: DrawingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentDrawableBinding.inflate(inflater, container, false)

        binding.backButton.setOnClickListener{
            viewModel.setColor(0xFF000000.toInt())
            binding.penSizeBar.setProgress(12)
            parentFragmentManager.beginTransaction().remove(this).commit()
        }

        binding.penSizeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.setStrokeWidth(progress.toFloat())
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.eraserButton.setOnClickListener {
            viewModel.setColor(0xFFFFFFFF.toInt())
        }
        binding.penButton.setOnClickListener {
            viewModel.setColor(0xFF000000.toInt())
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val drawingView = view.findViewById<DrawingView>(R.id.canvas)

        initObservers(drawingView)
    }

    private fun initObservers(drawingView: DrawingView) {
        viewModel.currentColor.observe(viewLifecycleOwner, Observer { color ->
            drawingView.setPaintColor(color)
        })
        viewModel.currentStrokeWidth.observe(viewLifecycleOwner, Observer { strokeWidth ->
            drawingView.setBrushSize(strokeWidth)
        })
    }
}