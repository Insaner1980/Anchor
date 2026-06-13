package com.finnvek.anchor

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finnvek.anchor.domain.model.ThemeMode
import com.finnvek.anchor.ui.app.AnchorAppViewModel
import com.finnvek.anchor.ui.navigation.AnchorAppRoot
import com.finnvek.anchor.ui.theme.AnchorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AnchorAppViewModel = hiltViewModel()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val systemInDarkTheme = isSystemInDarkTheme()

            AnchorTheme(
                darkTheme = themeMode.shouldUseDarkTheme(systemInDarkTheme),
                calm = themeMode == ThemeMode.CALM,
            ) {
                AnchorAppRoot()
            }
        }
    }
}

private fun ThemeMode.shouldUseDarkTheme(systemInDarkTheme: Boolean): Boolean =
    when (this) {
        ThemeMode.SYSTEM -> systemInDarkTheme
        ThemeMode.LIGHT,
        ThemeMode.CALM -> false
        ThemeMode.DARK -> true
    }
