package com.the.drawingapp

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
            viewModel.initBitmap()
        }

        viewModel.getAllDrawings()
        binding.composeView?.setContent {
            Log.d("ComposeView Setting Content", "${viewModel.savedCanvases}")
            SavedCanvasList(viewModel.savedCanvases) { selectedBitmap ->
                viewModel.updateBitmap(selectedBitmap)
                findNavController().navigate(R.id.action_mainScreenFragment_to_drawableFragment2)
            }
        }
        return binding.root
    }

    @Composable
    fun SavedCanvasList(savedCanvases: Flow<List<Bitmap>>, onClick: (Bitmap) -> Unit) {
        val bitmaps by savedCanvases.collectAsState(initial = emptyList())
        Log.d("SavedCanvasList", "BitmapList size: ${bitmaps.size}")
        val scrollState = rememberScrollState()
        Row(modifier = Modifier.horizontalScroll(scrollState)) {
            bitmaps.reversed().forEach { bitmap ->
                SavedCanvas(bitmap, onClick)
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun SavedCanvas(canvas: Bitmap, onClick: (Bitmap) -> Unit) {
    Image(
        bitmap = canvas.asImageBitmap(),
        contentDescription = "A Saved Canvas",
        modifier = Modifier.clickable {
            onClick(canvas)
        }
    )
}


