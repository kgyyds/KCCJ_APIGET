package com.kgapp.kccjapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kgapp.kccjapi.ui.AppNavigation
import com.kgapp.kccjapi.ui.theme.KccjTheme

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
