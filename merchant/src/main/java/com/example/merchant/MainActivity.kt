package com.example.merchant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.merchant.ui.MerchantApp
import com.example.merchant.ui.MerchantViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MerchantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MerchantApp(viewModel) }
    }
}
