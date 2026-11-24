package com.example.rider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.rider.ui.RiderApp
import com.example.rider.ui.RiderViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: RiderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialDestination = intent?.getStringExtra("navigate_to")
        setContent {
            RiderApp(
                viewModel = viewModel,
                initialDestination = initialDestination
            )
        }
    }
}
