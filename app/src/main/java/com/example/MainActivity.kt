package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.QuickKartApp
import com.example.ui.QuickKartViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: QuickKartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialDestination = intent?.getStringExtra("navigate_to")
        setContent {
            QuickKartApp(
                viewModel = viewModel,
                initialDestination = initialDestination
            )
        }
    }
}
