package com.the.drawingapp

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.the.drawingapp.databinding.FragmentMainScreenBinding
import kotlinx.coroutines.flow.Flow


class MainScreenFragment : Fragment() {
    private lateinit var binding: FragmentMainScreenBinding
    private val viewModel : DrawingViewModel by activityViewModels{DrawingViewModel.DrawingViewModelFactory((getActivity()?.application as DrawingApplication).drawingAppRepository)}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainScreenBinding.inflate(layoutInflater)
        binding.newDrawingButton.setOnClickListener {
            findNavController().navigate(R.id.action_mainScreenFragment_to_drawableFragment2)
        }

        viewModel.getAllDrawings()
        binding.composeView?.setContent {
            Log.d("ComposeView Setting Content", "${viewModel.savedCanvases}")
            SavedCanvasList(viewModel.savedCanvases)
        }
        return binding.root
    }

    @Composable
    fun SavedCanvasList(savedCanvases: Flow<List<Bitmap>>) {
        val bitmaps by savedCanvases.collectAsState(initial = emptyList())
        Log.d("SavedCanvasList", "BitmapList size: ${bitmaps.size}")
        Row {
            bitmaps.forEach { bitmap ->
                SavedCanvas(bitmap)
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun SavedCanvas(canvas: Bitmap) {
    Log.d("SavedCanvas", "Creating image with Bitmap Width: ${canvas.width} and Height: ${canvas.height}")
    Image(
        bitmap = canvas.asImageBitmap(),
        contentDescription = "A Saved Canvas",
    )
}


