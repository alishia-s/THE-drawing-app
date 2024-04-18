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
    private val drawingViewModel : DrawingViewModel by activityViewModels{DrawingViewModel.DrawingViewModelFactory((getActivity()?.application as DrawingApplication).drawingAppRepository)}
    private val userViewModel: UserViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMainScreenBinding.inflate(layoutInflater)
        binding.newDrawingButton.setOnClickListener {
            findNavController().navigate(R.id.action_MainScreenToDrawableFragment)
        }


        binding.cloudBackup.setOnClickListener() {
            drawingViewModel.syncWithCloud()
        }

        // TODO: make actual logout and nuke button
        binding.moveLeft.setOnClickListener() {
            userViewModel.logout()
            findNavController().navigate(R.id.LoginFragment)
        }

        binding.moveRight.setOnClickListener() {
            drawingViewModel.NUKE()
        }

        drawingViewModel.getAllUserDrawings()
        binding.composeView.setContent {
            Log.d("ComposeView Setting Content", "${drawingViewModel.savedCanvases}")
            SavedCanvasList(drawingViewModel.savedCanvases) { selectedBitmap ->
                drawingViewModel.updateBitmap(selectedBitmap)
                findNavController().navigate(R.id.action_MainScreenToDrawableFragment)
            }
        }
        return binding.root
    }
}

@Composable
fun SavedCanvasList(savedCanvases: Flow<List<Drawing>>, onClick: (Bitmap) -> Unit) {
    val bitmaps by savedCanvases.collectAsState(initial = emptyList())
    Log.d("SavedCanvasList", "BitmapList size: ${bitmaps.size}")
    val scrollState = rememberScrollState()
    Row(modifier = Modifier.horizontalScroll(scrollState)) {
        bitmaps.reversed().forEach { drawing ->
            if(drawing.bitmap == null) {
                Log.d("SavedCanvasList", "Bitmap is null")
            } else {
                SavedCanvas(drawing.bitmap!!, onClick)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
fun SavedCanvas(canvas: Bitmap, onClick: (Bitmap) -> Unit) {
    Image(
        bitmap = canvas.asImageBitmap(),
        contentDescription = canvas.toString(),
        modifier = Modifier.clickable {
            onClick(canvas)
        }
    )
}


