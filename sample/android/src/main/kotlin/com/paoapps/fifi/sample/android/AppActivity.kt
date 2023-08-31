package com.paoapps.fifi.sample.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.rememberScaffoldState
import androidx.compose.ui.Modifier
import com.paoapps.fifi.sample.android.composeui.AppTheme
import com.paoapps.fifi.sample.android.composeui.MainScreen

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val scaffoldState = rememberScaffoldState()
                Box(
                    Modifier.padding(
                        WindowInsets.systemBars
                            .only(WindowInsetsSides.Start + WindowInsetsSides.End)
                            .asPaddingValues()
                    )
                ) {
                    Scaffold(
                        scaffoldState = scaffoldState,
                        snackbarHost = { hostState ->
                            SnackbarHost(
                                hostState = hostState,
                                modifier = Modifier.padding(
                                    WindowInsets.systemBars
                                        .only(WindowInsetsSides.Bottom)
                                        .asPaddingValues()
                                )
                            )
                        }
                    ) {
                        MainScreen(modifier = Modifier.padding(it))
                    }
                }
            }
        }
    }
}
