package com.kgapp.kccjUltra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kgapp.kccjUltra.ui.AppNavigation
import com.kgapp.kccjUltra.ui.theme.KccjTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KccjTheme {
                AppNavigation()
            }
        }
    }
}
