package com.upchiapas.mitiendita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.mitiendita.navigation.AppNavHost
import com.upchiapas.mitiendita.ui.theme.MiTienditaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiTienditaTheme {
                val mainViewModel: MainViewModel = viewModel(factory = MainViewModel.Factory)
                AppNavHost(startDestination = mainViewModel.startDestination)
            }
        }
    }
}
