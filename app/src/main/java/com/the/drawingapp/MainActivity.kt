package com.the.drawingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.the.drawingapp.databinding.ActivityMainScreenBinding


class MainActivity : AppCompatActivity() {
    val binding: ActivityMainScreenBinding by lazy { ActivityMainScreenBinding.inflate(layoutInflater) }
    val recycler by lazy { binding.recycler }

    private val viewModel : DrawingViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        val drawableFragment = DrawableFragment()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)
        with(recycler){
            layoutManager = LinearLayoutManager(this@MainActivity)
            //Implementation will be written in Phase 2
        }

        binding.newDrawingButton.setOnClickListener{
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, drawableFragment)
                .addToBackStack(null)
                .commit()
        }
        setContentView(binding.root)
    }
}