package com.yogeshpaliyal.keypass.ui.generate

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.yogeshpaliyal.keypass.ui.generate.ui.GeneratePasswordScreen
import com.yogeshpaliyal.keypass.ui.style.KeyPassTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GeneratePasswordActivity : AppCompatActivity() {
    private val viewModel: GeneratePasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        setContent {
            KeyPassTheme {
                GeneratePasswordScreen(viewModel)
            }
        }
    }
}
